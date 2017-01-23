package lu.jimenez.research.mwdbtoken.nlp.ngram.actions;

import org.mwg.task.Action;

public class MwdbNgramActions {

    public static Action initializeNgram() {
        return new ActionInitializeNgram();
    }

    public static Action retrieveNgramMainNode() {
        return new ActionRetrieveNgramMainNode();
    }
}
