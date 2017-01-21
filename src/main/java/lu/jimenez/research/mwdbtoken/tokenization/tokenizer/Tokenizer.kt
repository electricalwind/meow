package lu.jimenez.research.mwdbtoken.tokenization.tokenizer

import lu.jimenez.research.mwdbtoken.tokenization.preprocessor.TokenPreprocessor


interface Tokenizer {

    fun countTokens() : Int

    fun getTokens(): List<String>

    fun getTypeOfToken():String?

    fun hasMoreTokens(): Boolean

    fun nextToken():String

    fun setTokenPreprocessor(tokenPreprocessor: TokenPreprocessor?)

    fun setTypeOfToken(typeOfToken:String?)
}