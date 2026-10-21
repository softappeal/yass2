package ch.softappeal.yass2.remote

import kotlin.reflect.KClass

public annotation class Proxies(vararg val value: KClass<*>)
