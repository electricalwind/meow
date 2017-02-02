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
import org.mwg.task.TaskResult;

import static lu.jimenez.research.mwgtoken.core.CoreConstants.ENTRY_POINT_NODE_NAME;
import static lu.jimenez.research.mwgtoken.nlp.corpus.CorpusConstants.CORPUS_MAIN_NODE;
import static lu.jimenez.research.mwgtoken.nlp.corpus.actions.MwdbCorpusActions.initializeCorpus;
import static lu.jimenez.research.mwgtoken.nlp.corpus.actions.MwdbCorpusActions.retrieveCorpusMainNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mwg.task.Tasks.newTask;

class ActionRetrieveCorpusMainNodeTest extends ActionTest {

    @Test
    public void testinit() {
        int counter = 1;
        final int[] i = {0};
        initGraph();
        newTask()
                .then(initializeCorpus())
                .inject("3")
                .then(retrieveCorpusMainNode())
                .thenDo(new ActionFunction() {
                            public void eval(TaskContext ctx) {
                                assertEquals(ctx.resultAsNodes().size(), 1);
                                TaskResult tr = ctx.result();
                                Node node = (Node) tr.get(0);
                                String name = (String) node.get(ENTRY_POINT_NODE_NAME);
                                assertEquals(name, CORPUS_MAIN_NODE);
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
                .then(retrieveCorpusMainNode())
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