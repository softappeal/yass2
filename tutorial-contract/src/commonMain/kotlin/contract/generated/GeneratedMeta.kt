package ch.softappeal.yass2.tutorial.contract.generated

import ch.softappeal.yass2.*
import ch.softappeal.yass2.serialize.*
import ch.softappeal.yass2.serialize.binary.*
import ch.softappeal.yass2.transport.*
import ch.softappeal.yass2.tutorial.contract.*

public val ContractSerializer: BinarySerializer = generatedBinarySerializer(BaseEncoders)
public val MessageSerializer: Serializer = binaryMessageSerializer(ContractSerializer)

public val Dumper: Dumper = dumper(GeneratedDumperProperties, StringBuilder::valueDumper)
