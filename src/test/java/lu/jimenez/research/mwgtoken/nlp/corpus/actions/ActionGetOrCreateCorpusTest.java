/**
 * Copyright 2017 Matthieu Jimenez.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lu.jimenez.research.mwgtoken.nlp.corpus.actions;

import lu.jimenez.research.mwgtoken.nlp.ActionTest;
import org.junit.jupiter.api.Test;
import org.mwg.Node;
import org.mwg.task.ActionFunction;
import org.mwg.task.TaskContext;

import static lu.jimenez.research.mwgtoken.nlp.corpus.CorpusConstants.CORPUS_NAME;
import static lu.jimenez.research.mwgtoken.nlp.corpus.actions.MwdbCorpusActions.getOrCreateCorpus;
import static lu.jimenez.research.mwgtoken.nlp.corpus.actions.MwdbCorpusActions.initializeCorpus;
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