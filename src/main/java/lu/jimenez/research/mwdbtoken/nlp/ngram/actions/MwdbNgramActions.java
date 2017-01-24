package lu.jimenez.research.mwdbtoken.nlp.ngram.actions;

import lu.jimenez.research.mwdbtoken.nlp.ngram.actions.corpus.ActionGetOrCreateCorpus;
import lu.jimenez.research.mwdbtoken.nlp.ngram.actions.corpus.ActionInitializeCorpus;
import lu.jimenez.research.mwdbtoken.nlp.ngram.actions.corpus.ActionRetrieveCorpusMainNode;
import lu.jimenez.research.mwdbtoken.nlp.ngram.actions.ngram.ActionGetOrCreateNgramFromString;
import lu.jimenez.research.mwdbtoken.nlp.ngram.actions.ngram.ActionGetOrCreateTokenFromVar;
import lu.jimenez.research.mwdbtoken.nlp.ngram.actions.ngram.ActionInitializeNgram;
import lu.jimenez.research.mwdbtoken.nlp.ngram.actions.ngram.ActionRetrieveNgramMainNode;
import org.mwg.task.Action;

public class MwdbNgramActions {

    public static Action initializeNgram() {
        return new ActionInitializeNgram();
    }

    public static Action retrieveNgramMainNode() {
        return new ActionRetrieveNgramMainNode();
    }

    public static Action getOrCreateNgramFromString(String... grams) {
        return new ActionGetOrCreateNgramFromString(grams);
    }

    public static Action getOrCreateNgramFromVar(String var) {
        return new ActionGetOrCreateTokenFromVar(var);
    }

    public static Action initializeCorpus() {
        return new ActionInitializeCorpus();
    }

    public static Action retrieveCorpusMainNode() {
        return new ActionRetrieveCorpusMainNode();
    }

    public static Action getOrCreateCorpus(String corpusName) {
        return new ActionGetOrCreateCorpus(corpusName);
    }
}
