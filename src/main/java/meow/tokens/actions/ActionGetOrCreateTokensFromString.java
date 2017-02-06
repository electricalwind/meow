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
package meow.tokens.actions;

import greycat.*;
import greycat.internal.task.TaskHelper;
import greycat.plugin.SchedulerAffinity;
import meow.tokens.task.VocabularyTask;

public class ActionGetOrCreateTokensFromString implements Action {

    private final String[] _tokenString;

    public ActionGetOrCreateTokensFromString(final String... p_tokenString) {
        this._tokenString = p_tokenString;
    }

    public void eval(final TaskContext ctx) {
        VocabularyTask.getOrCreateTokensFromString(_tokenString)
                .executeFrom(ctx, ctx.result(), SchedulerAffinity.SAME_THREAD,
                        new Callback<TaskResult>() {
                            public void on(TaskResult res) {
                                Exception exceptionDuringTask = null;
                                if (res != null) {
                                    if (res.output() != null) {
                                        ctx.append(res.output());
                                    }
                                    if (res.exception() != null) {
                                        exceptionDuringTask = res.exception();
                                    }
                                }
                                if (exceptionDuringTask != null) {
                                    ctx.endTask(res, exceptionDuringTask);
                                } else {
                                    ctx.continueWith(res);
                                }
                            }
                        });
    }

    public void serialize(StringBuilder builder) {
        builder.append(TokenActionNames.GET_OR_CREATE_TOKENS_FROM_STRING);
        builder.append(Constants.TASK_PARAM_OPEN);
        if (_tokenString != null && _tokenString.length > 0) {
            TaskHelper.serializeStringParams(_tokenString, builder);
        }
        builder.append(Constants.TASK_PARAM_CLOSE);
    }

    @Override
    public String toString() {
        final StringBuilder res = new StringBuilder();
        serialize(res);
        return res.toString();
    }

}
