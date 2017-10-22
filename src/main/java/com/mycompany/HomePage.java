package com.mycompany;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HomePage extends WebPage {
    private static final long serialVersionUID = -8896769873107976128L;

    public HomePage() {
        // no longer intresting
        // this.add(new AStatelessLabel("message", "Hello World!"));

        // no longer needed, since Link.getStatelessHint() returns false, making this page stateful.
        // required to 'force' serialization: this.isPageStateless() returns true before executing the statement below, and false thereafter.
        // this.setStatelessHint(false);

        this.add(new Link<Void>("link") {
            private static final long serialVersionUID = 227596780653601723L;

            @Override
            public void onClick() {
                this.setResponsePage(new HomePage());
            }
        });

        int branching = 2;
        int depth = 14;
        List<Integer> branchNumbers = IntStream.range(0, branching).boxed().collect(Collectors.toList());
        this.add(new RecursiveListViewPanel("recursiveListViewPanel", branchNumbers, depth));
    }

    /**
     * After stream.defaultReadObject(), this.getRecursiveListViewPanel() has parent null, which will result in:
     * org.apache.wicket.markup.MarkupNotFoundException: Can not determine Markup. Component is not yet connected to a parent. [RecursiveListViewPanel [Component id = recursiveListViewPanel]]
     * Calling this.getRecursiveListViewPanel().setParent(this) would solve this, but that's a dirty hack (as setParent's javadoc suggests).
     * this.replace(this.getRecursiveListViewPanel()) seems like a clean way to reset the correct parent-child relationship.
     */
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.replace(this.getRecursiveListViewPanel());
    }

    private Component getRecursiveListViewPanel() {
        return this.get("recursiveListViewPanel");
    }

    @Override
    protected void onAfterRenderChildren() {
        super.onAfterRenderChildren();

        AtomicInteger nbOfItems = new AtomicInteger(0);
        this.<RecursiveListViewPanel, AtomicInteger>visitChildren(RecursiveListViewPanel.class, (item, visit) -> nbOfItems.incrementAndGet());
        // nbOfItems.get() == 2^(depth) - 1
        System.out.println("Number of panels: " + nbOfItems.get());
    }

    /**
     * Wicket only serializes stateful pages, i.e. pages for which page.isPageStateless() returns false.
     * During page serialization, all components on the page are serialized, even the stateless ones.
     */
    private static class AStatelessLabel extends Label {
        private static final long serialVersionUID = 8426515046501421127L;

        AStatelessLabel(String id, Serializable label) {
            super(id, label);
        }

        private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
            System.out.println("AStatelessLabel.readObject() - AStatelessLabel is stateless? " + this.isStateless());
            s.defaultReadObject();
        }

        private void writeObject(ObjectOutputStream s) throws IOException {
            System.out.println("AStatelessLabel.writeObject() - AStatelessLabel is stateless? " + this.isStateless());
            s.defaultWriteObject();
        }
    }
}
