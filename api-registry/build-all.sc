//> using repository central
//> using repository m2Local
//> using dep "tech.purebrew.smithy:protocol:0.1.0-SNAPSHOT"
//> using dep "software.amazon.smithy:smithy-aws-traits:1.62.0"
//> using dep "software.amazon.smithy:smithy-cli:1.62.0"
//> using dep "io.circe::circe-core:0.14.15"
//> using dep "com.lihaoyi::os-lib:0.11.5"

import software.amazon.smithy.cli.Cli
import software.amazon.smithy.cli.Command.Env
import software.amazon.smithy.build.model.SmithyBuildConfig
import software.amazon.smithy.cli.dependencies.DependencyResolver
import software.amazon.smithy.cli.dependencies.MavenDependencyResolver
import software.amazon.smithy.cli.commands.SmithyCommand

import io.circe.*
import io.circe.syntax.*
import software.amazon.smithy.model.knowledge.TopDownIndex
import software.amazon.smithy.model.knowledge.OperationIndex
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.loader.ModelAssembler
import software.amazon.smithy.model.shapes.ShapeId
import software.amazon.smithy.model.traits.HttpHeaderTrait
import software.amazon.smithy.model.traits.HttpTrait
import tech.purebrew.apigw.JwtClaimTrait
import tech.purebrew.apigw.PublicServiceTrait

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

def loadModel: Model =
  Model.assembler
    .addImport("smithy/")
    .discoverModels
    .assemble
    .unwrap

def generateKrakendEndpointConfig(model: Model): String =
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

val model = loadModel
val endpointConfig = generateKrakendEndpointConfig(model)
os.write.over(os.pwd / "build" / "endpoints.tmpl", endpointConfig)
