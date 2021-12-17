package ch.softappeal.yass2.tutorial.contract.generated

@Suppress("UNCHECKED_CAST", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier")
public fun generatedBinarySerializer(
    baseEncodersSupplier: () -> List<ch.softappeal.yass2.serialize.binary.BaseEncoder<*>>,
): ch.softappeal.yass2.serialize.binary.BinarySerializer =
    ch.softappeal.yass2.serialize.binary.BinarySerializer(baseEncodersSupplier() + listOf(
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.tutorial.contract.Address::class,
            { w, i ->
                w.writeNoIdRequired(3, i.street)
                w.writeNoIdOptional(2, i.number)
            },
            { r ->
                val i = ch.softappeal.yass2.tutorial.contract.Address(
                    r.readNoIdRequired(3) as kotlin.String,
                )
                i.number = r.readNoIdOptional(2) as kotlin.Int?
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.tutorial.contract.Person::class,
            { w, i ->
                w.writeNoIdRequired(3, i.name)
                w.writeNoIdRequired(4, i.gender)
                w.writeNoIdRequired(1, i.addresses)
            },
            { r ->
                val i = ch.softappeal.yass2.tutorial.contract.Person(
                    r.readNoIdRequired(3) as kotlin.String,
                    r.readNoIdRequired(4) as ch.softappeal.yass2.tutorial.contract.Gender,
                    r.readNoIdRequired(1) as kotlin.collections.List<ch.softappeal.yass2.tutorial.contract.Address>,
                )
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.tutorial.contract.DivideByZeroException::class,
            { _, _ -> },
            {
                val i = ch.softappeal.yass2.tutorial.contract.DivideByZeroException(
                )
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.tutorial.contract.SubClass::class,
            { w, i ->
                w.writeNoIdRequired(3, i.baseClassProperty)
                w.writeNoIdRequired(3, i.subClassProperty)
            },
            { r ->
                val i = ch.softappeal.yass2.tutorial.contract.SubClass(
                    r.readNoIdRequired(3) as kotlin.String,
                    r.readNoIdRequired(3) as kotlin.String,
                )
                i
            }
        ),
    ))
