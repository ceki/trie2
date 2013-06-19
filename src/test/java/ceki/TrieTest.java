package ceki;

import org.junit.Test;

public class TrieTest {

  Trie<Object> trie = new Trie<Object>();

  @Test
  public void t() {
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
