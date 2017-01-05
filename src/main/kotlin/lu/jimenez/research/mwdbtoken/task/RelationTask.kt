package lu.jimenez.research.mwdbtoken.task

import lu.jimenez.research.mylittleplugin.MyLittleActions.*
import org.mwg.Node
import org.mwg.core.task.Actions.*
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
                    .defineAsVar("tokenizer")
                    .inject(relationList[0])
                    .defineAsVar("relation")
                    .readVar(nodesVar)
                    .forEach(
                            newTask()
                                    .defineAsVar("node")
                                    .mapReduce(uocTokenizeRelation())
                    )
        } else {
            return newTask()
                    .readVar(tokenizersVar)
                    .defineAsVar("tokenizer")
                    .readVar(nodesVar)
                    .then(count())
                    .ifThenElse(cond("result == ${relationList.size}"),
                            newTask()
                                    .readVar(nodesVar)
                                    .forEach(
                                            newTask()
                                                    .defineAsVar("node")
                                                    .thenDo { ctx ->
                                                        val increment = ctx.variable("i")[0] as Int
                                                        val relation = relationList[increment]
                                                        ctx.defineVariable("relation", relation)
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
                                .defineAsVar("node")
                                .readVar(tokenizersVar)
                                .forEach(newTask()
                                        .defineAsVar("tokenizer")
                                        .thenDo { ctx ->
                                            val increment = ctx.variable("i")[0] as Int
                                            val relation = relationList[increment]
                                            ctx.defineVariable("relation", relation)
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
                                                                .defineAsVar("tokenizer")
                                                                .thenDo { ctx ->
                                                                    val increment = ctx.variable("i")[0] as Int
                                                                    val relation = relationList[increment]
                                                                    val node = ctx.variable(nodesVar)[0] as Node
                                                                    ctx.defineVariable("relation", relation)
                                                                    ctx.defineVariable("node", node)
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
                .readVar("node")
                .traverse("relation")
                .then(ifEmptyThenElse(
                        newTask(),
                        newTask()
                ))
    }

}