package lu.jimenez.research.mwdbtoken.core.task

import lu.jimenez.research.mwdbtoken.core.CoreConstants.NODE_TYPE
import org.mwg.task.Task
import org.mwg.task.Tasks.newTask


object UtilTask {

    fun checkNodesType(varTorCheck: String, expectedType: String): Task {
        return newTask()
                .readVar(varTorCheck)
                .forEach(
                        newTask()
                                .thenDo { ctx ->
                                    val nodeType = ctx.resultAsNodes()[0].get(NODE_TYPE)
                                    if (nodeType != expectedType) {
                                        ctx.endTask(ctx.result(), RuntimeException("Not a $expectedType"))
                                    } else {
                                        ctx.continueTask()
                                    }
                                }
                )
    }



}