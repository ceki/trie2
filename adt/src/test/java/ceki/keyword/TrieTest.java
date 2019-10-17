package ceki.keyword;

import ceki.keyword.Trie;
import org.junit.Test;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;

// nn = nearestNode.key.length
// k = key.length
// d = indexOfMismatch
//
// case A0  nn == k, d == nn
// case A1  nn == k, d < nn
// case B0  k < nn,  d == k
// case B1  k < nn,  d < k
// case C0  nn < k,  d == nn
// case C1  nn < k,  d < nn

public class TrieTest {

    Trie<Object> trie = new Trie<Object>();

    @Test
    public void smoke() {
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
    public void verifyAllSixCasesForChildFreeSplitting() {
        // A0
        putSame("aaa");
        putSame("aaa");
        assertEquals(2, trie.nodeCount());
        assertSameness("aaa");
        trie.clear();

        // A1
        putSame("aaa");
        putSame("a11");
        assertEquals(4, trie.nodeCount());
        dump();

        assertSameness("aaa");
        assertSameness("a11");
        assertNull(trie.get("a"));
        trie.clear();

        // B0
        putSame("abc");
        putSame("abcd");
        assertEquals(3, trie.nodeCount());
        assertSameness("abc");
        assertSameness("abcd");
        trie.clear();

        // B1
        putSame("abc");
        putSame("abx");
        assertSameness("abc");
        assertSameness("abx");
        assertNull(trie.get("ab"));
        assertEquals(4, trie.nodeCount());
        trie.clear();

        // C0
        putSame("ab");
        putSame("abc");
        assertSameness("ab");
        assertSameness("abc");
        assertEquals(3, trie.nodeCount());
        trie.clear();

        // C1
        putSame("abc");
        putSame("az");
        assertSameness("abc");
        assertSameness("az");
        assertNull(trie.get("a"));
        assertEquals(4, trie.nodeCount());
        trie.clear();
    }

    // case A1 nn == k, d < nn
    @Test
    public void verifySplittingWithChildrenA1() {
        putSame("aaa");
        putSame("aaaabbb");
        putSame("a11");
        assertEquals(5, trie.nodeCount());
        assertSameness("aaa");
        assertSameness("aaaabbb");
        assertSameness("a11");
        assertNull(trie.get("a"));
    }

    // case B0 k < nn, d == k
    @Test
    public void verifySplittingWithChildrenB0() {
        putSame("abc");
        putSame("abc000");
        putSame("abc111");
        assertEquals(4, trie.nodeCount());
        assertSameness("abc");
        assertSameness("abc000");
        assertSameness("abc111");
    }

    // case B1 k < nn, d < k
    @Test
    public void verifySplittingWithChildrenB1() {
        putSame("abc");
        putSame("abc000");
        putSame("ax");
        assertSameness("abc");
        assertSameness("abc000");
        assertSameness("ax");
        assertNull(trie.get("a"));
        assertEquals(5, trie.nodeCount());
    }

    // case C0 nn < k, d == nn
    @Test
    public void verifySplittingWithChildrenC0() {
        putSame("ab");
        putSame("ab000");
        putSame("abc");
        assertSameness("ab");
        assertSameness("ab000");
        assertSameness("abc");
        assertEquals(4, trie.nodeCount());
    }

    // case C1 nn < k, d < nn
    @Test
    public void verifySplittingWithChildrenC1() {
        putSame("ab");
        putSame("ab000");
        putSame("ac");
        assertSameness("ab");
        assertSameness("ab000");
        assertSameness("ac");
        assertEquals(5, trie.nodeCount());
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
    }

    void putSame(String k) {
        trie.put(k, k);
    }

    void assertSameness(String k) {
        assertEquals(k, trie.get(k));
    }

    void dump() {
        System.out.println(trie.root);
    }

}
