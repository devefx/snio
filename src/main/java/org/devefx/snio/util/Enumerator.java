package org.devefx.snio.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

public final class Enumerator<E> implements Enumeration<E> {

    private Iterator<E> iterator;

    public Enumerator(Collection<E> collection) {
        this(collection.iterator());
    }

    public Enumerator(Collection<E> collection, boolean clone) {
        this(collection.iterator(), clone);
    }

    public Enumerator(Iterator<E> iterator) {
        this.iterator = null;
        this.iterator = iterator;
    }

    public Enumerator(Iterator<E> iterator, boolean clone) {
        this.iterator = null;
        if(!clone) {
            this.iterator = iterator;
        } else {
            ArrayList<E> list = new ArrayList<E>();
            while(iterator.hasNext()) {
                list.add(iterator.next());
            }
            this.iterator = list.iterator();
        }
    }

    @Override
    public boolean hasMoreElements() {
        return iterator.hasNext();
    }

    @Override
    public E nextElement() {
        return iterator.next();
    }
}
