- . `yass2-core`
    - Cleanup.kt `[common]`
    - Dumper.kt `[common]`
    - Interceptor.kt `[common]`
    - generate `yass2-generate`
        - Generate.kt `[jvm]`
        - GenerateBinarySerializer.kt `[jvm]`
        - GenerateDumperProperties.kt `[jvm]`
        - GenerateProxyFactory.kt `[jvm]`
        - GenerateRemote.kt `[jvm]`
    - reflect `yass2-reflect`
        - ReflectionDumper.kt `[jvm]`
        - ReflectionInterceptor.kt `[jvm]`
    - remote `yass2-core`
        - Message.kt `[common]`
        - Remote.kt `[common]`
        - coroutines `yass2-coroutines`
            - Sync.kt `[common]`
            - session `yass2-coroutines`
                - Session.kt `[common]`
                - SessionConnector.kt `[common]`
        - reflect `yass2-reflect`
            - ReflectionRemote.kt `[jvm]`
    - serialize `yass2-core`
        - Serializer.kt `[common]`
        - binary `yass2-core`
            - BaseEncoders.kt `[common]`
            - BinarySerializer.kt `[common]`
            - Int.kt `[common]`
            - VarInt.kt `[common]`
            - reflect `yass2-reflect`
                - BinarySerializerMeta.kt `[common]`
                - ReflectionBinarySerializer.kt `[jvm]`
    - transport `yass2-core`
        - BinaryMessageSerializer.kt `[common]`
        - Bytes.kt `[common]`
        - Transport.kt `[common]`
        - js `yass2-coroutines`
            - Connect.kt `[js]`
            - Tunnel.kt `[js]`
        - ktor `yass2-ktor`
            - ByteChannel.kt `[common]`
            - Http.kt `[common]`
            - HttpServer.kt `[jvmAndNix]`
            - Socket.kt `[jvmAndNix]`
            - WebSocket.kt `[common]`
        - session `yass2-coroutines`
            - BinaryPacketSerializer.kt `[common]`