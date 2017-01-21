package lu.jimenez.research.mwdbtoken.actions;

import lu.jimenez.research.mwdbtoken.task.VocabularyTask;
import org.mwg.Callback;
import org.mwg.Constants;
import org.mwg.internal.task.TaskHelper;
import org.mwg.plugin.SchedulerAffinity;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

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
        builder.append(MwdbTokenActionNames.GET_OR_CREATE_TOKENS_FROM_STRING);
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
