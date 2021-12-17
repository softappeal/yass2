package ch.softappeal.yass2.tutorial.contract

import ch.softappeal.yass2.*
import ch.softappeal.yass2.transport.*
import ch.softappeal.yass2.transport.session.*
import ch.softappeal.yass2.tutorial.contract.generated.*

// This file describes the needed contract metadata that depends on generated artifacts.

val ContractSerializer = generatedBinarySerializer(::baseEncoders)
val MessageSerializer = binaryMessageSerializer(ContractSerializer)
val PacketSerializer = binaryPacketSerializer(MessageSerializer)

val MessageTransport = Transport(MessageSerializer, 100)
val PacketTransport = Transport(PacketSerializer, 100)

val Dumper = dumper(generatedDumperProperties(), StringBuilder::valueDumper)
