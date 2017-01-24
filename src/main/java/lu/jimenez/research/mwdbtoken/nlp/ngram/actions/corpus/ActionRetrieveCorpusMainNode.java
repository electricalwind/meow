package lu.jimenez.research.mwdbtoken.nlp.ngram.actions.corpus;

import lu.jimenez.research.mwdbtoken.nlp.ngram.actions.MwdbNgramActionNames;
import lu.jimenez.research.mwdbtoken.nlp.ngram.exception.UninitializeCorpusMainNodeException;
import lu.jimenez.research.mwdbtoken.nlp.ngram.task.CorpusTask;
import org.mwg.Callback;
import org.mwg.Constants;
import org.mwg.plugin.SchedulerAffinity;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

public class ActionRetrieveCorpusMainNode implements Action {
    public void eval(final TaskContext ctx) {
        CorpusTask.retrieveCorpusMainNode()
                .executeFrom(ctx, ctx.result(), SchedulerAffinity.SAME_THREAD,
                        new Callback<TaskResult>() {
                            public void on(TaskResult res) {
                                if (res != null) {
                                    if (res.size() == 0) {
                                        ctx.endTask(res, new UninitializeCorpusMainNodeException());
                                    } else {
                                        if (res.output() != null) {
                                            ctx.append(res.output());
                                        }
                                        ctx.continueWith(res);
                                    }
                                } else {
                                    ctx.endTask(res, new UninitializeCorpusMainNodeException());
                                }
                            }
                        });
    }

    public void serialize(StringBuilder builder) {
        builder.append(MwdbNgramActionNames.RETRIEVE_CORPUS_MAIN_NODE);
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
