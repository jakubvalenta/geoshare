package page.ooooo.geoshare.lib.extensions

fun <K, V> Collection<Pair<K, V>>.group(): Map<K, List<V>> = buildMap<K, MutableList<V>> {
    for ((key, value) in this@group) {
        this.getOrPut(key) { mutableListOf() }.add(value)
    }
}
