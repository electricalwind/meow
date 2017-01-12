package lu.jimenez.research.mwdbtoken.task;

import org.junit.jupiter.api.Test;

import static lu.jimenez.research.mwdbtoken.actions.MwdbTokenActions.tokenizeStringsUsingTokenizer;
import static org.mwg.core.task.Actions.newTask;

/**
 * Test are as follows
 *
 *  Tokenizer == 1
 *  |
 *  |_ Yes (spreadingATokenizerToNodes)
 *      relation list size equal to one ?
 *      |
 *      |_Yes -> test 1 (spreading a tokenizer to several nodes using the same relation name)
 *      |
 *      |_No
 *        Is the number of relation equal to the number of Node
 *        |
 *        |_Yes -> test 2 (spreading a tokenizer to several nodes using different relation names each time
 *        |
 *        |_ No -> test 3 exception
 *  |
 *  |_No
 *     Is the number of node equal to one?
 *     |
 *     |_Yes (updating or creating SeveralTokenizerToANode)
 *        Is the number of tokenizer equal to the number of relation
 *        |
 *        |_Yes -> test 4 (adding/updating several tokenize relation to a node
 *        |
 *        |_No -> test 5 exception
 *     |
 *     |_ No (updating or creating SeveralTokenizerToSeveralNodes)
 *        Is the number of tokenizer equal to the number of relation
 *        |
 *        |_Yes -> test 6
 *        |
 *        |_No -> test 7 exception
 *
 */
public class RelationTasksTest extends TaskTest{

    public static String text1="an apple was looking over the cloud";
    public static String text2="an orange was riding a skateboard";
    public static String text3="this may have no sense";
    public static String text4="but it is far enough for testing purposes";

    @Test
    public void testcreation1WithType(){
        initGraph();
        newTask()
                .travelInTime("0")
                .then(tokenizeStringsUsingTokenizer("default", null, "true", "my type", text1))
                .defineAsVar("tokenizer")
                .print("{{result}}")
                .readGlobalIndex("roots")
                .defineAsVar("nodevar")
                .print("{{result}}")
                .flatMap(RelationTask.updateOrCreateTokenizeRelationsToNodes("tokenizer","nodevar", new String[]{"text1"}))
                .print("{{result}}")
                //.addHook(VerboseHook())
                .execute(graph,null);
        assert(true);
    }
}
