package it.feio.android.omninotes.export;

import java.util.ArrayList;

/**
 * A node in the document structure.
 * @param <T> Some type that holds the data used by the implementing exporter.
 */
class Node<T> {
    final private Node parent;
    final private ArrayList<Node<T>> children;
    final private T content;

    public Node(T content, Node<T> parent, ArrayList<Node<T>> children) {
        this.parent = parent;
        this.children = children;  // TODO: Make immutable
        this.content = content;
    }

    public Node getParent() {
        return parent;
    }

    public ArrayList<Node<T>> getChildren() {
        return children;
    }

    public T getContent() {
        return content;
    }
}
