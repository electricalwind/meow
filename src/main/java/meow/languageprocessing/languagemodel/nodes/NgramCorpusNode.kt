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
package meow.languageprocessing.languagemodel.nodes

import greycat.*
import greycat.base.BaseNode
import greycat.ml.ProfilingNode
import greycat.struct.Relation
import meow.languageprocessing.corpus.CorpusConstants.CORPUS_TO_TOKENIZEDCONTENTS_RELATION
import meow.languageprocessing.languagemodel.LanguageModelConstants.NGRAM_CORPUS_NODE_CORPUS


class NgramCorpusNode(p_world: Long, p_time: Long, p_id: Long, p_graph: Graph) : ProfilingNode, BaseNode(p_world, p_time, p_id, p_graph) {


    override fun learn(callback: Callback<Boolean>) {
        extractFeatures(Callback<DoubleArray> { values ->
            learnWith(values)
        })
    }


    private fun extractFeatures(callback: Callback<DoubleArray>) {
        super.relation(NGRAM_CORPUS_NODE_CORPUS, Callback { father ->
            val corpus: Node = father[0]
            val tc = corpus.get(CORPUS_TO_TOKENIZEDCONTENTS_RELATION) as Relation
            val ids = tc.all().take(tc.size()).map(Long::toDouble).toDoubleArray()
            callback.on(ids)
        })
    }

    override fun learnWith(values: DoubleArray) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun predict(callback: Callback<DoubleArray>?) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}