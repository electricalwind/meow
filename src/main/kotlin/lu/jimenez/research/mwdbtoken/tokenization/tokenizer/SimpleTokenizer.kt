package lu.jimenez.research.mwdbtoken.tokenization.tokenizer

import java.util.*


class SimpleTokenizer(tokens: String) : AbstractTokenizer() {


    val tokenizer: StringTokenizer = StringTokenizer(tokens)


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



}