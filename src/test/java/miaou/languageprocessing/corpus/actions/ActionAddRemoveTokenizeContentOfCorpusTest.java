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
package miaou.languageprocessing.corpus.actions;

import greycat.ActionFunction;
import greycat.Task;
import greycat.TaskContext;
import miaou.languageprocessing.ActionTest;
import miaou.tokens.task.RelationTask;
import org.junit.jupiter.api.Test;

import static greycat.Tasks.newTask;
import static miaou.languageprocessing.corpus.CorpusConstants.CORPUS_TO_TOKENIZEDCONTENTS_RELATION;
import static miaou.languageprocessing.corpus.actions.MwdbCorpusActions.*;
import static miaou.tokens.TokensConstants.*;
import static miaou.tokens.actions.MwdbTokenActions.initializeVocabulary;
import static miaou.tokens.actions.MwdbTokenActions.tokenizeStringsUsingTokenizer;
import static org.junit.jupiter.api.Assertions.assertEquals;


class ActionAddRemoveTokenizeContentOfCorpusTest extends ActionTest {

    public static String text1 = "the apple was looking over the cloud";
    public static String text11 = "an ordinary apple was looking at a cloud";
    @Test
    public void add() {
        initGraph();
        final int[] counter = {0};
        newTask()
                .pipe(createTokenizeContent())
                .travelInTime("2")
                .traverse(TOKENIZE_CONTENT_RELATION)
                .defineAsVar("tokenizeContent")
                .then(initializeCorpus())
                .then(addRemoveTokenizeContentsOfCorpus(true,"tokenizeContent","myCorpus"))
                .then(getOrCreateCorpus("myCorpus"))
                .traverse(CORPUS_TO_TOKENIZEDCONTENTS_RELATION)
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        assertEquals(1,ctx.result().size());
                        assertEquals("text1",ctx.resultAsNodes().get(0).get(TOKENIZE_CONTENT_NAME));
                        counter[0]++;
                        ctx.continueTask();
                    }
                })
                .then(addRemoveTokenizeContentsOfCorpus(true,"tokenizeContent","myCorpus"))
                .then(getOrCreateCorpus("myCorpus"))
                .traverse(CORPUS_TO_TOKENIZEDCONTENTS_RELATION)
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        assertEquals(1,ctx.result().size());
                        assertEquals("text1",ctx.resultAsNodes().get(0).get(TOKENIZE_CONTENT_NAME));
                        counter[0]++;
                        ctx.continueTask();
                    }
                })
                .execute(graph,null);
        assertEquals(2,counter[0]);
        removeGraph();
    }


    @Test
    public void remove() {
        initGraph();
        final int[] counter = {0};
        newTask()
                .pipe(createTokenizeContent())
                .travelInTime("2")
                .traverse(TOKENIZE_CONTENT_RELATION)
                .defineAsVar("tokenizeContent")
                .then(initializeCorpus())
                .then(addRemoveTokenizeContentsOfCorpus(true,"tokenizeContent","myCorpus"))
                .then(addRemoveTokenizeContentsOfCorpus(false,"tokenizeContent","myCorpus"))
                .then(getOrCreateCorpus("myCorpus"))
                .traverse(CORPUS_TO_TOKENIZEDCONTENTS_RELATION)
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        assertEquals(0,ctx.result().size());
                        counter[0]++;
                        ctx.continueTask();
                    }
                })
                .then(addRemoveTokenizeContentsOfCorpus(false,"tokenizeContent","myCorpus"))
                .execute(graph,null);
        assertEquals(1,counter[0]);
        removeGraph();
    }



    private Task createTokenizeContent(){
        return newTask()
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
                .pipe(RelationTask.updateOrCreateTokenizeRelationsToNodes("tokenizer", "nodevar", new String[]{"text1"}));
    }

}