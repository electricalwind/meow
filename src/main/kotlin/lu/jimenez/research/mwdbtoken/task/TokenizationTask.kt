package lu.jimenez.research.mwdbtoken.task

import lu.jimenez.research.mwdbtoken.tokenization.*
import lu.jimenez.research.mwdbtoken.tokenization.preprocessor.TokenPreprocessor
import org.mwg.core.task.Actions.newTask
import org.mwg.task.*


class TokenizationTask(val tokenizer: String) {
    val factory: TokenizerFactory = TokenizerFactory(tokenizer)

    fun setPreprocessor(preprocessor: TokenPreprocessor?) {
        factory.tokenPreprocess = preprocessor
    }

    fun tokenizeString(toTokenize: Array<String>): Task {
        return newTask()
                .inject(toTokenize)
                .map(tokenize())
    }

    private fun tokenize(): Task {
        return newTask().thenDo { ctx: TaskContext ->
            var tokenizer = factory.create(ctx.resultAsStrings()[0])
            ctx.continueWith(ctx.wrap(tokenizer))
        }
    }

    companion object statical {
        @JvmStatic
        fun tokenizeStringsUsingTokenizer(tokenizer: String, tpreprocessor: String?, toTokenize: Array<String>): Task {
            val preprocessor = PreProcessorFactory.create(tpreprocessor)
            val tokenization = TokenizationTask(tokenizer)
            tokenization.setPreprocessor(preprocessor)
            return tokenization.tokenizeString(toTokenize)
        }
    }
}