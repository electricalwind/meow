package lu.jimenez.research.mwdbtoken.actions;

import org.mwg.base.BasePlugin;
import org.mwg.task.Action;
import org.mwg.task.Task;
import org.mwg.task.TaskActionFactory;

import java.util.Map;

import static lu.jimenez.research.mwdbtoken.actions.MwdbTokenActions.*;

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

        declareTaskAction(MwdbTokenActionNames.TOKENIZE_STRINGS_USING_TOKENIZER, new TaskActionFactory() {
            public Action create(String[] params, Map<Integer, Task> contextTasks) {
                if (params.length < 3) {
                    throw new RuntimeException(MwdbTokenActionNames.TOKENIZE_STRINGS_USING_TOKENIZER + " action needs at least 3 parameters. Received:" + params.length);
                }
                final String[] getParams = new String[params.length - 2];
                if (params.length > 2) {
                    System.arraycopy(params, 2, getParams, 0, params.length - 2);
                }
                return tokenizeStringsUsingTokenizer(params[0], params[1], getParams);
            }
        });
    }
}
