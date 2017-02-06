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

import greycat.Action;

public class MwdbNgramActions {

    public static Action initializeNgram() {
        return new ActionInitializeNgram();
    }

    public static Action retrieveNgramMainNode() {
        return new ActionRetrieveNgramMainNode();
    }

    public static Action getOrCreateNgramFromString(String... grams) {
        return new ActionGetOrCreateNgramFromString(grams);
    }

    public static Action getOrCreateNgramFromVar(String var) {
        return new ActionGetOrCreateTokenFromVar(var);
    }

    public static Action updateNgramTokenizedContentFromVar(String var) {
        return new ActionUpdateNgramTokenizeContentFromVar(var);
    }


}
