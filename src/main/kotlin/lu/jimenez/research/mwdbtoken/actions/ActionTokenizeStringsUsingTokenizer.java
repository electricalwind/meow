package lu.jimenez.research.mwdbtoken.actions;

import lu.jimenez.research.mwdbtoken.task.TokenizationTask;
import org.mwg.Callback;
import org.mwg.Constants;
import org.mwg.core.task.TaskHelper;
import org.mwg.plugin.SchedulerAffinity;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

public class ActionTokenizeStringsUsingTokenizer implements Action {

    private final String _tokenizer;
    private final String _preprocessor;
    private final String[] _toTokenize;

    public ActionTokenizeStringsUsingTokenizer(String p_tokenizer, String p_preprocessor, String... p_toTokenize) {
        this._tokenizer = p_tokenizer;
        this._preprocessor = p_preprocessor;
        this._toTokenize = p_toTokenize;
    }

    public void eval(final TaskContext ctx) {
        TokenizationTask.tokenizeStringsUsingTokenizer(_tokenizer, _preprocessor, _toTokenize)
                .executeFrom(ctx, ctx.result(), SchedulerAffinity.SAME_THREAD,
                        new Callback<TaskResult>() {
                            public void on(TaskResult res) {
                                ctx.continueWith(res);
                            }
                        });
    }

    public void serialize(StringBuilder builder) {
        builder.append(MwdbTokenActionNames.TOKENIZE_STRINGS_USING_TOKENIZER);
        builder.append(Constants.TASK_PARAM_OPEN);
        builder.append(_tokenizer);
        builder.append(Constants.TASK_PARAM_SEP);
        builder.append(_preprocessor);
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