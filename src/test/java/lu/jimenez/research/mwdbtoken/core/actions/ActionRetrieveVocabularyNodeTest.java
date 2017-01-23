package lu.jimenez.research.mwdbtoken.core.actions;

import org.junit.jupiter.api.Test;
import org.mwg.Node;
import org.mwg.task.ActionFunction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

import static lu.jimenez.research.mwdbtoken.core.CoreConstants.ENTRY_POINT_NODE_NAME;
import static lu.jimenez.research.mwdbtoken.core.CoreConstants.VOCABULARY_NODE_NAME;
import static lu.jimenez.research.mwdbtoken.core.actions.MwdbTokenActions.initializeVocabulary;
import static lu.jimenez.research.mwdbtoken.core.actions.MwdbTokenActions.retrieveVocabularyNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mwg.task.Tasks.newTask;

public class ActionRetrieveVocabularyNodeTest extends ActionTest {

    @Test
    public void testinit() {
        int counter = 1;
        final int[] i = {0};
        initGraph();
        newTask()
                .then(initializeVocabulary())
                .inject("3")
                .then(retrieveVocabularyNode())
                .thenDo(new ActionFunction() {
                            public void eval(TaskContext ctx) {
                                assertEquals(ctx.resultAsNodes().size(), 1);
                                TaskResult tr = ctx.result();
                                Node node = (Node) tr.get(0);
                                String name = (String) node.get(ENTRY_POINT_NODE_NAME);
                                assertEquals(name, VOCABULARY_NODE_NAME);
                                i[0]++;
                                ctx.continueTask();
                            }
                        }
                )
                .execute(graph, null);
        assertEquals(counter, i[0]);
        removeGraph();
    }

    @Test
    public void testnoinit() {
        initGraph();
        final int[] i = {0};
        newTask()
                .then(retrieveVocabularyNode())
                .thenDo(new ActionFunction() {
                            public void eval(TaskContext ctx) {
                                assert (false);
                                i[0]++;
                                ctx.continueTask();
                            }
                        }
                )
                //.addHook(new VerboseHook())
                .execute(graph, null);
        assertEquals(0, i[0]);
        removeGraph();
    }
}