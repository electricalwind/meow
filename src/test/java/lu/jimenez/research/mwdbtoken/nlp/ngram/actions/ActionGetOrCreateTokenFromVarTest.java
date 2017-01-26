package lu.jimenez.research.mwdbtoken.nlp.ngram.actions;

import lu.jimenez.research.mwdbtoken.nlp.ActionTest;
import org.junit.jupiter.api.Test;
import org.mwg.Node;
import org.mwg.task.ActionFunction;
import org.mwg.task.TaskContext;

import static lu.jimenez.research.mwdbtoken.core.CoreConstants.TOKEN_NAME;
import static lu.jimenez.research.mwdbtoken.core.actions.MwdbTokenActions.getOrCreateTokensFromString;
import static lu.jimenez.research.mwdbtoken.core.actions.MwdbTokenActions.initializeVocabulary;
import static lu.jimenez.research.mwdbtoken.nlp.ngram.actions.MwdbNgramActions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mwg.task.Tasks.newTask;

class ActionGetOrCreateTokenFromVarTest extends ActionTest {

    @Test
    public void test() {
        initGraph();
        final int[] counter = {0};

        newTask()
                .then(initializeVocabulary())
                .then(initializeNgram())
                .then(getOrCreateTokensFromString("This"))
                .defineAsVar("token")
                .then(getOrCreateNgramFromVar("token"))
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        assertEquals(1, ctx.resultAsNodes().get(0).get("order"));
                        counter[0]++;
                        ctx.continueTask();
                    }
                })
                .defineAsVar("myNgram")
                .traverse("gram")
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        assertEquals("This", ctx.resultAsNodes().get(0).get(TOKEN_NAME));
                        counter[0]++;
                        ctx.continueTask();
                    }
                })
                .then(getOrCreateNgramFromString("This"))
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        assertEquals(((Node) ctx.variable("myNgram").get(0)).id(), ctx.resultAsNodes().get(0).id());
                        counter[0]++;
                        ctx.continueTask();
                    }
                })

                .execute(graph, null);
        assertEquals(3, counter[0]);
        removeGraph();
    }

    @Test
    public void test2() {
        initGraph();
        final int[] counter = {0};
        newTask()
                .then(initializeVocabulary())
                .then(initializeNgram())
                .then(getOrCreateTokensFromString("This", "is", "me"))
                .defineAsVar("token")
                .then(getOrCreateNgramFromVar("token"))
                .defineAsVar("myNgram")
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        assertEquals(3, ctx.resultAsNodes().get(0).get("order"));
                        counter[0]++;
                        ctx.continueTask();
                    }
                })
                .traverse("gram")
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        assertEquals("This", ctx.resultAsNodes().get(0).get(TOKEN_NAME));
                        assertEquals("is", ctx.resultAsNodes().get(1).get(TOKEN_NAME));
                        assertEquals("me", ctx.resultAsNodes().get(2).get(TOKEN_NAME));
                        counter[0]++;
                        ctx.continueTask();
                    }

                })
                .readVar("myNgram")
                .traverse("history")
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        assertEquals(2, ctx.resultAsNodes().get(0).get("order"));
                        counter[0]++;
                        ctx.continueTask();
                    }
                })
                .traverse("gram")
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        assertEquals("This", ctx.resultAsNodes().get(0).get(TOKEN_NAME));
                        assertEquals("is", ctx.resultAsNodes().get(1).get(TOKEN_NAME));
                        counter[0]++;
                        ctx.continueTask();
                    }

                })
                .readVar("myNgram")
                .traverse("backOff")
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        assertEquals(2, ctx.resultAsNodes().get(0).get("order"));
                        counter[0]++;
                        ctx.continueTask();
                    }
                })
                .defineAsVar("backoffngram")
                .traverse("gram")
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        assertEquals("is", ctx.resultAsNodes().get(0).get(TOKEN_NAME));
                        assertEquals("me", ctx.resultAsNodes().get(1).get(TOKEN_NAME));
                        counter[0]++;
                        ctx.continueTask();
                    }

                })
                .then(getOrCreateNgramFromString("is", "me"))
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        assertEquals(((Node) ctx.variable("backoffngram").get(0)).id(), ctx.resultAsNodes().get(0).id());
                        counter[0]++;
                        ctx.continueTask();
                    }
                })
                .execute(graph, null);
        assertEquals(7, counter[0]);
        removeGraph();
    }

}