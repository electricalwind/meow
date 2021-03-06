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
import greycat.plugin.SchedulerAffinity;
import greycat.struct.Buffer;
import meow.languageprocessing.ngram.exception.UninitializeNgramMainNodeException;
import meow.languageprocessing.ngram.task.NgramTask;

public class ActionRetrieveNgramMainNode implements Action {


    public void eval(final TaskContext ctx) {
        NgramTask.retrieveNgramMainNode()
                .executeFrom(ctx, ctx.result(), SchedulerAffinity.SAME_THREAD,
                        new Callback<TaskResult>() {
                            public void on(TaskResult res) {
                                if (res != null) {
                                    if (res.size() == 0) {
                                        ctx.endTask(res, new UninitializeNgramMainNodeException());
                                    } else {
                                        if (res.output() != null) {
                                            ctx.append(res.output());
                                        }
                                        ctx.continueWith(res);
                                    }
                                } else {
                                    ctx.endTask(res, new UninitializeNgramMainNodeException());
                                }
                            }
                        });
    }

    public void serialize(Buffer builder) {
        builder.writeString(NgramActionNames.RETRIEVE_NGRAM_MAIN_NODE);
        builder.writeChar(Constants.TASK_PARAM_OPEN);
        builder.writeChar(Constants.TASK_PARAM_CLOSE);

    }

}
