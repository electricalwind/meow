package lu.jimenez.research.mwdbtoken.nlp.corpus.task

import lu.jimenez.research.mwdbtoken.core.CoreConstants.*
import lu.jimenez.research.mwdbtoken.nlp.corpus.CorpusConstants.*
import lu.jimenez.research.mwdbtoken.nlp.corpus.actions.MwdbCorpusActions
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
                .then(MwdbCorpusActions.retrieveCorpusMainNode())
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
                                .then(MwdbCorpusActions.retrieveCorpusMainNode())
                                .defineAsVar("corpusMain")
                                .addVarToRelation(CORPUS_RELATION, "corpusNode", CORPUS_NAME)
                                .readVar("corpusNode")
                )
        )
    }


    fun addTokenizeContentToCorpus(tokenizeContentVar: String, corpusName: String): Task {
        return newTask()
                .then(MwdbCorpusActions.getOrCreateCorpus(corpusName))
                .defineAsVar("corpus")
                .pipe(addTokenizeContentToCorpusVar(tokenizeContentVar, "corpus"))
    }

    fun addTokenizeContentToCorpusVar(tokenizeContentVar: String, corpusVar: String): Task {
        return newTask()
                .readVar(corpusVar)


    }

    /**private fun updateNgramForTokenizeContent(tokenizeContentVar: String): Task {

        return newTask()
                .readVar(tokenizeContentVar)
                .forEach(
                        newTask()
                                .defineAsVar("tokenizedContent")
                                .declareVar("timepointsTC")
                                .thenDo { ctx ->
                                    ctx.resultAsNodes()[0].timepoints(BEGINNING_OF_TIME, END_OF_TIME, { timepoints ->
                                        ctx.addToVariable("timepointsTC", timepoints)
                                        ctx.continueTask()
                                    })
                                }
                                .thenDo { ctx ->
                                    ctx.setVariable("firstTime"
                                            , ctx.variable("timepointsTC")[0])
                                }
                                .travelInTime("firstTime")
                                .traverse(TOKENIZED_CONTENT_PLUGIN, TOKENIZED_CONTENT_PLUGIN_NGRAM)
                                .then(ifEmptyThenElse(
                                        newTask(),
                                        newTask()
                                )
                                )
                )

    }*/

}