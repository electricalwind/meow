package lu.jimenez.research.mwdbtoken.nlp.ngram.actions;

import lu.jimenez.research.mwdbtoken.core.actions.MwdbTokenActionPlugin;
import lu.jimenez.research.mylittleplugin.MyLittleActionPlugin;
import org.mwg.*;
import org.mwg.internal.scheduler.NoopScheduler;
import org.mwg.internal.scheduler.TrampolineScheduler;
import org.mwg.memory.offheap.OffHeapMemoryPlugin;

import static lu.jimenez.research.mwdbtoken.core.CoreConstants.ENTRY_POINT_INDEX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mwg.Constants.BEGINNING_OF_TIME;

public class ActionTest {

    protected Graph graph;


    protected void initGraph() {
        graph = new GraphBuilder()
                .withPlugin(new MwdbTokenActionPlugin())
                .withPlugin(new MyLittleActionPlugin())
                .withPlugin(new MwdbNgramActionPlugin())
                .withScheduler(new TrampolineScheduler()).build();
        final ActionTest selfPointer = this;
        graph.connect(new Callback<Boolean>() {

            public void on(Boolean result) {
                final Node root = selfPointer.graph.newNode(0, BEGINNING_OF_TIME);
                root.set("name", Type.STRING, "root");
                selfPointer.graph.index(0, BEGINNING_OF_TIME, ENTRY_POINT_INDEX, new Callback<NodeIndex>() {
                    public void on(NodeIndex rootsIndex) {
                        rootsIndex.addToIndex(root, "name");
                    }
                });
            }
        });
    }

    protected void initGraphO() {
        graph = new GraphBuilder()
                .withMemorySize(1000000)
                .withPlugin(new MwdbTokenActionPlugin())
                .withPlugin(new MyLittleActionPlugin())
                .withPlugin(new MwdbNgramActionPlugin())
                .withPlugin(new OffHeapMemoryPlugin())
                .withScheduler(new NoopScheduler()).build();
        final ActionTest selfPointer = this;
        graph.connect(new Callback<Boolean>() {

            public void on(Boolean result) {
                final Node root = selfPointer.graph.newNode(0, BEGINNING_OF_TIME);
                root.set("name", Type.STRING, "root");
                selfPointer.graph.index(0, BEGINNING_OF_TIME, ENTRY_POINT_INDEX, new Callback<NodeIndex>() {
                    public void on(NodeIndex rootsIndex) {
                        rootsIndex.addToIndex(root, "name");
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
