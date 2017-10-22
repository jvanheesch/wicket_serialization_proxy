package com.mycompany;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This component has a tree-like structure:
 * - The non-leaf nodes are of type RecursiveListViewPanel
 * - The leafs are of type EmptyPanel.
 */
class RecursiveListViewPanel extends Panel {
    private static final long serialVersionUID = 1990133896287407016L;

    private final List<Integer> branchNumbers;
    private final int depth;
    private final Serializable someGarbageStateThatWillBeSerialized;

    RecursiveListViewPanel(String id, List<Integer> branchNumbers, int depth) {
        super(id);
        this.branchNumbers = branchNumbers;
        this.depth = depth;

        this.add(new IntegerListView(branchNumbers, depth));

        this.someGarbageStateThatWillBeSerialized = IntStream.range(0, 1000)
                .boxed()
                .collect(Collectors.toCollection(ArrayList::new));

        // needed to prevent Page.checkHierarchyChange(this) [called in Page.replace(this)] from throwing WicketRuntimeException
        this.setAuto(true);
    }

    private static class IntegerListView extends ListView<Integer> {
        private static final long serialVersionUID = 6984926832542404514L;
        private final List<Integer> branchNumbers;
        private final int depth;

        IntegerListView(List<Integer> branchNumbers, int depth) {
            super("lv", branchNumbers);
            this.branchNumbers = branchNumbers;
            this.depth = depth;
        }

        @Override
        protected void populateItem(ListItem<Integer> item) {
            int leftover = this.depth - 1;
            String componentId = "panel";

            if (leftover > 0) {
                item.add(new RecursiveListViewPanel(componentId, this.branchNumbers, leftover));
            }
            if (leftover == 0) {
                item.add(new EmptyPanel(componentId));
            }
            if (leftover < 0) {
                throw new IllegalArgumentException();
            }
        }
    }

    private Object writeReplace() {
        return new RecursiveListViewPanelSerializationProxy(this.getId(), this.branchNumbers, this.depth);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    private static class RecursiveListViewPanelSerializationProxy implements Serializable {
        private static final long serialVersionUID = 6844492640693436523L;

        private final String id;
        private final List<Integer> branchNumbers;
        private final int depth;

        private RecursiveListViewPanelSerializationProxy(String id, List<Integer> branchNumbers, int depth) {
            this.id = id;
            this.branchNumbers = branchNumbers;
            this.depth = depth;
        }

        private Object readResolve() {
            return new RecursiveListViewPanel(this.id, this.branchNumbers, this.depth);
        }
    }
}
