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
package lu.jimenez.research.mwgtoken.nlp.ngram.actions;

import lu.jimenez.research.mwgtoken.nlp.ngram.exception.UninitializeNgramMainNodeException;
import lu.jimenez.research.mwgtoken.nlp.ngram.task.NgramTask;
import org.mwg.Callback;
import org.mwg.Constants;
import org.mwg.plugin.SchedulerAffinity;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

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

    public void serialize(StringBuilder builder) {
        builder.append(MwdbNgramActionNames.RETRIEVE_NGRAM_MAIN_NODE);
        builder.append(Constants.TASK_PARAM_OPEN);
        builder.append(Constants.TASK_PARAM_CLOSE);

    }

    @Override
    public String toString() {
        final StringBuilder res = new StringBuilder();
        serialize(res);
        return res.toString();
    }

}
