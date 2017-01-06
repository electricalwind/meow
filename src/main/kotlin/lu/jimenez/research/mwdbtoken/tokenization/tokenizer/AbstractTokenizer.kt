package lu.jimenez.research.mwdbtoken.tokenization.tokenizer

import lu.jimenez.research.mwdbtoken.tokenization.preprocessor.TokenPreprocessor


abstract class AbstractTokenizer : Tokenizer {

    var tokenPreprocess: TokenPreprocessor? = null
    var type: String? = null

    override fun setTokenPreprocessor(tokenPreprocessor: TokenPreprocessor?) {
        tokenPreprocess = tokenPreprocessor
    }

    override fun getTypeOfToken(): String? {
        return type
    }

    override fun setTypeOfToken(typeOfToken: String?) {
        type = typeOfToken
    }

}