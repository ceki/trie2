package ceki;

import java.util.ArrayList;
import java.util.List;

class Node<VN> {
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
