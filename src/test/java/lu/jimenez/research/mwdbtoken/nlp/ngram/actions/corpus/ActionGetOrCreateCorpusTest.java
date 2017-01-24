package lu.jimenez.research.mwdbtoken.nlp.ngram.actions.corpus;

import lu.jimenez.research.mwdbtoken.nlp.ngram.actions.ActionTest;
import org.junit.jupiter.api.Test;
import org.mwg.Node;
import org.mwg.task.ActionFunction;
import org.mwg.task.TaskContext;

import static lu.jimenez.research.mwdbtoken.nlp.ngram.NgramConstants.CORPUS_NAME;
import static lu.jimenez.research.mwdbtoken.nlp.ngram.actions.MwdbNgramActions.getOrCreateCorpus;
import static lu.jimenez.research.mwdbtoken.nlp.ngram.actions.MwdbNgramActions.initializeCorpus;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mwg.task.Tasks.newTask;

class ActionGetOrCreateCorpusTest extends ActionTest{

    @Test
    public void test(){
      initGraph();
      final int[] counter = {0};
      newTask()
              .then(initializeCorpus())
              .then(getOrCreateCorpus("my corpus"))
              .thenDo(new ActionFunction() {
                  @Override
                  public void eval(TaskContext ctx) {
                     assertEquals("my corpus",ctx.resultAsNodes().get(0).get(CORPUS_NAME));
                     counter[0]++;
                     ctx.continueTask();
                  }
              })
              .defineAsVar("myCorp")
              .inject("3")
              .then(getOrCreateCorpus("my corpus"))
              .thenDo(new ActionFunction() {
                  @Override
                  public void eval(TaskContext ctx) {
                      assertEquals(((Node)ctx.variable("myCorp").get(0)).id(),ctx.resultAsNodes().get(0).id());
                      counter[0]++;
                      ctx.continueTask();
                  }
              })
              .execute(graph,null);
      assertEquals(2,counter[0]);
      removeGraph();
    }

}