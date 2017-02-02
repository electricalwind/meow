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
package lu.jimenez.research.mwgtoken.core.actions;

import org.mwg.task.Action;

public class MwdbTokenActions {

    public static Action initializeVocabulary() {
        return new ActionInitializeVocabulary();
    }

    public static Action retrieveVocabularyNode() {
        return new ActionRetrieveVocabularyNode();
    }

    public static Action getOrCreateTokensFromString(String... stringTokens) {
        return new ActionGetOrCreateTokensFromString(stringTokens);
    }

    public static Action tokenizeStringsUsingTokenizer(String tokenizer, String preprocessor, String type, String... toTokenize) {
        return new ActionTokenizeStringsUsingTokenizer(tokenizer, preprocessor, type, toTokenize);
    }

    public static Action uocTokenizeRelationsToNodes(String tokenizersVar, String nodesVar, String... relationList) {
        return new ActionCreateOrUpdateTokenizeRelationsToNodes(tokenizersVar, nodesVar, relationList);
    }

    public static Action rebuildingTokenizedContents(String tokenizedContentsVar) {
        return new ActionRebuildingTokenizeContent(tokenizedContentsVar);
    }
}
