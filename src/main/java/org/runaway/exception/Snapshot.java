package org.runaway.exception;

import java.io.Serializable;

/**
* Snapshot of some variable - [name x value] pair -
* at a specific execution point in a stack trace frame.
*/

class Snapshot implements Serializable {
  private static final long serialVersionUID = 20030101L;

  /**
   * Name of variable, or any other info or id string.
   */

  private final String name;

  /**
   * Value of the variable as string.
   */

  private final String value;

  /**
   * Constructor.
   * @param name var name or info string, usually not null nor empty.
   * @param value could be null.
   */

  public Snapshot(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }
  
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.format("[%s=%s]", name, value);
  }
}