package org.runaway.exception;

import java.io.Serializable;

/**
 * Stack frame number of the stack frames listed in reverse order - [length-1 .. 0],
 * where [0] is the application program start frame and [length-1] - the most recent frame, top of the stack.
 * The top is the last invocation, typically the point at which the throwable was created and thrown.
 * The most recent frames could be the frames that we should skip/ignore,
 * e.g. those of the Thread class or of the RunawayException methods.
 */

class TraceNumber implements Serializable {
  private static final long serialVersionUID = 20030101L;

  /**
   * Trace frame number.
   */

  private final int value;

  private static final int VALUE_UNDEFINED = -1;

  /**
   * Value denoting that we failed to determine frame number.
   */

  private static final TraceNumber UNDEFINED = new TraceNumber(VALUE_UNDEFINED);

  public TraceNumber(int value) {
    this.value = value;
  }

  /**
   * Determine current stack trace frame number for specified class and the current thread.
   * Get current stack trace and search for the specified class name,
   * take the first/next frame after the matching class name.
   * For example, if this method is called from some method of the RunawayException
   * we want to find the first trace frame not of the RunawayException.
   * Note, normally class name, method, and even line number could be
   * encountered many times, e.g. for recursive methods.
   * This method is suggested to be used for non-recursive calls.
   * @param className class name or its case sensitive suffix, e.g. ".RunawayException"
   * @return current frame number.
   */

  // TODO is it possible to handle recursive calls?
  public static TraceNumber determineCurrentFrame(String className) {

    StackTraceElement[] stack = Thread.currentThread().getStackTrace();
    int traceLength = stack.length;

    // stack size could be 0
    if (traceLength == 0) {
      return TraceNumber.UNDEFINED;
    }

    int currentFrame = 0;
    boolean foundClass = false; //-- class to skip
    currentFrame = traceLength;

    for (StackTraceElement frame: stack) {
      --currentFrame;

      if (frame.getClassName().endsWith(className)) {
        foundClass = true;
        continue;
      }

      if (!foundClass) {
        continue;
      }

      // the first frame after and different from specified class
      return new TraceNumber(currentFrame);
    }

    return TraceNumber.UNDEFINED;
  }

  /**
   * Check if frame number is undefined.
   * @return boolean
   */

  public boolean isUndefined() {
    return this.value == VALUE_UNDEFINED;
  }

  public int getValue() {
    return value;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (other == this) {
      return true;
    }
    if (!(other instanceof TraceNumber)) {
      return false;
    }

    return (this.value == ((TraceNumber) other).value);
  }

  @Override
  public int hashCode() {
    return this.value;
  }
}
