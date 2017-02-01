package lu.jimenez.research.mwdbtoken.nlp.ngram.actions;

import lu.jimenez.research.mwdbtoken.nlp.ngram.task.RelationTask;
import org.mwg.Callback;
import org.mwg.Constants;
import org.mwg.internal.task.TaskHelper;
import org.mwg.plugin.SchedulerAffinity;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

public class ActionUpdateNgramTokenizeContentFromVar implements Action {

    private final String _var;

    public ActionUpdateNgramTokenizeContentFromVar(final String p_var){
        this._var =p_var;
    }

    @Override
    public void eval(TaskContext ctx) {
        RelationTask.updateNgramTokenizeContentVar(_var)
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
        builder.append(MwdbNgramActionNames.UPDATE_NGRAM_TC_FROM_VAR);
        builder.append(Constants.TASK_PARAM_OPEN);
        TaskHelper.serializeString(_var,builder,true);
        builder.append(Constants.TASK_PARAM_CLOSE);
    }
    @Override
    public String toString() {
        final StringBuilder res = new StringBuilder();
        serialize(res);
        return res.toString();
    }
}
