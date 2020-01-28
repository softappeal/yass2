# yass2 (Yet Another Service Solution)

* a [very small](doc/loc.md) [Kotlin Multiplatform](https://kotlinlang.org/docs/reference/multiplatform.html)
  library for efficient asynchronous peer-to-peer communication

* Open Source ([BSD-3-Clause license](license.txt))

* asynchronous/non-blocking servers/clients with
  [Kotlin Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html)

* provides dumper, serializer, interceptor and remoting either with reflection or by generated code
  * generated code works on any platform and is faster
  * reflection works only on the Jvm platform and is slower

* provides unidirectional and session based bidirectional remoting

* supports type-safe contracts with data transfer objects and interfaces

* provides [Ktor](https://ktor.io) transport adaptors for Http, WebSocket and Socket

* Javascript transport uses native fetch and WebSocket Api instead of Ktor

* provides a fast, compact and extendable binary serializer for high throughput and low latency

* pick another multiplatform serializer if you don't like the provided one

* uses [Semantic Versioning](https://semver.org)

* artifacts on [Maven Central](https://search.maven.org/search?q=g:ch.softappeal.yass2) (GroupId: ch.softappeal.yass2)

* supersedes [yass](https://github.com/softappeal/yass/)

* [Tutorial](https://github.com/softappeal/yass2-tutorial)
