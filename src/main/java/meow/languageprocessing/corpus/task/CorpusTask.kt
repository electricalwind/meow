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
package meow.languageprocessing.corpus.task

import greycat.*
import greycat.Constants.BEGINNING_OF_TIME
import greycat.Tasks.*
import greycat.struct.Relation
import meow.languageprocessing.corpus.CorpusConstants.*
import meow.languageprocessing.corpus.actions.CorpusActions
import meow.tokens.TokensConstants.*
import meow.tokens.task.UtilTask.checkNodesType
import mylittleplugin.MyLittleActions.*


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
                .then(CorpusActions.retrieveCorpusMainNode())
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
                                .setAttribute(CORPUS_VERSION,Type.INT,"0")
                                .timeSensitivity("-1", "0")
                                .defineAsVar("corpusNode")
                                .then(CorpusActions.retrieveCorpusMainNode())
                                .defineAsVar("corpusMain")
                                .addVarToRelation(CORPUS_RELATION, "corpusNode", CORPUS_NAME)
                                .readVar("corpusNode")
                )
        )
    }

    @JvmStatic
    fun addTokenizeContentToCorpus(tokenizeContentVar: String, corpusName: String): Task {
        return newTask()
                .then(CorpusActions.getOrCreateCorpus(corpusName))
                .defineAsVar("corpus")
                .thenDo { ctx ->
                    val corpus = ctx.resultAsNodes()[0]
                    val previous = corpus.get(CORPUS_VERSION) as Int
                    corpus.set(CORPUS_VERSION,Type.INT,previous+1)
                    ctx.continueTask()
                }
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
                .then(CorpusActions.getOrCreateCorpus(corpusName))
                .defineAsVar("corpus")
                .thenDo { ctx ->
                    val corpus = ctx.resultAsNodes()[0]
                    val previous = corpus.get(CORPUS_VERSION) as Int
                    corpus.set(CORPUS_VERSION,Type.INT,previous+1)
                    ctx.continueTask()
                }
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