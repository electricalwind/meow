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

    fun predict(ngramIds: LongArray, result: Callback<IntArray>) {
        val ngToII = getNgramToNgramInvertedIndex()
        val resultArray: IntArray = IntArray(ngramIds.size, { 0 })

        if (ngToII.size() != 0) {
            val arrayOfResult = Array<MutableList<Int>>(ngramIds.size,{ mutableListOf()})

            loopPar("0", "${ngramIds.size - 1}",

                    thenDo { ctx ->
                        val i = ctx.variable("i") as Int
                        ctx.setVariable("iter",i)
                        val ngramId = ngramIds[i]
                        val iingram = ngToII[ngramId]
                        if (iingram.isNotEmpty()) {
                            ctx.setVariable("iingrams", iingram.joinToString(prefix = "[", separator = ",", postfix = "]"))
                            ctx.continueWith(ctx.wrap(true))
                        } else {
                            ctx.continueWith(ctx.wrap(false))
                        }
                    }.ifThen({ ctx -> ctx.result()[0] as Boolean },
                            newTask()
                                    .lookupAll("{{iingrams}}")
                                    .forEach(
                                            thenDo { ctx ->
                                                val i =ctx.variable("iter")[0] as Int
                                                val ngramII = ctx.resultAsNodes()[0]
                                                val pos = ngramII.get("position") as IntArray
                                                arrayOfResult[i].add(pos.size)
                                                ctx.continueTask()
                                            }
                                    )
                    )
            ).execute(graph(),
                    {
                        taskresult ->
                        for(i in 0..ngramIds.size-1){
                            resultArray[i] =arrayOfResult[i].sum()
                        }
                        result.on(resultArray)
                    }
                    )
            /**for (i in 0..doubleArray.size - 1) {

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
            waiter.then { result.on(resultArray) }*/

        } else {
            throw RuntimeException("Trying to predic on a empty ngramCorpus")
        }

    }

    fun learn(done: Callback<Boolean>) {
        super.relation(NGRAM_CORPUS_NODE_CORPUS, Callback { father ->
            //if no corpus attach no need to continue
            if (father.size != 1) throw RuntimeException("No corpus declared for this NgramCorpus Node")

            //retrieve the corpus relation is supposed to be of size one
            val corpus = father[0]

            //retrieve all the Long LongArray Map
            val tcToII = getTCToNgramInvertedIndex() //tokenizedContent to InvertedIndex
            val ngToII = getNgramToNgramInvertedIndex() //ngram to InvertedIndex
            val tcMN = getTCMagicNumbers() //tokenized Content to Magic Numbers


            val tcCurrent = keyOfLongToLongArrayMap(tcToII).toList() //current tokenize Content known by the node

            val tcCorpusRel = (corpus.get(CORPUS_TO_TOKENIZEDCONTENTS_RELATION) as Relation)
            val tcCorpus = tcCorpusRel.all().take(tcCorpusRel.size()) //tokenize Content known by the corpus

            val addedContents = tcCorpus.minus(tcCurrent) //Tokenize Content present in the corpus but not known by the node
            val removedContents = tcCurrent.minus(tcCorpus) //Tokenize Content known by the node but not present in the corpus anymore
            val similarContents = tcCorpus.intersect(tcCurrent) //Tokenize Content known by the node and still present in the corpus

            newTask()
                    .ifThen({ removedContents.isNotEmpty() }, //If there are tokenize content to remove
                            newTask()
                                    .then(retrieveNgramMainNode())
                                    .traverse(NGRAM_INDEX)
                                    .setAsVar("ngram") // retrieve all Ngram
                                    .inject(removedContents.toLongArray())// inject content to remove
                                    .map(//for each tokenize content
                                            newTask()
                                                    .setAsVar("toRemove")
                                                    .readVar("ngram")
                                                    .traverse(NGRAM_INVERTED_INDEX_RELATION, II_TC, "{{toRemove}}") // retrieve all ngram that are using it
                                    )
                                    .flat() // flatten the result
                                    .thenDo { ctx ->

                                        ctx.result().asArray()
                                                .forEach { // for all ngram inverted index retrieved
                                                    result ->
                                                    val node = result as Node
                                                    val nodeId = node.id()
                                                    val tc = node.get(II_TC) as Long
                                                    val ngram = node.get(INVERTED_NGRAM_INDEX_RELATION) as Relation
                                                    val ngramId = ngram[0]

                                                    ngToII.delete(ngramId, nodeId) //delete it from ngram map
                                                    tcToII.delete(tc, nodeId) //delete it from tokenize Content map
                                                }
                                        removedContents.forEach { tc ->
                                            // for all content to remove
                                            val values = tcMN.get(tc) // retrieve all magic number
                                            values.forEach { value ->
                                                tcMN.delete(tc, value) //delete them
                                            }
                                        }
                                        ctx.continueTask()
                                    }
                    )
                    .ifThen({ addedContents.isNotEmpty() }, // If there are tokenize Content to add
                            newTask()
                                    .lookupAll(addedContents.joinToString(prefix = "[", separator = ",", postfix = "]")) //retrieve all the new tc
                                    .setAsVar("tc")

                                    .then(updateNgramTokenizedContentFromVar("tc")) //update their Ngram

                                    .readVar("tc")
                                    .forEach(// for each tc retrieve their magic numbers and add them to the map of magic number
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

                                    .then(retrieveNgramMainNode()) // then retrieve all ngram
                                    .traverse(NGRAM_INDEX)
                                    .setAsVar("ngram")

                                    .inject(addedContents.toLongArray())
                                    .map(// for each tc
                                            newTask()
                                                    .setAsVar("toAdd")
                                                    .readVar("ngram")
                                                    .traverse(NGRAM_INVERTED_INDEX_RELATION, II_TC, "{{toAdd}}") //retrieve all their corresponding ngram inverted index
                                    )
                                    .flat() //flatten

                                    .thenDo { ctx ->

                                        ctx.result().asArray().forEach { // for each ngram ii
                                            result ->
                                            val node = result as Node
                                            val nodeId = node.id()
                                            val tc = node.get(II_TC) as Long
                                            val ngram = node.get(INVERTED_NGRAM_INDEX_RELATION) as Relation
                                            val ngramId = ngram[0]

                                            ngToII.put(ngramId, nodeId) //add them to the ngram map
                                            tcToII.put(tc, nodeId) //add them to the tc map
                                        }
                                        ctx.continueTask()
                                    }

                    )
                    .ifThen({ similarContents.isNotEmpty() }, // If there are tokenize Content to that stay
                            newTask()
                                    .then(retrieveNgramMainNode())
                                    .traverse(NGRAM_INDEX)
                                    .setAsVar("ngram") //retrieve all ngram

                                    .lookupAll(addedContents.joinToString(prefix = "[", separator = ",", postfix = "]")) //retrieve all tokenize content
                                    .declareVar("tcToUpdate")
                                    .declareVar("tcIdToUpdate")
                                    .forEach(// for each of them if their magic number changes then add it to the tctoUpdate variable
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

                                    .then(updateNgramTokenizedContentFromVar("tcToUpdate")) //then update the ngram

                                    .readVar("tcToUpdate")
                                    .forEach(//then update the magic number map
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
                                                    .setAsVar("toUpdate")// for each tc in this situation retrieve the current list of their ngram
                                                    .thenDo { ctx ->
                                                        val idTC = ctx.result()[0] as Long
                                                        tcToII[idTC].forEach { ngramId ->
                                                            ctx.addToVariable("alreadyExistingNgramII", ngramId)
                                                        }
                                                        ctx.continueTask()
                                                    }
                                                    .readVar("ngram")
                                                    .traverse(NGRAM_INVERTED_INDEX_RELATION, II_TC, "{{toUpdate}}") //then retrieve the new list of their ngram
                                    )
                                    .flat() //flatten

                                    .declareVar("alreadyExistingNgramII")

                                    .forEach(// for each ngram if it was not present before put it in the alreadyExistingNgramII var
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
                                    .readVar("listOfNewNgramII") // from this list
                                    .thenDo { ctx ->
                                        val taskresult = ctx.result()
                                        taskresult.asArray().forEach {
                                            result ->
                                            val node = result as Node
                                            val nodeId = node.id()
                                            val tc = node.get(II_TC) as Long
                                            val ngram = node.get(INVERTED_NGRAM_INDEX_RELATION) as Relation
                                            val ngramId = ngram[0]

                                            ngToII.put(ngramId, nodeId) // add the nex ngram ii to ngram map
                                            tcToII.put(tc, nodeId) //add the new ngram ii to the tc map
                                        }
                                        ctx.continueTask()
                                    }
                    )
                    .execute(graph(), {
                        done.on(true)
                    })
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