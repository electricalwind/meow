/**
 * Copyright 2017 Matthieu Jimenez.  All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package miaou.languageprocessing.ngram.actions;

import greycat.ActionFunction;
import greycat.Callback;
import greycat.Node;
import greycat.TaskContext;
import miaou.languageprocessing.ActionTest;
import miaou.tokens.task.RelationTask;
import miaou.tokens.tokenization.TokenizerFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static greycat.Constants.BEGINNING_OF_TIME;
import static greycat.Constants.END_OF_TIME;
import static greycat.Tasks.newTask;
import static greycat.Tasks.thenDo;
import static miaou.languageprocessing.ngram.NgramConstants.*;
import static miaou.languageprocessing.ngram.actions.MwdbNgramActions.initializeNgram;
import static miaou.languageprocessing.ngram.actions.MwdbNgramActions.updateNgramTokenizedContentFromVar;
import static miaou.tokens.TokensConstants.*;
import static miaou.tokens.actions.MwdbTokenActions.initializeVocabulary;
import static miaou.tokens.actions.MwdbTokenActions.tokenizeStringsUsingTokenizer;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ActionUpdateNgramTokenizeContentFromVarTest extends ActionTest {

    public static String text1 = "the apple was looking over the cloud";
    public static String text2 = "an orange was riding a skateboard";
    public static String text3 = "this may have no sense";
    public static String text11 = "an ordinary apple was looking at a cloud";

    @Test
    public void test() {
        initGraph();
        final int[] counter = {0};
        TokenizerFactory tf = new TokenizerFactory("");
        final List<String> tokenizer = tf.create(text1, null).getTokens();
        final List<String> tokenizer2 = tf.create(text11, null).getTokens();
        newTask()
                .travelInTime("0")
                .then(initializeVocabulary())
                .then(tokenizeStringsUsingTokenizer("default", null, "true", "my type", text1))
                .defineAsVar("tokenizer")
                .readGlobalIndex(ENTRY_POINT_INDEX, "name", "root")
                .defineAsVar("nodevar")
                .pipe(RelationTask.updateOrCreateTokenizeRelationsToNodes("tokenizer", "nodevar", new String[]{"text1"}))
                .traverse("tokenizedContents")
                .travelInTime("1")
                .then(tokenizeStringsUsingTokenizer("default", null, "true", "my type", text11))
                .defineAsVar("tokenizer")
                .readGlobalIndex(ENTRY_POINT_INDEX, "name", "root")
                .defineAsVar("nodevar")
                .pipe(RelationTask.updateOrCreateTokenizeRelationsToNodes("tokenizer", "nodevar", new String[]{"text1"}))
                .traverse(TOKENIZE_CONTENT_RELATION)
                .setAsVar("tc")
                .then(initializeNgram())
                .then(updateNgramTokenizedContentFromVar("tc"))
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        ctx.setVariable("tcid", ctx.resultAsNodes().get(0).id());
                        ctx.continueTask();
                    }
                })
                .traverse("plugin")
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        assertEquals(NODE_TYPE_NGRAM_TOKENIZED_CONTENT, ctx.resultAsNodes().get(0).get(NODE_TYPE));
                        ctx.resultAsNodes().get(0).timepoints(BEGINNING_OF_TIME, END_OF_TIME, new Callback<long[]>() {
                            @Override
                            public void on(long[] result) {
                                assertEquals(2, result.length);
                                assertEquals(1, result[0]);
                                assertEquals(0, result[1]);
                                counter[0]++;
                                ctx.continueTask();
                            }
                        });


                    }
                })
                .loop("1", Integer.toString(MAXIMUM_ORDER_OF_N),
                        newTask()
                                .traverse("{{i}}")
                                .thenDo(new ActionFunction() {
                                    @Override
                                    public void eval(TaskContext ctx) {
                                        int order = (int) ctx.variable("i").get(0);
                                        ctx.setVariable("order", order);
                                        if (order < 9)
                                            assertEquals(9 - order, ctx.resultAsNodes().size());
                                        counter[0]++;
                                        ctx.continueTask();
                                    }
                                })
                                .forEach(
                                        thenDo(new ActionFunction() {
                                            @Override
                                            public void eval(TaskContext ctx) {
                                                Node ngram = ctx.resultAsNodes().get(0);
                                                assertEquals((int) ctx.variable("order").get(0), (int) ngram.get("order"));
                                                ctx.continueTask();
                                            }
                                        })
                                                .setAsVar("ngram")
                                                .traverse(NGRAM_INVERTED_INDEX_RELATION, "id", "{{tcid}}")
                                                .thenDo(new ActionFunction() {
                                                    @Override
                                                    public void eval(TaskContext ctx) {
                                                        int i = (int) ctx.variable("i").get(0);
                                                        assert (IntStream.of((int[]) ctx.resultAsNodes().get(0).get("position")).anyMatch(x -> x == i));
                                                        counter[0]++;
                                                        ctx.continueTask();
                                                    }
                                                })
                                                .readVar("ngram")
                                                .traverse(GRAMS_TOKENS)
                                                .traverse(TOKEN_NAME)
                                                .thenDo(new ActionFunction() {
                                                    @Override
                                                    public void eval(TaskContext ctx) {
                                                        int order = (int) ctx.variable("order").get(0);
                                                        int position = (int) ctx.variable("i").get(0);
                                                        List<String> supposedResult = tokenizer2.subList(position, position + order);
                                                        assertEquals(supposedResult.size(), ctx.resultAsStrings().size());
                                                        for (int i = 0; i < supposedResult.size(); i++) {
                                                            assertEquals(supposedResult.get(i), ctx.resultAsStrings().get(i));
                                                        }
                                                        counter[0]++;
                                                        ctx.continueTask();
                                                    }
                                                })
                                )
                )


                .readVar("tc")
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        int i=0;
                        ctx.continueTask();
                    }
                })
                .travelInTime("0")
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        int i=0;
                        ctx.continueTask();
                    }
                })
                .traverse("plugin")
                .loop("1", Integer.toString(MAXIMUM_ORDER_OF_N),
                        newTask()
                                .traverse("{{i}}")
                                .thenDo(new ActionFunction() {
                                    @Override
                                    public void eval(TaskContext ctx) {
                                        int order = (int) ctx.variable("i").get(0);
                                        ctx.setVariable("order", order);
                                        if (order < 8)
                                            assertEquals(8 - order, ctx.resultAsNodes().size());
                                        counter[0]++;
                                        ctx.continueTask();
                                    }
                                })
                                .forEach(
                                        thenDo(new ActionFunction() {
                                            @Override
                                            public void eval(TaskContext ctx) {
                                                Node ngram = ctx.resultAsNodes().get(0);
                                                assertEquals((int) ctx.variable("order").get(0), (int) ngram.get("order"));
                                                ctx.continueTask();
                                            }
                                        })
                                                .setAsVar("ngram")
                                                .traverse(NGRAM_INVERTED_INDEX_RELATION, "id", "{{tcid}}")
                                                .thenDo(new ActionFunction() {
                                                    @Override
                                                    public void eval(TaskContext ctx) {
                                                        int i = (int) ctx.variable("i").get(0);
                                                        assert (IntStream.of((int[]) ctx.resultAsNodes().get(0).get("position")).anyMatch(x -> x == i));
                                                        counter[0]++;
                                                        ctx.continueTask();
                                                    }
                                                })
                                                .readVar("ngram")
                                                .traverse(GRAMS_TOKENS)
                                                .traverse(TOKEN_NAME)
                                                .thenDo(new ActionFunction() {
                                                    @Override
                                                    public void eval(TaskContext ctx) {
                                                        int order = (int) ctx.variable("order").get(0);
                                                        int position = (int) ctx.variable("i").get(0);
                                                        List<String> supposedResult = tokenizer.subList(position, position + order);
                                                        assertEquals(supposedResult.size(), ctx.resultAsStrings().size());
                                                        for (int i = 0; i < supposedResult.size(); i++) {
                                                            assertEquals(supposedResult.get(i), ctx.resultAsStrings().get(i));
                                                        }
                                                        counter[0]++;
                                                        ctx.continueTask();
                                                    }
                                                })
                                )
                )






                .execute(graph, null);
        System.out.println(counter[0]);
        removeGraph();
    }
}