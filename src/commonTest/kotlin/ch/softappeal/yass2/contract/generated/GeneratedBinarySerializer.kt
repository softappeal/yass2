package ch.softappeal.yass2.contract.generated

@Suppress("UNCHECKED_CAST", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier")
public fun generatedBinarySerializer(
    baseEncoders: List<ch.softappeal.yass2.serialize.binary.BaseEncoder<*>>
): ch.softappeal.yass2.serialize.binary.BinarySerializer =
    ch.softappeal.yass2.serialize.binary.BinarySerializer(baseEncoders + listOf(
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.IntException::class, // 6
            { w, i ->
                w.writeNoIdOptional(3, i.i)
            },
            { r ->
                val i = r.created(ch.softappeal.yass2.contract.IntException(
                    r.readNoIdOptional(3) as kotlin.Int?
                ))
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.PlainId::class, // 7
            { w, i ->
                w.writeNoIdRequired(3, i.id)
            },
            { r ->
                val i = r.created(ch.softappeal.yass2.contract.PlainId(
                    r.readNoIdRequired(3) as kotlin.Int
                ))
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.ComplexId::class, // 8
            { w, i ->
                w.writeWithId(i.baseId)
                w.writeWithId(i.baseIdOptional)
                w.writeWithId(i.plainId)
                w.writeWithId(i.plainIdOptional)
                w.writeNoIdRequired(3, i.id)
            },
            { r ->
                val i = r.created(ch.softappeal.yass2.contract.ComplexId(
                    r.readWithId() as ch.softappeal.yass2.contract.Id,
                    r.readWithId() as ch.softappeal.yass2.contract.Id?,
                    r.readWithId() as ch.softappeal.yass2.contract.PlainId,
                    r.readWithId() as ch.softappeal.yass2.contract.PlainId?
                ))
                i.id = r.readNoIdRequired(3) as kotlin.Int
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.Lists::class, // 9
            { w, i ->
                w.writeNoIdRequired(1, i.list)
                w.writeNoIdOptional(1, i.listOptional)
                w.writeNoIdRequired(1, i.mutableList)
            },
            { r ->
                val i = r.created(ch.softappeal.yass2.contract.Lists(
                    r.readNoIdRequired(1) as kotlin.collections.List<ch.softappeal.yass2.contract.Id>,
                    r.readNoIdOptional(1) as kotlin.collections.List<ch.softappeal.yass2.contract.Id>?,
                    r.readNoIdRequired(1) as kotlin.collections.MutableList<ch.softappeal.yass2.contract.Id>
                ))
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.Id2::class, // 10
            { w, i ->
                w.writeNoIdRequired(3, i.id)
            },
            { r ->
                val i = r.created(ch.softappeal.yass2.contract.Id2(
                ))
                i.id = r.readNoIdRequired(3) as kotlin.Int
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.Id3::class, // 11
            { w, i ->
                w.writeNoIdRequired(3, i.id)
            },
            { r ->
                val i = r.created(ch.softappeal.yass2.contract.Id3(
                ))
                i.id = r.readNoIdRequired(3) as kotlin.Int
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.IdWrapper::class, // 12
            { w, i ->
                w.writeWithId(i.id)
                w.writeWithId(i.idOptional)
            },
            { r ->
                val i = r.created(ch.softappeal.yass2.contract.IdWrapper(
                    r.readWithId() as ch.softappeal.yass2.contract.Id2,
                    r.readWithId() as ch.softappeal.yass2.contract.Id2?
                ))
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.ManyProperties::class, // 13
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
                val i = r.created(ch.softappeal.yass2.contract.ManyProperties(
                    r.readNoIdRequired(3) as kotlin.Int,
                    r.readNoIdRequired(3) as kotlin.Int,
                    r.readNoIdRequired(3) as kotlin.Int,
                    r.readNoIdRequired(3) as kotlin.Int,
                    r.readNoIdRequired(3) as kotlin.Int
                ))
                i.a = r.readNoIdRequired(3) as kotlin.Int
                i.c = r.readNoIdRequired(3) as kotlin.Int
                i.e = r.readNoIdRequired(3) as kotlin.Int
                i.i = r.readNoIdRequired(3) as kotlin.Int
                i.j = r.readNoIdRequired(3) as kotlin.Int
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.remote.Request::class, // 14
            { w, i ->
                w.writeNoIdRequired(3, i.serviceId)
                w.writeNoIdRequired(3, i.functionId)
                w.writeNoIdRequired(1, i.parameters)
            },
            { r ->
                val i = r.created(ch.softappeal.yass2.remote.Request(
                    r.readNoIdRequired(3) as kotlin.Int,
                    r.readNoIdRequired(3) as kotlin.Int,
                    r.readNoIdRequired(1) as kotlin.collections.List<kotlin.Any?>
                ))
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.remote.ValueReply::class, // 15
            { w, i ->
                w.writeWithId(i.value)
            },
            { r ->
                val i = r.created(ch.softappeal.yass2.remote.ValueReply(
                    r.readWithId()
                ))
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.remote.ExceptionReply::class, // 16
            { w, i ->
                w.writeWithId(i.exception)
            },
            { r ->
                val i = r.created(ch.softappeal.yass2.remote.ExceptionReply(
                    r.readWithId() as kotlin.Exception /* = java.lang.Exception */
                ))
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.DivideByZeroException::class, // 17
            { _, _ -> },
            { r ->
                val i = r.created(ch.softappeal.yass2.contract.DivideByZeroException(
                ))
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.remote.coroutines.session.Packet::class, // 18
            { w, i ->
                w.writeNoIdRequired(3, i.requestNumber)
                w.writeWithId(i.message)
            },
            { r ->
                val i = r.created(ch.softappeal.yass2.remote.coroutines.session.Packet(
                    r.readNoIdRequired(3) as kotlin.Int,
                    r.readWithId() as ch.softappeal.yass2.remote.Message
                ))
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.Node::class, // 19
            { w, i ->
                w.writeNoIdRequired(3, i.id)
                w.writeWithId(i.link)
            },
            { r ->
                val i = r.created(ch.softappeal.yass2.contract.Node(
                    r.readNoIdRequired(3) as kotlin.Int
                ))
                i.link = r.readWithId() as ch.softappeal.yass2.contract.Node?
                i
            }
        )
    ))
