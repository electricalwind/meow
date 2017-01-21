package lu.jimenez.research.mwdbtoken.actions;

import lu.jimenez.research.mwdbtoken.task.RelationTask;
import org.mwg.Callback;
import org.mwg.Constants;
import org.mwg.internal.task.TaskHelper;
import org.mwg.plugin.SchedulerAffinity;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

public class ActionCreateOrUpdateTokenizeRelationsToNodes implements Action {

    private final String _tokenizersVar;
    private final String _nodesVar;
    private final String[] _relationList;

    public ActionCreateOrUpdateTokenizeRelationsToNodes(String p_tokenizersVar, String p_nodesVar, String... p_relationList) {
        this._tokenizersVar = p_tokenizersVar;
        this._nodesVar = p_nodesVar;
        this._relationList = p_relationList;
    }

    @Override
    public void eval(TaskContext ctx) {
        RelationTask.updateOrCreateTokenizeRelationsToNodes(_tokenizersVar, _nodesVar, _relationList)
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
        builder.append(MwdbTokenActionNames.CREATE_OR_UPDATE_TOKENIZE_RELATIONS_TO_NODES);
        builder.append(Constants.TASK_PARAM_OPEN);
        TaskHelper.serializeString(_tokenizersVar, builder, true);
        builder.append(Constants.TASK_PARAM_SEP);
        TaskHelper.serializeString(_nodesVar, builder, true);
        if (_relationList != null && _relationList.length > 0) {
            builder.append(Constants.TASK_PARAM_SEP);
            TaskHelper.serializeStringParams(_relationList, builder);
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
