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
package meow.tokens.tokenization.tokenizer

import meow.utils.SplitWithDelimiters


class SimpleTokenizer(tokens: String) : AbstractTokenizer() {

    val tokenizer = SplitWithDelimiters.split(tokens, "[^A-Za-z0-9]")
    var i = 0

    //val tokenizer: StringTokenizer = StringTokenizer(tokens, "[^A-Za-z0-9]", true)
    val listOfTokens: MutableList<String> = mutableListOf()


    override fun countTokens(): Int {
        return getTokens().size
    }

    override fun getTokens(): List<String> {

        while (hasMoreTokens()) {
            nextToken()
        }
        return listOfTokens
    }

    override fun hasMoreTokens(): Boolean {
        return tokenizer.size>i
    }


    override fun nextToken(): String? {
        if(hasMoreTokens()) {
            var base = tokenizer[i]
            i++
            while (base.isBlank() && hasMoreTokens()) {
                if(base == "\n") break
                base = tokenizer[i]
                i++
            }
            if(base.isBlank() && base != "\n"){
                return null
            }
            if (tokenPreprocess != null) {
                val tok = tokenPreprocess?.preProcess(base) ?: throw RuntimeException("error while preprocessing")
                listOfTokens.add(tok)
                return tok
            }
            listOfTokens.add(base)
            return base
        }
        else{
            return null
        }
    }


}