package ceki;


import java.util.ArrayList;
import java.util.List;

public class Trie<V> {

  Node<V> root = new Node<V>("");

  private static class Node<VN> {
    String key;
    VN value;
    List<Node> children = new ArrayList<Node>();

    Node(String key, VN value) {
      this.key = key;
      this.value = value;
    }

    Node(String key) {
      this(key, null);
    }

    public Node<VN> childMatching(char c) {
      int pointer = key.length();
      for (int i = 0; i < children.size(); i++) {
        Node<VN> child = children.get(i);
        if (c == child.key.charAt(pointer))
          return child;
      }
      return null;
    }

    void add(Node child) {
      children.add(child);
    }

    public String toString() {
      StringBuilder sb = new StringBuilder();
      innerToString(sb, this, "");
      return sb.toString();
    }

    void innerToString(StringBuilder sb, Node<VN> n, String padding) {
      sb.append(padding).append('"').append(n.key).append('"');
      for (Node child : n.children) {
        sb.append("\r\n");
        innerToString(sb, child, padding + "    ");
      }
    }

  }

  void put(String key, V value) {
    Node nearestParent = getNearestNode(root, key, 0);
    if (nearestParent == root) {
      root.add(new Node(key, value));
    } else {
      put(nearestParent, key, value, 0);
    }
  }

  public int nodeCount() {
    return nodeCount(root);
  }

  private int nodeCount(Node<V> aNode) {
    int count = 1;
    for (Node child : aNode.children) {
      count += nodeCount(child);
    }
    return count;
  }

  // nearestNode is the nearest node with common prefix with key
  // it means that at least one char of nearestNode matches key
  private void put(Node<V> nearestNode, String key, V value, int d) {
    int indexOfFirstMismatch = advancePointer(key, nearestNode, d);

    if (indexOfFirstMismatch == d)
      throw new IllegalStateException("no overlap between node [" + nearestNode + "] and key [" + key + "]");

    int nearestNodeLen = nearestNode.key.length();
    int keyLen = key.length();


    if (keyLen == nearestNodeLen && indexOfFirstMismatch == keyLen) {
      nearestNode.value = value;
      return;
    }

    split(nearestNode, key, value, indexOfFirstMismatch);

  }

  private void split(Node<V> nn, String key, V value, int mismatchIndex) {
    if (isSplitRequired(nn, key, mismatchIndex)) {
      Node<V> clone = new Node<V>(nn.key, nn.value);
      String commonPrefix = clone.key.substring(0, mismatchIndex);
      nn.key = commonPrefix;
      nn.add(clone);
    }

    if (isNewChildRequired(nn, key, mismatchIndex)) {
      nn.add(new Node(key, value));
    }
  }

  private boolean isSplitRequired(Node nn, String key, int mismatchIndex) {
    int nnKeyLen = nn.key.length();
    return nnKeyLen >= key.length() || mismatchIndex != nnKeyLen;
  }

  private boolean isNewChildRequired(Node n, String key, int mismatchIndex) {
    int kLen = key.length();
    return (kLen < n.key.length() || mismatchIndex != kLen);
  }

  public V get(String key) {
    Node<V> n = getNearestNode(root, key, 0);
    if (n == null) return null;
    if (n.key.length() == key.length()) {
      return n.value;
    } else
      return null;
  }

  public String getNearestKey(String key) {
    Node<V> n = getNearestNode(root, key, 0);
    return n.key;
  }

  private Node<V> getNearestNode(Node<V> n, String key, int d) {
    d = advancePointer(key, n, d);
    if (d == key.length())
      return n;
    return exploreChildren(n, key, d);
  }

  private Node<V> exploreChildren(Node<V> n, String key, int d) {
    char c = key.charAt(d); // Use dth key char to identify subtrie.
    Node<V> nearerChild = n.childMatching(c);
    if (nearerChild == null) {
      return n;
    } else {
      return getNearestNode(nearerChild, key, d);
    }
  }

  private int advancePointer(String key, Node node, int d) {
    String nodeKey = node.key;
    int boundary = Math.min(nodeKey.length(), key.length());
    while (d < boundary && key.charAt(d) == nodeKey.charAt(d)) {
      d++;
    }
    return d;
  }

}
