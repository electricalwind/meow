/**
 * Copyright 2017 Matthieu Jimenez.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package meow.utils

import greycat.Tasks.newTask
import greycat.Type
import greycat.struct.LongLongArrayMap
import meow.tokens.actions.ActionTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GreyCatUtilitaryTest : ActionTest() {

    @Test
    fun llmapKeysTest() {
        initGraph()
        val counter = arrayOf(0)
        newTask().travelInTime("0")
                .readGlobalIndex("roots")
                .thenDo { ctx ->
                    val root = ctx.resultAsNodes()[0]
                    val llmap = root.getOrCreate("llmap", Type.LONG_TO_LONG_ARRAY_MAP) as LongLongArrayMap
                    for (i in 0..1000000){
                        llmap.put(i.toLong(), i.toLong())
                    }
                    val keys = GreyCatUtilitary.keyOfLongToLongArrayMap(llmap)
                    assertEquals(1000001,keys.size)
                    counter[0]++
                    ctx.continueTask()
                }
                .execute(graph,null)
        assertEquals(1,counter[0])
        removeGraph()
    }
}