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
package meow.languageprocessing.ngram.actions;

public class MwdbNgramActionNames {
    public static String INITIALIZE_NGRAM = "initializeNgram";
    public static String RETRIEVE_NGRAM_MAIN_NODE = "retrieveNgramMainNode";
    public static String GET_OR_CREATE_NGRAM_FROM_STRING = "getOrCreateNgramFromString";
    public static String GET_OR_CREATE_NGRAM_FROM_VAR = "getOrCreateNgramFromVar";
    public static String UPDATE_NGRAM_TC_FROM_VAR = "updateNgramTCFromVar";
}