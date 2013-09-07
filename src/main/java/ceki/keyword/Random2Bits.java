package ceki.keyword;

import java.util.Random;

public class Random2Bits extends Random {

  public int next() {
    return this.next(2);
  }
}
