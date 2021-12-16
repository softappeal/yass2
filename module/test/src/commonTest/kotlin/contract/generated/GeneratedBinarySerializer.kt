package ch.softappeal.yass2.contract.generated

@Suppress("UNCHECKED_CAST", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier")
public fun generatedBinarySerializer(
    baseEncoders: List<ch.softappeal.yass2.serialize.binary.BaseEncoder<*>>,
): ch.softappeal.yass2.serialize.binary.BinarySerializer =
    ch.softappeal.yass2.serialize.binary.BinarySerializer(baseEncoders + listOf(
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.IntException::class,
            { w, i ->
                w.writeNoIdOptional(2, i.i)
            },
            { r ->
                val i = ch.softappeal.yass2.contract.IntException(
                    r.readNoIdOptional(2) as kotlin.Int?,
                )
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.PlainId::class,
            { w, i ->
                w.writeNoIdRequired(2, i.id)
            },
            { r ->
                val i = ch.softappeal.yass2.contract.PlainId(
                    r.readNoIdRequired(2) as kotlin.Int,
                )
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.ComplexId::class,
            { w, i ->
                w.writeWithId(i.baseId)
                w.writeWithId(i.baseIdOptional)
                w.writeNoIdRequired(6, i.plainId)
                w.writeNoIdOptional(6, i.plainIdOptional)
                w.writeNoIdRequired(2, i.id)
            },
            { r ->
                val i = ch.softappeal.yass2.contract.ComplexId(
                    r.readWithId() as ch.softappeal.yass2.contract.Id,
                    r.readWithId() as ch.softappeal.yass2.contract.Id?,
                    r.readNoIdRequired(6) as ch.softappeal.yass2.contract.PlainId,
                    r.readNoIdOptional(6) as ch.softappeal.yass2.contract.PlainId?,
                )
                i.id = r.readNoIdRequired(2) as kotlin.Int
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.Lists::class,
            { w, i ->
                w.writeNoIdRequired(1, i.list)
                w.writeNoIdOptional(1, i.listOptional)
                w.writeNoIdRequired(1, i.mutableList)
            },
            { r ->
                val i = ch.softappeal.yass2.contract.Lists(
                    r.readNoIdRequired(1) as kotlin.collections.List<ch.softappeal.yass2.contract.Id>,
                    r.readNoIdOptional(1) as kotlin.collections.List<ch.softappeal.yass2.contract.Id>?,
                    r.readNoIdRequired(1) as kotlin.collections.MutableList<ch.softappeal.yass2.contract.Id>,
                )
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.Id2::class,
            { w, i ->
                w.writeNoIdRequired(2, i.id)
            },
            { r ->
                val i = ch.softappeal.yass2.contract.Id2(
                )
                i.id = r.readNoIdRequired(2) as kotlin.Int
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.Id3::class,
            { w, i ->
                w.writeNoIdRequired(2, i.id)
            },
            { r ->
                val i = ch.softappeal.yass2.contract.Id3(
                )
                i.id = r.readNoIdRequired(2) as kotlin.Int
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.IdWrapper::class,
            { w, i ->
                w.writeWithId(i.id)
                w.writeWithId(i.idOptional)
            },
            { r ->
                val i = ch.softappeal.yass2.contract.IdWrapper(
                    r.readWithId() as ch.softappeal.yass2.contract.Id2,
                    r.readWithId() as ch.softappeal.yass2.contract.Id2?,
                )
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.ManyProperties::class,
            { w, i ->
                w.writeNoIdRequired(2, i.h)
                w.writeNoIdRequired(2, i.d)
                w.writeNoIdRequired(2, i.f)
                w.writeNoIdRequired(2, i.g)
                w.writeNoIdRequired(2, i.b)
                w.writeNoIdRequired(2, i.a)
                w.writeNoIdRequired(2, i.c)
                w.writeNoIdRequired(2, i.e)
                w.writeNoIdRequired(2, i.i)
                w.writeNoIdRequired(2, i.j)
            },
            { r ->
                val i = ch.softappeal.yass2.contract.ManyProperties(
                    r.readNoIdRequired(2) as kotlin.Int,
                    r.readNoIdRequired(2) as kotlin.Int,
                    r.readNoIdRequired(2) as kotlin.Int,
                    r.readNoIdRequired(2) as kotlin.Int,
                    r.readNoIdRequired(2) as kotlin.Int,
                )
                i.a = r.readNoIdRequired(2) as kotlin.Int
                i.c = r.readNoIdRequired(2) as kotlin.Int
                i.e = r.readNoIdRequired(2) as kotlin.Int
                i.i = r.readNoIdRequired(2) as kotlin.Int
                i.j = r.readNoIdRequired(2) as kotlin.Int
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.DivideByZeroException::class,
            { _, _ -> },
            {
                val i = ch.softappeal.yass2.contract.DivideByZeroException(
                )
                i
            }
        ),
    ))
