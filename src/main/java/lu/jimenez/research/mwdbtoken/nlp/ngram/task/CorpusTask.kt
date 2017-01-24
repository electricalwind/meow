package lu.jimenez.research.mwdbtoken.nlp.ngram.task

import lu.jimenez.research.mwdbtoken.core.CoreConstants.*
import lu.jimenez.research.mwdbtoken.nlp.ngram.NgramConstants.*
import lu.jimenez.research.mwdbtoken.nlp.ngram.actions.MwdbNgramActions
import lu.jimenez.research.mylittleplugin.MyLittleActions.*
import org.mwg.Constants.BEGINNING_OF_TIME
import org.mwg.Type
import org.mwg.task.Task
import org.mwg.task.Tasks.*


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

    @JvmStatic
    fun getOrCreateCorpus(corpusName: String): Task {
        return newTask()
                .then(MwdbNgramActions.retrieveCorpusMainNode())
                .traverse(CORPUS_RELATION, CORPUS_NAME, corpusName)
                .then(ifEmptyThen(
                        createCorpus(corpusName)
                ))

    }

    private fun createCorpus(corpusName: String): Task {
        return then(
                executeAtWorldAndTime("0", "$BEGINNING_OF_TIME",
                        newTask()
                                .createNode()
                                .setAttribute(CORPUS_NAME, Type.STRING, corpusName)
                                .timeSensitivity("-1", "0")
                                .defineAsVar("corpusNode")
                                .then(MwdbNgramActions.retrieveCorpusMainNode())
                                .defineAsVar("corpusMain")
                                .addVarToRelation(CORPUS_RELATION, "corpusNode", CORPUS_NAME)
                                .readVar("corpusNode")
                )
        )
    }


    fun addTokenizeContentToCorpus(): Task {
        return newTask()
    }

}