package lu.jimenez.research.mwdbtoken.actions;

import org.mwg.base.BasePlugin;
import org.mwg.task.Action;
import org.mwg.task.Task;
import org.mwg.task.TaskActionFactory;

import java.util.Map;

import static lu.jimenez.research.mwdbtoken.actions.MwdbTokenActions.getOrCreateTokensFromString;
import static lu.jimenez.research.mwdbtoken.actions.MwdbTokenActions.retrieveVocabularyNode;

public class MwdbTokenActionPlugin extends BasePlugin {

    public MwdbTokenActionPlugin() {

        declareTaskAction(MwdbTokenActionNames.RETRIEVE_VOCABULARY_NODE, new TaskActionFactory() {
            public Action create(String[] params, Map<Integer, Task> contextTasks) {
                return retrieveVocabularyNode();
            }
        });

        declareTaskAction(MwdbTokenActionNames.GET_OR_CREATE_TOKENS_FROM_STRING, new TaskActionFactory() {
            public Action create(String[] params, Map<Integer, Task> contextTasks) {
                return getOrCreateTokensFromString(params);
            }
        });
    }
}
