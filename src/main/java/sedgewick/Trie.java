package sedgewick;

public class Trie {


  private class Node {
    Node l;
    Node r;
    Item item;

    public Node(Item i) {
      item = i;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("{");
      if(item!=null) {
        sb.append("item=" + item.key().val);
      } else {
        sb.append("null");
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

  Node head;

  static final int BITS_IN_INT = 5; //31;
  static final int SHIFTS_FOR_BIT0 = BITS_IN_INT - 1;

  /**
   * Get the the most significant d'th bit in k.val
   *
   * @param k
   * @param d bit index starting from the left
   * @return
   */
  private int mostSignificantBit(Key k, int d) {
    return (k.val >> (SHIFTS_FOR_BIT0 - d)) & 1;
  }

  private boolean equals(Key k, Key other) {
    return k.val == other.val;

  }

  public Item search(Key key) {
    return searchR(head, key, 0);
  }

  private Item searchR(Node h, Key v, int d) {
    if (h == null) return null;
    if (h.l == null && h.r == null) {
      if (equals(v, h.item.key()))
        return h.item;
      else return null;
    }
    if (mostSignificantBit(v, d) == 0)
      return searchR(h.l, v, d + 1);
    else return searchR(h.r, v, d + 1);
  }

  void insert(Item x) {
    head = insertR(head, x, 0);
  }


  private Node insertR(Node h, Item x, int d) {
    if (h == null)
      return new Node(x);
    if (h.l == null && h.r == null)
      return split(new Node(x), h, d);
    if (mostSignificantBit(x.key(), d) == 0)
      h.l = insertR(h.l, x, d + 1);
    else h.r = insertR(h.r, x, d + 1);
    return h;
  }

  Node split(Node p, Node q, int d) {
    Node t = new Node(null);
    Key v = p.item.key();
    Key w = q.item.key();
    switch (mostSignificantBit(v, d) * 2 + mostSignificantBit(w, d)) {
      case 0:
        t.l = split(p, q, d + 1);
        break;
      case 1:
        t.l = p;
        t.r = q;
        break;
      case 2:
        t.r = p;
        t.l = q;
        break;
      case 3:
        t.r = split(p, q, d + 1);
        break;
    }
    return t;
  }

  public static void main(String[] args) {
    Trie trie = new Trie();
    trie.insert(new Item(new Key(0)));
    trie.insert(new Item(new Key(1)));
    System.out.println(trie.head);

  }

}
