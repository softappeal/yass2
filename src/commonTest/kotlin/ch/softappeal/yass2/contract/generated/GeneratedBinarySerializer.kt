package ch.softappeal.yass2.contract.generated

@Suppress("UNCHECKED_CAST", "RemoveRedundantQualifierName", "SpellCheckingInspection")
fun generatedBinarySerializer(
    baseEncoders: List<ch.softappeal.yass2.serialize.binary.BaseEncoder<*>>
) = ch.softappeal.yass2.serialize.binary.BinarySerializer(baseEncoders + listOf(
    ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.IntException::class, // 5
        { w, i ->
            w.writeNoIdOptional(2, i.i)
        },
        { r ->
            val pi = r.readNoIdOptional(2) as kotlin.Int?
            val i = ch.softappeal.yass2.contract.IntException(pi)
            i
        }
    ),
    ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.PlainId::class, // 6
        { w, i ->
            w.writeNoIdRequired(2, i.id)
        },
        { r ->
            val pid = r.readNoIdRequired(2) as kotlin.Int
            val i = ch.softappeal.yass2.contract.PlainId(pid)
            i
        }
    ),
    ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.ComplexId::class, // 7
        { w, i ->
            w.writeWithId(i.baseId)
            w.writeWithId(i.baseIdOptional)
            w.writeNoIdRequired(2, i.id)
            w.writeNoIdRequired(6, i.plainId)
            w.writeNoIdOptional(6, i.plainIdOptional)
        },
        { r ->
            val pbaseId = r.readWithId() as ch.softappeal.yass2.contract.Id
            val pbaseIdOptional = r.readWithId() as ch.softappeal.yass2.contract.Id?
            val pid = r.readNoIdRequired(2) as kotlin.Int
            val pplainId = r.readNoIdRequired(6) as ch.softappeal.yass2.contract.PlainId
            val pplainIdOptional = r.readNoIdOptional(6) as ch.softappeal.yass2.contract.PlainId?
            val i = ch.softappeal.yass2.contract.ComplexId(pbaseId, pbaseIdOptional, pplainId, pplainIdOptional)
            i.id = pid
            i
        }
    ),
    ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.Lists::class, // 8
        { w, i ->
            w.writeNoIdRequired(1, i.list)
            w.writeNoIdOptional(1, i.listOptional)
            w.writeNoIdRequired(1, i.mutableList)
        },
        { r ->
            val plist = r.readNoIdRequired(1) as kotlin.collections.List<ch.softappeal.yass2.contract.Id>
            val plistOptional = r.readNoIdOptional(1) as kotlin.collections.List<ch.softappeal.yass2.contract.Id>?
            val pmutableList = r.readNoIdRequired(1) as kotlin.collections.MutableList<ch.softappeal.yass2.contract.Id>
            val i = ch.softappeal.yass2.contract.Lists(plist, plistOptional, pmutableList)
            i
        }
    ),
    ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.Id2::class, // 9
        { w, i ->
            w.writeNoIdRequired(2, i.id)
        },
        { r ->
            val pid = r.readNoIdRequired(2) as kotlin.Int
            val i = ch.softappeal.yass2.contract.Id2()
            i.id = pid
            i
        }
    ),
    ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.Id3::class, // 10
        { w, i ->
            w.writeNoIdRequired(2, i.id)
        },
        { r ->
            val pid = r.readNoIdRequired(2) as kotlin.Int
            val i = ch.softappeal.yass2.contract.Id3()
            i.id = pid
            i
        }
    ),
    ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.IdWrapper::class, // 11
        { w, i ->
            w.writeWithId(i.id)
            w.writeWithId(i.idOptional)
        },
        { r ->
            val pid = r.readWithId() as ch.softappeal.yass2.contract.Id2
            val pidOptional = r.readWithId() as ch.softappeal.yass2.contract.Id2?
            val i = ch.softappeal.yass2.contract.IdWrapper(pid, pidOptional)
            i
        }
    ),
    ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.ManyProperties::class, // 12
        { w, i ->
            w.writeNoIdRequired(2, i.a)
            w.writeNoIdRequired(2, i.b)
            w.writeNoIdRequired(2, i.c)
            w.writeNoIdRequired(2, i.d)
            w.writeNoIdRequired(2, i.e)
            w.writeNoIdRequired(2, i.f)
            w.writeNoIdRequired(2, i.g)
            w.writeNoIdRequired(2, i.h)
            w.writeNoIdRequired(2, i.i)
            w.writeNoIdRequired(2, i.j)
        },
        { r ->
            val pa = r.readNoIdRequired(2) as kotlin.Int
            val pb = r.readNoIdRequired(2) as kotlin.Int
            val pc = r.readNoIdRequired(2) as kotlin.Int
            val pd = r.readNoIdRequired(2) as kotlin.Int
            val pe = r.readNoIdRequired(2) as kotlin.Int
            val pf = r.readNoIdRequired(2) as kotlin.Int
            val pg = r.readNoIdRequired(2) as kotlin.Int
            val ph = r.readNoIdRequired(2) as kotlin.Int
            val pi = r.readNoIdRequired(2) as kotlin.Int
            val pj = r.readNoIdRequired(2) as kotlin.Int
            val i = ch.softappeal.yass2.contract.ManyProperties(ph, pd, pf, pg, pb)
            i.a = pa
            i.c = pc
            i.e = pe
            i.i = pi
            i.j = pj
            i
        }
    ),
    ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.remote.Request::class, // 13
        { w, i ->
            w.writeNoIdRequired(2, i.functionId)
            w.writeNoIdRequired(1, i.parameters)
            w.writeNoIdRequired(2, i.serviceId)
        },
        { r ->
            val pfunctionId = r.readNoIdRequired(2) as kotlin.Int
            val pparameters = r.readNoIdRequired(1) as kotlin.collections.List<kotlin.Any?>
            val pserviceId = r.readNoIdRequired(2) as kotlin.Int
            val i = ch.softappeal.yass2.remote.Request(pserviceId, pfunctionId, pparameters)
            i
        }
    ),
    ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.remote.ValueReply::class, // 14
        { w, i ->
            w.writeWithId(i.value)
        },
        { r ->
            val pvalue = r.readWithId()
            val i = ch.softappeal.yass2.remote.ValueReply(pvalue)
            i
        }
    ),
    ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.remote.ExceptionReply::class, // 15
        { w, i ->
            w.writeWithId(i.exception)
        },
        { r ->
            val pexception = r.readWithId() as kotlin.Exception /* = java.lang.Exception */
            val i = ch.softappeal.yass2.remote.ExceptionReply(pexception)
            i
        }
    ),
    ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.DivideByZeroException::class, // 16
        { _, _ -> },
        {
            val i = ch.softappeal.yass2.contract.DivideByZeroException()
            i
        }
    ),
    ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.remote.session.Packet::class, // 17
        { w, i ->
            w.writeWithId(i.message)
            w.writeNoIdRequired(2, i.requestNumber)
        },
        { r ->
            val pmessage = r.readWithId() as ch.softappeal.yass2.remote.Message
            val prequestNumber = r.readNoIdRequired(2) as kotlin.Int
            val i = ch.softappeal.yass2.remote.session.Packet(prequestNumber, pmessage)
            i
        }
    )
))
