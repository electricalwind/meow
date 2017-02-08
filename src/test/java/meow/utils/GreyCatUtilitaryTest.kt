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