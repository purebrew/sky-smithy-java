//> using repository central
//> using repository m2Local
//> using dep "tech.purebrew.smithy:protocol:0.1.0-SNAPSHOT"
//> using dep "software.amazon.smithy:smithy-aws-traits:1.62.0"
//> using dep "software.amazon.smithy:smithy-cli:1.62.0"
//> using dep "io.circe::circe-core:0.14.15"
//> using dep "com.lihaoyi::os-lib:0.11.5"
//> using dep "com.lihaoyi::mill-libs-javalib:1.0.6"

import cats.conversions.variance
import io.circe.*
import io.circe.syntax.*
import mill.api.Logger
import mill.api.PathRef
import mill.api.TaskCtx
import mill.api.daemon.Logger.Prompt
import mill.api.daemon.SystemStreams
import mill.javalib.publish.Artifact
import mill.javalib.publish.LocalM2Publisher
import mill.javalib.publish.Pom
import mill.javalib.publish.PomSettings
import mill.javalib.publish.PublishInfo
import os.Path
import os.zip.ZipSource
import software.amazon.smithy.build.model.SmithyBuildConfig
import software.amazon.smithy.cli.Cli
import software.amazon.smithy.cli.Command.Env
import software.amazon.smithy.cli.commands.SmithyCommand
import software.amazon.smithy.cli.dependencies.DependencyResolver
import software.amazon.smithy.cli.dependencies.MavenDependencyResolver
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.knowledge.OperationIndex
import software.amazon.smithy.model.knowledge.TopDownIndex
import software.amazon.smithy.model.loader.ModelAssembler
import software.amazon.smithy.model.shapes.ShapeId
import software.amazon.smithy.model.traits.HttpHeaderTrait
import software.amazon.smithy.model.traits.HttpTrait
import tech.purebrew.apigw.JwtClaimTrait
import tech.purebrew.apigw.PublicServiceTrait

import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.xml.PrettyPrinter

val worldArtifact = Artifact(
  group = "tech.purebrew.smithy.apiregistry",
  id = "world-model",
  version = "0.1.0-SNAPSHOT"
)

def loadModel: Model =
  Model.assembler
    .addImport("smithy/")
    .discoverModels
    .assemble
    .unwrap

def generateKrakendEndpointConfig(model: Model): String = {
  val topDownIndex = TopDownIndex.of(model)

  val endpointConfigs = for
    service <- model
      .getServiceShapesWithTrait(classOf[PublicServiceTrait])
      .asScala
    operation <-
      topDownIndex
        .getContainedOperations(service)
        .asScala
  yield
    val publicService = service.expectTrait(classOf[PublicServiceTrait])
    val http = operation.expectTrait(classOf[HttpTrait])

    val inputMembers = model
      .expectShape(operation.getInputShape)
      .getAllMembers
      .asScala
      .values

    val inputHeaders = inputMembers
      .flatMap { member =>
        member.getTrait(classOf[HttpHeaderTrait]).toScala.map(_.getValue)
      }

    val base = JsonObject(
      "endpoint" -> s"${publicService.getGatewayBasePath}${http.getUri}".asJson,
      "input_headers" -> inputHeaders.map(Json.fromString).asJson,
      "method" -> http.getMethod.asJson,
      "backend" -> Json.arr(
        Json.obj(
          "method" -> http.getMethod.asJson,
          "url_pattern" -> http.getUri.toString.asJson,
          "host" -> Json.arr(s"http://${publicService.getHost}".asJson).asJson
        )
      )
    )

    val tokenClaimsAndHeaders = inputMembers
      .flatMap { member =>
        member.getTrait(classOf[JwtClaimTrait]).toScala.map { jwtClaim =>
          (
            jwtClaim.getClaimName,
            member.expectTrait(classOf[HttpHeaderTrait]).getValue
          )
        }
      }

    if tokenClaimsAndHeaders.isEmpty then base
    else
      base.add(
        "extra_config",
        Json.obj(
          "auth/validator" -> Json.obj(
            "JWK_SETTINGS" -> "PLACEHOLDER".asJson,
            "propagate_claims" -> Json.arr(
              tokenClaimsAndHeaders.map { case (claim, header) =>
                Json.arr(claim.asJson, header.asJson)
              }.toSeq*
            )
          )
        )
      )

  val jsonString = Json.arr(endpointConfigs.toSeq.map(_.asJson)*).spaces2
  jsonString.replaceAll(
    """"JWK_SETTINGS"\s*:\s*"PLACEHOLDER"""",
    """{{ include "jwk-settings" }}"""
  )
}

/* Same as running `smithy build`, but more portable as you don't need the CLI installed */
def runSmithyBuild: Unit =
  new Cli(
    SmithyCommand(
      new DependencyResolver.Factory:
        override def create(
            config: SmithyBuildConfig,
            env: Env
        ): DependencyResolver = MavenDependencyResolver()
    ),
    getClass.getClassLoader()
  ).run(Array("build"))

def publishWorldArtifact: Unit = {
  val buildDir = os.pwd / "build"
  val smithySourceDir = os.pwd / "build" / "smithy" / "source" / "sources"

  val jarFile = buildDir / "world.jar"

  if os.exists(jarFile) then os.remove(jarFile)

  val manifestFile =
    ZipSource.fromPathTuple(
      os.temp(
        s"Manifest-Version: 1.0\nBuild-Timestamp: ${Instant.now.truncatedTo(ChronoUnit.SECONDS)}"
      ) -> (os.sub / "META-INF" / "MANIFEST.MF")
    )

  val smithyFiles = os.walk(smithySourceDir).map { smithyFile =>
    ZipSource.fromPathTuple(
      smithyFile -> (os.sub / "META-INF" / "smithy" / smithyFile
        .relativeTo(smithySourceDir))
    )
  }

  os.zip(
    jarFile,
    manifestFile +: smithyFiles
  )

  val pomContentNode =
    <project
        xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"
        xmlns:xsi ="http://www.w3.org/2001/XMLSchema-instance"
        xmlns ="http://maven.apache.org/POM/4.0.0">

        <modelVersion>4.0.0</modelVersion>
        <name>API Registry World Model</name>
        <groupId>{worldArtifact.group}</groupId>
        <artifactId>{worldArtifact.id}</artifactId>
        <packaging>jar</packaging>
        <version>{worldArtifact.version}</version>
      </project>

  val pp = new PrettyPrinter(120, 4)
  val pom = os.temp(pp.format(pomContentNode))

  given TaskCtx.Log = new TaskCtx.Log:
    def log: Logger = mill.api.Logger.DummyLogger

  LocalM2Publisher(os.home / ".m2" / "repository").publish(
    worldArtifact,
    LocalM2Publisher.createFileSetContents(
      pom,
      worldArtifact,
      Seq(PublishInfo(PathRef(jarFile), ivyConfig = "*"))
    )
  )
}

runSmithyBuild

val model = loadModel

val endpointConfig = generateKrakendEndpointConfig(model)
os.write.over(os.pwd / "build" / "endpoints.tmpl", endpointConfig)

publishWorldArtifact
