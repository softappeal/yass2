package ch.softappeal.yass2.transport

import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.serialize.*

public class ContextMessageSerializer<C>(
    private val contextSerializer: Serializer, private val messageSerializer: Serializer,
    private val getContext: () -> C, private val setContext: (context: C) -> Unit,
) : Serializer {
    override fun write(writer: Writer, value: Any?) {
        contextSerializer.write(writer, getContext())
        messageSerializer.write(writer, value)
    }

    override fun read(reader: Reader): Message {
        setContext(@Suppress("UNCHECKED_CAST") (contextSerializer.read(reader) as C))
        return messageSerializer.read(reader) as Message
    }
}
