package lu.jimenez.research.mwdbtoken.utils

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class MinimunEditDistanceTest {

    @Test
    fun testEq() {
        val former = arrayOf<Int>(1, 2, 3, 3, 4, 9)
        val newer = arrayOf<Int>(1, 2, 3, 3, 4, 9)
        val med = MinimunEditDistance(former, newer)
        assertEquals(0, med.editDistance())
        val path = med.path()
        assertEquals(6, path.size)
        for (action in path) {
            assertEquals(action.second, MinimunEditDistance.Modification.Keep)
        }
    }


    @Test
    fun testNEq() {
        val former = arrayOf<Int>(1, 2, 3, 3, 4, 9)
        val newer = arrayOf<Int>(6, 4, 2, 6, 7, 8)
        val med = MinimunEditDistance(former, newer)
        val edi = med.editDistance()
        val path = med.path()
        val i = path.count { it.second != MinimunEditDistance.Modification.Keep }
        assertEquals(edi,i)
    }

    @Test
    fun pureInsert(){
        val former = arrayOf<Int>()
        val newer = arrayOf<Int>(6, 4, 2, 6, 7, 8)
        val med = MinimunEditDistance(former, newer)
        val edi = med.editDistance()
        assertEquals(6,edi)
        val path = med.path()
        val i = path.count { it.second == MinimunEditDistance.Modification.Insertion }
        assertEquals(edi,i)
    }

    @Test
    fun pureDelet(){
        val former = arrayOf<Int>(6, 4, 2, 6, 7, 8)
        val newer = arrayOf<Int>()
        val med = MinimunEditDistance(former, newer)
        val edi = med.editDistance()
        assertEquals(6,edi)
        val path = med.path()
        val i = path.count { it.second == MinimunEditDistance.Modification.Suppression }
        assertEquals(edi,i)
    }
}