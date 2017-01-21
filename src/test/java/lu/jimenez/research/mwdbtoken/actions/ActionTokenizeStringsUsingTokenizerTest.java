package lu.jimenez.research.mwdbtoken.actions;

import lu.jimenez.research.mwdbtoken.Constants;
import lu.jimenez.research.mwdbtoken.tokenization.tokenizer.Tokenizer;
import org.junit.jupiter.api.Test;
import org.mwg.task.ActionFunction;
import org.mwg.task.TaskContext;

import static lu.jimenez.research.mwdbtoken.actions.MwdbTokenActions.tokenizeStringsUsingTokenizer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mwg.task.Tasks.newTask;

public class ActionTokenizeStringsUsingTokenizerTest extends ActionTest {

    @Test
    public void testtypeOneString() {
        int counter = 1;
        final int[] i = {0};
        initGraph();
        newTask()
                .then(tokenizeStringsUsingTokenizer("default", null, "true", "my type", "This is a lovely String"))
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        assertEquals(ctx.result().size(), 1);
                        Tokenizer tokenizer = (Tokenizer) ctx.result().get(0);
                        assertEquals(tokenizer.countTokens(), 5);
                        assertEquals(tokenizer.getTypeOfToken(), "my type");
                        i[0]++;
                        ctx.continueTask();
                    }
                })
                //.addHook(new VerboseHook())
                .execute(graph, null);

        assertEquals(counter, i[0]);
        removeGraph();
    }

    @Test
    public void testtypeSeveralString() {
        int counter = 1;
        final int[] i = {0};
        initGraph();
        newTask()
                .then(tokenizeStringsUsingTokenizer("default", null, "true", "my type", "This is a lovely String", "my second type", "and This one is even lovelier", "my other type", "you don't say"))
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        assertEquals(ctx.result().size(), 3);
                        Tokenizer tokenizer = (Tokenizer) ctx.result().get(0);
                        assertEquals(tokenizer.countTokens(), 5);
                        assertEquals(tokenizer.getTypeOfToken(), "my type");
                        Tokenizer tokenizer2 = (Tokenizer) ctx.result().get(1);
                        assertEquals(tokenizer2.countTokens(), 6);
                        assertEquals(tokenizer2.getTypeOfToken(), "my second type");
                        Tokenizer tokenizer3 = (Tokenizer) ctx.result().get(2);
                        assertEquals(tokenizer3.countTokens(), 3);
                        assertEquals(tokenizer3.getTypeOfToken(), "my other type");
                        i[0]++;
                        ctx.continueTask();
                    }
                })
                //.addHook(new VerboseHook())
                .execute(graph, null);

        assertEquals(counter, i[0]);
        removeGraph();
    }


    @Test
    public void testnotypeOneString() {
        int counter = 1;
        final int[] i = {0};
        initGraph();
        newTask()
                .then(tokenizeStringsUsingTokenizer("default", null, "false", "This is a lovely String"))
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        assertEquals(ctx.result().size(), 1);
                        Tokenizer tokenizer = (Tokenizer) ctx.result().get(0);
                        assertEquals(tokenizer.getTypeOfToken(), Constants.NO_TYPE_TOKENIZE);
                        i[0]++;
                        ctx.continueTask();
                    }
                })
                //.addHook(new VerboseHook())
                .execute(graph, null);

        assertEquals(counter, i[0]);
        removeGraph();
    }

    @Test
    public void testnotypeSeveralString() {
        int counter = 1;
        final int[] i = {0};
        initGraph();
        newTask()
                .then(tokenizeStringsUsingTokenizer("default", null, "false", "This is a lovely String", "and This one is even lovelier", "you don't say"))
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        assertEquals(ctx.result().size(), 3);
                        Tokenizer tokenizer = (Tokenizer) ctx.result().get(2);
                        assertEquals(tokenizer.getTypeOfToken(), Constants.NO_TYPE_TOKENIZE);
                        i[0]++;
                        ctx.continueTask();
                    }
                })
                //.addHook(new VerboseHook())
                .execute(graph, null);

        assertEquals(counter, i[0]);
        removeGraph();
    }

}