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
import greycat.Tasks.*
import greycat.base.BaseNode
import greycat.chunk.TimeTreeChunk
import greycat.struct.*
import meow.languageprocessing.corpus.CorpusConstants.CORPUS_TO_TOKENIZEDCONTENTS_RELATION
import meow.languageprocessing.languagemodel.LanguageModelConstants.NGRAM_CORPUS_NODE_CORPUS
import meow.languageprocessing.ngram.NgramConstants.*
import meow.languageprocessing.ngram.actions.NgramActions.*
import meow.tokens.TokensConstants.II_TC
import meow.utils.GreyCatUtilitary.keyOfLongToLongArrayMap


class NgramCorpusNode(p_world: Long, p_time: Long, p_id: Long, p_graph: Graph) : BaseNode(p_world, p_time, p_id, p_graph) {

    fun predict(doubleArray: LongArray, result: Callback<DoubleArray>) {
        val ngToII = getNgramToNgramInvertedIndex()
        val resultArray: DoubleArray = DoubleArray(doubleArray.size, { 0.0 })

        if (ngToII.size() != 0) {
            val waiter: DeferCounter = graph().newCounter(doubleArray.size)

            for (i in 0..doubleArray.size - 1) {

                val ngramId = doubleArray[i]
                val arrayOfTCContainingNgram = ngToII[ngramId]

                if (arrayOfTCContainingNgram.isNotEmpty()) {
                    val counter = IntArray(arrayOfTCContainingNgram.size, { 0 })
                    newTask()
                            .lookupAll(arrayOfTCContainingNgram.joinToString(prefix = "[", separator = ",", postfix = "]"))
                            .forEach(
                                    thenDo { ctx ->
                                        val ngramII = ctx.resultAsNodes()[0]
                                        val index = ctx.variable("i")[0] as Int
                                        val pos = ngramII.get("position") as IntArray
                                        counter[index] = pos.size
                                        ctx.continueTask()
                                    }
                            )
                            .execute(graph(), {
                                taskresult ->
                                resultArray[i] = counter.sum().toDouble()
                                waiter.count()
                            })
                } else {
                    waiter.count()
                }
            }
            waiter.then { result.on(resultArray) }

        } else {
            throw RuntimeException("Trying to predic on a empty ngramCorpus")
        }

    }

    fun learn(done: Callback<Boolean>) {
        super.relation(NGRAM_CORPUS_NODE_CORPUS, Callback { father ->
            if (father.size != 1) throw RuntimeException("No corpus declared for this NgramCorpus Node")

            val corpus = father[0]

            val tcToII = getTCToNgramInvertedIndex()
            val ngToII = getNgramToNgramInvertedIndex()
            val tcMN = getTCMagicNumbers()


            val tcCurrent = keyOfLongToLongArrayMap(tcToII).toList()

            val tcCorpusRel = (corpus.get(CORPUS_TO_TOKENIZEDCONTENTS_RELATION) as Relation)
            val tcCorpus = tcCorpusRel.all().take(tcCorpusRel.size())

            val addedContents = tcCorpus.minus(tcCurrent)
            val removedContents = tcCurrent.minus(tcCorpus)
            val similarContents = tcCorpus.intersect(tcCurrent)

            newTask()
                    .ifThen({ removedContents.isNotEmpty() },
                            newTask().lookupAll(removedContents.joinToString(prefix = "[", separator = ",", postfix = "]"))
                                    .then(retrieveNgramMainNode())
                                    .traverse(NGRAM_INDEX)
                                    .setAsVar("ngram")
                                    .inject(removedContents.toLongArray())
                                    .map(
                                            newTask()
                                                    .setAsVar("toRemove")
                                                    .readVar("ngram")
                                                    .traverse(NGRAM_INVERTED_INDEX_RELATION, II_TC, "{{toRemove}}")
                                    )
                                    .flat()
                                    .thenDo { ctx ->
                                        val taskresult = ctx.result()
                                        taskresult.asArray().forEach {
                                            result ->
                                            val node = result as Node
                                            val nodeId = node.id()
                                            val tc = node.get(II_TC) as Long
                                            val ngram = node.get(INVERTED_NGRAM_INDEX_RELATION) as Relation
                                            val ngramId = ngram[0]

                                            ngToII.delete(ngramId, nodeId)
                                            tcToII.delete(tc, nodeId)
                                        }
                                        removedContents.forEach { tc ->
                                            val values = tcMN.get(tc)
                                            values.forEach { value ->
                                                tcMN.delete(tc, value)
                                            }
                                        }
                                        ctx.continueTask()
                                    }
                    )
                    .ifThen({ addedContents.isNotEmpty() },
                            newTask()
                                    .lookupAll(addedContents.joinToString(prefix = "[", separator = ",", postfix = "]"))
                                    .setAsVar("tc")
                                    .then(updateNgramTokenizedContentFromVar("tc"))
                                    .readVar("tc")
                                    .forEach(
                                            thenDo { ctx ->
                                                val node = ctx.resultAsNodes()[0] as BaseNode
                                                val nodeid = node.id()
                                                val supertime = node._index_superTimeTree
                                                val time = node._index_timeTree

                                                val magicTime = (this.graph().space().get(time) as TimeTreeChunk).magic()
                                                val magicsuperTime = (this.graph().space().get(supertime) as TimeTreeChunk).magic()

                                                tcMN.put(nodeid, magicTime)
                                                tcMN.put(nodeid, magicsuperTime)
                                                ctx.continueTask()
                                            }
                                    )
                                    .then(retrieveNgramMainNode())
                                    .traverse(NGRAM_INDEX)
                                    .setAsVar("ngram")
                                    .inject(addedContents.toLongArray())
                                    .map(
                                            newTask()
                                                    .setAsVar("toAdd")
                                                    .readVar("ngram")
                                                    .traverse(NGRAM_INVERTED_INDEX_RELATION, II_TC, "{{toAdd}}")
                                    )
                                    .flat()
                                    .thenDo { ctx ->
                                        val taskresult = ctx.result()
                                        taskresult.asArray().forEach {
                                            result ->
                                            val node = result as Node
                                            val nodeId = node.id()
                                            val tc = node.get(II_TC) as Long
                                            val ngram = node.get(INVERTED_NGRAM_INDEX_RELATION) as Relation
                                            val ngramId = ngram[0]

                                            ngToII.put(ngramId, nodeId)
                                            tcToII.put(tc, nodeId)
                                        }
                                        ctx.continueTask()
                                    }

                    )
                    .ifThen({ similarContents.isNotEmpty() },
                            newTask()
                                    .then(retrieveNgramMainNode())
                                    .traverse(NGRAM_INDEX)
                                    .setAsVar("ngram")

                                    .lookupAll(addedContents.joinToString(prefix = "[", separator = ",", postfix = "]"))
                                    .declareVar("tcToUpdate")
                                    .declareVar("tcIdToUpdate")
                                    .forEach(
                                            thenDo { ctx ->
                                                val node = ctx.resultAsNodes()[0] as BaseNode
                                                val nodeid = node.id()
                                                val supertime = node._index_superTimeTree
                                                val time = node._index_timeTree

                                                val magicTime = (this.graph().space().get(time) as TimeTreeChunk).magic()
                                                val magicsuperTime = (this.graph().space().get(supertime) as TimeTreeChunk).magic()

                                                val storedMagicTime = tcMN[nodeid][0]
                                                val storedSuperMagicTime = tcMN[nodeid][1]

                                                if (magicTime != storedMagicTime || magicsuperTime != storedSuperMagicTime) {
                                                    ctx.addToVariable("tcToUpdate", node)
                                                    ctx.addToVariable("tcIdToUpdate", nodeid)
                                                }
                                                ctx.continueTask()
                                            }
                                    )

                                    .readVar("tcIdToUpdate")
                                    .map(
                                            newTask()
                                                    .setAsVar("toUpdate")
                                                    .readVar("ngram")
                                                    .traverse(NGRAM_INVERTED_INDEX_RELATION, II_TC, "{{toUpdate}}")
                                    )
                                    .flat()
                                    .map(
                                            thenDo { ctx ->
                                                ctx.continueWith(ctx.wrap(ctx.resultAsNodes()[0].id()))
                                            })
                                    .setAsVar("alreadyExistingNgramII")

                                    .then(updateNgramTokenizedContentFromVar("tcToUpdate"))
                                    .readVar("tcToUpdate")
                                    .forEach(
                                            thenDo { ctx ->
                                                val node = ctx.resultAsNodes()[0] as BaseNode
                                                val nodeId = node.id()
                                                val supertime = node._index_superTimeTree
                                                val time = node._index_timeTree

                                                val magicTime = (this.graph().space().get(time) as TimeTreeChunk).magic()
                                                val magicsuperTime = (this.graph().space().get(supertime) as TimeTreeChunk).magic()

                                                tcMN.delete(nodeId, tcMN[nodeId][0])
                                                tcMN.delete(nodeId, tcMN[nodeId][1])
                                                tcMN.put(node.id(), magicTime)
                                                tcMN.put(node.id(), magicsuperTime)
                                                ctx.continueTask()
                                            }
                                    )

                                    .declareVar("listOfNewNgramII")

                                    .readVar("tcIdToUpdate")
                                    .map(
                                            newTask()
                                                    .setAsVar("toUpdate")
                                                    .readVar("ngram")
                                                    .traverse(NGRAM_INVERTED_INDEX_RELATION, II_TC, "{{toUpdate}}")
                                    )
                                    .flat()

                                    .forEach(
                                            newTask()
                                                    .thenDo { ctx ->
                                                        val node = ctx.resultAsNodes()[0]
                                                        val nodeId = node.id()
                                                        val listOfAlreadyExisting = ctx.variable("alreadyExistingNgramII").asArray().map { id -> id as Long }

                                                        if (!listOfAlreadyExisting.contains(nodeId)) {
                                                            ctx.addToVariable("listOfNewNgramII", node)
                                                        }
                                                        ctx.continueTask()
                                                    }
                                    )
                                    .readVar("listOfNewNgramII")
                                    .thenDo { ctx ->
                                        val taskresult = ctx.result()
                                        taskresult.asArray().forEach {
                                            result ->
                                            val node = result as Node
                                            val nodeId = node.id()
                                            val tc = node.get(II_TC) as Long
                                            val ngram = node.get(INVERTED_NGRAM_INDEX_RELATION) as Relation
                                            val ngramId = ngram[0]

                                            ngToII.put(ngramId, nodeId)
                                            tcToII.put(tc, nodeId)
                                        }
                                        ctx.continueTask()
                                    }
                    )
                    .execute(graph(), {
                        done.on(true)
                    }
                    )
        })
    }

    private fun getTCToNgramInvertedIndex(): LongLongArrayMap {
        return getOrCreate("tcToII", Type.LONG_TO_LONG_ARRAY_MAP) as LongLongArrayMap
    }

    private fun getNgramToNgramInvertedIndex(): LongLongArrayMap {
        return getOrCreate("NgramToII", Type.LONG_TO_LONG_ARRAY_MAP) as LongLongArrayMap
    }

    private fun getTCMagicNumbers(): LongLongArrayMap {
        return getOrCreate("tcMN", Type.LONG_TO_LONG_ARRAY_MAP) as LongLongArrayMap
    }

}