package lu.jimenez.research.mwdbtoken.nlp.ngram.task

import lu.jimenez.research.mwdbtoken.core.CoreConstants.*
import lu.jimenez.research.mwdbtoken.core.actions.MwdbTokenActions.getOrCreateTokensFromString
import lu.jimenez.research.mwdbtoken.nlp.ngram.NgramConstants.*
import lu.jimenez.research.mwdbtoken.nlp.ngram.actions.MwdbNgramActions
import lu.jimenez.research.mylittleplugin.MyLittleActions.*
import org.mwg.*
import org.mwg.Constants.BEGINNING_OF_TIME
import org.mwg.plugin.SchedulerAffinity
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
                .then(ifNotEmptyThen(
                        getOrCreateNgramFromTokenVar("tokenList")
                )
                )
    }

    @JvmStatic
    fun getOrCreateNgramFromTokenVar(tokensVar: String): Task {
        return newTask()
                .then(MwdbNgramActions.retrieveNgramMainNode())
                .defineAsVar("ngramMain")
                .traverse(NGRAM_INDEX, GRAMS_TOKENS, "{{$tokensVar}}")
                .then(
                        ifEmptyThen(
                                createNgramFromToken(tokensVar)
                        )
                )
    }

    private fun createNgramFromToken(tokensVar: String): Task {
        return newTask().then(executeAtWorldAndTime("0", "$BEGINNING_OF_TIME",
                newTask()
                        //NGRAM
                        .createNode()
                        .defineAsVar("newNgram")
                        .timeSensitivity("-1", "0")
                        .addVarToRelation(GRAMS_TOKENS, tokensVar)
                        .thenDo { ctx ->
                            val order = ctx.variable(tokensVar).size()
                            val node = ctx.resultAsNodes()[0]
                            node.set("order", Type.INT, order)
                            if (order != 1) {
                                newTask()
                                        .inject(ctx.result().asArray().take(ctx.result().size() - 1))
                                        .defineAsVar("history")
                                        .pipe(getOrCreateNgramFromTokenVar("history"))
                                        .defineAsVar("historyNgram")
                                        .addVarToRelation("historyTo", "newNgram")
                                        .readVar("newNgram")
                                        .addVarToRelation("history", "historyNgram")


                                        .inject(ctx.result().asArray().takeLast(ctx.result().size() - 1))
                                        .defineAsVar("backoff")
                                        .pipe(getOrCreateNgramFromTokenVar("backoff"))
                                        .defineAsVar("backOffNgram")
                                        .addVarToRelation("backoffTo", "newNgram")
                                        .readVar("newNgram")
                                        .addVarToRelation("backOff", "backOffNgram")
                                        .executeFrom(ctx, ctx.result(), SchedulerAffinity.SAME_THREAD, {})
                            }
                            ctx.continueTask()
                        }
                        .readVar("ngramMain")
                        .addVarToRelation(NGRAM_INDEX, "newNgram", GRAMS_TOKENS)
        ))
    }
}