package ch.softappeal.yass2.tutorial.contract.generated

@Suppress("RedundantSuppression", "UNCHECKED_CAST", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier")
public fun generatedBinarySerializer(
    baseEncoders: List<ch.softappeal.yass2.serialize.binary.BaseEncoder<*>>,
): ch.softappeal.yass2.serialize.binary.BinarySerializer =
    ch.softappeal.yass2.serialize.binary.BinarySerializer(baseEncoders + listOf(
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.tutorial.contract.Address::class, false,
            { w, i ->
                w.writeNoIdRequired(4, i.street)
                w.writeNoIdOptional(3, i.number)
            },
            { r ->
                val i = ch.softappeal.yass2.tutorial.contract.Address(
                    r.readNoIdRequired(4) as kotlin.String,
                )
                i.number = r.readNoIdOptional(3) as kotlin.Int?
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.tutorial.contract.Person::class, false,
            { w, i ->
                w.writeNoIdRequired(4, i.name)
                w.writeNoIdRequired(5, i.gender)
                w.writeNoIdRequired(1, i.addresses)
            },
            { r ->
                val i = ch.softappeal.yass2.tutorial.contract.Person(
                    r.readNoIdRequired(4) as kotlin.String,
                    r.readNoIdRequired(5) as ch.softappeal.yass2.tutorial.contract.Gender,
                    r.readNoIdRequired(1) as kotlin.collections.List<ch.softappeal.yass2.tutorial.contract.Address>,
                )
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.tutorial.contract.DivideByZeroException::class, false,
            { _, _ -> },
            {
                val i = ch.softappeal.yass2.tutorial.contract.DivideByZeroException(
                )
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.tutorial.contract.SubClass::class, false,
            { w, i ->
                w.writeNoIdRequired(4, i.baseClassProperty)
                w.writeNoIdRequired(4, i.subClassProperty)
            },
            { r ->
                val i = ch.softappeal.yass2.tutorial.contract.SubClass(
                    r.readNoIdRequired(4) as kotlin.String,
                    r.readNoIdRequired(4) as kotlin.String,
                )
                i
            }
        ),
    ))
