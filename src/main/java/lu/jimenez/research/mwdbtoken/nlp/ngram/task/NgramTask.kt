package lu.jimenez.research.mwdbtoken.nlp.ngram.task

import org.mwg.task.Task
import org.mwg.task.Tasks.newTask


object NgramTask {


    @JvmStatic
    fun initializeNgram(): Task {
        return newTask()
    }
}