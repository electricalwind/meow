package lu.jimenez.research.mwdbtoken.core.actions;

import lu.jimenez.research.mwdbtoken.core.task.VocabularyTask;
import org.mwg.Callback;
import org.mwg.Constants;
import org.mwg.internal.task.TaskHelper;
import org.mwg.plugin.SchedulerAffinity;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

public class ActionRebuildingTokenizeContent implements Action {
    private final String _tokenizedContentVar;

    public ActionRebuildingTokenizeContent(String p_tokenizedContentsVar){
        this._tokenizedContentVar = p_tokenizedContentsVar;
    }

    @Override
    public void eval(TaskContext ctx) {
        VocabularyTask.rebuildingTokenizeContent(_tokenizedContentVar)
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
    public void serialize(StringBuilder builder) {
        builder.append(MwdbTokenActionNames.REBUILDING_TOKENIZE_CONTENTS);
        builder.append(Constants.TASK_PARAM_OPEN);
        TaskHelper.serializeString(_tokenizedContentVar,builder,true);
        builder.append(Constants.TASK_PARAM_CLOSE);
    }

    @Override
    public String toString() {
        final StringBuilder res = new StringBuilder();
        serialize(res);
        return res.toString();
    }
}
