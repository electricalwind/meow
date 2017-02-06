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
import meow.tokens.task.TokenizationTask;

public class ActionTokenizeStringsUsingTokenizer implements Action {

    private final String _tokenizer;
    private final String _preprocessor;
    private final String[] _toTokenize;
    private final String _type;

    public ActionTokenizeStringsUsingTokenizer(String p_tokenizer, String p_preprocessor, String p_type, String... p_toTokenize) {
        this._tokenizer = p_tokenizer;
        if (p_preprocessor == null)
            this._preprocessor = "";
        else
            this._preprocessor = p_preprocessor;
        this._type = p_type;
        this._toTokenize = p_toTokenize;
    }

    public void eval(final TaskContext ctx) {
        TokenizationTask.tokenizeStringsUsingTokenizer(_tokenizer, _preprocessor, _type, _toTokenize)
                .executeFrom(ctx, ctx.result(), SchedulerAffinity.SAME_THREAD,
                        new Callback<TaskResult>() {
                            public void on(TaskResult res) {
                                ctx.continueWith(res);
                            }
                        });
    }

    public void serialize(StringBuilder builder) {
        builder.append(TokenActionNames.TOKENIZE_STRINGS_USING_TOKENIZER);
        builder.append(Constants.TASK_PARAM_OPEN);
        TaskHelper.serializeString(_tokenizer, builder, true);
        builder.append(Constants.TASK_PARAM_SEP);
        TaskHelper.serializeString(_preprocessor, builder, true);
        builder.append(Constants.TASK_PARAM_SEP);
        builder.append(_type);
        if (_toTokenize != null && _toTokenize.length > 0) {
            builder.append(Constants.TASK_PARAM_SEP);
            TaskHelper.serializeStringParams(_toTokenize, builder);
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
