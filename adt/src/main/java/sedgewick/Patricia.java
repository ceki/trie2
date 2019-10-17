package sedgewick;

public class Patricia {

    private class Node {
        Item item;
        Node l;
        Node r;
        int bit;

        Node(Item x, int i) {
            item = x;
            bit = i;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("{");
            sb.append("bit=" + bit);
            if (item != null) {
                sb.append(", item=" + item.key().val);
            } else {
                sb.append(", item=null");
            }
            if (this.l != null) {
                sb.append(", l={");
                sb.append(this.l.toString());
                sb.append("}");
            }
            if (this.r != null) {
                sb.append(", r={");
                sb.append(this.r.toString());
                sb.append("}");
            }

            sb.append("}");
            return sb.toString();
        }
    }

    private Node head;

    public Patricia(int maxN) {
        head = new Node(null, -1);
        head.l = head;
    }

    // See Program 15.7
    public String toString() {
        return null;
    }

    Item search(Key key) {
        Item t = searchR(head.l, key, -1);
        if (t == null)
            return null;
        if (equals(t.key(), key))
            return t;
        return null;
    }

    private Item searchR(Node h, Key v, int i) {
        if (maxDepth(h, i))
            return h.item;

        if (bit(v, h.bit) == 0)
            return searchR(h.l, v, h.bit);
        else
            return searchR(h.r, v, h.bit);
    }

    private boolean maxDepth(Node h, int i) {
        return h.bit <= i;
    }

    public void insert(Item x) {
        int i = 0;
        Key v = x.key();
        Item t = searchR(head.l, v, -1);
        Key w = (t == null) ? null : t.key();
        if (v == w)
            return;
        while (bit(v, i) == bit(w, i))
            i++;
        head.l = insertR(head.l, x, i, head);
    }

    private Node insertR(Node h, Item x, int i, Node p) {
        Key v = x.key();
        if ((h.bit >= i) || (h.bit <= p.bit)) {
            Node t = new Node(x, i);
            t.l = bit(v, t.bit) == 0 ? t : h;
            t.r = bit(v, t.bit) == 0 ? h : t;
            return t;
        }
        if (bit(v, h.bit) == 0)
            h.l = insertR(h.l, x, i, h);
        else
            h.r = insertR(h.r, x, i, h);
        return h;
    }

    private boolean equals(Key k, Key other) {
        return k.val == other.val;
    }

    static final int BITS_IN_INT = 5; // 31;
    static final int SHIFTS_FOR_BIT0 = BITS_IN_INT - 1;

    /**
     * Get the the most significant d'th bit in k.val
     *
     * @param k
     * @param d bit index starting from the left
     * @return
     */
    private int bit(Key k, int d) {
        return (k.val >> (SHIFTS_FOR_BIT0 - d)) & 1;
    }

    public static void main(String[] args) {
        Patricia trie = new Patricia(6);
        trie.insert(new Item(new Key(0)));
        trie.insert(new Item(new Key(1)));
        System.out.println(trie.head);

    }
}
