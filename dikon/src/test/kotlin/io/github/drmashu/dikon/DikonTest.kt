package io.github.drmashu.dikon

import org.junit.Test
import org.junit.Test as test
import org.junit.Before as before
import org.junit.After as after

import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * @author NAGASAWA Takahiro<drmashu@gmail.com>
 */
class DikonTest() {
    val dikon = Dikon(hashMapOf(
            Pair("Test", Singleton(Create(TestComponent::class))),
            Pair("A", Holder("A")),
            Pair("B", Singleton(object : Factory<String> {
                override fun get(dikon: Container): String? {
                    return "B"
                }
            })),
            Pair("Test2", Singleton(Injection(TestComponent2::class))),
            Pair("Test3", object : Factory<TestComponent3> {
                override fun get(dikon: Container): TestComponent3? {
                    val result = TestComponent3()
                    result.setA(dikon["A"] as String)
                    return result
                }
            })
    ))
    @Test fun testDikonGet() {
        val comp = dikon["Test"]
        assertNotNull(comp)
        assert(comp is TestComponent)
    }
    @Test fun testDikonInjection() {
        val comp = dikon["Test"]
        if (comp is TestComponent) {
            assertEquals("B", comp.A)
        }
    }
    @Test fun testDikonNotInjection() {
        val comp = dikon["Test"]
        if (comp is TestComponent) {
            assertNull(comp.B)
        }
    }
    @Test fun testSingleton() {
        val comp = dikon["Test"]
        if (comp is TestComponent) {
            val comp2 = dikon["Test"]
            assert(comp == comp2)
        }
    }
    @Test fun testInjection() {
        val comp = dikon["Test"]
        if (comp is TestComponent) {
            assertEquals("A", comp.Test2!!.A)
        }
    }
    @Test fun testInjectionWithAnnotation() {
        val comp = dikon.get("Test")
        if (comp is TestComponent) {
            assertEquals("A", comp.Test2!!.B)
        }
    }

    @Test fun testInjectionGet() {
        val comp = Injection(TestComponent2::class).get(dikon)
        if (comp is TestComponent2) {
            assertEquals("A", comp.A)
            assertEquals("A", comp.B)
        }
    }
    @Test fun testSingletonGet() {
        val comp = Singleton(Create(TestComponent::class)).get(dikon)
        assert(comp is TestComponent)
        if (comp is TestComponent) {
            assertNull(comp.A)
            assertNull(comp.B)
            assertNull(comp.Test2)
        }
    }
    @Test fun testSingletonInjectionGet() {
        val comp = Singleton(Injection(TestComponent2::class)).get(dikon)
        if (comp is TestComponent2) {
            assertEquals("A", comp.A)
            assertEquals("A", comp.B)
        }
    }
    @Test fun testDikonSetterInjection() {
        val comp = dikon["Test"]
        if (comp is TestComponent) {
            assertEquals("A", comp.C)
        }
    }
    @Test fun testDikonJavaClass() {
        val comp = dikon["Test3"]
        if (comp is TestComponent3) {
            assertEquals("A", comp.a)
        }
    }
    @Test fun testInjectionMenyType() {
        val comp = Injection(TestComponent2_::class).get(dikon)
        if (comp is TestComponent2_) {
            assertEquals("A", comp.A)
            assert(comp.Test3 is TestComponent3)
        }
    }
}

data class TestComponent {
    @inject("B") var A : String? = null
    @inject var Test2 : TestComponent2? = null
    var B: String? = null
    var C: String? = null
    @inject("A") fun setCC(c:String) {
        C = c
    }
}
data class TestComponent2(@inject("A") val B: String, val A: String = "X")
class TestComponent2_(val A: String, val Test3: TestComponent3_) {

}
