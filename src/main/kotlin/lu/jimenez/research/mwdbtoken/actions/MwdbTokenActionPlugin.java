package lu.jimenez.research.mwdbtoken.actions;

import org.mwg.Graph;
import org.mwg.Type;
import org.mwg.plugin.ActionFactory;
import org.mwg.plugin.Plugin;
import org.mwg.task.Action;

import static lu.jimenez.research.mwdbtoken.actions.MwdbTokenActions.*;

public class MwdbTokenActionPlugin implements Plugin {


    public void start(Graph graph) {

        graph.actionRegistry()
                .declaration(MwdbTokenActionNames.INITIALIZE_VOCABULARY)
                .setParams()
                .setDescription("Initialize the plugin by creating a Vocabulary Node")
                .setFactory(new ActionFactory() {
                    @Override
                    public Action create(Object[] params) {
                        return initializeVocabulary();
                    }
                });

        graph.actionRegistry()
                .declaration(MwdbTokenActionNames.RETRIEVE_VOCABULARY_NODE)
                .setParams()
                .setDescription("retrieve the Vocabulary Node")
                .setFactory(new ActionFactory() {
                    @Override
                    public Action create(Object[] params) {
                        return retrieveVocabularyNode();
                    }
                });


        graph.actionRegistry()
                .declaration(MwdbTokenActionNames.GET_OR_CREATE_TOKENS_FROM_STRING)
                .setParams(Type.STRING_ARRAY)
                .setDescription("Retrieve all the node corresponding to a token and create one if not existing")
                .setFactory(new ActionFactory() {
                    @Override
                    public Action create(Object[] params) {
                        if (params[0] != null) {
                            return getOrCreateTokensFromString((String[]) params[0]);
                        } else return null;
                    }
                });
        graph.actionRegistry()
                .declaration(MwdbTokenActionNames.TOKENIZE_STRINGS_USING_TOKENIZER)
                .setParams(Type.STRING, Type.STRING, Type.STRING, Type.STRING_ARRAY)
                .setDescription("Tokenize a content and put the tokenizer in result, 1)tokenizer type, 2)preprocessor,3)type of content,4) content")
                .setFactory(new ActionFactory() {
                    @Override
                    public Action create(Object[] params) {
                        if (params[3] != null) {
                            return tokenizeStringsUsingTokenizer((String) params[0], (String) params[1], (String) params[2], (String[]) params[3]);
                        } else return null;
                    }
                });

        graph.actionRegistry()
                .declaration(MwdbTokenActionNames.CREATE_OR_UPDATE_TOKENIZE_RELATIONS_TO_NODES)
                .setParams(Type.STRING, Type.STRING, Type.STRING_ARRAY)
                .setDescription("update or create a tokenized Content relation in one or several nodes with the given index")
                .setFactory(new ActionFactory() {
                    @Override
                    public Action create(Object[] params) {
                        if (params[2] != null) {
                            return uocTokenizeRelationsToNodes((String) params[0], (String) params[1], (String[]) params[2]);
                        } else return null;
                    }
                });


    }

    @Override
    public void stop() {

    }
}
