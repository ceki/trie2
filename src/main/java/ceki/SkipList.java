package ceki;

import java.util.Iterator;

public class SkipList implements Iterable<Node> {

  static final int MAX_LEVEL = 3;

  static class SkipNode {
    Node payload;
    char c;
    SkipNode[] forward;

    SkipNode(Node payload, char c, int level) {
      this.payload = payload;
      this.c = c;
      forward = new SkipNode[level + 1];
    }
  }

  Random2Bits random = new Random2Bits();
  public final SkipNode head = new SkipNode(null, (char) 0, MAX_LEVEL);
  int maxLevel = 0;


  public void add(Node payload, char c) {
    int nodeLevel = getRandomLevel();
    updateMaxLevelIfNecessary(nodeLevel);

    SkipNode newSkipNode = new SkipNode(payload, c, nodeLevel);
    SkipNode[] leftNodes = getLargestSmallerNodesAs(newSkipNode);
    updateAllPointers(leftNodes, newSkipNode);
  }

  private void updateAllPointers(SkipNode[] leftNodes, SkipNode newSkipNode) {
    for (int lvl = leftNodes.length - 1; lvl >= 0; lvl--) {
      updatePointers(leftNodes[lvl], newSkipNode, lvl);
    }
  }

  private void updatePointers(SkipNode leftSkipNode, SkipNode newSkipNode, int lvl) {
    SkipNode t = leftSkipNode.forward[lvl];
    leftSkipNode.forward[lvl] = newSkipNode;
    newSkipNode.forward[lvl] = t;
  }

  private SkipNode[] getLargestSmallerNodesAs(SkipNode aSkipNode) {
    int level = aSkipNode.forward.length - 1;
    char c = aSkipNode.c;
    SkipNode[] largestSmallerNodes = new SkipNode[level + 1];
    SkipNode n = head;
    for (int lvl = level; lvl >= 0; lvl--) {
      n = getTheHighestSmallerNodeForLevel(c, n, lvl);
      largestSmallerNodes[lvl] = n;
    }
    return largestSmallerNodes;
  }


  public Iterator<Node> iterator() {

    return new Iterator<Node>() {
      SkipNode n = head.forward[0];

      @Override
      public boolean hasNext() {
        return n != null;
      }

      @Override
      public Node next() {
        Node payload = n.payload;
        n = n.forward[0];
        return payload;
      }

      @Override
      public void remove() {
      }
    };
  }

  public void clear() {
    for (int lvl = maxLevel; lvl >= 0; lvl--) {
      clearPointersAtLevel(lvl);
    }
    maxLevel = 0;
  }

  private void clearPointersAtLevel(int level) {
    SkipNode n = head;
    while (n != null) {
      SkipNode t = n.forward[level];
      n.forward[level] = null;
      n = t;
    }
  }

  int size() {
    SkipNode n = head.forward[0];
    int count = 0;
    while (n != null) {
      count++;
      n = n.forward[0];
    }
    return count;
  }

  private int getRandomLevel() {
    int level = 0;
    while (random.next() == 0 && level < MAX_LEVEL) {
      level++;
    }
    return level;
  }


  public Node get(char c) {
    SkipNode n = head;
    n = getTheHighestSmallerNode(c, n);
    n = n.forward[0];
    if (n != null && n.c == c)
      return n.payload;
    else
      return null;
  }

  private SkipNode getTheHighestSmallerNode(char c, SkipNode n) {
    for (int i = maxLevel; i >= 0; i--) {
      n = getTheHighestSmallerNodeForLevel(c, n, i);
    }
    return n;
  }

  private SkipNode getTheHighestSmallerNodeForLevel(char c, SkipNode n, int i) {
    while (n.forward[i] != null && n.forward[i].c < c) {
      n = n.forward[i];
    }
    return n;
  }

  private void updateMaxLevelIfNecessary(int nodeLevel) {
    if (nodeLevel > maxLevel)
      maxLevel = nodeLevel;
  }


  public String toString() {
    StringBuilder sb = new StringBuilder();
    SkipNode n = head.forward[0];
    while (n != null) {
      sb.append(n.c);
      sb.append(" -> ");
      n = n.forward[0];
    }
    return sb.toString();
  }
}
