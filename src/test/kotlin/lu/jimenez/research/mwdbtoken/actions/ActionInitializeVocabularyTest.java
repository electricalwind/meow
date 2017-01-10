package lu.jimenez.research.mwdbtoken.actions;

import org.junit.jupiter.api.Test;
import org.mwg.task.ActionFunction;
import org.mwg.task.TaskContext;

import static lu.jimenez.research.mwdbtoken.Constants.*;
import static lu.jimenez.research.mwdbtoken.actions.MwdbTokenActions.initializeVocabulary;
import static org.junit.Assert.assertEquals;
import static org.mwg.core.task.Actions.newTask;

public class ActionInitializeVocabularyTest extends ActionTest {

    @Test
    public void test() {
        initGraph();
        newTask()
                .then(initializeVocabulary())
                .readGlobalIndex(ENTRY_POINT_INDEX, ENTRY_POINT_NODE_NAME, VOCABULARY_NODE_NAME)
                .thenDo(new ActionFunction() {
                            public void eval(TaskContext context) {
                                assertEquals(context.resultAsNodes().size(), 1);
                            }
                        }
                )
                .execute(graph,null);
    }

}