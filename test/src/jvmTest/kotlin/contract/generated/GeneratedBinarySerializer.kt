@file:Suppress(
    "UNCHECKED_CAST",
    "USELESS_CAST",
    "PARAMETER_NAME_CHANGED_ON_OVERRIDE",
    "unused",
    "RemoveRedundantQualifierName",
    "SpellCheckingInspection",
    "RedundantVisibilityModifier",
    "RedundantNullableReturnType",
    "KotlinRedundantDiagnosticSuppress",
    "RedundantSuppression",
)

package ch.softappeal.yass2.contract.generated

public fun createSerializer(): ch.softappeal.yass2.serialize.binary.BinarySerializer = ch.softappeal.yass2.serialize.binary.BinarySerializer(listOf(
    ch.softappeal.yass2.serialize.binary.IntEncoder(),
    ch.softappeal.yass2.serialize.binary.StringEncoder(),
    ch.softappeal.yass2.serialize.binary.ByteArrayEncoder(),
    ch.softappeal.yass2.contract.GenderEncoder(),
    ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.IntException::class, false,
        { w, i ->
            w.writeNoIdOptional(3, i.i)
        },
        { r ->
            val i = ch.softappeal.yass2.contract.IntException(
                r.readNoIdOptional(3) as kotlin.Int?,
            )
            i
        }
    ),
    ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.PlainId::class, false,
        { w, i ->
            w.writeNoIdRequired(3, i.id)
        },
        { r ->
            val i = ch.softappeal.yass2.contract.PlainId(
                r.readNoIdRequired(3) as kotlin.Int,
            )
            i
        }
    ),
    ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.ComplexId::class, false,
        { w, i ->
            w.writeWithId(i.baseId)
            w.writeWithId(i.baseIdOptional)
            w.writeWithId(i.plainId)
            w.writeWithId(i.plainIdOptional)
            w.writeNoIdRequired(3, i.id)
        },
        { r ->
            val i = ch.softappeal.yass2.contract.ComplexId(
                r.readWithId() as ch.softappeal.yass2.contract.Id,
                r.readWithId() as ch.softappeal.yass2.contract.Id?,
                r.readWithId() as ch.softappeal.yass2.contract.PlainId,
                r.readWithId() as ch.softappeal.yass2.contract.PlainId?,
            )
            i.id = r.readNoIdRequired(3) as kotlin.Int
            i
        }
    ),
    ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.Lists::class, false,
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
    ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.Id2::class, false,
        { w, i ->
            w.writeNoIdRequired(3, i.id)
        },
        { r ->
            val i = ch.softappeal.yass2.contract.Id2(
            )
            i.id = r.readNoIdRequired(3) as kotlin.Int
            i
        }
    ),
    ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.Id3::class, false,
        { w, i ->
            w.writeNoIdRequired(3, i.id)
        },
        { r ->
            val i = ch.softappeal.yass2.contract.Id3(
            )
            i.id = r.readNoIdRequired(3) as kotlin.Int
            i
        }
    ),
    ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.IdWrapper::class, false,
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
    ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.ManyProperties::class, false,
        { w, i ->
            w.writeNoIdRequired(3, i.h)
            w.writeNoIdRequired(3, i.d)
            w.writeNoIdRequired(3, i.f)
            w.writeNoIdRequired(3, i.g)
            w.writeNoIdRequired(3, i.b)
            w.writeNoIdRequired(3, i.a)
            w.writeNoIdRequired(3, i.c)
            w.writeNoIdRequired(3, i.e)
            w.writeNoIdRequired(3, i.i)
            w.writeNoIdRequired(3, i.j)
        },
        { r ->
            val i = ch.softappeal.yass2.contract.ManyProperties(
                r.readNoIdRequired(3) as kotlin.Int,
                r.readNoIdRequired(3) as kotlin.Int,
                r.readNoIdRequired(3) as kotlin.Int,
                r.readNoIdRequired(3) as kotlin.Int,
                r.readNoIdRequired(3) as kotlin.Int,
            )
            i.a = r.readNoIdRequired(3) as kotlin.Int
            i.c = r.readNoIdRequired(3) as kotlin.Int
            i.e = r.readNoIdRequired(3) as kotlin.Int
            i.i = r.readNoIdRequired(3) as kotlin.Int
            i.j = r.readNoIdRequired(3) as kotlin.Int
            i
        }
    ),
    ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.DivideByZeroException::class, false,
        { _, _ -> },
        {
            val i = ch.softappeal.yass2.contract.DivideByZeroException(
            )
            i
        }
    ),
    ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.ThrowableFake::class, false,
        { w, i ->
            w.writeNoIdRequired(4, i.cause)
            w.writeNoIdRequired(4, i.message)
        },
        { r ->
            val i = ch.softappeal.yass2.contract.ThrowableFake(
                r.readNoIdRequired(4) as kotlin.String,
                r.readNoIdRequired(4) as kotlin.String,
            )
            i
        }
    ),
    ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.Node::class, true,
        { w, i ->
            w.writeNoIdRequired(3, i.id)
            w.writeWithId(i.link)
        },
        { r ->
            val i = r.created(ch.softappeal.yass2.contract.Node(
                r.readNoIdRequired(3) as kotlin.Int,
            ))
            i.link = r.readWithId() as ch.softappeal.yass2.contract.Node?
            i
        }
    ),
))
