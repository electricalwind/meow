package lu.jimenez.research.mwdbtoken.actions;

import org.mwg.task.Action;

public class MwdbTokenActions {

    public static Action initializeVocabulary(){
        return new ActionInitializeVocabulary();
    }

    public static Action retrieveVocabularyNode(){
        return new ActionRetrieveVocabularyNode();
    }

    public static Action getOrCreateTokensFromString(String... stringTokens){
        return new ActionGetOrCreateTokensFromString(stringTokens);
    }

    public static Action tokenizeStringsUsingTokenizer(String tokenizer, String preprocessor,String type, String... toTokenize){
        return new ActionTokenizeStringsUsingTokenizer(tokenizer,preprocessor,type,toTokenize);
    }


    public static Action uocTokenizeRelationsToNodes(String tokenizersVar, String nodesVar, String... relationList){
        return new ActionCreateOrUpdateTokenizeRelationsToNodes(tokenizersVar,nodesVar,relationList);
    }
}
