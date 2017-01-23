package lu.jimenez.research.mwdbtoken.nlp.ngram.actions;

import lu.jimenez.research.mwdbtoken.nlp.ngram.task.NgramTask;
import org.mwg.Callback;
import org.mwg.Constants;
import org.mwg.internal.task.TaskHelper;
import org.mwg.plugin.SchedulerAffinity;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

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

    public void serialize(StringBuilder builder) {
        builder.append(MwdbNgramActionNames.GET_OR_CREATE_NGRAM_FROM_STRING);
        builder.append(Constants.TASK_PARAM_OPEN);
        TaskHelper.serializeStringParams(_grams,builder);
        builder.append(Constants.TASK_PARAM_CLOSE);
    }

    @Override
    public String toString() {
        final StringBuilder res = new StringBuilder();
        serialize(res);
        return res.toString();
    }
}
