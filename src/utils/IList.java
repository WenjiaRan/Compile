package utils;

import java.util.Iterator;

import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;

public class IList<N, L> implements Iterable<INode<N, L>> {
    public INode<N, L> begin;
    public INode<N, L> end;
    public final L value;
    public int size;

    public IList(L value) {
        this.begin = null;
        this.value = value;
        this.end = null;
        this.size = 0;
    }
    public boolean isEmpty() {
        return (this.begin == null) && (this.end == null) && (size == 0);
    }

    @Override
    public Iterator<INode<N, L>> iterator() {
        return new ListIterator(this.begin);
    }

    @Override
    public void forEach(Consumer<? super INode<N, L>> action) {
        Iterable.super.forEach(action);
    }

    @Override
    public Spliterator<INode<N, L>> spliterator() {
        return Iterable.super.spliterator();
    }
    class ListIterator implements Iterator<INode<N, L>> {
        INode<N, L> now = new INode<>(null);
        INode<N, L> next = null;

        public ListIterator(INode<N, L> head) {
            now.next=head;
        }

        @Override
        public boolean hasNext() {
            return next != null || now.next != null;
        }

        @Override
        public INode<N, L> next() {
            if (next == null) {
                now = now.next;
            } else {
                now = next;
            }
            next = null;
            return now;
        }

        @Override
        public void remove() {
            INode<N, L> prev = now.prev;
            INode<N, L> next = now.next;
            IList<N, L> parent = now.parent;
            if (prev != null) {
                prev.next=next;
            } else {
                parent.begin=next;
            }
            if (next != null) {
                next.prev=prev;
            } else {
                parent.end=prev;
            }
            parent.size-=1;
            this.next = next;
            now.clear();
        }
    }
}
