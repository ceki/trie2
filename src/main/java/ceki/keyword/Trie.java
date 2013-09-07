package ceki.keyword;

public class Trie<V> {

  Node<V> root = new Node<V>("");

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

  public void clear() {
    for (Node<V> child : root.children) {
      clearNode(child);
    }
    root.children.clear();
  }

  private void clearNode(Node<V> aNode) {
    aNode.value = null;
    aNode.key = null;
    for (Node<V> child : aNode.children) {
      clearNode(child);
    }
    aNode.children.clear();
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
    else {
      char c = key.charAt(d); // Use dth key char to identify subtrie.
      Node<V> nearerChild = n.childMatching(c);
      if (nearerChild == null)
        return n;
      else
        return getNearestNode(nearerChild, key, d);
    }
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


  public void put(String key, V value) {
    Node nearestParent = getNearestNode(root, key, 0);
    if (nearestParent == root) {
      char c = key.charAt(0);
      root.add(new Node(key, value), c);
    } else {
      put(nearestParent, key, value, 0);
    }
  }


  // nearestNode is the nearest node with common prefix with key
  // it means that at least one char of nearestNode matches key
  private void put(Node<V> nearestNode, String key, V value, int d) {
    int indexOfFirstMismatch = advancePointer(key, nearestNode, d);

    if (indexOfFirstMismatch == d)
      throw new IllegalStateException("no overlap between node [" + nearestNode + "] and key [" + key + "]");

    if (isFullMatch(nearestNode, key, indexOfFirstMismatch)) {
      nearestNode.value = value;
      return;
    }
    split(nearestNode, key, value, indexOfFirstMismatch);
  }

  private boolean isFullMatch(Node<V> nearestNode, String key, int indexOfFirstMismatch) {
    int keyLen = key.length();
    return keyLen == nearestNode.key.length() && indexOfFirstMismatch == keyLen;
  }

  private void split(Node<V> nn, String key, V value, int mismatchIndex) {
    if (isSplitRequired(nn, key, mismatchIndex)) {
      Node<V> clone = new Node<V>(nn.key, nn.value);
      String commonPrefix = clone.key.substring(0, mismatchIndex);
      nn.key = commonPrefix;
      nn.value = null;
      Node.swapChildren(nn, clone);
      nn.add(clone, clone.key.charAt(mismatchIndex));
    }

    if (isNewChildRequired(nn, key, mismatchIndex)) {
      nn.add(new Node(key, value), key.charAt(mismatchIndex));
    } else {
      nn.value = value;
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


  private int advancePointer(String key, Node node, int d) {
    String nodeKey = node.key;
    int boundary = Math.min(nodeKey.length(), key.length());
    while (d < boundary && key.charAt(d) == nodeKey.charAt(d)) {
      d++;
    }
    return d;
  }

}
