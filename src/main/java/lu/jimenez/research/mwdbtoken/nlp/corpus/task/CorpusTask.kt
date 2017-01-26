package lu.jimenez.research.mwdbtoken.nlp.corpus.task

import lu.jimenez.research.mwdbtoken.core.CoreConstants.*
import lu.jimenez.research.mwdbtoken.core.task.UtilTask.checkNodesType
import lu.jimenez.research.mwdbtoken.nlp.corpus.CorpusConstants.*
import lu.jimenez.research.mwdbtoken.nlp.corpus.actions.MwdbCorpusActions
import lu.jimenez.research.mylittleplugin.MyLittleActions.*
import org.mwg.Constants.BEGINNING_OF_TIME
import org.mwg.Type
import org.mwg.struct.Relation
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
                                .setAttribute(NODE_TYPE, Type.STRING, NODE_TYPE_CORPUS)
                                .timeSensitivity("-1", "0")
                                .defineAsVar("corpusNode")
                                .then(MwdbCorpusActions.retrieveCorpusMainNode())
                                .defineAsVar("corpusMain")
                                .addVarToRelation(CORPUS_RELATION, "corpusNode", CORPUS_NAME)
                                .readVar("corpusNode")
                )
        )
    }

    @JvmStatic
    fun addTokenizeContentToCorpus(tokenizeContentVar: String, corpusName: String): Task {
        return newTask()
                .then(MwdbCorpusActions.getOrCreateCorpus(corpusName))
                .defineAsVar("corpus")
                .pipe(checkNodesType(tokenizeContentVar, NODE_TYPE_TOKENIZE_CONTENT))
                .readVar(tokenizeContentVar)
                .forEach(
                        newTask()
                                .defineAsVar("tokenizeContent")
                                .thenDo {
                                    ctx ->
                                    ctx.setVariable("id", ctx.resultAsNodes()[0].id())
                                    ctx.continueTask()
                                }
                                .readVar("corpus")
                                .thenDo { ctx ->
                                    val relation = ctx.resultAsNodes()[0].getOrCreate(CORPUS_TO_TOKENIZEDCONTENTS_RELATION, Type.RELATION) as Relation
                                    val id = ctx.variable("id")[0] as Long
                                    if (relation.size() == 0 || !relation.all().contains(id))
                                        relation.add(id)

                                    ctx.continueTask()

                                }
                )
    }

    @JvmStatic
    fun removeTokenizeContentFromCorpus(tokenizeContentVar: String, corpusName: String): Task {

        return newTask()
                .then(MwdbCorpusActions.getOrCreateCorpus(corpusName))
                .defineAsVar("corpus")
                .pipe(checkNodesType(tokenizeContentVar, NODE_TYPE_TOKENIZE_CONTENT))
                .readVar(tokenizeContentVar)
                .forEach(
                        newTask()
                                .defineAsVar("tokenizeContent")
                                .thenDo {
                                    ctx ->
                                    ctx.setVariable("id", ctx.resultAsNodes()[0].id())
                                    ctx.continueTask()
                                }
                                .readVar("corpus")
                                .thenDo { ctx ->
                                    val relation = ctx.resultAsNodes()[0].getOrCreate(CORPUS_TO_TOKENIZEDCONTENTS_RELATION,Type.RELATION) as Relation
                                    val id = ctx.variable("id")[0] as Long
                                    if (relation.size() != 0 && relation.all().contains(id))
                                        relation.remove(id)

                                    ctx.continueTask()
                                }
                )
    }


}