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
package lu.jimenez.research.mwgtoken.core.task

import lu.jimenez.research.mwgtoken.tokenization.*
import lu.jimenez.research.mwgtoken.tokenization.preprocessor.TokenPreprocessor
import org.mwg.task.*
import org.mwg.task.Tasks.newTask


class TokenizationTask(tokenizer: String) {
    val factory: TokenizerFactory = TokenizerFactory(tokenizer)

    private fun setPreprocessor(preprocessor: TokenPreprocessor?) {
        factory.tokenPreprocess = preprocessor
    }

    private fun tokenizeString(toTokenize: Array<String>, types: Array<String>?): Task {
        return newTask()
                .inject(toTokenize)
                .map(tokenize(types))
                .flat()
    }

    private fun tokenize(types: Array<String>?): Task {
        return newTask()
                .thenDo { ctx: TaskContext ->
                    var type: String? = types?.get(ctx.variable("i")[0] as Int)
                    var tokenizer = factory.create(ctx.resultAsStrings()[0],type)
                    ctx.continueWith(ctx.wrap(tokenizer))
                }
    }

    companion object statical {
        @JvmStatic
        fun tokenizeStringsUsingTokenizer(tokenizer: String, tpreprocessor: String?, type: String, params: Array<String>): Task {
            val preprocessor = PreProcessorFactory.create(tpreprocessor)
            val tokenization = TokenizationTask(tokenizer)
            tokenization.setPreprocessor(preprocessor)
            val toTokenize: Array<String>
            val types: Array<String>?
            if (type.toBoolean()) {
                if (params.size % 2 != 0) {
                    throw RuntimeException("not the same number of types and stringToTokenize")
                } else {
                    val listString = mutableListOf<String>()
                    val listType = mutableListOf<String>()
                    var i = 0
                    while (i < params.size) {
                        listString.add(params[i+1])
                        listType.add(params[i])
                        i += 2
                    }
                    toTokenize = listString.toTypedArray()
                    types = listType.toTypedArray()
                }
            } else {
                toTokenize = params
                types = null
            }
            return tokenization.tokenizeString(toTokenize, types)
        }
    }
}