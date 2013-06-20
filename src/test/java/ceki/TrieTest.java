package ceki;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TrieTest {

  Trie<Object> trie = new Trie<Object>();


  @Test
  public void t1() {
    trie.put("abc", "");
    trie.put("a", "");
    System.out.println(trie.root);
    assertEquals(3, trie.nodeCount());
  }

  @Test
  public void proximity() {
    trie.put("she", "");
    assertEquals("she", trie.getNearestKey("sells"));
    trie.put("sells", "");
    assertEquals("s", trie.getNearestKey("sx"));
    trie.put("sea", "");
    assertEquals("se", trie.getNearestKey("se_"));
  }

  @Test
  public void proximity1() {
    trie.put("she", "");
    trie.put("sells", "");
    trie.put("sea", "");
    trie.put("shore", "");
    trie.put("shell", "");

    System.out.println(trie.root);
  }




  @Test
  public void goal() {
    trie.put("she", "");
    trie.put("sells", "");
    System.out.println("**" + trie.root);

    System.out.println(trie.root);
    String result = trie.getNearestKey("sea");


    trie.put("sea", "");
    trie.put("shells", "");
    trie.put("by", "");
    trie.put("the", "");
    trie.put("sea", "");
    trie.put("shore", "");

    System.out.println(trie.root);
  }

}
