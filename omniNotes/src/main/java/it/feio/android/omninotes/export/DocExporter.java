package it.feio.android.omninotes.export;

import java.io.OutputStream;

/**
 * This is implemented for each file type.
 * @param <T> Type to be stored in the nodes.
 */
interface DocExporter<T> {
    ElementFactory<T> getElementFactory();
    boolean init();
    boolean create(Node<T> document);
    boolean write(OutputStream os);
    boolean close();
}
