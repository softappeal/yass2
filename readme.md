### yass2 (Yet Another Service Solution)

* yass2 is
    * a small [Kotlin](https://kotlinlang.org/)
      [Multiplatform](https://kotlinlang.org/docs/reference/multiplatform.html) library
    * for efficient asynchronous/non-blocking
      [Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) based peer-to-peer communication
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

* provided either with reflection (works only on the JVM platform and is slower) or by generated code
  (works on any platform and is faster)
    * fast, compact and extendable binary serializer for high throughput and low latency
    * dumper (generic object printer)
    * interceptor (around advice, aspect-oriented programming)
    * remoting (remote proxy and invoker)

* yass2 has multiple modules with the following dependencies

```
yass2-core
     ^
     |
     +--- yass2-coroutines ---> kotlinx-coroutines-core
     |         ^
     |         |
     |    yass2-ktor ---> io.ktor
     |
     |
     +--- yass2-reflect ---> kotlin-reflect
               ^
               |
          yass2-generate
```

* artifacts on [Maven Central](https://search.maven.org/search?q=g:ch.softappeal.yass2) (GroupId: ch.softappeal.yass2)

* uses [Semantic Versioning](https://semver.org)

* packages

```
+--------------------------------------------+--------+------------+
|  file                                      | target | module     |
+--------------------------------------------+--------+------------+
|  Cleanup.kt                                | common | core       |
|  Dumper.kt                                 | common | core       |
|  Interceptor.kt                            | common | core       |
+- remote                                    |        |            |
|   |  Message.kt                            | common | core       |
|   |  Remote.kt                             | common | core       |
|   +- coroutines                            |        |            |
|   |   |  RemoteFlow.kt                     | common | coroutines |
|   |   |  Sync.kt                           | common | coroutines |
|   |   +- session                           |        |            |
|   |       |  Session.kt                    | common | coroutines |
|   |       |  SessionConnector.kt           | common | coroutines |
|   +- reflect                               |        |            |
|       |  ServiceReflection.kt              | jvm    | reflect    |
+- serialize                                 |        |            |
|   |  Serializer.kt                         | common | core       |
|   +- binary                                |        |            |
|       |  BaseEncoders.kt                   | common | core       |
|       |  BinarySerializer.kt               | common | core       |
|       |  Int.kt                            | common | core       |
|       |  VarInt.kt                         | common | core       |
|       +- reflect                           |        |            |
|           |  BinarySerializerMeta.kt       | common | reflect    |
|           |  BinarySerializerReflection.kt | jvm    | reflect    |
+- transport                                 |        |            |
|   |  BinaryMessageSerializer.kt            | common | core       |
|   |  Bytes.kt                              | common | core       |
|   |  TransportConfig.kt                    | common | core       |
|   +- session                               |        |            |
|   |   |  BinaryPacketSerializer.kt         | common | coroutines |
|   +- js                                    |        |            |
|   |   |  Connect.kt                        | js     | coroutines |
|   |   |  Tunnel.kt                         | js     | coroutines |
|   +- ktor                                  |        |            |
|       |  ByteChannel.kt                    | common | ktor       |
|       |  Http.kt                           | common | ktor       |
|       |  WebSocket.kt                      | common | ktor       |
|       |  HttpServer.kt                     | jvm    | ktor       |
|       |  Socket.kt                         | jvm    | ktor       |
+- reflect                                   |        |            |
|   |  DumperReflection.kt                   | jvm    | reflect    |
|   |  InterceptorReflection.kt              | jvm    | reflect    |
+- generate                                  |        |            |
    |  Generate.kt                           | jvm    | generate   |
    |  GenerateBinarySerializer.kt           | jvm    | generate   |
    |  GenerateDumperProperties.kt           | jvm    | generate   |
    |  GenerateProxyFactory.kt               | jvm    | generate   |
    |  GenerateRemote.kt                     | jvm    | generate   |
+--------------------------------------------+--------+------------+
```

* [Tutorial](https://github.com/softappeal/yass2-tutorial)

* Open Source ([BSD-3-Clause license](license.txt))

* supersedes [yass](https://github.com/softappeal/yass/)
