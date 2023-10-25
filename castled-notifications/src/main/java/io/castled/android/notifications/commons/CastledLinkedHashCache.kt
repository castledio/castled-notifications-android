package io.castled.android.notifications.commons

internal class CastledLinkedHashCache<K, V>(private val maxSize: Int) :
    LinkedHashMap<K, V>(maxSize, 0.75f, true) {

    override fun removeEldestEntry(eldest: Map.Entry<K, V>): Boolean {
        return size > maxSize
    }
}