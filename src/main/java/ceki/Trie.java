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

  // n is the nearest node with common prefix with key
  // it means that at least one char of n matches key
  private void put(Node<V> nearestNode, String key, V value, int d) {
    int indexOfFirstMismatch = advancePointer(key, nearestNode, d);

    if (indexOfFirstMismatch == d)
      throw new IllegalStateException("no overlap between node [" + nearestNode + "] and key [" + key + "]");

    int nearestNodeLen = nearestNode.key.length();
    int keyLen = key.length();


    if (keyLen == nearestNodeLen) {
      if (indexOfFirstMismatch == keyLen) {
        nearestNode.value = value;
      } else {
        threeWaySplit(nearestNode, key, value, indexOfFirstMismatch);
      }
    } else if (keyLen < nearestNodeLen) {
      if (indexOfFirstMismatch == keyLen) {
        Node<V> clone = new Node<V>(nearestNode.key, nearestNode.value);
        String commonPrefix = clone.key.substring(0, indexOfFirstMismatch);
        nearestNode.key = commonPrefix;
        nearestNode.add(clone);
      } else {
        threeWaySplit(nearestNode, key, value, indexOfFirstMismatch);
      }
    } else { // if (nearestNodeLen < keyLen)
      if (indexOfFirstMismatch == nearestNodeLen) {
        nearestNode.add(new Node(key, value));
      } else {
        threeWaySplit(nearestNode, key, value, indexOfFirstMismatch);
      }
    }
  }

  private void threeWaySplit(Node<V> node2split, String key, V value, int mismatchIndex) {
    System.out.println("threeWaySplit [" + node2split.key + "] for key [" + key + "] d=" + mismatchIndex);

    int nearestNodeLen = node2split.key.length();
    int keyLen = key.length();


    Node<V> clone = new Node<V>(node2split.key, node2split.value);
    String commonPrefix = clone.key.substring(0, mismatchIndex);
    node2split.key = commonPrefix;
    node2split.add(clone);

    node2split.add(new Node(key, value));

//    if (keyLen >= nearestNodeLen || mismatchIndex != keyLen) {
//      node2split.add(new Node(key, value));
//    }
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
