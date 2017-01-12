package lu.jimenez.research.mwdbtoken.actions;

import lu.jimenez.research.mwdbtoken.task.VocabularyTask;
import org.junit.jupiter.api.Test;
import org.mwg.Node;
import org.mwg.task.ActionFunction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

import static lu.jimenez.research.mwdbtoken.Constants.TOKEN_NAME;
import static lu.jimenez.research.mwdbtoken.actions.MwdbTokenActions.initializeVocabulary;
import static org.junit.Assert.assertEquals;
import static org.mwg.core.task.Actions.newTask;

public class ActionGetOrCreateTokensFromStringTest extends ActionTest{

    @Test
    public void createOneTokennoInit(){
        initGraph();
        newTask()
                .mapReduce(VocabularyTask.getOrCreateTokensFromString(new String[]{"Token"}))
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        assert(false);
                    }
                })
                .execute(graph,null);
    }

    @Test
    public void createOneToken(){
        initGraph();
        newTask()
                .then(initializeVocabulary())
                .mapReduce(VocabularyTask.getOrCreateTokensFromString(new String[]{"Token"}))
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        TaskResult<Node> tok = ctx.resultAsNodes();
                        assertEquals(1,tok.size());
                        Node n = tok.get(0);
                        assertEquals("Token",n.get(TOKEN_NAME));
                    }
                });
    }

    @Test
    public void createSeveralTokens(){

        initGraph();

    }

    @Test
    public void retrieveOneAlreadyExistingToken(){
        initGraph();
    }

    @Test
    public void retrieveSeveralAlreadyExistingToken(){
        initGraph();
    }

    @Test
    public void mix(){
        initGraph();
    }

}