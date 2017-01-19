package lu.jimenez.research.mwdbtoken.task;

import lu.jimenez.research.mwdbtoken.tokenization.TokenizerFactory;
import lu.jimenez.research.mwdbtoken.tokenization.tokenizer.Tokenizer;
import org.junit.jupiter.api.Test;
import org.mwg.Callback;
import org.mwg.Node;
import org.mwg.Type;
import org.mwg.task.ActionFunction;
import org.mwg.task.TaskContext;

import java.util.stream.IntStream;

import static lu.jimenez.research.mwdbtoken.Constants.ENTRY_POINT_INDEX;
import static lu.jimenez.research.mwdbtoken.Constants.TOKENIZE_CONTENT_NAME;
import static lu.jimenez.research.mwdbtoken.actions.MwdbTokenActions.initializeVocabulary;
import static lu.jimenez.research.mwdbtoken.actions.MwdbTokenActions.tokenizeStringsUsingTokenizer;
import static org.junit.Assert.assertEquals;
import static org.mwg.task.Tasks.newTask;

/**
 * Test are as follows
 * <p>
 * Tokenizer == 1
 * .|
 * .|_ Yes (spreadingATokenizerToNodes)
 * ...relation list size equal to one ?
 * ...|
 * ...|_Yes -> test 1 (spreading a tokenizer to several nodes using the same relation name)
 * ...|
 * ...|_No
 * .....Is the number of relation equal to the number of Node
 * .....|
 * .....|_Yes -> test 2 (spreading a tokenizer to several nodes using different relation names each time
 * .....|
 * .....|_ No -> test 3 exception
 * ...|
 * .|_No
 * ...Is the number of node equal to one?
 * ...|
 * ...|_Yes (updating or creating SeveralTokenizerToANode)
 * .....Is the number of tokenizer equal to the number of relation
 * .....|
 * .....|_Yes -> test 4 (adding/updating several tokenize relation to a node
 * .....|
 * .....|_No -> test 5 exception
 * ...|
 * ...|_ No (updating or creating SeveralTokenizerToSeveralNodes)
 * .....Is the number of tokenizer equal to the number of relation
 * .....|
 * .....|_Yes -> test 6
 * .....|
 * .....|_No -> test 7 exception
 */
public class RelationTasksTest extends TaskTest {

    public static String text1 = "the apple was looking over the cloud";
    public static String text2 = "an orange was riding a skateboard";
    public static String text3 = "this may have no sense";
    public static String text4 = "but it is far enough for testing purposes";

    @Test
    public void testcreation1WithType() {
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
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        assertEquals(1, ctx.resultAsNodes().size());
                        assertEquals("text1", ctx.resultAsNodes().get(0).get(TOKENIZE_CONTENT_NAME));
                        assertEquals("my type", ctx.resultAsNodes().get(0).get("type"));
                        ctx.resultAsNodes().get(0).relation("tokens", new Callback<Node[]>() {
                            public void on(Node[] result) {
                                assertEquals(7, result.length);
                            }
                        });
                        counter[0]++;
                        ctx.continueTask();
                    }
                })
                .traverse("tokens")
                .forEach(
                        newTask()
                                .thenDo(new ActionFunction() {
                                    public void eval(TaskContext ctx) {
                                        assertEquals(tokenizer.nextToken(), ctx.resultAsNodes().get(0).get("name"));
                                        counter[0]++;
                                        ctx.continueTask();
                                    }
                                })
                                .traverse("invertedIndex")
                                .thenDo(new ActionFunction() {
                                    public void eval(TaskContext ctx) {
                                        assertEquals("my type", ctx.resultAsNodes().get(0).get("type"));
                                        int i = (Integer) ctx.variable("i").get(0);
                                        System.out.println(i);
                                        assert (IntStream.of((int[]) ctx.resultAsNodes().get(0).get("position")).anyMatch(x -> x == i));
                                        counter[0]++;
                                        ctx.continueTask();
                                    }
                                })
                )
                //.flat()
                //.addHook(VerboseHook())
                .execute(graph, null);
        assertEquals(15, counter[0]);
        removeGraph();
    }


    @Test
    public void testcreation2WithType() {
        initGraph();
        final int[] counter = {0};
        TokenizerFactory tf = new TokenizerFactory("");
        final Tokenizer tokenizer = tf.create(text1, null);


        newTask()
                .travelInTime("0")
                .then(initializeVocabulary())
                .then(tokenizeStringsUsingTokenizer("default", null, "true", "my type", text1))
                .defineAsVar("tokenizer")
                .declareVar("nodevar")
                .loop("0", "2",
                        newTask()
                                .createNode()
                                .setAttribute("name", Type.STRING, "{{i}}")
                                .addToVar("nodevar")
                )


                .pipe(RelationTask.updateOrCreateTokenizeRelationsToNodes("tokenizer", "nodevar", new String[]{"text0", "text1", "text2"}))
                .traverse("tokenizedContents")
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        assertEquals(3, ctx.resultAsNodes().size());
                        assertEquals("text0", ctx.resultAsNodes().get(0).get(TOKENIZE_CONTENT_NAME));
                        assertEquals("text1", ctx.resultAsNodes().get(1).get(TOKENIZE_CONTENT_NAME));
                        assertEquals("text2", ctx.resultAsNodes().get(2).get(TOKENIZE_CONTENT_NAME));
                        assertEquals("my type", ctx.resultAsNodes().get(0).get("type"));
                        ctx.resultAsNodes().get(1).relation("tokens", new Callback<Node[]>() {
                            public void on(Node[] result) {
                                assertEquals(7, result.length);
                            }
                        });
                        counter[0]++;
                        ctx.continueTask();
                    }
                })
                .traverse("tokens")
                .forEach(
                        newTask()
                                .thenDo(new ActionFunction() {
                                    public void eval(TaskContext ctx) {
                                        assertEquals(
                                                tokenizer
                                                        .getTokens()
                                                        .get((int) ctx.variable("i").get(0) % 7), ctx.resultAsNodes().get(0).get("name"));
                                        counter[0]++;
                                        ctx.continueTask();
                                    }
                                })
                                .traverse("invertedIndex")
                                .thenDo(new ActionFunction() {
                                    public void eval(TaskContext ctx) {
                                        assertEquals("my type", ctx.resultAsNodes().get(0).get("type"));
                                        int i = (Integer) ctx.variable("i").get(0) % 7;
                                        System.out.println(i);
                                        assert (IntStream.of((int[]) ctx.resultAsNodes().get(0).get("position")).anyMatch(x -> x == i));
                                        counter[0]++;
                                        ctx.continueTask();
                                    }
                                })
                )
                //.flat()
                //.addHook(VerboseHook())
                .execute(graph, null);
        assertEquals(43, counter[0]);
        removeGraph();
    }

    @Test
    public void testcreation3WithType() {
        initGraph();
        final int[] counter = {0};
        TokenizerFactory tf = new TokenizerFactory("");
        final Tokenizer tokenizer = tf.create(text1, null);


        newTask()
                .travelInTime("0")
                .then(initializeVocabulary())
                .then(tokenizeStringsUsingTokenizer("default", null, "true", "my type", text1))
                .defineAsVar("tokenizer")
                .declareVar("nodevar")
                .loop("0", "2",
                        newTask()
                                .createNode()
                                .setAttribute("name", Type.STRING, "{{i}}")
                                .addToVar("nodevar")
                )


                .pipe(RelationTask.updateOrCreateTokenizeRelationsToNodes("tokenizer", "nodevar", new String[]{"text0", "text1"}))
                .thenDo(ctx -> {
                            assert (false);
                            counter[0]++;
                            ctx.continueTask();
                        }

                ).execute(graph, null);
        assertEquals(0, counter[0]);
    }

    @Test
    public void testcreation4WithType() {
        initGraph();
        final int[] counter = {0};
        TokenizerFactory tf = new TokenizerFactory("");
        final Tokenizer tokenizer = tf.create(text1, null);
        final Tokenizer tokenizer2 = tf.create(text2, null);

        newTask()
                .travelInTime("0")
                .then(initializeVocabulary())
                .then(tokenizeStringsUsingTokenizer("default", null, "true", "my type", text1))
                .defineAsVar("tokenizer")
                .then(tokenizeStringsUsingTokenizer("default", null, "true", "my type", text2))
                .addToVar("tokenizer")
                .readGlobalIndex(ENTRY_POINT_INDEX, "name", "root")
                .defineAsVar("nodevar")
                .pipe(RelationTask.updateOrCreateTokenizeRelationsToNodes("tokenizer", "nodevar", new String[]{"text1","text2"}))
                .println("{{result}}")
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        ctx.resultAsNodes().get(0).relation("tokenizedContents", new Callback<Node[]>() {
                            public void on(Node[] result) {
                                assertEquals(2, result.length);
                            }
                        });
                        counter[0]++;
                        ctx.continueTask();
                    }
                })
                .defineAsVar("res")
                .traverse("tokenizedContents",TOKENIZE_CONTENT_NAME,"text1")
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        assertEquals("text1", ctx.resultAsNodes().get(0).get(TOKENIZE_CONTENT_NAME));
                        assertEquals("my type", ctx.resultAsNodes().get(0).get("type"));
                        ctx.resultAsNodes().get(0).relation("tokens", new Callback<Node[]>() {
                            public void on(Node[] result) {
                                assertEquals(7, result.length);
                            }
                        });
                        counter[0]++;
                        ctx.continueTask();
                    }
                })
                .readVar("res")
                .traverse("tokenizedContents",TOKENIZE_CONTENT_NAME,"text2")
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        assertEquals("text2", ctx.resultAsNodes().get(0).get(TOKENIZE_CONTENT_NAME));
                        assertEquals("my type", ctx.resultAsNodes().get(0).get("type"));
                        ctx.resultAsNodes().get(0).relation("tokens", new Callback<Node[]>() {
                            public void on(Node[] result) {
                                assertEquals(6, result.length);
                            }
                        });
                        counter[0]++;
                        ctx.continueTask();
                    }
                })
                .traverse("tokens")
                .forEach(
                        newTask()
                                .thenDo(new ActionFunction() {
                                    public void eval(TaskContext ctx) {
                                        assertEquals(tokenizer2.nextToken(), ctx.resultAsNodes().get(0).get("name"));
                                        counter[0]++;
                                        ctx.continueTask();
                                    }
                                })
                                .traverse("invertedIndex")
                                .thenDo(new ActionFunction() {
                                    public void eval(TaskContext ctx) {
                                        assertEquals("my type", ctx.resultAsNodes().get(0).get("type"));
                                        int i = (Integer) ctx.variable("i").get(0);
                                        System.out.println(i);
                                        assert (IntStream.of((int[]) ctx.resultAsNodes().get(0).get("position")).anyMatch(x -> x == i));
                                        counter[0]++;
                                        ctx.continueTask();
                                    }
                                })
                )
                //.flat()
                //.addHook(VerboseHook())
                .execute(graph, null);
        assertEquals(15, counter[0]);
        removeGraph();
    }

    @Test
    public void testcreation5WithType() {

    }

    @Test
    public void testcreation6WithType() {

    }

    @Test
    public void testcreation7WithType() {

    }
}
