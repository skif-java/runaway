package org.runaway.example;

import org.runaway.exception.RunawayException;

//import java.sql.SQLException;

public class A {

  public static void main(String[] args) {

    String testParam = "my test param"; //"   ";
    B b;

    try {
      b = new B();
      testParam = b.calc(testParam);

    } catch (/*SQLException |*/ RuntimeException e) {
      RunawayException be = RunawayException.of(e);
      be.snap("testParam", testParam);
      //throw be;
      System.out.println(
          String.format("Bubbled up exception: [%d] - %s", be.getTechSupportCode(), be));
    }

  }

}
