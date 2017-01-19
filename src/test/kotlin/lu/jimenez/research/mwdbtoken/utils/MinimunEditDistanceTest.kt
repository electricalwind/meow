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
        for(action in path){
            assertEquals(action.second, MinimunEditDistance.Modification.Keep)
        }
    }


    @Test
    fun testNEq() {
        val former = arrayOf<Int>(1, 2, 3, 3, 4, 9)
        val newer = arrayOf<Int>(6, 4, 2, 6, 7, 8)
        val med = MinimunEditDistance(former, newer)

        val path = med.path()
        print("pat")

    }
}