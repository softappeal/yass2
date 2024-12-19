package ch.softappeal.yass2.contract.child

import ch.softappeal.yass2.GenerateProxy
import ch.softappeal.yass2.Interceptor

@GenerateProxy
interface NoSuspend {
    fun x()
}

expect fun NoSuspend.proxy(intercept: Interceptor): NoSuspend
