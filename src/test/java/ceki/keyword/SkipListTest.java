package ceki.keyword;


import ceki.keyword.Node;
import ceki.keyword.SkipList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SkipListTest {

  SkipList<Node> skipList = new SkipList<Node>();

  @Test
  public void smoke() {
    skipList.add(getNode("a"), 'a');
    skipList.add(getNode("b"), 'b');
    assertEquals("a", skipList.get('a').key);
    assertEquals("b", skipList.get('b').key);
    dump();
  }

  @Test
  public void moreNodes() {
    char c = 'a';
    for (int i = 0; i < 10; i++) {
      skipList.add(getNode("" + c), c);
      c++;
    }

    c = 'a';
    for (int i = 0; i < 10; i++) {
      assertEquals("" + c, skipList.get(c).key);
      c++;
    }
    dump();
  }


  @Test
  public void clear() {
    char c = 'a';
    for (int i = 0; i < 10; i++) {
      skipList.add(getNode("" + c), c);
      c++;
    }
    skipList.clear();
    dump();
    assertEquals(0, skipList.size());
  }

  private Node<String> getNode(String k) {
    return new Node<String>(k, k);
  }

  private void dump() {
    System.out.println(skipList.toString());
  }

}
