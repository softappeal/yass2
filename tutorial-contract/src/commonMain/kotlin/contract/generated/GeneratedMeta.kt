package ch.softappeal.yass2.tutorial.contract.generated

import ch.softappeal.yass2.*
import ch.softappeal.yass2.serialize.*
import ch.softappeal.yass2.serialize.binary.*
import ch.softappeal.yass2.transport.*
import ch.softappeal.yass2.transport.session.*
import ch.softappeal.yass2.tutorial.contract.*

// This file describes the needed contract metadata that depends on generated artifacts.

public val Dumper: Dumper = dumper(GeneratedDumperProperties, StringBuilder::valueDumper)

public val ContractSerializer: BinarySerializer = generatedBinarySerializer(BaseEncoders)
public val MessageSerializer: Serializer = binaryMessageSerializer(ContractSerializer)
public val PacketSerializer: Serializer = binaryPacketSerializer(MessageSerializer)

private const val INITIAL_WRITER_CAPACITY = 100
public val MessageTransport: Transport = Transport(MessageSerializer, INITIAL_WRITER_CAPACITY)
public val PacketTransport: Transport = Transport(PacketSerializer, INITIAL_WRITER_CAPACITY)
