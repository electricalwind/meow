package lu.jimenez.research.mwdbtoken.nlp.corpus.actions;

import org.mwg.task.Action;

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
