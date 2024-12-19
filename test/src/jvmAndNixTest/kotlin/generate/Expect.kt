package ch.softappeal.yass2.generate

import ch.softappeal.yass2.Dumper
import ch.softappeal.yass2.Interceptor
import ch.softappeal.yass2.SuspendInterceptor
import ch.softappeal.yass2.ValueDumper
import ch.softappeal.yass2.remote.Service
import ch.softappeal.yass2.remote.ServiceId
import ch.softappeal.yass2.remote.Tunnel
import ch.softappeal.yass2.serialize.binary.BinarySerializer

// TODO: shows how to use KSP generate in none-platform code
//       https://slack-chats.kotlinlang.org/t/16366233/i-m-trying-out-kotlin-2-0-beta-3-and-it-looks-like-generated
//       Common/intermediate (= none-platform) code cannot reference generated code in the compilation of platform code.
//       Generated codes are treated as platform code (you'll have to use expect/actual).

expect fun Adder.proxy(intercept: Interceptor): Adder

expect fun SuspendAdder.proxy(suspendIntercept: SuspendInterceptor): SuspendAdder
expect fun ServiceId<SuspendAdder>.proxy(tunnel: Tunnel): SuspendAdder
expect fun ServiceId<SuspendAdder>.service(implementation: SuspendAdder): Service

expect fun MixedAdder.proxy(intercept: Interceptor, suspendIntercept: SuspendInterceptor): MixedAdder

expect fun createBinarySerializer(): BinarySerializer

expect fun createDumper(dumpValue: ValueDumper): Dumper
