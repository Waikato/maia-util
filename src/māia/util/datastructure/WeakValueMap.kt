package māia.util.datastructure

import java.lang.ref.WeakReference

/**
 * TODO: What class does.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
class WeakValueMap<K, V: Any> {

    private val map = HashMap<K, WeakReference<V>>()

    val size : Int
        get() = map.size

    operator fun contains(key: K): Boolean {
        return this[key] != null
    }

    operator fun get(key: K): V? {
        return map[key]?.get().also { if (it == null) map.remove(key) }
    }

    operator fun set(key: K, value: V) {
        map[key] = WeakReference(value)
    }
}
