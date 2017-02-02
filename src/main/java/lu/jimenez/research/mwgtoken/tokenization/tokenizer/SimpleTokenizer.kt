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
package lu.jimenez.research.mwgtoken.tokenization.tokenizer

import java.util.*


class SimpleTokenizer(tokens: String) : AbstractTokenizer() {


    val tokenizer: StringTokenizer = StringTokenizer(tokens)
    val listOfTokens: MutableList<String> = mutableListOf()


    override fun countTokens(): Int {
        return tokenizer.countTokens()
    }

    override fun getTokens(): List<String> {


        while (hasMoreTokens()) {
            nextToken()
        }
        return listOfTokens
    }

    override fun hasMoreTokens(): Boolean {
        return tokenizer.hasMoreTokens()
    }


    override fun nextToken(): String {
        val base = tokenizer.nextToken()
        if (tokenPreprocess != null) {
            val tok = tokenPreprocess?.preProcess(base) ?: throw RuntimeException("error while preprocessing")
            listOfTokens.add(tok)
            return tok
        }
        listOfTokens.add(base)
        return base

    }


}