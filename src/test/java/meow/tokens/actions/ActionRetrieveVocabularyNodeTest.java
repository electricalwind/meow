/**
 * Copyright 2017 Matthieu Jimenez.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package meow.tokens.actions;

import greycat.ActionFunction;
import greycat.Node;
import greycat.TaskContext;
import greycat.TaskResult;
import org.junit.jupiter.api.Test;

import static greycat.Tasks.newTask;
import static meow.tokens.TokensConstants.ENTRY_POINT_NODE_NAME;
import static meow.tokens.TokensConstants.VOCABULARY_NODE_NAME;
import static meow.tokens.actions.TokenActions.initializeVocabulary;
import static meow.tokens.actions.TokenActions.retrieveVocabularyNode;
import static org.junit.jupiter.api.Assertions.assertEquals;


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