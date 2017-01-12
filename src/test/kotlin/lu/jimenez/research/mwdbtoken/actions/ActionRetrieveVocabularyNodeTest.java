package lu.jimenez.research.mwdbtoken.actions;

import org.junit.jupiter.api.Test;
import org.mwg.Node;
import org.mwg.task.ActionFunction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;
import org.mwg.utility.VerboseHook;

import static lu.jimenez.research.mwdbtoken.Constants.ENTRY_POINT_NODE_NAME;
import static lu.jimenez.research.mwdbtoken.Constants.VOCABULARY_NODE_NAME;
import static lu.jimenez.research.mwdbtoken.actions.MwdbTokenActions.initializeVocabulary;
import static lu.jimenez.research.mwdbtoken.actions.MwdbTokenActions.retrieveVocabularyNode;
import static org.junit.Assert.assertEquals;
import static org.mwg.core.task.Actions.newTask;

public class ActionRetrieveVocabularyNodeTest extends ActionTest {

    @Test
    public void testinit() {
        initGraph();
        newTask()
                .then(initializeVocabulary())
                .inject("3")
                .then(retrieveVocabularyNode())
                .println("{{result}}")
                .thenDo(new ActionFunction() {
                            public void eval(TaskContext ctx) {
                                assertEquals(ctx.resultAsNodes().size(), 1);
                                TaskResult tr= ctx.result();
                                Node node = (Node)tr.get(0);
                                String name  = (String) node.get(ENTRY_POINT_NODE_NAME);
                                assertEquals(name, VOCABULARY_NODE_NAME);
                                ctx.continueTask();
                            }
                        }
                ).execute(graph, null);
        removeGraph();
    }

    @Test
    public void testnoinit() {
        initGraph();
        newTask()
                .then(retrieveVocabularyNode())
                .thenDo(new ActionFunction() {
                            public void eval(TaskContext ctx) {
                                assert (false);
                            }
                        }
                )
                .addHook(new VerboseHook())
                .execute(graph, null);
        removeGraph();
    }
}