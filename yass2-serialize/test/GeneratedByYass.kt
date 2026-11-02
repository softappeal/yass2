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
)

package ch.softappeal.yass2.serialize

public fun BinarySerializer(): ch.softappeal.yass2.serialize.binary.BinarySerializer =
    object : ch.softappeal.yass2.serialize.binary.BinarySerializer() {
        init {
            initialize(
                // kotlin.collections.List: 1
                ch.softappeal.yass2.serialize.binary.IntBinaryEncoder, // 2
                ch.softappeal.yass2.serialize.binary.BooleanBinaryEncoder, // 3
                ch.softappeal.yass2.serialize.binary.StringBinaryEncoder, // 4
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.serialize.A::class, // 5
                    { i ->
                        writeOptional(i.a, 2)
                    },
                    {
                        ch.softappeal.yass2.serialize.A(
                            a = readOptional(2) as kotlin.Int?,
                        ).apply {
                        }
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.serialize.B::class, // 6
                    { i ->
                        writeRequired(i.a, 2)
                        writeRequired(i.b, 2)
                    },
                    {
                        ch.softappeal.yass2.serialize.B(
                            a = readRequired(2) as kotlin.Int,
                            b = readRequired(2) as kotlin.Int,
                        ).apply {
                        }
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.serialize.Poly::class, // 7
                    { i ->
                        writeObject(i.a)
                        writeRequired(i.b, 6)
                    },
                    {
                        ch.softappeal.yass2.serialize.Poly(
                            a = readObject() as ch.softappeal.yass2.serialize.A,
                            b = readRequired(6) as ch.softappeal.yass2.serialize.B,
                        ).apply {
                        }
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.serialize.Example::class, // 8
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
                        ).apply {
                            list?.forEach {}
                            listOptional?.forEach {}
                        }
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.serialize.TextBuiltIn::class, // 9
                    { i ->
                        writeRequired(i.boolean, 3)
                        writeOptional(i.booleanOptional, 3)
                        writeRequired(i.string, 4)
                        writeOptional(i.stringOptional, 4)
                    },
                    {
                        ch.softappeal.yass2.serialize.TextBuiltIn(
                            boolean = readRequired(3) as kotlin.Boolean,
                            booleanOptional = readOptional(3) as kotlin.Boolean?,
                            string = readRequired(4) as kotlin.String,
                            stringOptional = readOptional(4) as kotlin.String?,
                        ).apply {
                        }
                    }
                ),
            )
        }
    }

public fun TextSerializer(): ch.softappeal.yass2.serialize.text.TextSerializer =
    ch.softappeal.yass2.serialize.text.TextSerializer(
        // kotlin.String: 0
        // kotlin.Boolean: 1
        // kotlin.collections.List: 2
        ch.softappeal.yass2.serialize.text.IntTextEncoder, // 3
        ch.softappeal.yass2.serialize.text.ClassTextEncoder(
            ch.softappeal.yass2.serialize.A::class, // 4
            { i ->
                writeProperty("a", i.a, 3)
            },
            {
                ch.softappeal.yass2.serialize.A(
                    getProperty("a") as kotlin.Int?,
                ).apply {
                }
            },
            "a" to 3,
        ),
        ch.softappeal.yass2.serialize.text.ClassTextEncoder(
            ch.softappeal.yass2.serialize.B::class, // 5
            { i ->
                writeProperty("a", i.a, 3)
                writeProperty("b", i.b, 3)
            },
            {
                ch.softappeal.yass2.serialize.B(
                    getProperty("a") as kotlin.Int,
                    getProperty("b") as kotlin.Int,
                ).apply {
                }
            },
            "a" to 3,
            "b" to 3,
        ),
        ch.softappeal.yass2.serialize.text.ClassTextEncoder(
            ch.softappeal.yass2.serialize.Poly::class, // 6
            { i ->
                writeProperty("a", i.a)
                writeProperty("b", i.b)
            },
            {
                ch.softappeal.yass2.serialize.Poly(
                    getProperty("a") as ch.softappeal.yass2.serialize.A,
                    getProperty("b") as ch.softappeal.yass2.serialize.B,
                ).apply {
                }
            },
            "a" to -1,
            "b" to -1,
        ),
        ch.softappeal.yass2.serialize.text.ClassTextEncoder(
            ch.softappeal.yass2.serialize.Example::class, // 7
            { i ->
                writeProperty("int", i.int, 3)
                writeProperty("intOptional", i.intOptional, 3)
                writeProperty("any", i.any)
                writeProperty("anyOptional", i.anyOptional)
                writeProperty("list", i.list, 2)
                writeProperty("listOptional", i.listOptional, 2)
            },
            {
                ch.softappeal.yass2.serialize.Example(
                    getProperty("int") as kotlin.Int,
                    getProperty("intOptional") as kotlin.Int?,
                    getProperty("any") as kotlin.Any,
                    getProperty("anyOptional") as kotlin.Any?,
                    getProperty("list") as kotlin.collections.List<kotlin.Int>,
                    getProperty("listOptional") as kotlin.collections.List<kotlin.Int>?,
                ).apply {
                    list?.forEach {}
                    listOptional?.forEach {}
                }
            },
            "int" to 3,
            "intOptional" to 3,
            "any" to -1,
            "anyOptional" to -1,
            "list" to -1,
            "listOptional" to -1,
        ),
        ch.softappeal.yass2.serialize.text.ClassTextEncoder(
            ch.softappeal.yass2.serialize.TextBuiltIn::class, // 8
            { i ->
                writeProperty("boolean", i.boolean, 1)
                writeProperty("booleanOptional", i.booleanOptional, 1)
                writeProperty("string", i.string, 0)
                writeProperty("stringOptional", i.stringOptional, 0)
            },
            {
                ch.softappeal.yass2.serialize.TextBuiltIn(
                    getProperty("boolean") as kotlin.Boolean,
                    getProperty("booleanOptional") as kotlin.Boolean?,
                    getProperty("string") as kotlin.String,
                    getProperty("stringOptional") as kotlin.String?,
                ).apply {
                }
            },
            "boolean" to -1,
            "booleanOptional" to -1,
            "string" to -1,
            "stringOptional" to -1,
        ),
    )
