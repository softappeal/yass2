### yass2 (Yet Another Service Solution)

* yass2 is
    * a small [Kotlin](https://kotlinlang.org/)
      [Multiplatform](https://kotlinlang.org/docs/mpp-intro.html) library
    * for efficient asynchronous/non-blocking
      [Coroutines](https://kotlinlang.org/docs/coroutines-guide.html) based peer-to-peer communication
    * enforcing type-safe contracts with interfaces and data transfer objects

```
                         contract library
                       +-----------------------------------------+
                       | // contract definition                  |
                       | interface Calculator {                  |
                       |   suspend fun add(a: Int, b: Int): Int  |
                       | }                                       |
                       +-----------------------------------------+

  client process                             server process
+----------------------------------+       +----------------------------------------------------+
| // contract usage                |       | // contract implementation                         |
| val calculator: Calculator = ... |       | class CalculatorImpl : Calculator {                |
| println(calculator.add(1, 2))    |       |   override suspend fun add(a: Int, b: Int) = a + b |
|                                  |       | }                                                  |
|..................................|       |....................................................|
|         contract library         |       |                 contract library                   |
|..................................|       |....................................................|
|          yass2 library           |       |                  yass2 library                     |
|..................................|       |....................................................|
|        transport library         | <---> |                transport library                   |
+----------------------------------+       +----------------------------------------------------+
```

* provides unidirectional and session based bidirectional remoting

* provides [Ktor](https://ktor.io) transport adaptors for Http, WebSocket and plain Socket

* Javascript transport uses the Fetch and the WebSocket API instead of Ktor

* provided by generated code (works on any platform and is fast)
    * fast, compact and extendable binary serializer for high throughput and low latency
    * dumper (generic object printer)
    * interceptor (around advice, aspect-oriented programming)
    * remoting (remote proxy and invoker)

* yass2 has multiple modules with the following dependencies

```
yass2-core
     ^
     |
yass2-coroutines --> kotlinx-coroutines-core
     ^
     |
yass2-ktor --> io.ktor
```

* artifacts on [Maven Central](https://search.maven.org/search?q=g:ch.softappeal.yass2) (GroupId: ch.softappeal.yass2)

* uses [Semantic Versioning](https://semver.org)

* [Tutorial](tutorial-app/src)

* Open Source ([BSD-3-Clause license](license.txt))

* supersedes [yass](https://github.com/softappeal/yass/)
