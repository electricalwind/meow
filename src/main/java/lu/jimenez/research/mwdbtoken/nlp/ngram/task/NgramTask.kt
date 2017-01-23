package lu.jimenez.research.mwdbtoken.nlp.ngram.task

import lu.jimenez.research.mwdbtoken.core.CoreConstants.*
import lu.jimenez.research.mwdbtoken.core.actions.MwdbTokenActions.getOrCreateTokensFromString
import lu.jimenez.research.mwdbtoken.nlp.ngram.NgramConstants.NGRAM_NODE_NAME
import lu.jimenez.research.mwdbtoken.nlp.ngram.actions.MwdbNgramActions
import lu.jimenez.research.mylittleplugin.MyLittleActions.*
import org.mwg.*
import org.mwg.task.Task
import org.mwg.task.Tasks.newTask


object NgramTask {


    @JvmStatic
    fun initializeNgram(): Task {
        return newTask()
                .readGlobalIndex(ENTRY_POINT_INDEX, ENTRY_POINT_NODE_NAME, NGRAM_NODE_NAME)
                .then(ifEmptyThen(
                        newTask().then(executeAtWorldAndTime(
                                "0",
                                "${Constants.BEGINNING_OF_TIME}",
                                newTask()
                                        .createNode()
                                        .setAttribute(ENTRY_POINT_NODE_NAME, Type.STRING, NGRAM_NODE_NAME)
                                        .timeSensitivity("-1", "0")
                                        .addToGlobalIndex(ENTRY_POINT_INDEX, ENTRY_POINT_NODE_NAME)
                        ))
                )
                )

    }

    @JvmStatic
    fun retrieveNgramMainNode(): Task {
        return newTask()
                .readGlobalIndex(ENTRY_POINT_INDEX, ENTRY_POINT_NODE_NAME, NGRAM_NODE_NAME)
    }

    @JvmStatic
    fun getOrCreateNgramFromString(grams: Array<String>): Task {
        return newTask()
                .then(getOrCreateTokensFromString(*grams))
                .defineAsVar("tokenList")
                .then(MwdbNgramActions.retrieveNgramMainNode())
        //TODO
    }
}