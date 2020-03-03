### yass2 (Yet Another Service Solution)

* yass2 is
  * a small [Kotlin](https://kotlinlang.org/)
    [Multiplatform](https://kotlinlang.org/docs/reference/multiplatform.html) library
  * for efficient asynchronous/non-blocking [Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html)
    based peer-to-peer communication
  * enforcing type-safe contracts with interfaces and data transfer objects

```
                       +-----------------------------------------+
                       | // contract definition                  |
                       | interface Calculator {                  |
                       |   suspend fun add(a: Int, b: Int): Int  |
                       | }                                       |
                       +-----------------------------------------+


  client process                                      server process
+-----------------------------------------+         +----------------------------------------------------+
|            contract library             |         |                  contract library                  |
|.........................................|         |....................................................|
|     // contract usage                   |         | // contract implementation                         |
|     val calculator: Calculator = ...    |         | class CalculatorImpl : Calculator {                |
|     println(calculator.add(1, 2))       |         |   override suspend fun add(a: Int, b: Int) = a + b |
|                                         |         | }                                                  |
|.........................................|         |....................................................|
|             yass2 library               |         |                  yass2 library                     |
|.........................................|         |....................................................|
|          transport library              | <-----> |                transport library                   |
+-----------------------------------------+         +----------------------------------------------------+


                           yass2
                         +--------------------------------------------+
                         | remoting                                   |
                         |   maps contract to messages                |
                         |............................................|
                         | serialize                                  |
                         |   transforms messages to byte chunks       |
                         |............................................|
                         | transport adaptor                          |
                         |   transports byte chunks between processes |
                         +--------------------------------------------+
```

* provides unidirectional and session based bidirectional remoting

* provides [Ktor](https://ktor.io) transport adaptors for Http, WebSocket and plain Socket                                                                                                                                                                

* Javascript transport uses native fetch and WebSocket API instead of Ktor

* provided either with reflection (works only on the JVM platform and is slower) or
  by generated code (works on any platform and is faster)
  * fast, compact and extendable binary serializer for high throughput and low latency
  * dumper (generic object printer)
  * interceptor (around advice, aspect-oriented programming)
  * remoting (remote proxy and invoker)

* [Tutorial](https://github.com/softappeal/yass2-tutorial)

* Open Source ([BSD-3-Clause license](license.txt))

* artifacts on [Maven Central](https://search.maven.org/search?q=g:ch.softappeal.yass2) (GroupId: ch.softappeal.yass2)

* uses [Semantic Versioning](https://semver.org)

* supersedes [yass](https://github.com/softappeal/yass/)
