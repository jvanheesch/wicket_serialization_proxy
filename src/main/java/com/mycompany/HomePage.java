package com.mycompany;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class HomePage extends WebPage {
    private static final long serialVersionUID = -8896769873107976128L;

    public HomePage() {
        this.add(new AStatelessLabel("message", "Hello World!"));

        // required to 'force' serialization: this.isPageStateless() returns true before executing the statement below, and false thereafter.
        this.setStatelessHint(false);
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
