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
package meow.tokens.actions;

import greycat.ActionFunction;
import greycat.TaskContext;
import meow.tokens.tokenization.tokenizer.Tokenizer;
import org.junit.jupiter.api.Test;

import static greycat.Tasks.newTask;
import static meow.tokens.TokensConstants.NO_TYPE_TOKENIZE;
import static meow.tokens.actions.MwdbTokenActions.tokenizeStringsUsingTokenizer;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class ActionTokenizeStringsUsingTokenizerTest extends ActionTest {

    @Test
    public void testtypeOneString() {
        int counter = 1;
        final int[] i = {0};
        initGraph();
        newTask()
                .then(tokenizeStringsUsingTokenizer("default", null, "true", "my type", "This is a lovely String"))
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        assertEquals(ctx.result().size(), 1);
                        Tokenizer tokenizer = (Tokenizer) ctx.result().get(0);
                        assertEquals(tokenizer.countTokens(), 5);
                        assertEquals(tokenizer.getTypeOfToken(), "my type");
                        i[0]++;
                        ctx.continueTask();
                    }
                })
                //.addHook(new VerboseHook())
                .execute(graph, null);

        assertEquals(counter, i[0]);
        removeGraph();
    }

    @Test
    public void testtypeSeveralString() {
        int counter = 1;
        final int[] i = {0};
        initGraph();
        newTask()
                .then(tokenizeStringsUsingTokenizer("default", null, "true", "my type", "This is a lovely String", "my second type", "and This one is even lovelier", "my other type", "you don't say"))
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        assertEquals(ctx.result().size(), 3);
                        Tokenizer tokenizer = (Tokenizer) ctx.result().get(0);
                        assertEquals(tokenizer.countTokens(), 5);
                        assertEquals(tokenizer.getTypeOfToken(), "my type");
                        Tokenizer tokenizer2 = (Tokenizer) ctx.result().get(1);
                        assertEquals(tokenizer2.countTokens(), 6);
                        assertEquals(tokenizer2.getTypeOfToken(), "my second type");
                        Tokenizer tokenizer3 = (Tokenizer) ctx.result().get(2);
                        assertEquals(tokenizer3.countTokens(), 3);
                        assertEquals(tokenizer3.getTypeOfToken(), "my other type");
                        i[0]++;
                        ctx.continueTask();
                    }
                })
                //.addHook(new VerboseHook())
                .execute(graph, null);

        assertEquals(counter, i[0]);
        removeGraph();
    }


    @Test
    public void testnotypeOneString() {
        int counter = 1;
        final int[] i = {0};
        initGraph();
        newTask()
                .then(tokenizeStringsUsingTokenizer("default", null, "false", "This is a lovely String"))
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        assertEquals(ctx.result().size(), 1);
                        Tokenizer tokenizer = (Tokenizer) ctx.result().get(0);
                        assertEquals(tokenizer.getTypeOfToken(), NO_TYPE_TOKENIZE);
                        i[0]++;
                        ctx.continueTask();
                    }
                })
                //.addHook(new VerboseHook())
                .execute(graph, null);

        assertEquals(counter, i[0]);
        removeGraph();
    }

    @Test
    public void testnotypeSeveralString() {
        int counter = 1;
        final int[] i = {0};
        initGraph();
        newTask()
                .then(tokenizeStringsUsingTokenizer("default", null, "false", "This is a lovely String", "and This one is even lovelier", "you don't say"))
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        assertEquals(ctx.result().size(), 3);
                        Tokenizer tokenizer = (Tokenizer) ctx.result().get(2);
                        assertEquals(tokenizer.getTypeOfToken(), NO_TYPE_TOKENIZE);
                        i[0]++;
                        ctx.continueTask();
                    }
                })
                //.addHook(new VerboseHook())
                .execute(graph, null);

        assertEquals(counter, i[0]);
        removeGraph();
    }

}