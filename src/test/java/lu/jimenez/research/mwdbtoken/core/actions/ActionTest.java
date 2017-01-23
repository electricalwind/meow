package lu.jimenez.research.mwdbtoken.core.actions;

import lu.jimenez.research.mylittleplugin.MyLittleActionPlugin;
import org.mwg.*;
import org.mwg.internal.scheduler.NoopScheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mwg.Constants.BEGINNING_OF_TIME;

public abstract class ActionTest {

    protected Graph graph;
    protected long startMemory;

    protected void initGraph() {
        graph = new GraphBuilder()
                .withPlugin(new MwdbTokenActionPlugin())
                .withPlugin(new MyLittleActionPlugin())
                .withScheduler(new NoopScheduler()).build();
        final ActionTest selfPointer = this;
        graph.connect(new Callback<Boolean>() {

            public void on(Boolean result) {

                //create graph nodes
                final Node n0 = selfPointer.graph.newNode(0, 0);
                n0.set("name", Type.STRING, "n0");
                n0.set("value", Type.INT, 8);

                final Node n1 = selfPointer.graph.newNode(0, 0);
                n1.set("name", Type.STRING, "n1");
                n1.set("value", Type.INT, 3);

                final Node root = selfPointer.graph.newNode(0, 0);
                root.set("name", Type.STRING, "root");
                root.addToRelation("children", n0);
                root.addToRelation("children", n1);

                //create some index
                selfPointer.graph.index(0, BEGINNING_OF_TIME, "roots", new Callback<NodeIndex>() {
                    public void on(NodeIndex rootsIndex) {
                        rootsIndex.addToIndex(root, "name");
                    }
                });
                selfPointer.graph.index(0, BEGINNING_OF_TIME, "nodes", new Callback<NodeIndex>() {

                    public void on(NodeIndex nodesIndex) {
                        nodesIndex.addToIndex(n0, "name");
                        nodesIndex.addToIndex(n1, "name");
                        nodesIndex.addToIndex(root, "name");
                    }
                });
            }
        });
    }

    protected void removeGraph() {
        graph.disconnect(new Callback<Boolean>() {
            public void on(Boolean result) {
                assertEquals(true, result);
            }
        });
    }
}
