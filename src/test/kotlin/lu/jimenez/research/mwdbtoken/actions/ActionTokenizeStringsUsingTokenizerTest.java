package lu.jimenez.research.mwdbtoken.actions;

import lu.jimenez.research.mwdbtoken.Constants;
import lu.jimenez.research.mwdbtoken.tokenization.tokenizer.Tokenizer;
import org.junit.jupiter.api.Test;
import org.mwg.task.ActionFunction;
import org.mwg.task.TaskContext;
import org.mwg.utility.VerboseHook;

import static lu.jimenez.research.mwdbtoken.actions.MwdbTokenActions.tokenizeStringsUsingTokenizer;
import static org.junit.Assert.assertEquals;
import static org.mwg.core.task.Actions.newTask;

public class ActionTokenizeStringsUsingTokenizerTest extends ActionTest {

    @Test
    public void testtypeOneString() {
        initGraph();
        newTask()
                .then(tokenizeStringsUsingTokenizer("default", null, "true", "my type", "This is a lovely String"))
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        assertEquals(ctx.result().size(), 1);
                        Tokenizer tokenizer = (Tokenizer) ctx.result().get(0);
                        assertEquals(tokenizer.countTokens(), 5);
                        assertEquals(tokenizer.getTypeOfToken(), "my type");
                    }
                })
                .addHook(new VerboseHook())
                .execute(graph, null);
    }

    @Test
    public void testtypeSeveralString() {
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
                        assertEquals(tokenizer.countTokens(), 6);
                        assertEquals(tokenizer.getTypeOfToken(), "my second type");
                    }
                })
                .addHook(new VerboseHook())
                .execute(graph, null);
    }


    @Test
    public void testnotypeOneString() {
        initGraph();
        newTask()
                .then(tokenizeStringsUsingTokenizer("default", null, "false", "This is a lovely String"))
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        assertEquals(ctx.result().size(), 1);
                        Tokenizer tokenizer = (Tokenizer) ctx.result().get(0);
                        assertEquals(tokenizer.getTypeOfToken(), Constants.NO_TYPE_TOKENIZE);
                    }
                })
                .addHook(new VerboseHook())
                .execute(graph, null);
    }

    @Test
    public void testnotypeSeveralString() {
        initGraph();
        newTask()
                .then(tokenizeStringsUsingTokenizer("default", null, "false", "This is a lovely String", "and This one is even lovelier", "you don't say"))
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        assertEquals(ctx.result().size(), 3);
                        Tokenizer tokenizer = (Tokenizer) ctx.result().get(2);
                        assertEquals(tokenizer.getTypeOfToken(), Constants.NO_TYPE_TOKENIZE);
                    }
                })
                .addHook(new VerboseHook())
                .execute(graph, null);
    }

}