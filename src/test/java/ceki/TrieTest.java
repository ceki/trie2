package ceki;

import org.junit.Test;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;

public class TrieTest {

  Trie<Object> trie = new Trie<Object>();


  @Test
  public void t1() {
    putSame("abc");
    putSame("a");
    assertEquals(3, trie.nodeCount());
  }

  @Test
  public void proximity() {
    putSame("she");
    assertEquals("she", trie.getNearestKey("sells"));
    putSame("sells");
    assertEquals("s", trie.getNearestKey("sx"));
    putSame("sea");
    assertEquals("se", trie.getNearestKey("se_"));
  }


  @Test
  public void proximity1() {
    putSame("she");
    putSame("sells");
    putSame("sea");
    putSame("shore");
    putSame("shell");
    assertEquals("sh", trie.getNearestKey("sh_"));
  }


  @Test
  public void verifyThatAllSixCasesForSplitting() {
    putSame("aaa");
    putSame("aaa");
    assertEquals(2, trie.nodeCount());
    assertEquals("aaa", trie.get("aaa"));
    trie.clear();

    putSame("aaa");
    putSame("a11");
    assertEquals(4, trie.nodeCount());
    assertEquals("aaa", trie.get("aaa"));
    assertEquals("a11", trie.get("a11"));
    assertNull(trie.get("a"));
    trie.clear();

    putSame("abc");
    putSame("abcd");
    assertEquals(3, trie.nodeCount());
    assertEquals("abc", trie.get("abc"));
    assertEquals("abcd", trie.get("abcd"));
    trie.clear();

    putSame("abc");
    putSame("abx");
    assertEquals("abc", trie.get("abc"));
    assertEquals("abx", trie.get("abx"));
    assertNull(trie.get("ab"));
    assertEquals(4, trie.nodeCount());
    trie.clear();

    putSame("abc");
    putSame("ab");
    assertEquals("abc", trie.get("abc"));
    assertEquals("ab", trie.get("ab"));
    assertEquals(3, trie.nodeCount());
    trie.clear();

    putSame("abc");
    putSame("az");
    assertEquals("abc", trie.get("abc"));
    assertEquals("az", trie.get("az"));
    assertNull(trie.get("a"));
    assertEquals(4, trie.nodeCount());
    trie.clear();
  }


  @Test
  public void goal() {
    putSame("she");
    putSame("sells");
    String result = trie.getNearestKey("sea");
    putSame("sea");
    putSame("shells");
    putSame("by");
    putSame("the");
    putSame("sea");
    putSame("shore");
    //dump();
  }

  void putSame(String k) {
    trie.put(k, k);
  }

  void dump() {
    System.out.println(trie.root);
  }

}
