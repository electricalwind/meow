package lu.jimenez.research.mwdbtoken.nlp.corpus.actions;

import lu.jimenez.research.mwdbtoken.core.task.RelationTask;
import lu.jimenez.research.mwdbtoken.nlp.ActionTest;
import org.junit.jupiter.api.Test;
import org.mwg.task.ActionFunction;
import org.mwg.task.Task;
import org.mwg.task.TaskContext;

import static lu.jimenez.research.mwdbtoken.core.CoreConstants.ENTRY_POINT_INDEX;
import static lu.jimenez.research.mwdbtoken.core.CoreConstants.TOKENIZE_CONTENT_NAME;
import static lu.jimenez.research.mwdbtoken.core.CoreConstants.TOKENIZE_CONTENT_RELATION;
import static lu.jimenez.research.mwdbtoken.core.actions.MwdbTokenActions.initializeVocabulary;
import static lu.jimenez.research.mwdbtoken.core.actions.MwdbTokenActions.tokenizeStringsUsingTokenizer;
import static lu.jimenez.research.mwdbtoken.nlp.corpus.CorpusConstants.CORPUS_TO_TOKENIZEDCONTENTS_RELATION;
import static lu.jimenez.research.mwdbtoken.nlp.corpus.actions.MwdbCorpusActions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mwg.task.Tasks.newTask;

class ActionAddRemoveTokenizeContentOfCorpusTest extends ActionTest {

    public static String text1 = "the apple was looking over the cloud";
    public static String text11 = "an ordinary apple was looking at a cloud";
    @Test
    public void add() {
        initGraph();
        final int[] counter = {0};
        newTask()
                .pipe(createTokenizeContent())
                .travelInTime("2")
                .traverse(TOKENIZE_CONTENT_RELATION)
                .defineAsVar("tokenizeContent")
                .then(initializeCorpus())
                .then(addRemoveTokenizeContentsOfCorpus(true,"tokenizeContent","myCorpus"))
                .then(getOrCreateCorpus("myCorpus"))
                .traverse(CORPUS_TO_TOKENIZEDCONTENTS_RELATION)
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        assertEquals(1,ctx.result().size());
                        assertEquals("text1",ctx.resultAsNodes().get(0).get(TOKENIZE_CONTENT_NAME));
                        counter[0]++;
                        ctx.continueTask();
                    }
                })
                .then(addRemoveTokenizeContentsOfCorpus(true,"tokenizeContent","myCorpus"))
                .then(getOrCreateCorpus("myCorpus"))
                .traverse(CORPUS_TO_TOKENIZEDCONTENTS_RELATION)
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        assertEquals(1,ctx.result().size());
                        assertEquals("text1",ctx.resultAsNodes().get(0).get(TOKENIZE_CONTENT_NAME));
                        counter[0]++;
                        ctx.continueTask();
                    }
                })
                .execute(graph,null);
        assertEquals(2,counter[0]);
        removeGraph();
    }


    @Test
    public void remove() {
        initGraph();
        final int[] counter = {0};
        newTask()
                .pipe(createTokenizeContent())
                .travelInTime("2")
                .traverse(TOKENIZE_CONTENT_RELATION)
                .defineAsVar("tokenizeContent")
                .then(initializeCorpus())
                .then(addRemoveTokenizeContentsOfCorpus(true,"tokenizeContent","myCorpus"))
                .then(addRemoveTokenizeContentsOfCorpus(false,"tokenizeContent","myCorpus"))
                .then(getOrCreateCorpus("myCorpus"))
                .traverse(CORPUS_TO_TOKENIZEDCONTENTS_RELATION)
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        assertEquals(0,ctx.result().size());
                        counter[0]++;
                        ctx.continueTask();
                    }
                })
                .then(addRemoveTokenizeContentsOfCorpus(false,"tokenizeContent","myCorpus"))
                .execute(graph,null);
        assertEquals(1,counter[0]);
        removeGraph();
    }



    private Task createTokenizeContent(){
        return newTask()
                .travelInTime("0")
                .then(initializeVocabulary())
                .then(tokenizeStringsUsingTokenizer("default", null, "true", "my type", text1))
                .defineAsVar("tokenizer")
                .readGlobalIndex(ENTRY_POINT_INDEX, "name", "root")
                .defineAsVar("nodevar")
                .pipe(RelationTask.updateOrCreateTokenizeRelationsToNodes("tokenizer", "nodevar", new String[]{"text1"}))
                .traverse("tokenizedContents")
                .travelInTime("1")
                .then(tokenizeStringsUsingTokenizer("default", null, "true", "my type", text11))
                .defineAsVar("tokenizer")
                .readGlobalIndex(ENTRY_POINT_INDEX, "name", "root")
                .defineAsVar("nodevar")
                .pipe(RelationTask.updateOrCreateTokenizeRelationsToNodes("tokenizer", "nodevar", new String[]{"text1"}));
    }

}