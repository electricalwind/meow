package lu.jimenez.research.mwdbtoken.actions;

import org.junit.jupiter.api.Test;
import org.mwg.task.ActionFunction;
import org.mwg.task.TaskContext;

import static lu.jimenez.research.mwdbtoken.Constants.ENTRY_POINT_NODE_NAME;
import static lu.jimenez.research.mwdbtoken.Constants.VOCABULARY_NODE_NAME;
import static lu.jimenez.research.mwdbtoken.actions.MwdbTokenActions.initializeVocabulary;
import static lu.jimenez.research.mwdbtoken.actions.MwdbTokenActions.retrieveVocabularyNode;
import static org.junit.Assert.assertEquals;
import static org.mwg.core.task.Actions.newTask;

public class ActionRetrieveVocabularyNodeTest extends ActionTest{

    @Test
    public void testinit(){
        initGraph();
        newTask()
                .then(initializeVocabulary())
                .then(retrieveVocabularyNode())
                .thenDo(new ActionFunction() {
                            public void eval(TaskContext ctx) {
                                assertEquals(ctx.resultAsNodes().size(),1);
                                assertEquals(ctx.resultAsNodes().get(0).get(ENTRY_POINT_NODE_NAME),VOCABULARY_NODE_NAME);
                            }
                        }
                ).execute(graph,null);
        removeGraph();
    }

    @Test
    public void testnoinit(){
        initGraph();
        newTask()
                .then(retrieveVocabularyNode())
                .thenDo(new ActionFunction() {
                            public void eval(TaskContext ctx) {
                                assertEquals(ctx.resultAsNodes().size(),0);
                            }
                        }
                ).execute(graph,null);
        removeGraph();
    }
}