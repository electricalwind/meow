/**
 * Copyright 2017 Matthieu Jimenez.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package meow.languageprocessing.ngram;

import greycat.Action;
import greycat.Graph;
import greycat.Type;
import greycat.plugin.ActionFactory;
import greycat.plugin.Plugin;
import meow.languageprocessing.ngram.actions.NgramActionNames;

import static meow.languageprocessing.ngram.actions.NgramActions.*;

public class NgramPlugin implements Plugin {
    @Override
    public void start(Graph graph) {
        graph.actionRegistry()
                .declaration(NgramActionNames.INITIALIZE_NGRAM)
                .setParams()
                .setDescription("Create the Ngram Main Node")
                .setFactory(new ActionFactory() {
                    @Override
                    public Action create(Object[] params) {
                        return initializeNgram();
                    }
                });

        graph.actionRegistry()
                .declaration(NgramActionNames.RETRIEVE_NGRAM_MAIN_NODE)
                .setParams()
                .setDescription("retrieve the Ngram Main Node")
                .setFactory(new ActionFactory() {
                    @Override
                    public Action create(Object[] params) {
                        return retrieveNgramMainNode();
                    }
                });

        graph.actionRegistry()
                .declaration(NgramActionNames.GET_OR_CREATE_NGRAM_FROM_VAR)
                .setParams(Type.STRING)
                .setDescription("get or create a n gram corresponding to the tokens store in the given var")
                .setFactory(new ActionFactory() {
                    @Override
                    public Action create(Object[] params) {
                        return getOrCreateNgramFromVar((String) params[0]);
                    }
                });

        graph.actionRegistry()
                .declaration(NgramActionNames.GET_OR_CREATE_NGRAM_FROM_STRING)
                .setParams(Type.STRING_ARRAY)
                .setDescription("get or create a n gram corresponding to the gram given")
                .setFactory(new ActionFactory() {
                    @Override
                    public Action create(Object[] params) {
                        if (params[0] != null)
                            return getOrCreateNgramFromString((String[]) params[0]);
                        else return null;
                    }
                });

        graph.actionRegistry()
                .declaration(NgramActionNames.UPDATE_NGRAM_TC_FROM_VAR)
                .setParams(Type.STRING)
                .setDescription("create or update for all time the ngram version of tokenized content present in var")
                .setFactory(new ActionFactory() {
                    @Override
                    public Action create(Object[] params) {
                        return updateNgramTokenizedContentFromVar((String) params[0]);
                    }
                });


    }

    @Override
    public void stop() {

    }
}
