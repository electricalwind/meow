package lu.jimenez.research.mwdbtoken.nlp.corpus.actions;

import org.mwg.Graph;
import org.mwg.Type;
import org.mwg.plugin.ActionFactory;
import org.mwg.plugin.Plugin;
import org.mwg.task.Action;

import static lu.jimenez.research.mwdbtoken.nlp.corpus.actions.MwdbCorpusActionNames.*;
import static lu.jimenez.research.mwdbtoken.nlp.corpus.actions.MwdbCorpusActions.*;

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
