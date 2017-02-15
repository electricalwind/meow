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
import greycat.struct.Buffer;
import meow.tokens.task.RelationTask;

public class ActionCreateOrUpdateTokenizeRelationsToNodes implements Action {

    private final String _tokenizersVar;
    private final String _nodesVar;
    private final String[] _relationList;

    public ActionCreateOrUpdateTokenizeRelationsToNodes(String p_tokenizersVar, String p_nodesVar, String... p_relationList) {
        this._tokenizersVar = p_tokenizersVar;
        this._nodesVar = p_nodesVar;
        this._relationList = p_relationList;
    }

    @Override
    public void eval(TaskContext ctx) {
        RelationTask.updateOrCreateTokenizeRelationsToNodes(_tokenizersVar, _nodesVar, _relationList)
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

    @Override
    public void serialize(Buffer builder) {
        builder.writeString(TokenActionNames.CREATE_OR_UPDATE_TOKENIZE_RELATIONS_TO_NODES);
        builder.writeChar(Constants.TASK_PARAM_OPEN);
        TaskHelper.serializeString(_tokenizersVar, builder, true);
        builder.writeChar(Constants.TASK_PARAM_SEP);
        TaskHelper.serializeString(_nodesVar, builder, true);
        if (_relationList != null && _relationList.length > 0) {
            builder.writeChar(Constants.TASK_PARAM_SEP);
            TaskHelper.serializeStringParams(_relationList, builder);
        }
        builder.writeChar(Constants.TASK_PARAM_CLOSE);

    }

}
