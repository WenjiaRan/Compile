package utils;

public class INode<N, L> {
    // N - INode
    // L - IList
    public INode<N, L> prev = null;
    public INode<N, L> next = null;

    public N value;
    public IList<N, L> parent = null;

    public INode(N value) {
        this.value = value;
    }

    public INode(N value, IList<N, L> parent) {
        this.value = value;
        this.parent = parent;
    }
    public void clear() {
        this.prev = null;
        this.next = null;
        this.parent = null;
    }
    public void insertBefore(INode<N, L> node) {
        this.next = node;
        this.prev = node.prev;
        node.prev = this;
        if (this.prev != null) {
            this.prev.next = this;
        }
        this.parent = node.parent;
        this.parent.size++;
        if (this.parent.begin == node) {
            this.parent.begin=this;
        }
    }

    public void insertAfter(INode<N, L> node) {
        this.prev = node;
        this.next = node.next;
        node.next = this;
        if (this.next != null) {
            this.next.prev=this;
        }
        this.parent = node.parent;
        this.parent.size++;
        if (this.parent.end == node) {
            this.parent.end=this;
        }
    }

    public void insertAtBegin(IList<N, L> parent) {
        this.parent = parent;
        if (parent.isEmpty()) {
            parent.begin=this;
            parent.end=this;
            parent.size++;
        } else {
            insertBefore(parent.begin);
        }
    }

    public void insertAtEnd(IList<N, L> parent) {
        this.parent = parent;
        if (parent.isEmpty()) {
            parent.begin=this;
            parent.end=this;
            parent.size++;
        } else {
            insertAfter(parent.end);
        }
    }

    public INode<N, L> removeFromList() {
        parent.size--;
        if (parent.begin == this) {
            this.parent.begin=this.next;
        }
        if (parent.end == this) {
            this.parent.end=this.prev;
        }
        if (this.prev != null) {
            this.prev.next=this.next;
        }
        if (this.next != null) {
            this.next.prev=this.prev;
        }
        clear();
        return this;
    }
    






}
