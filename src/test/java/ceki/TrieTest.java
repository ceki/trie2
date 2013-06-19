package ceki;

import org.junit.Test;

public class TrieTest {

  Trie<Object> trie = new Trie<Object>();

  @Test
  public void t() {
    trie.put("a", "a");
    trie.put("b", "b");

    trie.put("aa", "aa");

    System.out.println(trie.root);
  }

}
