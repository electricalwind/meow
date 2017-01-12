package lu.jimenez.research.mwdbtoken.actions;

import lu.jimenez.research.mwdbtoken.task.VocabularyTask;
import org.mwg.Callback;
import org.mwg.Constants;
import org.mwg.plugin.SchedulerAffinity;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

public class ActionInitializeVocabulary implements Action {
    public void eval(final TaskContext ctx) {
        VocabularyTask.initializeVocabulary()
                .executeFrom(ctx, ctx.result(), SchedulerAffinity.SAME_THREAD,
                        new Callback<TaskResult>() {
                            public void on(TaskResult res) {
                                ctx.continueWith(res);
                            }
                        });
    }

    public void serialize(StringBuilder builder) {
        builder.append(MwdbTokenActionNames.INITIALIZE_VOCABULARY);
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
