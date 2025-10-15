$version: "2"

metadata validators = []

namespace tech.purebrew.apigw

@length(min: 1)
string Hostname

@trait(
    selector: "service"
    breakingChanges: [
        {
            change: "remove"
            severity: "DANGER"
            message: "The service will no longer be accessible via the API Gateway."
        }
        {
            path: "/gatewayBasePath"
            change: "update"
            message: "The public URL path of all endpoints will change."
        }
    ]
)
structure publicService {
    @required
    host: String

    @required
    gatewayBasePath: String
}

// @trait(selector: "service [trait|com.example.apigw#publicService] ~> operation -[input]-> structure > member [trait|httpHeader]")
// @traitValidators(
//     "apigw.jwtClaim.HttpHeader": {
//         selector: ":not([trait|httpHeader])"
//         message: "@jwtClaim must be passed via an HTTP header, please add a @httpHeader trait"
//     }
// )
@trait(selector: "structure > member")
enum jwtClaim {
    SUB = "sub"
    USERNAME = "username"
    EMAIL = "email"
}
