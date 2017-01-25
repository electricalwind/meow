package lu.jimenez.research.mwdbtoken.nlp.corpus.actions;

import lu.jimenez.research.mwdbtoken.nlp.ngram.actions.ActionTest;
import org.junit.jupiter.api.Test;
import org.mwg.task.ActionFunction;
import org.mwg.task.TaskContext;

import static lu.jimenez.research.mwdbtoken.core.CoreConstants.ENTRY_POINT_INDEX;
import static lu.jimenez.research.mwdbtoken.core.CoreConstants.ENTRY_POINT_NODE_NAME;
import static lu.jimenez.research.mwdbtoken.nlp.corpus.CorpusConstants.CORPUS_MAIN_NODE;
import static lu.jimenez.research.mwdbtoken.nlp.corpus.actions.MwdbCorpusActions.initializeCorpus;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mwg.task.Tasks.newTask;

class ActionInitializeCorpusTest extends ActionTest{

    @Test
    public void test() {
        int counter = 1;
        final int[] i = {0};
        initGraph();
        newTask()
                .travelInTime("0")
                .then(initializeCorpus())
                .readGlobalIndex(ENTRY_POINT_INDEX, ENTRY_POINT_NODE_NAME, CORPUS_MAIN_NODE)
                .thenDo(new ActionFunction() {
                            public void eval(TaskContext context) {
                                assertEquals(context.resultAsNodes().size(), 1);
                                i[0]++;
                                context.continueTask();
                            }
                        }
                )
                .execute(graph, null);
        assertEquals(counter, i[0]);
        removeGraph();
    }

    @Test
    public void test2() {
        int counter = 1;
        final int[] i = {0};
        initGraph();
        newTask()
                .travelInTime("0")
                .then(initializeCorpus())
                .travelInTime("1")
                .readGlobalIndex(ENTRY_POINT_INDEX, ENTRY_POINT_NODE_NAME, CORPUS_MAIN_NODE)
                .thenDo(new ActionFunction() {
                            public void eval(TaskContext context) {
                                assertEquals(context.resultAsNodes().size(), 1);
                                i[0]++;
                                context.continueTask();
                            }
                        }
                )
                .execute(graph, null);
        assertEquals(counter, i[0]);
        removeGraph();
    }
}