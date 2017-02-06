/**
 * Copyright 2017 Matthieu Jimenez.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package meow.tokens.task

import greycat.*
import greycat.Constants.BEGINNING_OF_TIME
import greycat.Tasks.*
import greycat.plugin.SchedulerAffinity
import greycat.struct.Relation
import meow.tokens.TokensConstants.*
import meow.tokens.actions.MwdbTokenActions.getOrCreateTokensFromString
import meow.tokens.tokenization.tokenizer.Tokenizer
import meow.utils.MinimunEditDistance
import mylittleplugin.MyLittleActions.*


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
                    val node = ctx.resultAsNodes()[0]
                    val relationNodeId = node.id()
                    val type = node.get("type") as String
                    node.rephase()
                    val relation = node.get(TOKENIZE_CONTENT_TOKENS) as Relation
                    val relationsId = relation.all().take(relation.size())
                    val newContent = ctx.variable("newToken").asArray()
                    val newContentId = mutableListOf<Long>()
                    newContent.mapTo(newContentId) { (it as Node).id() }
                    val med = MinimunEditDistance(relationsId.toTypedArray(), newContentId.toTypedArray())
                    val path = med.path().toTypedArray()
                    ctx.setVariable("formerIndex", 0)
                    ctx.setVariable("newIndex", 0)
                    ctx.setVariable("relation", relation)
                    ctx.setVariable("relationId", relationNodeId)
                    ctx.setVariable("type", type)
                    ctx.continueWith(ctx.wrap(path))
                }.map(
                thenDo {
                    ctx ->
                    val action = ctx.result()[0] as Pair<Long, MinimunEditDistance.Modification>
                    val relation = ctx.variable("relation")[0] as Relation
                    val newIndex = ctx.variable("newIndex")[0] as Int
                    val formerIndex = ctx.variable("formerIndex")[0] as Int
                    val relationNodeId = ctx.variable("relationId")[0] as Long
                    val type = ctx.variable("type")[0] as String
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
                                    }.executeFrom(ctx, ctx.result(), SchedulerAffinity.SAME_THREAD, {
                                ctx.setVariable("formerIndex", formerIndex + 1)
                                ctx.continueTask()
                            })
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
                                    }.executeFrom(ctx, ctx.result(), SchedulerAffinity.SAME_THREAD, {
                                ctx.setVariable("newIndex", newIndex + 1)
                                ctx.continueTask()
                            })
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
                                    }.executeFrom(ctx, ctx.result(), SchedulerAffinity.SAME_THREAD, {
                                ctx.setVariable("formerIndex", formerIndex + 1)
                                ctx.setVariable("newIndex", newIndex + 1)
                                ctx.continueTask()
                            })

                        }
                    }
                })
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
                .setAttribute(NODE_TYPE, Type.STRING, NODE_TYPE_TOKENIZE_CONTENT)

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
                                                                        .setAttribute(NODE_TYPE, Type.STRING, NODE_TYPE_INVERTED_INDEX)
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