package lu.jimenez.research.mwdbtoken.nlp.ngram.actions;

import org.mwg.Graph;
import org.mwg.Type;
import org.mwg.plugin.ActionFactory;
import org.mwg.plugin.Plugin;
import org.mwg.task.Action;

import static lu.jimenez.research.mwdbtoken.nlp.ngram.actions.MwdbNgramActions.*;

public class MwdbNgramActionPlugin implements Plugin {
    @Override
    public void start(Graph graph) {
        graph.actionRegistry()
                .declaration(MwdbNgramActionNames.INITIALIZE_NGRAM)
                .setParams()
                .setDescription("Create the Ngram Main Node")
                .setFactory(new ActionFactory() {
                    @Override
                    public Action create(Object[] params) {
                        return initializeNgram();
                    }
                });

        graph.actionRegistry()
                .declaration(MwdbNgramActionNames.RETRIEVE_NGRAM_MAIN_NODE)
                .setParams()
                .setDescription("retrieve the Ngram Main Node")
                .setFactory(new ActionFactory() {
                    @Override
                    public Action create(Object[] params) {
                        return retrieveNgramMainNode();
                    }
                });

        graph.actionRegistry()
                .declaration(MwdbNgramActionNames.GET_OR_CREATE_NGRAM_FROM_VAR)
                .setParams(Type.STRING)
                .setDescription("get or create a n gram corresponding to the tokens store in the given var")
                .setFactory(new ActionFactory() {
                    @Override
                    public Action create(Object[] params) {
                        return getOrCreateNgramFromVar((String) params[0]);
                    }
                });

        graph.actionRegistry()
                .declaration(MwdbNgramActionNames.GET_OR_CREATE_NGRAM_FROM_STRING)
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
                .declaration(MwdbNgramActionNames.INITIALIZE_CORPUS)
                .setParams()
                .setDescription("Create the corpus Main Node")
                .setFactory(new ActionFactory() {
                    @Override
                    public Action create(Object[] params) {
                        return initializeCorpus();
                    }
                });

        graph.actionRegistry()
                .declaration(MwdbNgramActionNames.RETRIEVE_CORPUS_MAIN_NODE)
                .setParams()
                .setDescription("Retrieve the Corpus Main Node")
                .setFactory(new ActionFactory() {
                    @Override
                    public Action create(Object[] params) {
                        return retrieveCorpusMainNode();
                    }
                });
    }

    @Override
    public void stop() {

    }
}
