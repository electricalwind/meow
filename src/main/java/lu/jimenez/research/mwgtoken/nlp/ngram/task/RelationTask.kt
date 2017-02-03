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
package lu.jimenez.research.mwgtoken.nlp.ngram.task

import lu.jimenez.research.mwgtoken.core.CoreConstants.*
import lu.jimenez.research.mwgtoken.core.task.UtilTask.checkNodesType
import lu.jimenez.research.mwgtoken.nlp.ngram.NgramConstants.*
import lu.jimenez.research.mwgtoken.nlp.ngram.actions.MwdbNgramActions.getOrCreateNgramFromVar
import lu.jimenez.research.mwgtoken.utils.MinimunEditDistance
import lu.jimenez.research.mylittleplugin.MyLittleActions.*
import org.mwg.*
import org.mwg.Constants.*
import org.mwg.plugin.SchedulerAffinity
import org.mwg.struct.Relation
import org.mwg.task.Task
import org.mwg.task.Tasks.*


object RelationTask {

    val tokenizedContent: String = "tc"
    val tokenizedContentId: String = "tcId"
    val tokenizedContentTimepoints: String = "timepoints"
    val tokenizedContentType: String = "tcType"

    val ngramtokenizedContent: String = "ngramtc"

    @JvmStatic
    fun updateNgramTokenizeContentVar(tokenizeContentVar: String): Task {
        return newTask()
                .pipe(checkNodesType(tokenizeContentVar, NODE_TYPE_TOKENIZE_CONTENT))
                .forEach(
                        updateNgramTokenizeContent()
                )
    }

    private fun updateNgramTokenizeContent(): Task {
        return newTask()
                .defineAsVar(tokenizedContent)
                .thenDo { ctx ->
                    ctx.setVariable(tokenizedContentId, ctx.resultAsNodes()[0].id())
                    ctx.setVariable(tokenizedContentType, ctx.resultAsNodes()[0].get("type") as String)
                    ctx.continueTask()
                }
                .thenDo { ctx ->
                    ctx.resultAsNodes().get(0).timepoints(BEGINNING_OF_TIME, END_OF_TIME, {
                        timePoints ->
                        timePoints.reverse()
                        ctx.setVariable(tokenizedContentTimepoints, timePoints)
                        ctx.continueTask()
                    })
                }

                .traverse(TOKENIZE_CONTENT_PLUGIN, NODE_TYPE, NODE_TYPE_NGRAM_TOKENIZED_CONTENT)

                .then(
                        ifEmptyThen(
                                createFirstNgramTokenizedContent()
                        )
                )
                .setAsVar(ngramtokenizedContent)
                .thenDo { ctx ->
                    ctx.resultAsNodes().get(0).timepoints(BEGINNING_OF_TIME, END_OF_TIME, {
                        timePoints ->
                        val toStudy = ctx.variable(tokenizedContentTimepoints).asArray().map { id -> id as Long }.toMutableList()
                        toStudy.removeAll(timePoints.asList())
                        toStudy.reverse()
                        ctx.continueWith(ctx.wrap(toStudy.toLongArray()))
                    })
                }
                .forEach(
                        newTask()
                                .setAsVar("timepoint")
                                .travelInTime("{{timepoint}}")
                                .pipe(updateNgramsRelation())
                ).readVar(tokenizedContent)


    }

    private fun createFirstNgramTokenizedContent(): Task {
        return newTask()
                .thenDo { ctx ->
                    ctx.setVariable("ft", ctx.variable(tokenizedContentTimepoints).get(0))
                    ctx.continueTask()
                }
                .travelInTime("{{ft}}")

                .createNode()
                .addVarToRelation("$TOKENIZED_CONTENT_FATHER", "$tokenizedContent")
                .setAttribute(NODE_TYPE, Type.STRING, NODE_TYPE_NGRAM_TOKENIZED_CONTENT)
                .setAsVar(ngramtokenizedContent)
                .pipe(
                        createNgramsRelation()
                )
                .readVar(tokenizedContentTimepoints)
                .forEach(
                        newTask()
                                .setAsVar("time")
                                .travelInTime("{{time}}")
                                .then(readUpdatedTimeVar(tokenizedContent))
                                .addVarToRelation(TOKENIZE_CONTENT_PLUGIN, ngramtokenizedContent, NODE_TYPE)
                )
                .readVar(ngramtokenizedContent)


    }

    private fun createNgramsRelation(): Task {
        return newTask()
                .then(readUpdatedTimeVar(tokenizedContent))
                .traverse(TOKENIZE_CONTENT_TOKENS)
                .setAsVar("tokens")
                .loop("1", "$MAXIMUM_ORDER_OF_N",

                        thenDo { ctx ->
                            ctx.setVariable("order", ctx.variable("i")[0])
                            ctx.continueTask()
                        }
                                .pipe(retrieveNgram())
                                .forEach(
                                        newTask()
                                                .defineAsVar("ngram")
                                                .traverse(NGRAM_INVERTED_INDEX_RELATION, "id", "{{$tokenizedContentId}}")
                                                .then(
                                                        ifEmptyThenElse(
                                                                newTask()
                                                                        .then(
                                                                                executeAtWorldAndTime("0", "$BEGINNING_OF_TIME",
                                                                                        newTask()
                                                                                                .createNode()
                                                                                                .setAttribute("id", Type.LONG, "{{$tokenizedContentId}}")
                                                                                                .setAttribute(NODE_TYPE, Type.STRING, NODE_TYPE_INVERTED_INDEX)
                                                                                                .setAttribute("type", Type.STRING, "{{$tokenizedContentType}}")
                                                                                                .defineAsVar("invertedIndex")
                                                                                                .addVarToRelation(INVERTED_NGRAM_INDEX_RELATION, "ngram")
                                                                                                .readVar("ngram")
                                                                                                .addVarToRelation(NGRAM_INVERTED_INDEX_RELATION, "invertedIndex", "id")
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


                                                .readVar(ngramtokenizedContent)
                                                .addVarToRelation("{{order}}", "ngram")
                                )
                )
    }

    private fun retrieveNgram(): Task {
        return newTask()
                .thenDo { ctx ->
                    val n = ctx.variable("i").get(0) as Int
                    ctx.setVariable("n", n)
                    val tokens = ctx.variable("tokens").size()
                    ctx.setVariable("totalNgram", tokens - n)
                    ctx.continueTask()
                }
                .declareVar("ngramTokens")
                .loop(
                        "0",
                        "{{totalNgram}}",
                        newTask()
                                .thenDo { ctx ->
                                    val n = ctx.variable("n").get(0) as Int
                                    val i = ctx.variable("i").get(0) as Int
                                    val tokens = ctx.variable("tokens").asArray().map { node -> node as Node }.toTypedArray()
                                    ctx.setVariable("tokensVar", tokens.sliceArray(i..i + n - 1))
                                    ctx.continueTask()
                                }
                                .then(getOrCreateNgramFromVar("tokensVar"))
                                .addToVar("ngramTokens")
                )
                .readVar("ngramTokens")
    }

    private fun updateNgramsRelation(): Task {
        return newTask()
                .then(readUpdatedTimeVar(tokenizedContent))
                .traverse(TOKENIZE_CONTENT_TOKENS)
                .setAsVar("tokens")
                .loop("1", "$MAXIMUM_ORDER_OF_N",
                        retrieveNgram()
                                .setAsVar("newNgram")
                                .then(readUpdatedTimeVar(ngramtokenizedContent))
                                .thenDo { ctx ->
                                    val n = ctx.variable("i").get(0) as Int
                                    val node = ctx.resultAsNodes()[0]
                                    node.rephase()
                                    val relation = node.getOrCreate("$n", Type.RELATION) as Relation
                                    val relationsId = relation.all().take(relation.size())
                                    val newContent = ctx.variable("newNgram").asArray()
                                    val newContentId = mutableListOf<Long>()
                                    newContent.mapTo(newContentId) { (it as Node).id() }
                                    val med = MinimunEditDistance(relationsId.toTypedArray(), newContentId.toTypedArray())
                                    val path = med.path().toTypedArray()
                                    ctx.setVariable("formerIndex", 0)
                                    ctx.setVariable("newIndex", 0)
                                    ctx.setVariable("relation", relation)
                                    ctx.continueWith(ctx.wrap(path))
                                }
                                .map(
                                        thenDo {
                                            ctx ->
                                            val action = ctx.result()[0] as Pair<Long, MinimunEditDistance.Modification>
                                            val relation = ctx.variable("relation")[0] as Relation
                                            val newIndex = ctx.variable("newIndex")[0] as Int
                                            val formerIndex = ctx.variable("formerIndex")[0] as Int
                                            val relationNodeId = ctx.variable(tokenizedContentId)[0] as Long
                                            val type = ctx.variable(tokenizedContentType)[0] as String
                                            when (action.second) {
                                                MinimunEditDistance.Modification.Suppression -> {
                                                    relation.delete(newIndex)
                                                    newTask().lookup("${action.first}")
                                                            .traverse(NGRAM_INVERTED_INDEX_RELATION, "id", "$relationNodeId")
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
                                                            .traverse(NGRAM_INVERTED_INDEX_RELATION, "id", "$relationNodeId")
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
                                                                                                            .addVarToRelation(INVERTED_NGRAM_INDEX_RELATION, "token")
                                                                                                            .readVar("token")
                                                                                                            .addVarToRelation(NGRAM_INVERTED_INDEX_RELATION, "invertedIndex", "id")
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
                                                            .traverse(NGRAM_INVERTED_INDEX_RELATION, "id", "$relationNodeId")
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
                )
    }
}