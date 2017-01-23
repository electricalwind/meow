package lu.jimenez.research.mwdbtoken.nlp.ngram.actions;

import org.mwg.Graph;
import org.mwg.plugin.ActionFactory;
import org.mwg.plugin.Plugin;
import org.mwg.task.Action;

import static lu.jimenez.research.mwdbtoken.nlp.ngram.actions.MwdbNgramActions.initializeNgram;
import static lu.jimenez.research.mwdbtoken.nlp.ngram.actions.MwdbNgramActions.retrieveNgramMainNode;

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
    }

    @Override
    public void stop() {

    }
}
