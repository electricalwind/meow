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
package miaou.tokens.tokenization

import miaou.tokens.TokensConstants.NO_TYPE_TOKENIZE
import miaou.tokens.tokenization.preprocessor.TokenPreprocessor
import miaou.tokens.tokenization.tokenizer.*

class TokenizerFactory(var tokenizerType: String) {
    var tokenPreprocess: TokenPreprocessor? = null

    fun create(toTokenize: String, typeOfToken: String? = null): Tokenizer {
        val tokenizer: Tokenizer
        when (tokenizerType) {
            else -> tokenizer = SimpleTokenizer(toTokenize)
        }
        tokenizer.setTokenPreprocessor(tokenPreprocess)
        if (typeOfToken != null)
            tokenizer.setTypeOfToken(typeOfToken)
        else tokenizer.setTypeOfToken(NO_TYPE_TOKENIZE)
        return tokenizer
    }

    fun setTokenPreprocessor(tokenPreprocessor: TokenPreprocessor) {
        tokenPreprocess = tokenPreprocessor
    }

    fun getTokenPreprocessor(): TokenPreprocessor? {
        return tokenPreprocess
    }
}