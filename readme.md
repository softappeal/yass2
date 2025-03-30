# Ktor JsClient doesn't get response content-length header if run in browser

See [KTOR-8377](https://youtrack.jetbrains.com/issue/KTOR-8377/Ktor-JsClient-doesnt-get-response-content-length-header-if-run-in-browser).

This minimal project reproduces that `JsClient` doesn't get response `content-length` header if run in browser.

The following table shows under which Ktor versions and targets the bug occurs.

| Target       | Ktor       | 3.0.3 | 3.1.0 | 3.1.1 | 3.1.2 |
|--------------|------------|-------|-------|-------|-------|
| jvmTest      | CIO client | OK    | OK    | OK    | OK    |
| jsTest       | JsClient   | OK    | OK    | OK    | OK    |
| wasmJsTest   | JsClient   | OK    | OK    | OK    | OK    |
| browser js   | JsClient   | OK    | OK    | NOK   | NOK   |
| browser wasm | JsClient   | OK    | NOK   | NOK   | NOK   |

For example, Ktor 3.1.0 JsClient run in browser

- works for js
- doesn't work for wasm

The project is can be built with `./gradlew build`.

You can change the Ktor version in [libs.versions.toml](gradle/libs.versions.toml).

You can start the server for the browser by running [RunServer.kt](src/jvmTest/kotlin/RunServer.kt).

- In the browser use the url `http://localhost:28947/js/` for running the js code.
- In the browser use the url `http://localhost:28947/wasm/` for running the wasm code.
