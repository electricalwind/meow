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
package miaou.languageprocessing.ngram.actions;

import greycat.ActionFunction;
import greycat.Node;
import greycat.TaskContext;
import miaou.languageprocessing.ActionTest;
import org.junit.jupiter.api.Test;

import static greycat.Tasks.newTask;
import static miaou.tokens.TokensConstants.TOKEN_NAME;
import static miaou.tokens.actions.MwdbTokenActions.initializeVocabulary;
import static miaou.languageprocessing.ngram.actions.MwdbNgramActions.getOrCreateNgramFromString;
import static miaou.languageprocessing.ngram.actions.MwdbNgramActions.initializeNgram;
import static org.junit.jupiter.api.Assertions.assertEquals;


class ActionGetOrCreateNgramFromStringTest extends ActionTest {


    @Test
    public void test() {

        initGraph();
        final int[] counter = {0};
        newTask().then(initializeVocabulary())
                .then(initializeNgram())
                .then(getOrCreateNgramFromString("This"))
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        assertEquals(1, ctx.resultAsNodes().get(0).get("order"));
                        counter[0]++;
                        ctx.continueTask();
                    }
                })
                .defineAsVar("myNgram")
                .traverse("gram")
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        assertEquals("This", ctx.resultAsNodes().get(0).get(TOKEN_NAME));
                        counter[0]++;
                        ctx.continueTask();
                    }
                })
                .then(getOrCreateNgramFromString("This"))
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        assertEquals(((Node) ctx.variable("myNgram").get(0)).id(), ctx.resultAsNodes().get(0).id());
                        counter[0]++;
                        ctx.continueTask();
                    }
                })
                .execute(graph, null);
        assertEquals(3, counter[0]);
        removeGraph();
    }


    @Test
    public void test2() {

        initGraph();
        final int[] counter = {0};
        newTask().then(initializeVocabulary())
                .then(initializeNgram())
                .then(getOrCreateNgramFromString("This", "is", "me"))
                .defineAsVar("myNgram")
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        assertEquals(3, ctx.resultAsNodes().get(0).get("order"));
                        counter[0]++;
                        ctx.continueTask();
                    }
                })
                .traverse("gram")
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        assertEquals("This", ctx.resultAsNodes().get(0).get(TOKEN_NAME));
                        assertEquals("is", ctx.resultAsNodes().get(1).get(TOKEN_NAME));
                        assertEquals("me", ctx.resultAsNodes().get(2).get(TOKEN_NAME));
                        counter[0]++;
                        ctx.continueTask();
                    }

                })
                .readVar("myNgram")
                .traverse("history")
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        assertEquals(2, ctx.resultAsNodes().get(0).get("order"));
                        counter[0]++;
                        ctx.continueTask();
                    }
                })
                .traverse("gram")
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        assertEquals("This", ctx.resultAsNodes().get(0).get(TOKEN_NAME));
                        assertEquals("is", ctx.resultAsNodes().get(1).get(TOKEN_NAME));
                        counter[0]++;
                        ctx.continueTask();
                    }

                })
                .readVar("myNgram")
                .traverse("backOff")
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        assertEquals(2, ctx.resultAsNodes().get(0).get("order"));
                        counter[0]++;
                        ctx.continueTask();
                    }
                })
                .defineAsVar("backoffngram")
                .traverse("gram")
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        assertEquals("is", ctx.resultAsNodes().get(0).get(TOKEN_NAME));
                        assertEquals("me", ctx.resultAsNodes().get(1).get(TOKEN_NAME));
                        counter[0]++;
                        ctx.continueTask();
                    }

                })
                .then(getOrCreateNgramFromString("is", "me"))
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        assertEquals(((Node) ctx.variable("backoffngram").get(0)).id(), ctx.resultAsNodes().get(0).id());
                        counter[0]++;
                        ctx.continueTask();
                    }
                })
                .execute(graph, null);
        assertEquals(7, counter[0]);
        removeGraph();
    }
}