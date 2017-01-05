package lu.jimenez.research.mwdbtoken.tokenization.tokenizer

import lu.jimenez.research.mwdbtoken.tokenization.preprocessor.TokenPreprocessor
import java.util.*


class SimpleTokenizer(tokens: String) : Tokenizer {


    val tokenizer: StringTokenizer = StringTokenizer(tokens)
    private var tokenPreprocess: TokenPreprocessor? = null

    override fun countTokens(): Int {
        return tokenizer.countTokens()
    }

    override fun getTokens(): List<String> {
        val tokenList = mutableListOf<String>()
        while (hasMoreTokens()) {
            tokenList.add(nextToken())
        }
        return tokenList
    }

    override fun hasMoreTokens(): Boolean {
        return tokenizer.hasMoreTokens()
    }


    override fun nextToken(): String {
        val base = tokenizer.nextToken()
        if (tokenPreprocess != null) {
            return tokenPreprocess?.preProcess(base) ?: throw RuntimeException("error while preprocessing")
        }
        return base

    }

    override fun setTokenPreprocessor(tokenPreprocessor: TokenPreprocessor?) {
        tokenPreprocess = tokenPreprocessor
    }

}