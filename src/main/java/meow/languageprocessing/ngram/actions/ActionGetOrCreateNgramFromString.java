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
package meow.languageprocessing.ngram.actions;

import greycat.*;
import greycat.internal.task.TaskHelper;
import greycat.plugin.SchedulerAffinity;
import greycat.struct.Buffer;
import meow.languageprocessing.ngram.task.NgramTask;

public class ActionGetOrCreateNgramFromString implements Action {

    private final String[] _grams;

    public ActionGetOrCreateNgramFromString(String... p_grams){
        this._grams = p_grams;
    }


    public void eval(final TaskContext ctx) {
        NgramTask.getOrCreateNgramFromString(_grams)
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

    public void serialize(Buffer builder) {
        builder.writeString(NgramActionNames.GET_OR_CREATE_NGRAM_FROM_STRING);
        builder.writeChar(Constants.TASK_PARAM_OPEN);
        TaskHelper.serializeStringParams(_grams,builder);
        builder.writeChar(Constants.TASK_PARAM_CLOSE);
    }

}
