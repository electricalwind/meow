package lu.jimenez.research.mwdbtoken.tokenization.preprocessor


class LowerCasePreprocessor : TokenPreprocessor {
    override fun preProcess(token: String): String {
        return token.toLowerCase()
    }
}