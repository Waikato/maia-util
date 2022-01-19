package maia.util.datastructure

import kotlin.test.Test
import kotlin.test.assertEquals


/**
 * Tests the [OrderedHashSet] implementation.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
internal class OrderedHashSetTest {

    @Test
    fun test() {
        val set = OrderedHashSet<Int>()
        set.add(1)
        set.add(2)
        set.add(3)
        assertEquals(3, set.size)
        set.add(1, 3)
        assertEquals(3, set.size)
        for (i in 0 until 3) assertEquals(i + 1, set[i])
    }
}
