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
import meow.tokens.task.RelationTask;
import meow.tokens.task.TaskTest;
import meow.tokens.tokenization.TokenizerFactory;
import meow.tokens.tokenization.tokenizer.Tokenizer;
import org.junit.jupiter.api.Test;

import static greycat.Tasks.newTask;
import static meow.tokens.TokensConstants.ENTRY_POINT_INDEX;
import static meow.tokens.actions.MwdbTokenActions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


class ActionRebuildingTokenizeContentTest extends TaskTest {
    public static String text1 = "the apple was looking over the cloud";
    public static String text2 = "an orange was riding a skateboard";


    @Test
    public void test() {
        initGraph();
        final int[] counter = {0};
        TokenizerFactory tf = new TokenizerFactory("");
        final Tokenizer tokenizer = tf.create(text1, null);


        newTask()
                .travelInTime("0")
                .then(initializeVocabulary())
                .then(tokenizeStringsUsingTokenizer("default", null, "true", "my type", text1))
                .defineAsVar("tokenizer")
                .readGlobalIndex(ENTRY_POINT_INDEX, "name", "root")
                .defineAsVar("nodevar")
                .pipe(RelationTask.updateOrCreateTokenizeRelationsToNodes("tokenizer", "nodevar", new String[]{"text1"}))
                .traverse("tokenizedContents")
                .defineAsVar("tokenizedContents")
                .then(rebuildingTokenizedContents("tokenizedContents"))
                .flat()
                .thenDo(new ActionFunction() {
                            @Override
                            public void eval(TaskContext ctx) {
                                assertEquals(3, ctx.result().size());
                                assertEquals("text1", ctx.result().get(0));
                                assertEquals("my type", ctx.result().get(1));
                                assertEquals(text1, ctx.result().get(2));
                                counter[0]++;
                                ctx.continueTask();
                            }
                        }
                )
                .execute(graph, null);
        assertEquals(1, counter[0]);
        removeGraph();
    }




}