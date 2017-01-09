package lu.jimenez.research.mwdbtoken.task

import lu.jimenez.research.mwdbtoken.Constants.*
import lu.jimenez.research.mwdbtoken.tokenization.tokenizer.Tokenizer
import lu.jimenez.research.mylittleplugin.MyLittleActions.*
import org.mwg.*
import org.mwg.core.task.Actions.*
import org.mwg.plugin.SchedulerAffinity
import org.mwg.task.Task


object RelationTask {

    fun updateOrCreateTokenizeRelationsToNodes(tokenizersVar: String, nodesVar: String, relationList: Array<String>): Task {

        return newTask()
                .readVar(tokenizersVar)
                .then(count())
                .ifThenElse(cond("result == 1"),
                        spreadingATokenizerToNodes(tokenizersVar, nodesVar, relationList),

                        newTask()
                                .defineAsVar("tokenizerCount")
                                .readVar(nodesVar)
                                .then(count())
                                .ifThenElse(cond("result == 1"),
                                        uocSeveralTokenizerToANode(tokenizersVar, nodesVar, relationList),
                                        uocSeveralTokenizerToSeveralNodes(tokenizersVar, nodesVar, relationList)
                                )
                )

    }


    private fun spreadingATokenizerToNodes(tokenizersVar: String, nodesVar: String, relationList: Array<String>): Task {
        if (relationList.size == 1) {
            return newTask()
                    .readVar(tokenizersVar)
                    .defineAsVar(tokenizerVar)
                    .inject(relationList[0])
                    .defineAsVar(relationVar)
                    .readVar(nodesVar)
                    .forEach(
                            newTask()
                                    .defineAsVar(nodeVar)
                                    .mapReduce(uocTokenizeRelation())
                    )
        } else {
            return newTask()
                    .readVar(tokenizersVar)
                    .defineAsVar(tokenizerVar)
                    .readVar(nodesVar)
                    .then(count())
                    .ifThenElse(cond("result == ${relationList.size}"),
                            newTask()
                                    .readVar(nodesVar)
                                    .forEach(
                                            newTask()
                                                    .defineAsVar(nodeVar)
                                                    .thenDo { ctx ->
                                                        val increment = ctx.variable("i")[0] as Int
                                                        val relation = relationList[increment]
                                                        ctx.defineVariable(relationVar, relation)
                                                        ctx.continueTask()
                                                    }
                                                    .mapReduce(uocTokenizeRelation())
                                    ),
                            throw RuntimeException("The number of relations and nodes are not similar! (1 tokenizer)"))
        }
    }


    private fun uocSeveralTokenizerToANode(tokenizersVar: String, nodesVar: String, relationList: Array<String>): Task {
        return newTask()
                .ifThenElse(cond("tokenizerCount == ${relationList.size}"),
                        newTask().readVar(nodesVar)
                                .defineAsVar(nodeVar)
                                .readVar(tokenizersVar)
                                .forEach(newTask()
                                        .defineAsVar(tokenizerVar)
                                        .thenDo { ctx ->
                                            val increment = ctx.variable("i")[0] as Int
                                            val relation = relationList[increment]
                                            ctx.defineVariable(relationVar, relation)
                                            ctx.continueTask()
                                        }
                                        .mapReduce(uocTokenizeRelation())
                                ),
                        throw RuntimeException("The number of relations and tokenizers are not similar! (1 node)"))
    }

    private fun uocSeveralTokenizerToSeveralNodes(tokenizersVar: String, nodesVar: String, relationList: Array<String>): Task {
        return newTask()
                .ifThenElse(cond("tokenizerCount == ${relationList.size}"),
                        newTask()
                                .readVar(nodesVar)
                                .then(count())
                                .ifThenElse(cond("result == tokenizerCount"),
                                        newTask()
                                                .readVar(tokenizersVar)
                                                .forEach(
                                                        newTask()
                                                                .defineAsVar(tokenizerVar)
                                                                .thenDo { ctx ->
                                                                    val increment = ctx.variable("i")[0] as Int
                                                                    val relation = relationList[increment]
                                                                    val node = ctx.variable(nodesVar)[0] as Node
                                                                    ctx.defineVariable(relationVar, relation)
                                                                    ctx.defineVariable(nodeVar, node)
                                                                    ctx.continueTask()
                                                                }
                                                                .mapReduce(uocTokenizeRelation())
                                                ),
                                        throw RuntimeException("The number of nodes and tokenizers are not similar!")
                                ),
                        throw RuntimeException("The number of relations and tokenizers are not similar! ")
                )
    }


    private fun uocTokenizeRelation(): Task {
        return newTask()
                .readVar(nodeVar)
                .traverse(relationVar)
                .then(ifEmptyThenElse(
                        createTokenRelation(),
                        updateTokenRelation()
                ))
    }

    private fun updateTokenRelation(): Task {
        return newTask()
                .defineAsVar("relationNode")
    }

    private fun createTokenRelation(): Task {
        return newTask()
                .createNode()
                .defineAsVar("relationNode")
                .addVarToRelation(TOKENIZE_CONTENT_FATHER, nodeVar)
                .setAttribute(TOKENIZE_CONTENT_NAME, Type.STRING, relationVar)
                .readVar(nodeVar)
                .addVarToRelation(relationVar, "relationNode")
                .readVar(tokenizerVar)
                .thenDo { ctx ->
                    val tokenizer = ctx.result()[0] as Tokenizer
                    ctx.defineVariable("type", tokenizer.getTypeOfToken() ?: NO_TYPE_TOKENIZE)
                    newTask()
                            .mapReduce(VocabularyTask.getOrCreateTokensFromString(tokenizer.getTokens().toTypedArray()))
                            .executeFrom(ctx, ctx.result(), SchedulerAffinity.SAME_THREAD
                            ) { res -> ctx.continueWith(res) }
                }
                .forEach(
                        newTask()
                                .defineAsVar("token")
                                .addVarToRelation(TOKENIZE_CONTENT_TOKENS,"relationNode")
                                .traverse(WORD_INVERTED_INDEX_RELATION)
                                .thenDo { ctx ->
                                    //todo
                                }
                )


    }


    val nodeVar = "node"
    val relationVar = "relation"
    val tokenizerVar = "tokenizer"
}