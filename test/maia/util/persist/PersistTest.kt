package maia.util.persist

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Original version of the persistent.
 */
data class TestPersistentV1(
    private val a: Int
) {
    companion object: Persistent<TestPersistentV1> {
        override val persistenceRegistry: PersistenceRegistry<TestPersistentV1> by PersistenceRegistry {
            add(
                TestPersistentV1PersistenceV1.serializer()
            ) { TestPersistentV1PersistenceV1(a) }
        }

    }
}

/**
 * The persistence when there was only the original version.
 */
@Serializable
data class TestPersistentV1PersistenceV1(
    private val a: Int
) {
    companion object: Persists<TestPersistentV1, TestPersistentV1PersistenceV1> {
        override fun resume(persistence: TestPersistentV1PersistenceV1): TestPersistentV1 = with(persistence) {
            TestPersistentV1(a)
        }

        override val persistenceRegistration by PersistenceRegistry.Registration()

    }

    /** Test function for pretending this is the upgraded version of the persistence. */
    fun toV2(): TestPersistentV2PersistenceV1 {
        return TestPersistentV2PersistenceV1(a)
    }
}

/**
 * Latest version of the persistent.
 */
data class TestPersistentV2(
    val a: Int,
    val b: String
) {
    companion object: Persistent<TestPersistentV2> {
        override val persistenceRegistry: PersistenceRegistry<TestPersistentV2> by PersistenceRegistry {
            deprecated(TestPersistentV2PersistenceV1.serializer())
            add(
                TestPersistentV2PersistenceV2.serializer()
            ) { TestPersistentV2PersistenceV2(a, b) }
        }
    }
}

/**
 * An older version of the persistent that only had an integer field.
 */
@Serializable
data class TestPersistentV2PersistenceV1(
    private val a: Int
) {
    companion object: Persists<TestPersistentV2, TestPersistentV2PersistenceV1> {
        override fun resume(persistence: TestPersistentV2PersistenceV1): TestPersistentV2 = with(persistence) {
            return TestPersistentV2(a, "")
        }

        override val persistenceRegistration by PersistenceRegistry.Registration()
    }
}

/**
 * The latest version of the persistent.
 */
@Serializable
data class TestPersistentV2PersistenceV2(
    private val a: Int,
    private val b: String
) {
    companion object: Persists<TestPersistentV2, TestPersistentV2PersistenceV2> {
        override fun resume(persistence: TestPersistentV2PersistenceV2): TestPersistentV2  = with(persistence) {
            TestPersistentV2(a, b)
        }

        override val persistenceRegistration by PersistenceRegistry.Registration()

    }
}



/**
 * Tests the persistence mechanism.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
internal class PersistTest {

    @Test
    fun testSaveAndLoad() {
        val testV1 = TestPersistentV1(33)
        val persisted: TestPersistentV1PersistenceV1 = testV1.persist()
        val saved = Json.encodeToString(persisted.persistenceSerializer(), persisted)
        val loaded = Json.decodeFromString<TestPersistentV1PersistenceV1>(saved).toV2()
        val testV2: TestPersistentV2 = loaded.resume()
        assertEquals(33, testV2.a)
        assertEquals("", testV2.b)
    }
}

