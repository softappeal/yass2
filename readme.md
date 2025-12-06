### yass2 (Yet Another Service Solution)

* yass2 is
    * a small [Kotlin](https://kotlinlang.org/) [Multiplatform](https://kotlinlang.org/docs/multiplatform-intro.html) library
    * for efficient asynchronous/non-blocking [Coroutines](https://kotlinlang.org/docs/coroutines-guide.html) based
      peer-to-peer communication
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
+----------------------------------+       +----------------------------------------------+
| // contract usage                |       | // contract implementation                   |
| val calculator: Calculator = ... |       | class CalculatorImpl : Calculator {          |
| println(calculator.add(1, 2))    |       |   override suspend fun add(a: Int, b: Int) = |
|                                  |       |     a + b                                    |
|                                  |       | }                                            |
|..................................|       |..............................................|
|         contract library         |       |              contract library                |
|..................................|       |..............................................|
|          yass2 library           |       |               yass2 library                  |
|..................................|       |..............................................|
|        transport library         | <---> |             transport library                |
+----------------------------------+       +----------------------------------------------+
```

* generates code (works on any platform and is fast) either with
  [KSP](https://kotlinlang.org/docs/ksp-overview.html) or with reflection
    * fast, compact and extendable binary serializer for high throughput and low latency
    * extendable string-based serializers: TextSerializer, JsonSerializer and KotlinSerializer (serializes Kotlin source code)
    * interceptor (around advice, aspect-oriented programming)

* artifacts on [Maven Central](https://central.sonatype.com/search?q=g:ch.softappeal.yass2) (GroupId: ch.softappeal.yass2)

* API doc on [javadoc.io](https://javadoc.io/doc/ch.softappeal.yass2/yass2/latest)

* uses [Semantic Versioning](https://semver.org)

* Open Source ([BSD-3-Clause license](license.txt))

* supersedes [yass](https://github.com/softappeal/yass/)
