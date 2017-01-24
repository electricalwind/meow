package lu.jimenez.research.mwdbtoken.nlp.ngram.task

import lu.jimenez.research.mwdbtoken.core.CoreConstants.*
import lu.jimenez.research.mwdbtoken.nlp.ngram.NgramConstants.CORPUS_MAIN_NODE
import lu.jimenez.research.mylittleplugin.MyLittleActions.*
import org.mwg.Constants.BEGINNING_OF_TIME
import org.mwg.Type
import org.mwg.task.Task
import org.mwg.task.Tasks.newTask


object CorpusTask {

    @JvmStatic
    fun initializeCorpus(): Task {
        return retrieveCorpusMainNode()
                .then(
                        ifEmptyThen(
                                newTask().then(
                                        executeAtWorldAndTime("0", "$BEGINNING_OF_TIME",
                                                newTask()
                                                        .createNode()
                                                        .setAttribute(ENTRY_POINT_NODE_NAME, Type.STRING, CORPUS_MAIN_NODE)
                                                        .timeSensitivity("-1", "0")
                                                        .addToGlobalIndex(ENTRY_POINT_INDEX, ENTRY_POINT_NODE_NAME)
                                        )

                                )
                        )
                )
    }

    @JvmStatic
    fun retrieveCorpusMainNode(): Task {
        return newTask()
                .readGlobalIndex(ENTRY_POINT_INDEX, ENTRY_POINT_NODE_NAME, CORPUS_MAIN_NODE)
    }

    fun createCorpus(corpusName: String): Task {
        return newTask()

                .then(executeAtWorldAndTime("0", "$BEGINNING_OF_TIME",
                       newTask() ))
    }

    fun retrieveCorpus(corpusName: String): Task {
        return newTask()
    }

    fun addTokenizeContentToCorpus(): Task {
        return newTask()
    }



}