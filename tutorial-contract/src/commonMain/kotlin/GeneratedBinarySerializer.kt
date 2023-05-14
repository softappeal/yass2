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

package ch.softappeal.yass2.tutorial.contract

public val GeneratedBinarySerializer: ch.softappeal.yass2.serialize.binary.BinarySerializer =
    ch.softappeal.yass2.serialize.binary.BinarySerializer(ch.softappeal.yass2.tutorial.contract.BaseEncoders + listOf(
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.tutorial.contract.Address::class, false,
            { w, i ->
                w.writeNoIdRequired(5, i.street)
                w.writeNoIdOptional(3, i.number)
            },
            { r ->
                val i = ch.softappeal.yass2.tutorial.contract.Address(
                    r.readNoIdRequired(5) as kotlin.String,
                )
                i.number = r.readNoIdOptional(3) as kotlin.Int?
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.tutorial.contract.Person::class, false,
            { w, i ->
                w.writeNoIdRequired(5, i.name)
                w.writeNoIdRequired(6, i.gender)
                w.writeNoIdRequired(1, i.addresses)
            },
            { r ->
                val i = ch.softappeal.yass2.tutorial.contract.Person(
                    r.readNoIdRequired(5) as kotlin.String,
                    r.readNoIdRequired(6) as ch.softappeal.yass2.tutorial.contract.Gender,
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
                w.writeNoIdRequired(5, i.baseClassProperty)
                w.writeNoIdRequired(5, i.subClassProperty)
            },
            { r ->
                val i = ch.softappeal.yass2.tutorial.contract.SubClass(
                    r.readNoIdRequired(5) as kotlin.String,
                    r.readNoIdRequired(5) as kotlin.String,
                )
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.tutorial.contract.BooleanFlowId::class, false,
            { _, _ -> },
            {
                val i = ch.softappeal.yass2.tutorial.contract.BooleanFlowId(
                )
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.tutorial.contract.IntFlowId::class, false,
            { w, i ->
                w.writeNoIdRequired(3, i.max)
            },
            { r ->
                val i = ch.softappeal.yass2.tutorial.contract.IntFlowId(
                    r.readNoIdRequired(3) as kotlin.Int,
                )
                i
            }
        ),
    ))
