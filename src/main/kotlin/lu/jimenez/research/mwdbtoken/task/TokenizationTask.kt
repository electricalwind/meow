package lu.jimenez.research.mwdbtoken.task

import lu.jimenez.research.mwdbtoken.tokenization.*
import lu.jimenez.research.mwdbtoken.tokenization.preprocessor.TokenPreprocessor
import org.mwg.core.task.Actions.newTask
import org.mwg.task.*


class TokenizationTask(tokenizer: String) {
    val factory: TokenizerFactory = TokenizerFactory(tokenizer)

    private fun setPreprocessor(preprocessor: TokenPreprocessor?) {
        factory.tokenPreprocess = preprocessor
    }

    private fun tokenizeString(toTokenize: Array<String>, types: Array<String>?): Task {
        return newTask()
                .inject(toTokenize)
                .map(tokenize(types))
    }

    private fun tokenize(types: Array<String>?): Task {
        return newTask()
                .thenDo { ctx: TaskContext ->
                    var type: String? = types?.get(ctx.variable("i")[0] as Int)
                    var tokenizer = factory.create(ctx.resultAsStrings()[0],type)
                    ctx.continueWith(ctx.wrap(tokenizer))
                }
    }

    companion object statical {
        @JvmStatic
        fun tokenizeStringsUsingTokenizer(tokenizer: String, tpreprocessor: String?, type: String, params: Array<String>): Task {
            val preprocessor = PreProcessorFactory.create(tpreprocessor)
            val tokenization = TokenizationTask(tokenizer)
            tokenization.setPreprocessor(preprocessor)
            val toTokenize: Array<String>
            val types: Array<String>?
            if (type.toBoolean()) {
                if (params.size % 2 != 0) {
                    throw RuntimeException("not the same number of types and stringToTokenize")
                } else {
                    val listString = mutableListOf<String>()
                    val listType = mutableListOf<String>()
                    var i = 0
                    while (i < params.size) {
                        listString.add(params[i])
                        listType.add(params[i + 1])
                        i += 2
                    }
                    toTokenize = listString.toTypedArray()
                    types = listType.toTypedArray()
                }
            } else {
                toTokenize = params
                types = null
            }
            return tokenization.tokenizeString(toTokenize, types)
        }
    }
}