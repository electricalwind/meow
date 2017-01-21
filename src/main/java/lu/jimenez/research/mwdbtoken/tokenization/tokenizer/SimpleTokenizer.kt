package lu.jimenez.research.mwdbtoken.tokenization.tokenizer

import java.util.*


class SimpleTokenizer(tokens: String) : AbstractTokenizer() {


    val tokenizer: StringTokenizer = StringTokenizer(tokens)
    val listOfTokens: MutableList<String> = mutableListOf()


    override fun countTokens(): Int {
        return tokenizer.countTokens()
    }

    override fun getTokens(): List<String> {


        while (hasMoreTokens()) {
            nextToken()
        }
        return listOfTokens
    }

    override fun hasMoreTokens(): Boolean {
        return tokenizer.hasMoreTokens()
    }


    override fun nextToken(): String {
        val base = tokenizer.nextToken()
        if (tokenPreprocess != null) {
            val tok = tokenPreprocess?.preProcess(base) ?: throw RuntimeException("error while preprocessing")
            listOfTokens.add(tok)
            return tok
        }
        listOfTokens.add(base)
        return base

    }


}