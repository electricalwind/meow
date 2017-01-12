package lu.jimenez.research.mwdbtoken.task;

import lu.jimenez.research.mwdbtoken.actions.MwdbTokenActionPlugin;
import lu.jimenez.research.mylittleplugin.MyLittleActionPlugin;
import org.mwg.*;
import org.mwg.core.scheduler.NoopScheduler;

import static lu.jimenez.research.mwdbtoken.actions.MwdbTokenActions.initializeVocabulary;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mwg.core.task.Actions.newTask;

public abstract  class TaskTest {

    protected Graph graph;


    protected void initGraph() {
        graph = new GraphBuilder()
                .withPlugin(new MwdbTokenActionPlugin())
                .withPlugin(new MyLittleActionPlugin())
                .withScheduler(new NoopScheduler()).build();
        final TaskTest selfPointer = this;
        graph.connect(new Callback<Boolean>() {

            public void on(Boolean result) {
                final Node root = selfPointer.graph.newNode(0, 0);
                root.set("name", Type.STRING, "root");
                selfPointer.graph.index(0, 0, "roots", new Callback<NodeIndex>() {
                    public void on(NodeIndex rootsIndex) {
                        rootsIndex.addToIndex(root, "name");
                    }
                });
            }
        });
        newTask()
                .then(initializeVocabulary())
                .execute(graph,null);
    }

    protected void removeGraph() {
        graph.disconnect(new Callback<Boolean>() {
            public void on(Boolean result) {
                assertEquals(true, result);
            }
        });
    }
}
