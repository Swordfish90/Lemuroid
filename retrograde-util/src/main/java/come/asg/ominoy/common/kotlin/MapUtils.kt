package come.asg.ominoy.common.kotlin

fun <K, V> Map<K, V>.reverseLookup(): Map<V, K> {
    return entries.associateBy({ it.value }, { it.key })
}
