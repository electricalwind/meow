package lu.jimenez.research.mwdbtoken.tokenization

import lu.jimenez.research.mwdbtoken.tokenization.preprocessor.TokenPreprocessor
import lu.jimenez.research.mwdbtoken.tokenization.tokenizer.*


class TokenizerFactory(var tokenizerType: String) {
    var tokenPreprocess: TokenPreprocessor? = null

    fun create(toTokenize: String, typeOfToken: String? = null): Tokenizer {
        val tokenizer: Tokenizer
        when (tokenizerType) {
            else -> tokenizer = SimpleTokenizer(toTokenize)
        }
        tokenizer.setTokenPreprocessor(tokenPreprocess)
        if (typeOfToken != null)
            tokenizer.setTypeOfToken(typeOfToken)
        return tokenizer
    }

    fun setTokenPreprocessor(tokenPreprocessor: TokenPreprocessor) {
        tokenPreprocess = tokenPreprocessor
    }

    fun getTokenPreprocessor(): TokenPreprocessor? {
        return tokenPreprocess
    }
}