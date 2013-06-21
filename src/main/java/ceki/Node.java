package ceki;

import java.util.ArrayList;
import java.util.List;

class Node<VN> {
  String key;
  VN value;
  //List<Node> children = new ArrayList<Node>();

  SkipList children = new SkipList();

  Node(String key, VN value) {
    this.key = key;
    this.value = value;
  }

  Node(String key) {
    this(key, null);
  }

  public Node<VN> childMatching(char c) {
    return children.get(c);
  }

  static <VN> void swapChildren(Node<VN> n0, Node<VN> n1) {
    SkipList t = n0.children;
    n0.children = n1.children;
    n1.children = t;
  }

  void add(Node<VN> child, char c) {
    children.add(child, c);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    innerToString(sb, this, "");
    return sb.toString();
  }

  void innerToString(StringBuilder sb, Node<VN> n, String padding) {
    sb.append(padding).append('"').append(n.key).append('"');
    sb.append(" -> (").append(n.value).append(")");
    for (Node child : n.children) {
      sb.append("\r\n");
      innerToString(sb, child, padding + "    ");
    }
  }

}
