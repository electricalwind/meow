/**
 * Copyright 2017 Matthieu Jimenez.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package meow.languageprocessing.ngram.task

import greycat.*
import greycat.Constants.*
import greycat.Tasks.*
import greycat.plugin.SchedulerAffinity
import greycat.struct.Relation
import meow.languageprocessing.ngram.NgramConstants
import meow.languageprocessing.ngram.NgramConstants.*
import meow.languageprocessing.ngram.actions.NgramActions.getOrCreateNgramFromVar
import meow.tokens.TokensConstants.*
import meow.tokens.actions.TokenActions.getOrCreateTokensFromString
import meow.utils.MinimunEditDistance
import meow.utils.UtilTask.checkNodesType
import mylittleplugin.MyLittleActions.*


object RelationTask {

    val tokenizedContent: String = "tc"
    val tokenizedContentId: String = "tcId"
    val tokenizedContentTimepoints: String = "timepoints"
    val tokenizedContentType: String = "tcType"

    val ngramtokenizedContent: String = "ngramtc"

    @JvmStatic
    fun updateNgramTokenizeContentVar(tokenizeContentVar: String): Task {
        return newTask()
                .pipe(checkNodesType(tokenizeContentVar, NODE_TYPE_TOKENIZE_CONTENT)) // Verify that it is a tokenize Content
                .forEach(
                        updateNgramTokenizeContent()
                )
    }

    private fun updateNgramTokenizeContent(): Task {
        return newTask()
                .defineAsVar(tokenizedContent) // tc in var
                .thenDo { ctx -> //storing the type and the id of tc
                    ctx.setVariable(tokenizedContentId, ctx.resultAsNodes()[0].id())
                    ctx.setVariable(tokenizedContentType, ctx.resultAsNodes()[0].get("type") as String)
                    ctx.continueTask()
                }
                .thenDo { ctx -> //then retrieve all timepoints of the tc //TODO children world
                    ctx.resultAsNodes().get(0).timepoints(BEGINNING_OF_TIME, END_OF_TIME, {
                        timePoints ->
                        timePoints.reverse()
                        ctx.setVariable(tokenizedContentTimepoints, timePoints)
                        ctx.continueTask()
                    })
                }

                .traverse(TOKENIZE_CONTENT_PLUGIN, NODE_TYPE, NODE_TYPE_NGRAM_TOKENIZED_CONTENT) // traverse plugin ngram

                .then(
                        ifEmptyThen( // if no plugin ngram node create one
                                createFirstNgramTokenizedContent()
                        )
                )
                .setAsVar(ngramtokenizedContent) // and store it in a var

                .thenDo { ctx -> // find all timepoints of the tc that are not existing for the ngram plugin TODO children word
                    ctx.resultAsNodes().get(0).timepoints(BEGINNING_OF_TIME, END_OF_TIME, {
                        timePoints ->
                        val toStudy = ctx.variable(tokenizedContentTimepoints).asArray().map { id -> id as Long }.toMutableList()
                        toStudy.removeAll(timePoints.asList())
                        toStudy.reverse()
                        ctx.continueWith(ctx.wrap(toStudy.toLongArray()))
                    })
                }

                .forEach( // for each not existing timepoints update Ngram plugin node
                        newTask()
                                .setAsVar("timepoint")
                                .travelInTime("{{timepoint}}")
                                .pipe(updateNgramsRelation())
                )
        //.readVar(tokenizedContent) //return the


    }

    private fun createFirstNgramTokenizedContent(): Task {
        return newTask()
                .thenDo { ctx -> // retrieve the first timepoint
                    ctx.setVariable("ft", ctx.variable(tokenizedContentTimepoints).get(0))
                    ctx.continueTask()
                }
                .travelInTime("{{ft}}") //travel to it

                .createNode() //create the ngramNode plugin
                .addVarToRelation("$TOKENIZED_CONTENT_FATHER", "$tokenizedContent") // add relation to the tc
                .setAttribute(NODE_TYPE, Type.STRING, NODE_TYPE_NGRAM_TOKENIZED_CONTENT) //set its type
                .setAsVar(ngramtokenizedContent)
                .pipe(
                        createNgramsRelation() //create the ngramRelation
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
                .then(readUpdatedTimeVar(tokenizedContent)) //read tc at the good time
                .traverse(TOKENIZE_CONTENT_TOKENS) //traverse to get the tokens
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
                                                .traverse(NGRAM_INVERTED_INDEX_RELATION, II_TC, "{{$tokenizedContentId}}")
                                                .then(
                                                        ifEmptyThenElse(
                                                                newTask()
                                                                        .then(
                                                                                executeAtWorldAndTime("0", "$BEGINNING_OF_TIME",
                                                                                        newTask()
                                                                                                .createNode()
                                                                                                .setAttribute(II_TC, Type.LONG, "{{$tokenizedContentId}}")
                                                                                                .setAttribute(NODE_TYPE, Type.STRING, NODE_TYPE_INVERTED_INDEX)
                                                                                                .setAttribute("type", Type.STRING, "{{$tokenizedContentType}}")
                                                                                                .defineAsVar("invertedIndex")
                                                                                                .addVarToRelation(INVERTED_NGRAM_INDEX_RELATION, "ngram")
                                                                                                .readVar("ngram")
                                                                                                .addVarToRelation(NGRAM_INVERTED_INDEX_RELATION, "invertedIndex", II_TC)
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
                .declareVar("tokensfinal")
                .ifThenElse({NgramConstants.ADD_BOS_EOS_TO_NGRAM},
                        newTask()
                                .then(getOrCreateTokensFromString(BOS))
                                .addToVar("tokensfinal")
                                .readVar("tokens")
                                .addToVar("tokensfinal")
                                .then(getOrCreateTokensFromString(EOS))
                                .addToVar("tokensfinal")
                        ,
                        newTask()
                                .readVar("tokens")
                                .addToVar("tokensfinal")
                )
                .thenDo { ctx ->
                    val n = ctx.variable("i").get(0) as Int
                    ctx.setVariable("n", n)
                    val nbtokens = ctx.variable("tokensfinal").size()
                    ctx.setVariable("totalNgram", nbtokens - n)
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
                                    val tokens = ctx.variable("tokensfinal").asArray().map { node -> node as Node }.toTypedArray()
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
                                                            .traverse(NGRAM_INVERTED_INDEX_RELATION, II_TC, "$relationNodeId")
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
                                                            .traverse(NGRAM_INVERTED_INDEX_RELATION, II_TC, "$relationNodeId")
                                                            .then(
                                                                    ifEmptyThen(
                                                                            newTask()
                                                                                    .then(
                                                                                            executeAtWorldAndTime("0", "$BEGINNING_OF_TIME",
                                                                                                    newTask()
                                                                                                            .createNode()
                                                                                                            .setAttribute(II_TC, Type.LONG, "$relationNodeId")
                                                                                                            .setAttribute("type", Type.STRING, "$type")
                                                                                                            .defineAsVar("invertedIndex")
                                                                                                            .addVarToRelation(INVERTED_NGRAM_INDEX_RELATION, "token")
                                                                                                            .readVar("token")
                                                                                                            .addVarToRelation(NGRAM_INVERTED_INDEX_RELATION, "invertedIndex", II_TC)
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
                                                            .traverse(NGRAM_INVERTED_INDEX_RELATION, II_TC, "$relationNodeId")
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