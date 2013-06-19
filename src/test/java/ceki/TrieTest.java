package ceki;

import org.junit.Ignore;
import org.junit.Test;

public class TrieTest {

  Trie<Object> trie = new Trie<Object>();


  @Test
  public void t1() {
    trie.put("abc", "");
    trie.put("a", "");

    System.out.println(trie.root);
  }


  @Test
  @Ignore
  public void goal() {
    trie.put("she", "");
    trie.put("sells", "");
    trie.put("sea", "");
    trie.put("shells", "");
    trie.put("by", "");
    trie.put("the", "");
    trie.put("sea", "");
    trie.put("shore", "");

    System.out.println(trie.root);
  }

}
