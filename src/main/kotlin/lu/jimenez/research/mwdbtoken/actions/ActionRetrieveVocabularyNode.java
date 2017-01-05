package lu.jimenez.research.mwdbtoken.actions;

import org.mwg.Callback;
import org.mwg.Constants;
import org.mwg.NodeIndex;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;

import lu.jimenez.research.mwdbtoken.exception.UnitializeVocabularyException;
import static lu.jimenez.research.mwdbtoken.Constants.VOCABULARY_NODE_NAME;

public class ActionRetrieveVocabularyNode implements Action {
    public void eval(final TaskContext ctx) {
        ctx.graph().indexIfExists(ctx.world(), ctx.time(), VOCABULARY_NODE_NAME, new Callback<NodeIndex>() {
            public void on(NodeIndex result) {
                if (result != null){
                    ctx.continueWith(ctx.wrap(result));
                }
                else{
                    throw new UnitializeVocabularyException();
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
