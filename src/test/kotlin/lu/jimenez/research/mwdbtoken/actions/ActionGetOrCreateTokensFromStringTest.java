package lu.jimenez.research.mwdbtoken.actions;

import org.junit.jupiter.api.Test;
import org.mwg.Node;
import org.mwg.task.ActionFunction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

import static lu.jimenez.research.mwdbtoken.Constants.TOKEN_NAME;
import static lu.jimenez.research.mwdbtoken.actions.MwdbTokenActions.getOrCreateTokensFromString;
import static lu.jimenez.research.mwdbtoken.actions.MwdbTokenActions.initializeVocabulary;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mwg.task.Tasks.newTask;

public class ActionGetOrCreateTokensFromStringTest extends ActionTest {

    @Test
    public void createOneTokennoInit() {
        int counter = 0;
        final int[] i = {0};
        initGraph();
        newTask()
                .then(getOrCreateTokensFromString("Token"))
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        i[0]++;
                        ctx.continueTask();
                    }
                })
                .execute(graph, null);

        assertEquals(counter,i[0]);
        removeGraph();
    }

    @Test
    public void createOneToken() {
        int counter = 1;
        final int[] i = {0};
        initGraph();
        newTask()
                .then(initializeVocabulary())
                .then(getOrCreateTokensFromString("Token7"))
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        TaskResult<Node> tok = ctx.resultAsNodes();
                        assertEquals(1, tok.size());
                        Node n = tok.get(0);
                        assertEquals("Token7", n.get(TOKEN_NAME));
                        i[0]++;
                        ctx.continueTask();
                    }
                })
                //.addHook(new VerboseHook())
                .execute(graph, null);
        assertEquals(counter,i[0]);
        removeGraph();
    }

    @Test
    public void createSeveralTokens() {
        int counter = 1;
        final int[] i = {0};
        initGraph();

        newTask()
                .then(initializeVocabulary())
                .then(getOrCreateTokensFromString("Token","Token2","Token3","Token4"))
                .println("{{result}}")
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        TaskResult<Node> tok = ctx.resultAsNodes();
                        assertEquals(4, tok.size());
                        Node n = tok.get(0);
                        assertEquals("Token", n.get(TOKEN_NAME));
                        Node n1 = tok.get(1);
                        assertEquals("Token2", n1.get(TOKEN_NAME));
                        Node n2 = tok.get(2);
                        assertEquals("Token3", n2.get(TOKEN_NAME));
                        Node n3 = tok.get(3);
                        assertEquals("Token4", n3.get(TOKEN_NAME));
                        i[0]++;
                        ctx.continueTask();
                    }
                })
                //.addHook(new VerboseHook())
                .execute(graph, null);
        assertEquals(counter,i[0]);
        removeGraph();
    }

    @Test
    public void retrieveOneAlreadyExistingToken() {
        int counter = 2;
        final int[] i = {0};
        initGraph();
        newTask()
                .then(initializeVocabulary())
                .then(getOrCreateTokensFromString("Token4"))
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        i[0]++;
                        ctx.continueWith(ctx.wrap(ctx.resultAsNodes().get(0).id()));
                    }
                })
                .defineAsVar("id")
                .then(getOrCreateTokensFromString("Token4"))
                .println("{{result}}")
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        i[0]++;
                       assertEquals(ctx.variable("id").get(0),ctx.resultAsNodes().get(0).id());
                       ctx.continueTask();
                    }
                })
                //.addHook(new VerboseHook())
                .execute(graph, null);
        assertEquals(counter,i[0]);
        removeGraph();
    }

    @Test
    public void retrieveSeveralAlreadyExistingToken() {
        int counter = 2;
        final int[] i = {0};
        initGraph();
        newTask()
                .then(initializeVocabulary())
                .then(getOrCreateTokensFromString("Token","Token2","Token3","Token4"))
                //.println("{{result}}")
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        Long[] ids = new Long[ctx.resultAsNodes().size()];
                        i[0]++;
                        TaskResult<Node> nodes= ctx.resultAsNodes();
                        int size = nodes.size();
                        for(int i =0; i<size;i++){
                            ids[i]= nodes.get(i).id();
                        }
                        ctx.continueWith(ctx.wrap(ids));
                    }
                })
                .defineAsVar("ids")
                .then(getOrCreateTokensFromString("Token","Token2","Token3","Token4"))
                //.println("{{result}}")
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        assertEquals(ctx.variable("ids").get(0),ctx.resultAsNodes().get(0).id());
                        assertEquals(ctx.variable("ids").get(1),ctx.resultAsNodes().get(1).id());
                        assertEquals(ctx.variable("ids").get(2),ctx.resultAsNodes().get(2).id());
                        assertEquals(ctx.variable("ids").get(3),ctx.resultAsNodes().get(3).id());
                        i[0]++;
                        ctx.continueTask();
                    }
                })
                //.addHook(new VerboseHook())
                .execute(graph, null);
        assertEquals(counter,i[0]);
        removeGraph();
    }

    @Test
    public void mix() {
        int counter = 2;
        final int[] i = {0};
        initGraph();
        newTask()
                .then(initializeVocabulary())
                .then(getOrCreateTokensFromString("Token","Token2","Token3","Token4"))
                //.println("{{result}}")
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        Long[] ids = new Long[ctx.resultAsNodes().size()];

                        TaskResult<Node> nodes= ctx.resultAsNodes();
                        int size = nodes.size();
                        for(int i =0; i<size;i++){
                            ids[i]= nodes.get(i).id();
                        }
                        i[0]++;
                        ctx.continueWith(ctx.wrap(ids));
                    }
                })
                .defineAsVar("ids")
                .then(getOrCreateTokensFromString("Token","Token5","Token3","Token7"))
                //.println("{{result}}")
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        i[0]++;
                        assertEquals(ctx.variable("ids").get(0),ctx.resultAsNodes().get(0).id());
                        assertNotEquals(ctx.variable("ids").get(1),ctx.resultAsNodes().get(1).id());
                        assertEquals(ctx.variable("ids").get(2),ctx.resultAsNodes().get(2).id());
                        assertNotEquals(ctx.variable("ids").get(3),ctx.resultAsNodes().get(3).id());
                        ctx.continueTask();
                    }
                })
                //.addHook(new VerboseHook())
                .execute(graph, null);
        assertEquals(counter,i[0]);
        removeGraph();
    }

}