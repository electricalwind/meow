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
package miaou.tokens.actions;

import greycat.ActionFunction;
import greycat.Node;
import greycat.TaskContext;
import greycat.TaskResult;
import org.junit.jupiter.api.Test;

import static greycat.Tasks.newTask;
import static miaou.tokens.TokensConstants.*;
import static miaou.tokens.actions.MwdbTokenActions.getOrCreateTokensFromString;
import static miaou.tokens.actions.MwdbTokenActions.initializeVocabulary;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


public class ActionGetOrCreateTokensFromStringTest extends ActionTest {

    @Test
    public void createOneTokennoInit() {
        int counter = 0;
        final int[] i = {0};
        initGraph();
        newTask()
                .then(getOrCreateTokensFromString("Token"))
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        i[0]++;
                        ctx.continueTask();
                    }
                })
                .execute(graph, null);

        assertEquals(counter,i[0]);
        removeGraph();
    }



    @Test
    public void createOneToken() {
        int counter = 1;
        final int[] i = {0};
        initGraph();
        newTask()
                .then(initializeVocabulary())
                .then(getOrCreateTokensFromString("Token7"))
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        TaskResult<Node> tok = ctx.resultAsNodes();
                        assertEquals(1, tok.size());
                        Node n = tok.get(0);
                        assertEquals("Token7", n.get(TOKEN_NAME));
                        i[0]++;
                        ctx.continueTask();
                    }
                })
                //.addHook(new VerboseHook())
                .execute(graph, null);
        assertEquals(counter,i[0]);
        removeGraph();
    }

    @Test
    public void createSeveralTokens() {
        int counter = 1;
        final int[] i = {0};
        initGraph();

        newTask()
                .then(initializeVocabulary())
                .then(getOrCreateTokensFromString("Token","Token2","Token3","Token4"))
                .println("{{result}}")
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        TaskResult<Node> tok = ctx.resultAsNodes();
                        assertEquals(4, tok.size());
                        Node n = tok.get(0);
                        assertEquals("Token", n.get(TOKEN_NAME));
                        Node n1 = tok.get(1);
                        assertEquals("Token2", n1.get(TOKEN_NAME));
                        Node n2 = tok.get(2);
                        assertEquals("Token3", n2.get(TOKEN_NAME));
                        Node n3 = tok.get(3);
                        assertEquals("Token4", n3.get(TOKEN_NAME));
                        i[0]++;
                        ctx.continueTask();
                    }
                })
                //.addHook(new VerboseHook())
                .execute(graph, null);
        assertEquals(counter,i[0]);
        removeGraph();
    }

    @Test
    public void retrieveOneAlreadyExistingToken() {
        int counter = 2;
        final int[] i = {0};
        initGraph();
        newTask()
                .then(initializeVocabulary())
                .then(getOrCreateTokensFromString("Token4"))
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        i[0]++;
                        ctx.continueWith(ctx.wrap(ctx.resultAsNodes().get(0).id()));
                    }
                })
                .defineAsVar("id")
                .then(getOrCreateTokensFromString("Token4"))
                .println("{{result}}")
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        i[0]++;
                       assertEquals(ctx.variable("id").get(0),ctx.resultAsNodes().get(0).id());
                       ctx.continueTask();
                    }
                })
                //.addHook(new VerboseHook())
                .execute(graph, null);
        assertEquals(counter,i[0]);
        removeGraph();
    }

    @Test
    public void retrieveSeveralAlreadyExistingToken() {
        int counter = 2;
        final int[] i = {0};
        initGraph();
        newTask()
                .then(initializeVocabulary())
                .then(getOrCreateTokensFromString("Token","Token2","Token3","Token4"))
                //.println("{{result}}")
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        Long[] ids = new Long[ctx.resultAsNodes().size()];
                        i[0]++;
                        TaskResult<Node> nodes= ctx.resultAsNodes();
                        int size = nodes.size();
                        for(int i =0; i<size;i++){
                            ids[i]= nodes.get(i).id();
                        }
                        ctx.continueWith(ctx.wrap(ids));
                    }
                })
                .defineAsVar("ids")
                .then(getOrCreateTokensFromString("Token","Token2","Token3","Token4"))
                //.println("{{result}}")
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        assertEquals(ctx.variable("ids").get(0),ctx.resultAsNodes().get(0).id());
                        assertEquals(ctx.variable("ids").get(1),ctx.resultAsNodes().get(1).id());
                        assertEquals(ctx.variable("ids").get(2),ctx.resultAsNodes().get(2).id());
                        assertEquals(ctx.variable("ids").get(3),ctx.resultAsNodes().get(3).id());
                        i[0]++;
                        ctx.continueTask();
                    }
                })
                //.addHook(new VerboseHook())
                .execute(graph, null);
        assertEquals(counter,i[0]);
        removeGraph();
    }

    @Test
    public void mix() {
        int counter = 2;
        final int[] i = {0};
        initGraph();
        newTask()
                .then(initializeVocabulary())
                .then(getOrCreateTokensFromString("Token","Token2","Token3","Token4"))
                //.println("{{result}}")
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        Long[] ids = new Long[ctx.resultAsNodes().size()];

                        TaskResult<Node> nodes= ctx.resultAsNodes();
                        int size = nodes.size();
                        for(int i =0; i<size;i++){
                            ids[i]= nodes.get(i).id();
                        }
                        i[0]++;
                        ctx.continueWith(ctx.wrap(ids));
                    }
                })
                .defineAsVar("ids")
                .then(getOrCreateTokensFromString("Token","Token5","Token3","Token7"))
                //.println("{{result}}")
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        i[0]++;
                        assertEquals(ctx.variable("ids").get(0),ctx.resultAsNodes().get(0).id());
                        assertNotEquals(ctx.variable("ids").get(1),ctx.resultAsNodes().get(1).id());
                        assertEquals(ctx.variable("ids").get(2),ctx.resultAsNodes().get(2).id());
                        assertNotEquals(ctx.variable("ids").get(3),ctx.resultAsNodes().get(3).id());
                        ctx.continueTask();
                    }
                })
                //.addHook(new VerboseHook())
                .execute(graph, null);
        assertEquals(counter,i[0]);
        removeGraph();
    }


    @Test
    public void createOneTime() {
        int counter = 1;
        final int[] i = {0};
        initGraph();
        newTask().travelInTime("0")
                .then(initializeVocabulary())
                .defineAsVar("voc")
                .readGlobalIndex(ENTRY_POINT_INDEX, ENTRY_POINT_NODE_NAME, VOCABULARY_NODE_NAME)
                .println("{{result}}")
                .then(getOrCreateTokensFromString("Token7"))
                .readVar("voc")
                .println("{{result}}")
                /**.thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        System.out.println(ctx.variable("voc").get(0));
                        ((Node)ctx.variable("voc").get(0)).timepoints(TokensConstants.BEGINNING_OF_TIME, TokensConstants.END_OF_TIME, new Callback<long[]>() {
                            @Override
                            public void on(long[] result) {
                                for(int i=0;i<result.length;i++){
                                    graph.lookup(0, result[i], ((Node) ctx.variable("voc").get(0)).id(), new Callback<Node>() {
                                        @Override
                                        public void on(Node result) {
                                            System.out.println(result);
                                        }
                                    });
                                }

                                System.out.println(Arrays.toString(result));
                            }
                        });
                        ctx.continueTask();
                    }
                })*/
                .travelInTime("1")


                .readGlobalIndex(ENTRY_POINT_INDEX, ENTRY_POINT_NODE_NAME, VOCABULARY_NODE_NAME)
                .println("{{result}}")
                .then(getOrCreateTokensFromString("Token8"))
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        TaskResult<Node> tok = ctx.resultAsNodes();
                        assertEquals(1, tok.size());
                        Node n = tok.get(0);
                        assertEquals("Token8", n.get(TOKEN_NAME));
                        i[0]++;
                        ctx.continueTask();
                    }
                })
                //.addHook(new VerboseHook())
                .execute(graph, null);
        //assertEquals(counter,i[0]);
        removeGraph();
    }

}