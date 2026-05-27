$version: "2"

namespace smithy4s.example

use smithy.test#smokeTests

@smokeTests([
    {
        id: "SmokeTestExample"
        params: {}
        expect: {
            success: {}
        }
    }
])
@http(method: "GET", uri: "/smoke/test")
operation SmokeTestOperation {
    output := {
        test: String
    }
}
