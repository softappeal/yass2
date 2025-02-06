package ch.softappeal.yass2.serialize.utf8

// Doesn't work for whole numbers on js target: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/isInteger
public object DoubleUtf8Encoder : Utf8Encoder<Double>(Double::class,
    { value -> writeString(value.toString()) },
    { readString().toDouble() }
)
