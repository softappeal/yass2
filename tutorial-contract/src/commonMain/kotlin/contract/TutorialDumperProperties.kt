package ch.softappeal.yass2.tutorial.contract

@Suppress("RedundantSuppression", "UNCHECKED_CAST", "RedundantVisibilityModifier", "RemoveRedundantQualifierName")
public val TutorialDumperProperties: ch.softappeal.yass2.DumperProperties = ch.softappeal.yass2.dumperProperties(
    ch.softappeal.yass2.tutorial.contract.Address::class to listOf(
        ch.softappeal.yass2.tutorial.contract.Address::number as kotlin.reflect.KProperty1<Any, Any?>,
        ch.softappeal.yass2.tutorial.contract.Address::street as kotlin.reflect.KProperty1<Any, Any?>,
    ),
    ch.softappeal.yass2.tutorial.contract.Person::class to listOf(
        ch.softappeal.yass2.tutorial.contract.Person::addresses as kotlin.reflect.KProperty1<Any, Any?>,
        ch.softappeal.yass2.tutorial.contract.Person::gender as kotlin.reflect.KProperty1<Any, Any?>,
        ch.softappeal.yass2.tutorial.contract.Person::name as kotlin.reflect.KProperty1<Any, Any?>,
    ),
    ch.softappeal.yass2.tutorial.contract.DivideByZeroException::class to listOf(
    ),
    ch.softappeal.yass2.tutorial.contract.SubClass::class to listOf(
        ch.softappeal.yass2.tutorial.contract.SubClass::baseClassProperty as kotlin.reflect.KProperty1<Any, Any?>,
        ch.softappeal.yass2.tutorial.contract.SubClass::subClassProperty as kotlin.reflect.KProperty1<Any, Any?>,
    ),
    ch.softappeal.yass2.tutorial.contract.BooleanFlowId::class to listOf(
    ),
    ch.softappeal.yass2.tutorial.contract.IntFlowId::class to listOf(
        ch.softappeal.yass2.tutorial.contract.IntFlowId::max as kotlin.reflect.KProperty1<Any, Any?>,
    ),
)
