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
)

package ch.softappeal.yass2.serialize

public fun binarySerializer(): ch.softappeal.yass2.serialize.binary.BinarySerializer =
    object : ch.softappeal.yass2.serialize.binary.BinarySerializer() {
        init {
            initialize(
                // kotlin.collections.List: 1
                ch.softappeal.yass2.serialize.binary.BooleanBinaryEncoder, // 2
                ch.softappeal.yass2.serialize.binary.IntBinaryEncoder, // 3
                ch.softappeal.yass2.serialize.binary.LongBinaryEncoder, // 4
                ch.softappeal.yass2.serialize.binary.StringBinaryEncoder, // 5
                ch.softappeal.yass2.serialize.binary.ByteArrayBinaryEncoder, // 6
                ch.softappeal.yass2.serialize.binary.EnumBinaryEncoder(
                    ch.softappeal.yass2.serialize.Gender::class, kotlin.enums.enumEntries(), // 7
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.serialize.A::class, // 8
                    { i ->
                        writeRequired(i.a, 3)
                    },
                    {
                        ch.softappeal.yass2.serialize.A(
                            a = readRequired(3) as kotlin.Int,
                        )
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.serialize.B::class, // 9
                    { i ->
                        writeRequired(i.a, 3)
                        writeRequired(i.b, 3)
                    },
                    {
                        ch.softappeal.yass2.serialize.B(
                            a = readRequired(3) as kotlin.Int,
                            b = readRequired(3) as kotlin.Int,
                        )
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.serialize.Poly::class, // 10
                    { i ->
                        writeObject(i.a)
                        writeRequired(i.b, 9)
                    },
                    {
                        ch.softappeal.yass2.serialize.Poly(
                            a = readObject() as ch.softappeal.yass2.serialize.A,
                            b = readRequired(9) as ch.softappeal.yass2.serialize.B,
                        )
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.serialize.ManyProperties::class, // 11
                    { i ->
                        writeRequired(i.h, 3)
                        writeRequired(i.d, 3)
                        writeRequired(i.f, 3)
                        writeRequired(i.g, 3)
                        writeRequired(i.b, 3)
                    },
                    {
                        ch.softappeal.yass2.serialize.ManyProperties(
                            h = readRequired(3) as kotlin.Int,
                            d = readRequired(3) as kotlin.Int,
                            f = readRequired(3) as kotlin.Int,
                            g = readRequired(3) as kotlin.Int,
                            b = readRequired(3) as kotlin.Int,
                        )
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.serialize.DivideByZeroException::class, // 12
                    { i ->
                    },
                    {
                        ch.softappeal.yass2.serialize.DivideByZeroException(
                        )
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.serialize.ThrowableFake::class, // 13
                    { i ->
                        writeOptional(i.cause, 5)
                        writeRequired(i.message, 5)
                    },
                    {
                        ch.softappeal.yass2.serialize.ThrowableFake(
                            cause = readOptional(5) as kotlin.String?,
                            message = readRequired(5) as kotlin.String,
                        )
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.serialize.Types::class, // 14
                    { i ->
                        writeRequired(i.boolean, 2)
                        writeRequired(i.int, 3)
                        writeRequired(i.long, 4)
                        writeRequired(i.string, 5)
                        writeRequired(i.bytes, 6)
                        writeRequired(i.gender, 7)
                        writeRequired(i.list, 1)
                        writeRequired(i.b, 9)
                        writeOptional(i.booleanOptional, 2)
                        writeOptional(i.intOptional, 3)
                        writeOptional(i.longOptional, 4)
                        writeOptional(i.stringOptional, 5)
                        writeOptional(i.bytesOptional, 6)
                        writeOptional(i.genderOptional, 7)
                        writeOptional(i.listOptional, 1)
                        writeOptional(i.bOptional, 9)
                    },
                    {
                        ch.softappeal.yass2.serialize.Types(
                            boolean = readRequired(2) as kotlin.Boolean,
                            int = readRequired(3) as kotlin.Int,
                            long = readRequired(4) as kotlin.Long,
                            string = readRequired(5) as kotlin.String,
                            bytes = readRequired(6) as kotlin.ByteArray,
                            gender = readRequired(7) as ch.softappeal.yass2.serialize.Gender,
                            list = readRequired(1) as kotlin.collections.List<kotlin.Any?>,
                            b = readRequired(9) as ch.softappeal.yass2.serialize.B,
                            booleanOptional = readOptional(2) as kotlin.Boolean?,
                            intOptional = readOptional(3) as kotlin.Int?,
                            longOptional = readOptional(4) as kotlin.Long?,
                            stringOptional = readOptional(5) as kotlin.String?,
                            bytesOptional = readOptional(6) as kotlin.ByteArray?,
                            genderOptional = readOptional(7) as ch.softappeal.yass2.serialize.Gender?,
                            listOptional = readOptional(1) as kotlin.collections.List<kotlin.Any?>?,
                            bOptional = readOptional(9) as ch.softappeal.yass2.serialize.B?,
                        )
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.serialize.Example::class, // 15
                    { i ->
                        writeRequired(i.int, 3)
                        writeOptional(i.intOptional, 3)
                        writeObject(i.any)
                        writeObject(i.anyOptional)
                        writeRequired(i.list, 1)
                        writeOptional(i.listOptional, 1)
                    },
                    {
                        ch.softappeal.yass2.serialize.Example(
                            int = readRequired(3) as kotlin.Int,
                            intOptional = readOptional(3) as kotlin.Int?,
                            any = readObject() as kotlin.Any,
                            anyOptional = readObject() as kotlin.Any?,
                            list = readRequired(1) as kotlin.collections.List<kotlin.Int>,
                            listOptional = readOptional(1) as kotlin.collections.List<kotlin.Int>?,
                        )
                    }
                ),
            )
        }
    }

public fun stringEncoders(): List<ch.softappeal.yass2.serialize.string.StringEncoder<*>> =
    listOf(
        // kotlin.String: 0
        // kotlin.Boolean: 1
        // kotlin.collections.List: 2
        ch.softappeal.yass2.serialize.string.IntStringEncoder, // 3
        ch.softappeal.yass2.serialize.string.LongStringEncoder, // 4
        ch.softappeal.yass2.serialize.string.ByteArrayStringEncoder, // 5
        ch.softappeal.yass2.serialize.string.EnumStringEncoder(
            ch.softappeal.yass2.serialize.Gender::class, // 6
            ch.softappeal.yass2.serialize.Gender::valueOf,
        ),
        ch.softappeal.yass2.serialize.string.ClassStringEncoder(
            ch.softappeal.yass2.serialize.A::class, // 7
            { i ->
                writeProperty("a", i.a, 3)
            },
            {
                ch.softappeal.yass2.serialize.A(
                    getProperty("a") as kotlin.Int,
                )
            },
            "a" to 3,
        ),
        ch.softappeal.yass2.serialize.string.ClassStringEncoder(
            ch.softappeal.yass2.serialize.B::class, // 8
            { i ->
                writeProperty("a", i.a, 3)
                writeProperty("b", i.b, 3)
            },
            {
                ch.softappeal.yass2.serialize.B(
                    getProperty("a") as kotlin.Int,
                    getProperty("b") as kotlin.Int,
                )
            },
            "a" to 3,
            "b" to 3,
        ),
        ch.softappeal.yass2.serialize.string.ClassStringEncoder(
            ch.softappeal.yass2.serialize.Poly::class, // 9
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
        ch.softappeal.yass2.serialize.string.ClassStringEncoder(
            ch.softappeal.yass2.serialize.ManyProperties::class, // 10
            { i ->
                writeProperty("h", i.h, 3)
                writeProperty("d", i.d, 3)
                writeProperty("f", i.f, 3)
                writeProperty("g", i.g, 3)
                writeProperty("b", i.b, 3)
            },
            {
                ch.softappeal.yass2.serialize.ManyProperties(
                    getProperty("h") as kotlin.Int,
                    getProperty("d") as kotlin.Int,
                    getProperty("f") as kotlin.Int,
                    getProperty("g") as kotlin.Int,
                    getProperty("b") as kotlin.Int,
                )
            },
            "h" to 3,
            "d" to 3,
            "f" to 3,
            "g" to 3,
            "b" to 3,
        ),
        ch.softappeal.yass2.serialize.string.ClassStringEncoder(
            ch.softappeal.yass2.serialize.DivideByZeroException::class, // 11
            { i ->
            },
            {
                ch.softappeal.yass2.serialize.DivideByZeroException(
                )
            },
        ),
        ch.softappeal.yass2.serialize.string.ClassStringEncoder(
            ch.softappeal.yass2.serialize.ThrowableFake::class, // 12
            { i ->
                writeProperty("cause", i.cause, 0)
                writeProperty("message", i.message, 0)
            },
            {
                ch.softappeal.yass2.serialize.ThrowableFake(
                    getProperty("cause") as kotlin.String?,
                    getProperty("message") as kotlin.String,
                )
            },
            "cause" to -1,
            "message" to -1,
        ),
        ch.softappeal.yass2.serialize.string.ClassStringEncoder(
            ch.softappeal.yass2.serialize.Types::class, // 13
            { i ->
                writeProperty("boolean", i.boolean, 1)
                writeProperty("int", i.int, 3)
                writeProperty("long", i.long, 4)
                writeProperty("string", i.string, 0)
                writeProperty("bytes", i.bytes, 5)
                writeProperty("gender", i.gender, 6)
                writeProperty("list", i.list, 2)
                writeProperty("b", i.b)
                writeProperty("booleanOptional", i.booleanOptional, 1)
                writeProperty("intOptional", i.intOptional, 3)
                writeProperty("longOptional", i.longOptional, 4)
                writeProperty("stringOptional", i.stringOptional, 0)
                writeProperty("bytesOptional", i.bytesOptional, 5)
                writeProperty("genderOptional", i.genderOptional, 6)
                writeProperty("listOptional", i.listOptional, 2)
                writeProperty("bOptional", i.bOptional)
            },
            {
                ch.softappeal.yass2.serialize.Types(
                    getProperty("boolean") as kotlin.Boolean,
                    getProperty("int") as kotlin.Int,
                    getProperty("long") as kotlin.Long,
                    getProperty("string") as kotlin.String,
                    getProperty("bytes") as kotlin.ByteArray,
                    getProperty("gender") as ch.softappeal.yass2.serialize.Gender,
                    getProperty("list") as kotlin.collections.List<kotlin.Any?>,
                    getProperty("b") as ch.softappeal.yass2.serialize.B,
                    getProperty("booleanOptional") as kotlin.Boolean?,
                    getProperty("intOptional") as kotlin.Int?,
                    getProperty("longOptional") as kotlin.Long?,
                    getProperty("stringOptional") as kotlin.String?,
                    getProperty("bytesOptional") as kotlin.ByteArray?,
                    getProperty("genderOptional") as ch.softappeal.yass2.serialize.Gender?,
                    getProperty("listOptional") as kotlin.collections.List<kotlin.Any?>?,
                    getProperty("bOptional") as ch.softappeal.yass2.serialize.B?,
                )
            },
            "boolean" to -1,
            "int" to 3,
            "long" to 4,
            "string" to -1,
            "bytes" to 5,
            "gender" to 6,
            "list" to -1,
            "b" to -1,
            "booleanOptional" to -1,
            "intOptional" to 3,
            "longOptional" to 4,
            "stringOptional" to -1,
            "bytesOptional" to 5,
            "genderOptional" to 6,
            "listOptional" to -1,
            "bOptional" to -1,
        ),
        ch.softappeal.yass2.serialize.string.ClassStringEncoder(
            ch.softappeal.yass2.serialize.Example::class, // 14
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
                )
            },
            "int" to 3,
            "intOptional" to 3,
            "any" to -1,
            "anyOptional" to -1,
            "list" to -1,
            "listOptional" to -1,
        ),
    )
