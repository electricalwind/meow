package lu.jimenez.research.mwdbtoken.tokenization

import lu.jimenez.research.mwdbtoken.tokenization.preprocessor.TokenPreprocessor
import lu.jimenez.research.mwdbtoken.tokenization.tokenizer.SimpleTokenizer
import lu.jimenez.research.mwdbtoken.tokenization.tokenizer.Tokenizer


class TokenizerFactory(var tokenizerType: String) {
    var tokenPreprocess: TokenPreprocessor? = null

    fun create(toTokenize: String): Tokenizer {
        val tokenizer: Tokenizer
        when (tokenizerType) {
            else -> tokenizer = SimpleTokenizer(toTokenize)
        }
        tokenizer.setTokenPreprocessor(tokenPreprocess)
        return tokenizer
    }

    fun setTokenPreprocessor(tokenPreprocessor: TokenPreprocessor) {
        tokenPreprocess = tokenPreprocessor
    }

    fun getTokenPreprocessor(): TokenPreprocessor? {
        return tokenPreprocess
    }
}