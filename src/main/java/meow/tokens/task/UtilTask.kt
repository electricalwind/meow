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

import greycat.Task
import greycat.Tasks.newTask
import meow.tokens.TokensConstants.NODE_TYPE


object UtilTask {

    fun checkNodesType(varTorCheck: String, expectedType: String): Task {
        return newTask()
                .readVar(varTorCheck)
                .forEach(
                        newTask()
                                .thenDo { ctx ->
                                    val nodeType = ctx.resultAsNodes()[0].get(NODE_TYPE)
                                    if (nodeType != expectedType) {
                                        ctx.endTask(ctx.result(), RuntimeException("Not a $expectedType"))
                                    } else {
                                        ctx.continueTask()
                                    }
                                }
                )
    }



}