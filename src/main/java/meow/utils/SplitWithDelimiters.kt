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
package meow.utils

import java.util.regex.Pattern

/**
 * Original Code can be found here:
 * http://stackoverflow.com/a/279549
 */
object SplitWithDelimiters {
    fun split(s: String?, pattern: String?): List<String> {
        assert(s != null)
        assert(pattern != null)
        return split(s, Pattern.compile(pattern))
    }

    fun split(s: String?, pattern: Pattern?): List<String> {
        assert(s != null)
        assert(pattern != null)
        val m = pattern!!.matcher(s)
        val ret = mutableListOf<String>()
        var start = 0
        while (m.find()) {
            ret.add(s!!.substring(start, m.start()))
            ret.add(m.group())
            start = m.end()
        }
        ret.add(if (start >= s!!.length) "" else s.substring(start))
        return ret
    }
}