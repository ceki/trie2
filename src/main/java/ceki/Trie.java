package ceki;


import java.util.ArrayList;
import java.util.List;

public class Trie<V> {

  Node<V> root = new Node<V>("");

  private static class Node<VN> {
    String key;
    int depth;
    VN value;
    List<Node> children = new ArrayList<Node>();

    Node(String key, VN value) {
      this.key = key;
      this.value = value;
      this.depth = key.length() - 1;
    }

    Node(String key) { this(key, null); }

    public Node<VN> childMatching(char c) {
      for (int i = 0; i < children.size(); i++) {
        Node<VN> child = children.get(i);
        if (c == child.key.charAt(depth + 1))
          return child;
      }
      return null;
    }

    public String toString() {
      StringBuilder sb = new StringBuilder("{");
      if (key != null) {
        sb.append("key=\"");
        sb.append(key);
        sb.append('"');
      } else {
        sb.append("key=null");
      }
      if (children.size() > 0) {
        sb.append(" children:");
        sb.append(children);
      }
      sb.append("}");
      return sb.toString();
    }

  }

  void put(String key, V value) {
    put(root, key, value, 0);
  }

  private Node put(Node x, String key, V val, int d) {
    if (x == null)
      return new Node(key, val);

    if (d == key.length()) {
      x.value = val;
      return x;
    }
    char c = key.charAt(d); // Use dth key char to identify subtrie.

    Node newChild = put(x.childMatching(c), key, val, d + 1);
    x.children.add(newChild);
    return x;
  }

  public V get(String key) {
    Node<V> x = get(root, key, 0);
    if (x == null) return null;
    return x.value;
  }

  private Node<V> get(Node<V> n, String key, int d) {
    if (n == null)
      return null;

    // advance d as long as the d'th char in key and n match
    while (d <= n.depth && key.charAt(d) == key.charAt(d))
      d++;

    if (d == key.length())
      return n;

    int next_d = d + 1;
    char c = key.charAt(next_d); // Use dth key char to identify subtrie.
    return get(n.childMatching(c), key, next_d);
  }


}
