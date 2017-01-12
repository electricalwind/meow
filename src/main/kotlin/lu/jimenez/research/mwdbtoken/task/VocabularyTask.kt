package lu.jimenez.research.mwdbtoken.task

import lu.jimenez.research.mwdbtoken.Constants.*
import lu.jimenez.research.mwdbtoken.actions.MwdbTokenActions.retrieveVocabularyNode
import lu.jimenez.research.mylittleplugin.MyLittleActions.ifEmptyThen
import mu.KLogging
import org.mwg.*
import org.mwg.Constants.BEGINNING_OF_TIME
import org.mwg.core.task.Actions.newTask
import org.mwg.struct.EGraph
import org.mwg.task.Task

object VocabularyTask : KLogging() {

    /**
     * Create a NodeIndex vocabulary
     */
    @JvmStatic
    fun initializeVocabulary(): Task {
        return newTask()
                .thenDo { ctx ->
                    ctx.setVariable("time",ctx.time())
                    ctx.setVariable("world",ctx.world())
                    ctx.setTime(BEGINNING_OF_TIME)
                    ctx.setWorld(0L)
                }
                .createNode()
                .setAttribute(ENTRY_POINT_NODE_NAME, Type.STRING, VOCABULARY_NODE_NAME)
                .addToGlobalIndex(ENTRY_POINT_INDEX, ENTRY_POINT_NODE_NAME)
                .thenDo { ctx ->
                    ctx.setTime(ctx.variable("time")[0] as Long)
                    ctx.setWorld(ctx.variable("world")[0] as Long)
                }
    }

    @JvmStatic
    fun retrieveVocabulary(): Task {
        return newTask()
                .readGlobalIndex(ENTRY_POINT_INDEX, ENTRY_POINT_NODE_NAME, VOCABULARY_NODE_NAME)
    }

    @JvmStatic
    fun getOrCreateTokensFromString(tokens: Array<String>): Task {
        return newTask()
                .then(retrieveVocabularyNode())
                .defineAsVar("Vocabulary")
                .inject(tokens)
                .flatMap(retrieveToken())
    }

    private fun retrieveToken(): Task {
        return newTask()
                .defineAsVar("token")
                .readVar("Vocabulary")
                .traverse(VOCABULARY_TOKEN_INDEX, TOKEN_NAME, "{{token}}")
                .then(ifEmptyThen(
                        createToken()
                ))
    }

    private fun createToken(): Task {
        return newTask()
                .thenDo { ctx ->
                    ctx.setVariable("time",ctx.time())
                    ctx.setVariable("world",ctx.world())
                    ctx.setTime(BEGINNING_OF_TIME)
                    ctx.setWorld(0L)
                }
                .createNode()
                .setAttribute(TOKEN_NAME, Type.STRING, "{{token}}")
                .defineAsVar("newToken")
                .readVar("Vocabulary")
                .addVarToRelation(VOCABULARY_TOKEN_INDEX, "newToken", TOKEN_NAME)
                .createNode()
                .thenDo {
                    ctx ->

                    val invertedIndex = ctx.resultAsNodes()[0]

                    val newToken = ctx.variable("newToken")[0] as Node

                    val egraph: EGraph = invertedIndex.getOrCreate(INVERTED_INDEX_NODE_II, Type.EGRAPH) as EGraph

                    val root = egraph.newNode()

                    root.set("name", Type.STRING, "root")

                    root.getOrCreate("node", Type.ERELATION)

                    egraph.setRoot(root)

                    //to check
                    invertedIndex.addToRelation(INVERTED_INDEX_WORD_RELATION, newToken)
                    newToken.addToRelation(WORD_INVERTED_INDEX_RELATION, invertedIndex)
                    ctx.continueTask()
                }
                .thenDo { ctx ->
                    ctx.setTime(ctx.variable("time")[0] as Long)
                    ctx.setWorld(ctx.variable("world")[0] as Long)
                }
                .readVar("newToken")
        //}

    }

}