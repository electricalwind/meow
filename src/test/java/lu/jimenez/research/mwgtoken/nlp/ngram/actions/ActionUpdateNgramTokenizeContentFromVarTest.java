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
package lu.jimenez.research.mwgtoken.nlp.ngram.actions;

import lu.jimenez.research.mwgtoken.core.task.RelationTask;
import lu.jimenez.research.mwgtoken.nlp.ActionTest;
import org.junit.jupiter.api.Test;

import static lu.jimenez.research.mwgtoken.core.CoreConstants.ENTRY_POINT_INDEX;
import static lu.jimenez.research.mwgtoken.core.CoreConstants.TOKENIZE_CONTENT_RELATION;
import static lu.jimenez.research.mwgtoken.core.actions.MwdbTokenActions.initializeVocabulary;
import static lu.jimenez.research.mwgtoken.core.actions.MwdbTokenActions.tokenizeStringsUsingTokenizer;
import static lu.jimenez.research.mwgtoken.nlp.ngram.actions.MwdbNgramActions.initializeNgram;
import static lu.jimenez.research.mwgtoken.nlp.ngram.actions.MwdbNgramActions.updateNgramTokenizedContentFromVar;
import static org.mwg.task.Tasks.newTask;

class ActionUpdateNgramTokenizeContentFromVarTest extends ActionTest {

    public static String text1 = "the apple was looking over the cloud";
    public static String text2 = "an orange was riding a skateboard";
    public static String text3 = "this may have no sense";
    public static String text11 = "an ordinary apple was looking at a cloud";

    @Test
    public void test() {
        initGraph();
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
                .traverse("plugin")
                .println("{{result}}")
                /**.travelInTime("0")
                .println("{{result}}")
                /**.travelInTime("10")
                .println("{{result}}")*/
                .execute(graph, null);

        removeGraph();
    }
}