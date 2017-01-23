package lu.jimenez.research.mwdbtoken.core.actions;

import lu.jimenez.research.mwdbtoken.core.task.RelationTask;
import lu.jimenez.research.mwdbtoken.core.task.TaskTest;
import lu.jimenez.research.mwdbtoken.tokenization.TokenizerFactory;
import lu.jimenez.research.mwdbtoken.tokenization.tokenizer.Tokenizer;
import org.junit.jupiter.api.Test;
import org.mwg.task.ActionFunction;
import org.mwg.task.TaskContext;

import static lu.jimenez.research.mwdbtoken.core.Constants.ENTRY_POINT_INDEX;
import static lu.jimenez.research.mwdbtoken.core.actions.MwdbTokenActions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mwg.task.Tasks.newTask;

class ActionRebuildingTokenizeContentTest extends TaskTest {
    public static String text1 = "the apple was looking over the cloud";
    public static String text2 = "an orange was riding a skateboard";


    @Test
    public void test() {
        initGraph();
        final int[] counter = {0};
        TokenizerFactory tf = new TokenizerFactory("");
        final Tokenizer tokenizer = tf.create(text1, null);


        newTask()
                .travelInTime("0")
                .then(initializeVocabulary())
                .then(tokenizeStringsUsingTokenizer("default", null, "true", "my type", text1))
                .defineAsVar("tokenizer")
                .readGlobalIndex(ENTRY_POINT_INDEX, "name", "root")
                .defineAsVar("nodevar")
                .pipe(RelationTask.updateOrCreateTokenizeRelationsToNodes("tokenizer", "nodevar", new String[]{"text1"}))
                .traverse("tokenizedContents")
                .defineAsVar("tokenizedContents")
                .then(rebuildingTokenizedContents("tokenizedContents"))
                .flat()
                .thenDo(new ActionFunction() {
                            @Override
                            public void eval(TaskContext ctx) {
                                assertEquals(3, ctx.result().size());
                                assertEquals("text1", ctx.result().get(0));
                                assertEquals("my type", ctx.result().get(1));
                                assertEquals(text1, ctx.result().get(2));
                                counter[0]++;
                                ctx.continueTask();
                            }
                        }
                )
                .execute(graph, null);
        assertEquals(1, counter[0]);
        removeGraph();
    }




}