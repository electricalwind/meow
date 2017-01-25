package lu.jimenez.research.mwdbtoken.nlp.corpus.actions;

import lu.jimenez.research.mwdbtoken.nlp.corpus.task.CorpusTask;
import org.mwg.Callback;
import org.mwg.Constants;
import org.mwg.internal.task.TaskHelper;
import org.mwg.plugin.SchedulerAffinity;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

import static lu.jimenez.research.mwdbtoken.nlp.corpus.actions.MwdbCorpusActionNames.GET_OR_CREATE_CORPUS;

public class ActionGetOrCreateCorpus implements Action {

    private final String _corpusName;

    public ActionGetOrCreateCorpus(String p_corpusName) {
        this._corpusName = p_corpusName;
    }

    @Override
    public void eval(TaskContext ctx) {
        CorpusTask.getOrCreateCorpus(_corpusName)
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
        builder.append(GET_OR_CREATE_CORPUS);
        builder.append(Constants.TASK_PARAM_OPEN);
        TaskHelper.serializeString(_corpusName, builder, true);
        builder.append(Constants.TASK_PARAM_CLOSE);
    }

    @Override
    public String toString() {
        final StringBuilder res = new StringBuilder();
        serialize(res);
        return res.toString();
    }
}
