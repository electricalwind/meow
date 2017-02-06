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
package meow.languageprocessing.corpus.actions;


import greycat.Action;
import greycat.Graph;
import greycat.Type;
import greycat.plugin.ActionFactory;
import greycat.plugin.Plugin;

import static meow.languageprocessing.corpus.actions.MwdbCorpusActionNames.*;
import static meow.languageprocessing.corpus.actions.MwdbCorpusActions.*;

public class MwdbCorpusActionPlugin implements Plugin {
    @Override
    public void start(Graph graph) {
        graph.actionRegistry()
                .declaration(INITIALIZE_CORPUS)
                .setParams()
                .setDescription("Create the corpus Main Node")
                .setFactory(new ActionFactory() {
                    @Override
                    public Action create(Object[] params) {
                        return initializeCorpus();
                    }
                });

        graph.actionRegistry()
                .declaration(RETRIEVE_CORPUS_MAIN_NODE)
                .setParams()
                .setDescription("Retrieve the Corpus Main Node")
                .setFactory(new ActionFactory() {
                    @Override
                    public Action create(Object[] params) {
                        return retrieveCorpusMainNode();
                    }
                });

        graph.actionRegistry()
                .declaration(GET_OR_CREATE_CORPUS)
                .setParams(Type.STRING)
                .setDescription("Get or create a corpus Node")
                .setFactory(new ActionFactory() {
                    @Override
                    public Action create(Object[] params) {
                        return getOrCreateCorpus((String) params[0]);
                    }
                });

        graph.actionRegistry()
                .declaration(ADD_REMOVE_TOKENIZE_CONTENTS_OF_CORPUS)
                .setParams(Type.BOOL, Type.STRING, Type.STRING)
                .setDescription("adding or removing tokenize content from a corpus")
                .setFactory(new ActionFactory() {
                    @Override
                    public Action create(Object[] params) {
                        return addRemoveTokenizeContentsOfCorpus((boolean) params[0], (String) params[1], (String) params[2]);
                    }
                });
    }

    @Override
    public void stop() {

    }
}
