package lu.jimenez.research.mwdbtoken.actions;

import org.mwg.task.Action;

public class MwdbTokenActions {

    public static Action retrieveVocabularyNode(){
        return new ActionRetrieveVocabularyNode();
    }

    public static Action getOrCreateTokensFromString(String... stringTokens){
        return new ActionGetOrCreateTokensFromString(stringTokens);
    }

    public static Action tokenizeStringsUsingTokenizer(String tokenizer, String preprocessor, String... toTokenize){
        return new ActionTokenizeStringsUsingTokenizer(tokenizer,preprocessor,toTokenize);
    }
}
