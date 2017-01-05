package lu.jimenez.research.mwdbtoken.tokenization.preprocessor


interface TokenPreprocessor {

    fun preProcess(token : String):String
}