package lu.jimenez.research.mwdbtoken.task

import lu.jimenez.research.mwdbtoken.Constants
import lu.jimenez.research.mwdbtoken.Constants.TOKEN_NAME
import lu.jimenez.research.mwdbtoken.actions.MwdbTokenActions.retrieveVocabularyNode
import lu.jimenez.research.mylittleplugin.MyLittleActions.ifEmptyThen
import mu.KLogging
import org.mwg.Type
import org.mwg.core.CoreConstants
import org.mwg.core.task.Actions.newTask
import org.mwg.task.Task

object VocabularyTask : KLogging() {

    /**
     * Create a NodeIndex vocabulary
     */
    fun initializeVocabulary(): Task {
        return newTask()
                .createNode()
                .setAttribute(Constants.ENTRY_POINT_NODE_NAME, Type.STRING, Constants.VOCABULARY_NODE_NAME)
                .addToGlobalIndex(Constants.ENTRY_POINT_INDEX, Constants.ENTRY_POINT_NODE_NAME)
    }

    @JvmStatic
    fun getOrCreateTokensFromString(tokens: Array<String>): Task {
        return newTask()
                .then(retrieveVocabularyNode())
                .defineAsVar("Vocabulary")
                .inject(tokens)
                .forEach(retrieveToken())
    }

    private fun retrieveToken(): Task {
        return newTask()
                .defineAsVar("token")
                .readVar("Vocabulary")
                .traverse(CoreConstants.INDEX_ATTRIBUTE, TOKEN_NAME, "token")
                .then(ifEmptyThen(
                        createToken("token")
                ))
    }

    private fun createToken(token: String): Task {
        return newTask()
    }

}