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

    void add(Node child) {
      children.add(child);
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
    Node nearestParent = getNearestNode(root, key, 0);
    System.out.println("nearest parent="+nearestParent);
    put(nearestParent, key, value, 0);
  }

  private void put(Node n, String key, V value, int d) {

   System.out.println("key="+key+", d="+d);
   boolean overlap = false;
   int min = Math.min(n.depth+1, key.length());

   while (d < min && key.charAt(d) == n.key.charAt(d)) {
      overlap = true;
      d++;
    }

    if(!overlap) {
      n.add(new Node(key, value));
      return;
    }

    // match
    if (key.length() == n.key.length())  {
      n.value = value;
      return;
    }

    // n is exhausted
    if (d > n.depth) {
      if (d < key.length()) {
        char c = key.charAt(d);
        Node matchingChild = n.childMatching(c);
        if(matchingChild == null) {
          n.add(new Node(key, value)) ;
        } else {
          put(matchingChild, key, value, d+1);
        }
      }
    } else {
      System.out.println("***");
      split(n, value, d);
      return;
    }
  }

  private void split(Node<V> n, V val, int d) {
    Node<V>  nClone = new Node<V>(n.key, n.value);
    n.key = nClone.key.substring(0, d);
    n.add(nClone);
    n.value = val;

  }

  private void addNewNode(Node n, String key, V val) {
    Node newNode = new Node(key, val);
    n.children.add(newNode);
  }


  public V get(String key) {
    Node<V> n = getNearestNode(root, key, 0);
    if (n == null) return null;
    if(n.depth+1 == key.length()) {
      return n.value;
    } else
      return null;
  }

  private Node<V> getNearestNode(Node<V> n, String key, int d) {
    int min = Math.min(n.depth+1, key.length());

    while (d < min && key.charAt(d) == n.key.charAt(d))
      d++;

    if (d == key.length())
      return n;

    if(d+1 < key.length())
      d++;

    System.out.println("node="+n);
    System.out.println("key="+key+", next_d="+d);

    char c = key.charAt(d); // Use dth key char to identify subtrie.
    Node<V> nearerChild = n.childMatching(c);
    if(nearerChild == null) {
      return n;
    } else {
      return getNearestNode(nearerChild, key, d);
    }
  }


}
