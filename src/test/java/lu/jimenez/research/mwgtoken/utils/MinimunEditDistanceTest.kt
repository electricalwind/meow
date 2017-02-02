/**
 * Copyright 2017 Matthieu Jimenez.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lu.jimenez.research.mwgtoken.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test



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