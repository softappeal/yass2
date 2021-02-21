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

* package/file, module and target table

```
+--------------------------------------------+------------+--------+
|  package/file                              | module     | target |
+--------------------------------------------+------------+--------+
|  Cleanup.kt                                | core       | common |
|  Dumper.kt                                 | core       | common |
|  Interceptor.kt                            | core       | common |
+- reflect                                   |            |        |
|   |  DumperReflection.kt                   | reflect    | jvm    |
|   |  InterceptorReflection.kt              | reflect    | jvm    |
+- remote                                    |            |        |
|   |  Message.kt                            | core       | common |
|   |  Remote.kt                             | core       | common |
|   +- reflect                               |            |        |
|   |   |  ServiceReflection.kt              | reflect    | jvm    |
|   +- coroutines                            |            |        |
|       |  RemoteFlow.kt                     | coroutines | common |
|       |  Sync.kt                           | coroutines | common |
|       +- session                           |            |        |
|           |  Session.kt                    | coroutines | common |
|           |  SessionConnector.kt           | coroutines | common |
+- serialize                                 |            |        |
|   |  Serializer.kt                         | core       | common |
|   +- binary                                |            |        |
|       |  BaseEncoders.kt                   | core       | common |
|       |  BinarySerializer.kt               | core       | common |
|       |  Int.kt                            | core       | common |
|       |  VarInt.kt                         | core       | common |
|       +- reflect                           |            |        |
|           |  BinarySerializerMeta.kt       | reflect    | common |
|           |  BinarySerializerReflection.kt | reflect    | jvm    |
+- transport                                 |            |        |
|   |  BinaryMessageSerializer.kt            | core       | common |
|   |  Bytes.kt                              | core       | common |
|   |  TransportConfig.kt                    | core       | common |
|   +- session                               |            |        |
|   |   |  BinaryPacketSerializer.kt         | coroutines | common |
|   +- js                                    |            |        |
|   |   |  Connect.kt                        | coroutines | js     |
|   |   |  Tunnel.kt                         | coroutines | js     |
|   +- ktor                                  |            |        |
|       |  ByteChannel.kt                    | ktor       | common |
|       |  Http.kt                           | ktor       | common |
|       |  WebSocket.kt                      | ktor       | common |
|       |  HttpServer.kt                     | ktor       | jvm    |
|       |  Socket.kt                         | ktor       | jvm    |
+- generate                                  |            |        |
    |  Generate.kt                           | generate   | jvm    |
    |  GenerateBinarySerializer.kt           | generate   | jvm    |
    |  GenerateDumperProperties.kt           | generate   | jvm    |
    |  GenerateProxyFactory.kt               | generate   | jvm    |
    |  GenerateRemote.kt                     | generate   | jvm    |
+--------------------------------------------+------------+--------+
```

* [Tutorial](https://github.com/softappeal/yass2-tutorial)

* Open Source ([BSD-3-Clause license](license.txt))

* supersedes [yass](https://github.com/softappeal/yass/)
