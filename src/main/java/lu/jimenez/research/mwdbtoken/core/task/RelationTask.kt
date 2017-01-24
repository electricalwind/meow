package lu.jimenez.research.mwdbtoken.core.task

import lu.jimenez.research.mwdbtoken.core.CoreConstants.*
import lu.jimenez.research.mwdbtoken.core.actions.MwdbTokenActions.getOrCreateTokensFromString
import lu.jimenez.research.mwdbtoken.tokenization.tokenizer.Tokenizer
import lu.jimenez.research.mwdbtoken.utils.MinimunEditDistance
import lu.jimenez.research.mylittleplugin.MyLittleActions.*
import org.mwg.*
import org.mwg.Constants.BEGINNING_OF_TIME
import org.mwg.plugin.SchedulerAffinity
import org.mwg.struct.Relation
import org.mwg.task.Task
import org.mwg.task.Tasks.*


object RelationTask {

    @JvmStatic
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
                                    .pipe(uocTokenizeRelation())
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
                                                    .pipe(uocTokenizeRelation())
                                    ),
                            thenDo { ctx ->
                                ctx.endTask(ctx.result(), RuntimeException("The number of relations and nodes are not similar! (1 tokenizer)"))
                            })

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
                                        .pipe(uocTokenizeRelation())

                                ).readVar(nodeVar)
                        ,
                        thenDo { ctx ->
                            ctx.endTask(ctx.result(), RuntimeException("The number of relations and tokenizers are not similar! (1 node)"))
                        })
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
                                                                    val node = ctx.variable(nodesVar)[increment] as Node
                                                                    ctx.defineVariable(relationVar, relation)
                                                                    ctx.defineVariable(nodeVar, node)
                                                                    ctx.continueTask()
                                                                }
                                                                .pipe(uocTokenizeRelation())

                                                )
                                                .readVar(nodesVar)
                                        ,
                                        thenDo { ctx ->
                                            ctx.endTask(ctx.result(), RuntimeException("The number of nodes and tokenizers are not similar!"))
                                        })

                        ,
                        thenDo { ctx ->
                            ctx.endTask(ctx.result(), RuntimeException("The number of relations and tokenizers are not similar! "))
                        })

    }


    private fun uocTokenizeRelation(): Task {
        return newTask()
                .readVar(nodeVar)
                //.println("{{result}}")
                .traverse(TOKENIZE_CONTENT_RELATION, TOKENIZE_CONTENT_NAME, "{{$relationVar}}")
                .then(ifEmptyThenElse(
                        createTokenRelation(),
                        updateTokenRelation()
                ))
    }

    private fun updateTokenRelation(): Task {
        return newTask()
                .defineAsVar("relationNode")
                .then(checkForFuture())
                .thenDo { ctx ->
                    val dephasing = ctx.resultAsNodes()[0].timeDephasing()
                    if (dephasing == 0L)
                        ctx.endTask(ctx.result(), RuntimeException("Trying to modify a tokenize content at the time of the previous modification"))
                    else {
                        val newToken = ctx.variable(tokenizerVar)[0] as Tokenizer
                        newTask()
                                .then(getOrCreateTokensFromString(*newToken.getTokens().toTypedArray()))
                                .executeFrom(ctx, ctx.result(), SchedulerAffinity.SAME_THREAD
                                ) { res -> ctx.continueWith(res) }
                    }
                }


                .defineAsVar("newToken")
                .readVar("relationNode")
                .thenDo { ctx ->
                    val relationNodeId = ctx.resultAsNodes()[0].id()
                    val type = ctx.resultAsNodes()[0].get("type") as String
                    val relation = ctx.resultAsNodes()[0].get(TOKENIZE_CONTENT_TOKENS) as Relation
                    val relationsId = relation.all().take(relation.size())
                    val newContent = ctx.variable("newToken").asArray()
                    val newContentId = mutableListOf<Long>()
                    newContent.mapTo(newContentId) { (it as Node).id() }
                    val med = MinimunEditDistance(relationsId.toTypedArray(), newContentId.toTypedArray())
                    val path = med.path()
                    var formerIndex = 0
                    var newIndex = 0
                    val formerIndexMax = relationsId.size
                    val newIndexMax = newContentId.size
                    for (action in path) {
                        when (action.second) {
                            MinimunEditDistance.Modification.Suppression -> {
                                relation.delete(newIndex)
                                newTask().lookup("${action.first}")
                                        .traverse(WORD_INVERTED_INDEX_RELATION, "id", "$relationNodeId")
                                        .thenDo {
                                            ctx ->
                                            val node = ctx.resultAsNodes()[0]
                                            val position: MutableList<Int> = (node.get("position") as IntArray?)?.toMutableList() ?: throw RuntimeException("no position while delete")
                                            position.remove(formerIndex)
                                            node.set("position", Type.INT_ARRAY, position.toIntArray())
                                            ctx.continueTask()
                                        }.executeFrom(ctx, ctx.result(), SchedulerAffinity.SAME_THREAD, {})

                                formerIndex++
                            }

                            MinimunEditDistance.Modification.Insertion -> {
                                relation.insert(newIndex, action.first)
                                newTask().lookup("${action.first}")
                                        .defineAsVar("token")
                                        .traverse(WORD_INVERTED_INDEX_RELATION, "id", "$relationNodeId")
                                        .then(
                                                ifEmptyThen(
                                                        newTask()
                                                                .then(
                                                                        executeAtWorldAndTime("0", "$BEGINNING_OF_TIME",
                                                                                newTask()
                                                                                        .createNode()
                                                                                        .setAttribute("id", Type.LONG, "$relationNodeId")
                                                                                        .setAttribute("type", Type.STRING, "$type")
                                                                                        .defineAsVar("invertedIndex")
                                                                                        .addVarToRelation(INVERTED_WORD_INDEX_RELATION, "token")
                                                                                        .readVar("token")
                                                                                        .addVarToRelation(WORD_INVERTED_INDEX_RELATION, "invertedIndex", "id")
                                                                                        .readVar("invertedIndex")
                                                                        )
                                                                )
                                                )
                                        )
                                        .thenDo {
                                            ctx ->
                                            val node = ctx.resultAsNodes()[0]
                                            val position: MutableList<Int> = (node.get("position") as IntArray?)?.toMutableList() ?: mutableListOf<Int>()
                                            position.add(newIndex)
                                            node.set("position", Type.INT_ARRAY, position.toIntArray())
                                            ctx.continueTask()
                                        }.executeFrom(ctx, ctx.result(), SchedulerAffinity.SAME_THREAD, {})
                                newIndex++
                            }

                            MinimunEditDistance.Modification.Keep -> {
                                newTask().lookup("${action.first}")
                                        .traverse(WORD_INVERTED_INDEX_RELATION, "id", "$relationNodeId")
                                        .thenDo { ctx ->
                                            val node = ctx.resultAsNodes()[0]
                                            val position: MutableList<Int> = (node.get("position") as IntArray?)?.toMutableList() ?: mutableListOf<Int>()
                                            position.remove(formerIndex)
                                            position.add(newIndex)
                                            node.set("position", Type.INT_ARRAY, position.toIntArray())
                                            ctx.continueTask()
                                        }.executeFrom(ctx, ctx.result(), SchedulerAffinity.SAME_THREAD, {})
                                formerIndex++
                                newIndex++
                            }
                        }
                    }
                    if (formerIndex != formerIndexMax || newIndex != newIndexMax)
                        ctx.endTask(ctx.result(), RuntimeException("error while computing the edit distance"))
                    else
                        ctx.continueTask()
                }
    }

    private fun createTokenRelation(): Task {
        return newTask()

                .createNode()
                .defineAsVar("relationNode")
                .thenDo { ctx ->
                    ctx.setVariable("relationNodeId", ctx.resultAsNodes()[0].id())
                    ctx.continueTask()
                }
                .addVarToRelation(TOKENIZE_CONTENT_FATHER, nodeVar)
                .setAttribute(TOKENIZE_CONTENT_NAME, Type.STRING, "{{$relationVar}}")

                .readVar(nodeVar)
                .addVarToRelation(TOKENIZE_CONTENT_RELATION, "relationNode", TOKENIZE_CONTENT_NAME)

                .readVar(tokenizerVar)
                .thenDo { ctx ->
                    val tokenizer = ctx.result()[0] as Tokenizer
                    ctx.setVariable("type", tokenizer.getTypeOfToken() ?: NO_TYPE_TOKENIZE)
                    newTask()
                            .then(getOrCreateTokensFromString(*tokenizer.getTokens().toTypedArray()))
                            .executeFrom(ctx, ctx.result(), SchedulerAffinity.SAME_THREAD
                            ) { res -> ctx.continueWith(res) }
                }
                .forEach(
                        newTask()
                                .defineAsVar("token")
                                .traverse(WORD_INVERTED_INDEX_RELATION, "id", "{{relationNodeId}}")

                                .then(
                                        ifEmptyThenElse(
                                                newTask()
                                                        .then(executeAtWorldAndTime("0", "$BEGINNING_OF_TIME",
                                                                newTask()
                                                                        .createNode()
                                                                        .setAttribute("id", Type.LONG, "{{relationNodeId}}")

                                                                        .setAttribute("type", Type.STRING, "{{type}}")
                                                                        .defineAsVar("invertedIndex")
                                                                        .addVarToRelation(INVERTED_WORD_INDEX_RELATION, "token")
                                                                        .readVar("token")
                                                                        .addVarToRelation(WORD_INVERTED_INDEX_RELATION, "invertedIndex", "id")
                                                                        .readVar("invertedIndex")
                                                        )
                                                        )
                                                ,
                                                newTask().then(checkForFuture())
                                        )
                                )
                                .thenDo { ctx ->
                                    val node = ctx.resultAsNodes()[0]
                                    val position: MutableList<Int> = (node.get("position") as IntArray?)?.toMutableList() ?: mutableListOf<Int>()
                                    position.add(ctx.variable("i")[0] as Int)
                                    node.set("position", Type.INT_ARRAY, position.toIntArray())
                                    ctx.continueTask()
                                }


                                .readVar("relationNode")
                                .addVarToRelation(TOKENIZE_CONTENT_TOKENS, "token")

                )
                .readVar("relationNode")
                .setAttribute("type", Type.STRING, "{{type}}")


    }


    val nodeVar = "node"
    val relationVar = "relation"
    val tokenizerVar = "tokenizer"
}