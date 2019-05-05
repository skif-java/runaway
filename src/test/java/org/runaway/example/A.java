package org.runaway.example;

import org.runaway.exception.RunawayException;

public class A {

  public static void main(String[] args) {

    String testParam = "my test param"; //"   ";
    B b;

    try {
      b = new B();
      testParam = b.calc(testParam);

    } catch (/*SQLException |*/ RuntimeException e) {
      RunawayException re = RunawayException.of(e);
      re.snap("testParam", testParam);
      System.out.println(
          String.format("Bubbled up exception: [%d] - %s", re.getTechSupportCode(), re));
    }

  }

}
