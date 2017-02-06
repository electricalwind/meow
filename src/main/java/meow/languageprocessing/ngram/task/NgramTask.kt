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
package meow.languageprocessing.ngram.task

import greycat.*
import greycat.Constants.BEGINNING_OF_TIME
import greycat.Tasks.newTask
import greycat.plugin.SchedulerAffinity
import meow.languageprocessing.ngram.NgramConstants.*
import meow.languageprocessing.ngram.actions.MwdbNgramActions
import meow.languageprocessing.ngram.actions.MwdbNgramActions.getOrCreateNgramFromVar
import meow.tokens.TokensConstants.*
import meow.tokens.actions.MwdbTokenActions.getOrCreateTokensFromString
import mylittleplugin.MyLittleActions.*


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
                .then(
                        ifNotEmptyThen(
                                getOrCreateNgramFromTokenVar("tokenList")
                        )
                )
    }

    @JvmStatic
    fun getOrCreateNgramFromTokenVar(tokensVar: String): Task {
        return newTask()
                .readVar(tokensVar)
                .thenDo { ctx ->
                    val listOfId = ctx.resultAsNodes().asArray()
                            .map { (it as Node).id() }
                            .joinToString(separator = ",", prefix = "[", postfix = "]")

                    ctx.continueWith(ctx.wrap(listOfId))
                }
                .defineAsVar("tokensId")
                .then(MwdbNgramActions.retrieveNgramMainNode())
                .defineAsVar("ngramMain")
                .traverse(NGRAM_INDEX, GRAMS_TOKENS, "{{tokensId}}")
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
                        .setAttribute(NODE_TYPE, Type.STRING, NODE_TYPE_NGRAM)
                        .defineAsVar("newNgram")
                        .timeSensitivity("-1", "0")
                        .addVarToRelation(GRAMS_TOKENS, tokensVar)
                        .thenDo { ctx ->
                            val order = ctx.variable(tokensVar).size()
                            val node = ctx.resultAsNodes()[0]
                            node.set("order", Type.INT, order)
                            val tokens = ctx.variable(tokensVar).asArray()

                            if (order != 1) {
                                newTask()
                                        .inject(tokens.take(tokens.size - 1).toTypedArray())
                                        .defineAsVar("history")
                                        .then(getOrCreateNgramFromVar("history"))
                                        .defineAsVar("historyNgram")
                                        .addVarToRelation("historyTo", "newNgram")
                                        .readVar("newNgram")
                                        .addVarToRelation("history", "historyNgram")


                                        .inject(tokens.takeLast(tokens.size - 1).toTypedArray())
                                        .defineAsVar("backoff")
                                        .then(getOrCreateNgramFromVar("backoff"))
                                        .defineAsVar("backOffNgram")
                                        .addVarToRelation("backoffTo", "newNgram")
                                        .readVar("newNgram")
                                        .addVarToRelation("backOff", "backOffNgram")
                                        .executeFrom(ctx, ctx.result(), SchedulerAffinity.SAME_THREAD, {
                                            ctx.continueTask()
                                        })
                            } else {
                                ctx.continueTask()
                            }

                        }
                        .readVar("ngramMain")
                        .addVarToRelation(NGRAM_INDEX, "newNgram", GRAMS_TOKENS)
                        .readVar("newNgram")
        ))
    }
}