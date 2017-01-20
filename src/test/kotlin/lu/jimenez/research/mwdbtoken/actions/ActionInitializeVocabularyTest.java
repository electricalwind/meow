package lu.jimenez.research.mwdbtoken.actions;

import org.junit.jupiter.api.Test;
import org.mwg.Callback;
import org.mwg.task.ActionFunction;
import org.mwg.task.TaskContext;

import static lu.jimenez.research.mwdbtoken.Constants.*;
import static lu.jimenez.research.mwdbtoken.actions.MwdbTokenActions.initializeVocabulary;
import static lu.jimenez.research.mwdbtoken.actions.MwdbTokenActions.retrieveVocabularyNode;
import static org.junit.Assert.assertEquals;
import static org.mwg.task.Tasks.newTask;

public class ActionInitializeVocabularyTest extends ActionTest {

    @Test
    public void test() {
        int counter = 1;
        final int[] i = {0};
        initGraph();
        newTask()
                .travelInTime("0")
                .then(initializeVocabulary())
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        ctx.graph().indexNames(0L, 0L, new Callback<String[]>() {
                            public void on(String[] result) {
                                System.out.println(result.length);
                            }
                        });
                        ctx.continueTask();

                    }
                })
                .println("{{result}}")
                .readGlobalIndex(ENTRY_POINT_INDEX, ENTRY_POINT_NODE_NAME, VOCABULARY_NODE_NAME)
                .thenDo(new ActionFunction() {
                            public void eval(TaskContext context) {
                                assertEquals(context.resultAsNodes().size(), 1);
                                i[0]++;
                                context.continueTask();
                            }
                        }
                ).println("{{result}}")
                .execute(graph,null);
        assertEquals(counter,i[0]);
        removeGraph();
    }

    @Test
    public void test2() {
        int counter = 1;
        final int[] i = {0};
        initGraph();
        newTask()
                .travelInTime("0")
                .then(initializeVocabulary())
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        ctx.graph().indexNames(0L, 0L, new Callback<String[]>() {
                            public void on(String[] result) {
                                System.out.println(result.length);
                            }
                        });
                        ctx.continueTask();

                    }
                }).println("{{result}}")
                .travelInTime("1")
                .then(retrieveVocabularyNode())
                .readGlobalIndex(ENTRY_POINT_INDEX, ENTRY_POINT_NODE_NAME, VOCABULARY_NODE_NAME)
                .thenDo(new ActionFunction() {
                            public void eval(TaskContext context) {
                                assertEquals(context.resultAsNodes().size(), 1);
                                i[0]++;
                                context.continueTask();
                            }
                        }
                ).println("{{result}}")
                .execute(graph,null);
        assertEquals(counter,i[0]);
        removeGraph();
    }

}