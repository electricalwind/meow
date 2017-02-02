/**
 * Copyright 2017 Matthieu Jimenez.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lu.jimenez.research.mwgtoken.nlp;

import lu.jimenez.research.mwgtoken.core.actions.MwdbTokenActionPlugin;
import lu.jimenez.research.mwgtoken.nlp.ngram.actions.MwdbNgramActionPlugin;
import lu.jimenez.research.mylittleplugin.MyLittleActionPlugin;
import org.mwg.*;
import org.mwg.internal.scheduler.NoopScheduler;
import org.mwg.internal.scheduler.TrampolineScheduler;
import org.mwg.memory.offheap.OffHeapMemoryPlugin;

import static lu.jimenez.research.mwgtoken.core.CoreConstants.ENTRY_POINT_INDEX;
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
