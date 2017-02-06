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
import greycat.Tasks.newTask
import meow.tokens.TokensConstants.*
import meow.tokens.actions.MwdbTokenActions.retrieveVocabularyNode
import mu.KLogging
import mylittleplugin.MyLittleActions.*

object VocabularyTask : KLogging() {

    /**
     * Create a NodeIndex vocabulary
     */
    @JvmStatic
    fun initializeVocabulary(): Task {
        return newTask()
                .readGlobalIndex(ENTRY_POINT_INDEX, ENTRY_POINT_NODE_NAME, VOCABULARY_NODE_NAME)
                .then(ifEmptyThen(
                        newTask().then(executeAtWorldAndTime("0", "$BEGINNING_OF_TIME",
                                newTask()
                                        .createNode()
                                        .setAttribute(ENTRY_POINT_NODE_NAME, Type.STRING, VOCABULARY_NODE_NAME)
                                        .timeSensitivity("-1", "0")
                                        .addToGlobalIndex(ENTRY_POINT_INDEX, ENTRY_POINT_NODE_NAME)
                        ))
                ))


    }

    @JvmStatic
    fun retrieveVocabulary(): Task {
        return newTask()
                .readGlobalIndex(ENTRY_POINT_INDEX, ENTRY_POINT_NODE_NAME, VOCABULARY_NODE_NAME)
    }

    @JvmStatic
    fun rebuildingTokenizeContent(tokenizedContentsVar: String): Task {
        return newTask()
                .readVar(tokenizedContentsVar)
                .map(
                        newTask()
                                .defineAsVar("tokenizeContent")
                                .thenDo { ctx ->
                                    val tokenizedContentNode = ctx.resultAsNodes()[0]
                                    tokenizedContentNode.relation(TOKENIZE_CONTENT_TOKENS, { nodeArray ->
                                        val content = nodeArray.map { node -> node.get(TOKEN_NAME) as String }.joinToString(separator = " ")
                                        val type = tokenizedContentNode.get("type")
                                        val name = tokenizedContentNode.get(TOKENIZE_CONTENT_NAME)
                                        ctx.continueWith(ctx.wrap(arrayOf(name, type, content)))
                                    })
                                }
                )

    }


    @JvmStatic
    fun getOrCreateTokensFromString(tokens: Array<String>): Task {
        return newTask()
                .then(retrieveVocabularyNode())
                .defineAsVar("Vocabulary")
                .inject(tokens)
                .map(retrieveToken())
                .flat()
    }

    private fun retrieveToken(): Task {
        return newTask()
                .defineAsVar("token")
                .readVar("Vocabulary")
                .traverse(VOCABULARY_TOKEN_INDEX, TOKEN_NAME, "{{token}}")
                .then(
                        ifEmptyThen(
                                createToken()
                        )
                )
    }

    private fun createToken(): Task {
        return newTask()
                .then(executeAtWorldAndTime(
                        "0",
                        "$BEGINNING_OF_TIME",
                        newTask()
                                //Token
                                .createNode()
                                .timeSensitivity("-1", "0")
                                .setAttribute(TOKEN_NAME, Type.STRING, "{{token}}")
                                .setAttribute(NODE_TYPE, Type.STRING, NODE_TYPE_TOKEN)
                                .defineAsVar("newToken")
                                .readVar("Vocabulary")
                                .addVarToRelation(VOCABULARY_TOKEN_INDEX, "newToken", TOKEN_NAME)
                                .readVar("newToken")
                ))

    }


}