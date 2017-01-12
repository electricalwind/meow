package lu.jimenez.research.mwdbtoken.actions;

import lu.jimenez.research.mwdbtoken.exception.UnitializeVocabularyException;
import lu.jimenez.research.mwdbtoken.task.VocabularyTask;
import org.mwg.Callback;
import org.mwg.Constants;
import org.mwg.plugin.SchedulerAffinity;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

public class ActionRetrieveVocabularyNode implements Action {
    public void eval(final TaskContext ctx) {
        VocabularyTask.retrieveVocabulary()
                .executeFrom(ctx, ctx.result(), SchedulerAffinity.SAME_THREAD,
                        new Callback<TaskResult>() {
                            public void on(TaskResult res) {
                                if (res != null) {
                                    if (res.size() == 0) {
                                        ctx.endTask(res, new UnitializeVocabularyException());
                                    }
                                    if (res.output() != null) {
                                        ctx.append(res.output());
                                        ctx.continueWith(res);
                                    }
                                }else {
                                    ctx.endTask(res, new UnitializeVocabularyException());
                                }
                            }
                        });
    }

    public void serialize(StringBuilder builder) {
        builder.append(MwdbTokenActionNames.RETRIEVE_VOCABULARY_NODE);
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
