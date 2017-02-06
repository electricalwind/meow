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

import greycat.Action;

public class MwdbCorpusActions {

    public static Action initializeCorpus() {
        return new ActionInitializeCorpus();
    }

    public static Action retrieveCorpusMainNode() {
        return new ActionRetrieveCorpusMainNode();
    }

    public static Action getOrCreateCorpus(String corpusName) {
        return new ActionGetOrCreateCorpus(corpusName);
    }

    public static Action addRemoveTokenizeContentsOfCorpus(boolean add,String tokenizeContentVar,String corpusName){
        return new ActionAddRemoveTokenizeContentOfCorpus(add,tokenizeContentVar,corpusName);
    }
}
