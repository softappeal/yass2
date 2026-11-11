@file:Suppress(
    "unused",
    "UNCHECKED_CAST",
    "USELESS_CAST",
    "PARAMETER_NAME_CHANGED_ON_OVERRIDE",
    "RemoveRedundantQualifierName",
    "SpellCheckingInspection",
    "RedundantVisibilityModifier",
    "REDUNDANT_VISIBILITY_MODIFIER",
    "RedundantSuppression",
    "UNUSED_ANONYMOUS_PARAMETER",
    "KotlinRedundantDiagnosticSuppress",
    "RedundantUpperBound",
    "UNNECESSARY_SAFE_CALL",
    "ForEachParameterNotUsed",
    "ReplaceManualRangeWithIndicesCalls",
)

package ch.softappeal.yass2.serialize

public fun BinarySerializer(): ch.softappeal.yass2.serialize.binary.BinarySerializer =
    object : ch.softappeal.yass2.serialize.binary.BinarySerializer() {
        init {
            initialize(
                // kotlin.collections.List: 1
                ch.softappeal.yass2.serialize.binary.IntBinaryEncoder, // 2
                ch.softappeal.yass2.serialize.binary.StringBinaryEncoder, // 3
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.serialize.A::class, // 4
                    { i ->
                        writeOptional(i.a, 2)
                    },
                    {
                        ch.softappeal.yass2.serialize.A(
                            a = readOptional(2) as kotlin.Int?,
                        )
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.serialize.B::class, // 5
                    { i ->
                        writeRequired(i.a, 2)
                        writeRequired(i.b, 2)
                    },
                    {
                        ch.softappeal.yass2.serialize.B(
                            a = readRequired(2) as kotlin.Int,
                            b = readRequired(2) as kotlin.Int,
                        )
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.serialize.Poly::class, // 6
                    { i ->
                        writeObject(i.a)
                        writeRequired(i.b, 5)
                    },
                    {
                        ch.softappeal.yass2.serialize.Poly(
                            a = readObject() as ch.softappeal.yass2.serialize.A,
                            b = readRequired(5) as ch.softappeal.yass2.serialize.B,
                        )
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.serialize.Example::class, // 7
                    { i ->
                        writeRequired(i.int, 2)
                        writeOptional(i.intOptional, 2)
                        writeObject(i.any)
                        writeObject(i.anyOptional)
                        writeRequired(i.list, 1)
                        writeOptional(i.listOptional, 1)
                    },
                    {
                        ch.softappeal.yass2.serialize.Example(
                            int = readRequired(2) as kotlin.Int,
                            intOptional = readOptional(2) as kotlin.Int?,
                            any = readObject() as kotlin.Any,
                            anyOptional = readObject() as kotlin.Any?,
                            list = readRequired(1) as kotlin.collections.List<kotlin.Int>,
                            listOptional = readOptional(1) as kotlin.collections.List<kotlin.Int>?,
                        )
                            .apply {
                                list?.let { for (i in 0..<it.size) it[i] as kotlin.Int }
                                listOptional?.let { for (i in 0..<it.size) it[i] as kotlin.Int }
                            }
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.serialize.TextBuiltIn::class, // 8
                    { i ->
                        writeRequired(i.string, 3)
                        writeOptional(i.stringOptional, 3)
                    },
                    {
                        ch.softappeal.yass2.serialize.TextBuiltIn(
                            string = readRequired(3) as kotlin.String,
                            stringOptional = readOptional(3) as kotlin.String?,
                        )
                    }
                ),
            )
        }
    }

public fun TextSerializer(compact: Boolean = true): ch.softappeal.yass2.serialize.text.TextSerializer =
    ch.softappeal.yass2.serialize.text.TextSerializer(
        compact,
        // kotlin.String: 0
        // kotlin.collections.List: 1
        ch.softappeal.yass2.serialize.text.IntTextEncoder, // 2
        ch.softappeal.yass2.serialize.text.ClassTextEncoder(
            ch.softappeal.yass2.serialize.A::class, // 3
            { i ->
                writeProperty("a", i.a, 2)
            },
            {
                ch.softappeal.yass2.serialize.A(
                    getProperty("a") as kotlin.Int?,
                )
            },
            "a" to 2,
        ),
        ch.softappeal.yass2.serialize.text.ClassTextEncoder(
            ch.softappeal.yass2.serialize.B::class, // 4
            { i ->
                writeProperty("a", i.a, 2)
                writeProperty("b", i.b, 2)
            },
            {
                ch.softappeal.yass2.serialize.B(
                    getProperty("a") as kotlin.Int,
                    getProperty("b") as kotlin.Int,
                )
            },
            "a" to 2,
            "b" to 2,
        ),
        ch.softappeal.yass2.serialize.text.ClassTextEncoder(
            ch.softappeal.yass2.serialize.Poly::class, // 5
            { i ->
                writeProperty("a", i.a)
                writeProperty("b", i.b)
            },
            {
                ch.softappeal.yass2.serialize.Poly(
                    getProperty("a") as ch.softappeal.yass2.serialize.A,
                    getProperty("b") as ch.softappeal.yass2.serialize.B,
                )
            },
            "a" to -1,
            "b" to -1,
        ),
        ch.softappeal.yass2.serialize.text.ClassTextEncoder(
            ch.softappeal.yass2.serialize.Example::class, // 6
            { i ->
                writeProperty("int", i.int, 2)
                writeProperty("intOptional", i.intOptional, 2)
                writeProperty("any", i.any)
                writeProperty("anyOptional", i.anyOptional)
                writeProperty("list", i.list, 1)
                writeProperty("listOptional", i.listOptional, 1)
            },
            {
                ch.softappeal.yass2.serialize.Example(
                    getProperty("int") as kotlin.Int,
                    getProperty("intOptional") as kotlin.Int?,
                    getProperty("any") as kotlin.Any,
                    getProperty("anyOptional") as kotlin.Any?,
                    getProperty("list") as kotlin.collections.List<kotlin.Int>,
                    getProperty("listOptional") as kotlin.collections.List<kotlin.Int>?,
                )
                    .apply {
                        list?.let { for (i in 0..<it.size) it[i] as kotlin.Int }
                        listOptional?.let { for (i in 0..<it.size) it[i] as kotlin.Int }
                    }
            },
            "int" to 2,
            "intOptional" to 2,
            "any" to -1,
            "anyOptional" to -1,
            "list" to -1,
            "listOptional" to -1,
        ),
        ch.softappeal.yass2.serialize.text.ClassTextEncoder(
            ch.softappeal.yass2.serialize.TextBuiltIn::class, // 7
            { i ->
                writeProperty("string", i.string, 0)
                writeProperty("stringOptional", i.stringOptional, 0)
            },
            {
                ch.softappeal.yass2.serialize.TextBuiltIn(
                    getProperty("string") as kotlin.String,
                    getProperty("stringOptional") as kotlin.String?,
                )
            },
            "string" to -1,
            "stringOptional" to -1,
        ),
    )
