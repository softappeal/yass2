package ch.softappeal.yass2.tutorial

import ch.softappeal.yass2.Dumper
import ch.softappeal.yass2.serialize.Serializer
import ch.softappeal.yass2.transport.Transport
import ch.softappeal.yass2.transport.binaryMessageSerializer
import ch.softappeal.yass2.transport.session.binaryPacketSerializer

// This file describes the contract that depends on generated artifacts.

public val ContractSerializer: Serializer = createSerializer(BaseEncoders)
public val MessageSerializer: Serializer = binaryMessageSerializer(ContractSerializer)
public val PacketSerializer: Serializer = binaryPacketSerializer(MessageSerializer)

private const val INITIAL_WRITER_CAPACITY = 100
public val MessageTransport: Transport = Transport(MessageSerializer, INITIAL_WRITER_CAPACITY)
public val PacketTransport: Transport = Transport(PacketSerializer, INITIAL_WRITER_CAPACITY)

public val Dumper: Dumper = createDumper(Appendable::dumpValue)
