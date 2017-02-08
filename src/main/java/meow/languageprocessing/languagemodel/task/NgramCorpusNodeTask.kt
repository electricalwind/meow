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
package meow.languageprocessing.languagemodel.task

import greycat.*
import greycat.Constants.BEGINNING_OF_TIME
import greycat.Tasks.newTask
import meow.languageprocessing.corpus.CorpusConstants.*
import meow.languageprocessing.corpus.actions.CorpusActions.getOrCreateCorpus
import meow.languageprocessing.languagemodel.LanguageModelConstants.*
import meow.languageprocessing.languagemodel.nodes.NgramCorpusNode
import mylittleplugin.MyLittleActions.*


object NgramCorpusNodeTask {

    fun getOrCreateNgramCorpus(corpusName: String): Task {
        return newTask()
                .then(getOrCreateCorpus(corpusName))
                .defineAsVar("corpus")
                .traverse(CORPUS_PLUGIN, CORPUS_PLUGIN_INDEX, NGRAM_CORPUS_NODE_TYPE)
                .then(ifEmptyThen(
                        createNgramCorpus()
                ))

                .thenDo { ctx ->
                    val node = ctx.resultAsNodes()[0] as NgramCorpusNode
                    node.learn()
                    ctx.continueTask()
                }
    }

    fun countNgramOccurenceInCorpus(ngramCorpusVar: String, nGramToCount: String): Task {
        //todo
        return newTask()
    }

    private fun createNgramCorpus(): Task {
        return newTask()
                .then(
                        executeAtWorldAndTime("0", "$BEGINNING_OF_TIME",
                                newTask()
                                        .createTypedNode(NGRAM_CORPUS_NODE_TYPE)
                                        .setAsVar("ngramCorpus")
                                        .setAttribute(CORPUS_PLUGIN_INDEX, Type.STRING, NGRAM_CORPUS_NODE_TYPE)
                                        .timeSensitivity("-1", "0")
                                        .addVarToRelation(NGRAM_CORPUS_NODE_CORPUS, "corpus")
                                        .readVar("corpus")
                                        .addVarToRelation(CORPUS_PLUGIN, "ngramCorpus", CORPUS_PLUGIN_INDEX)
                                        .readVar("ngramCorpus")
                        )
                )
    }

}