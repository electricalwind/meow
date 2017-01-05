package lu.jimenez.research.mwdbtoken.tokenization

import lu.jimenez.research.mwdbtoken.tokenization.preprocessor.*


object PreProcessorFactory {

    fun create(preprocessor : String?): TokenPreprocessor? {
        when (preprocessor){
            "lower"-> return LowerCasePreprocessor()
           else -> return null
        }
    }
}