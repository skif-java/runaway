package org.runaway.exception;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * RunawayException is unchecked runtime exception raised in situations not properly handled -
 * some application bug, connection not open, unexpected null pointer, index
 * out of bounds, etc.
 *
 * <p>It is RuntimeException, so there is no need to declare each method as a buggy
 * method throwing RunawayException. We still can catch it if we want to add some
 * debug info and not to rethrow other exception wrapped in another exception wrapped in
 * another exception, etc.
 * 
 * <p>toString() is overridden not following the standard API - it suggests only
 * class name and message, while we also add serialized stack trace.
 */

public class RunawayException extends RuntimeException implements Serializable {
  private static final long serialVersionUID = 20030101L;

  /**
   * Instance class name, considering subclasses.
   */
  private final String className;

  private String causeExceptionName = null;

  private String causeExceptionMessage = null;

  /**
   * Thread Id for this RunawayException.
   * Note: RunawayExceptions created on different threads have different stack traces,
   * an example is bug thrown in scheduled HTTP proxy thread and caught/handled in this thread.
   */
  private final long threadId;

  /**
  * Stack trace frames - list of frames representing stack trace of original exception.
  * Each contains both standard StackTraceElement representing the specified execution point
  * as well as snapshots - [name x value] pairs, representing some variables at that
  * same execution point.
  * Could be empty but not null.
  */
  // TODO if empty coll needed?
  private List<TraceFrame> stackFrames = Collections.emptyList();

  /**
   * this error id could be both logged into log files and provided to user,
   * to bind/find general user-friendly error message to the real issue.
   */
  private final UUID errorGuid = UUID.randomUUID();

  /**
   * max number of stack trace elements to be serialized when logged.
   */
  // TODO allow external config as interface impl
  private static final int MAX_STACK_TRACE_SIZE = 32;

  private static final int BUFFER_SIZE = 512;
  
  /**
   * Convenience method to add one snapshot to current stack trace frame.
   * @param name Name of variable, or any other info or id string.
   * @object Value of the variable.
   */
  public <T> void snap(String name, T object) {
    if (stackFrames.isEmpty()) {
      //-- unlikely but possible, we will loose any snapshots collected
      // TODO add somewhere else
      return;
    }

    String value = null;
    if (object != null) {
      value = object.toString();
    }

    Snapshot snapshot = new Snapshot(name, value);
    TraceNumber frameNumber = TraceNumber.determineCurrentFrame(this.className);

    if (frameNumber.isUndefined()) {
      // unlikely but possible, add snapshots to the first (the most recent) frame
      // as the oldest frame most likely will be cut off
      stackFrames.get(0).addSnapshot(snapshot);
      return;
    }

    // searching list by frame number, sequential is Ok for small lists.
    boolean found = false;
    for (TraceFrame frame: stackFrames) {
      if (frame.getTraceNumber().equals(frameNumber)) {
        frame.addSnapshot(snapshot);
        found = true;
        break;
      }
    }

    if (!found) {
      // unlikely but possible, add snapshots to the first (the most recent) frame
      // as the oldest frame most likely will be cut off
      stackFrames.get(0).addSnapshot(snapshot);
    }

  }

  /**
   * Convenience method to add one line of debug info.
   */
  public void snap(String name, int value) {
    snap(name, Integer.toString(value));
  }

  /**
  * Convenience method to add one line of debug info.
  */
  public void snap(String name, long value) {
    snap(name, Long.toString(value));
  }

  /**
   * Convenience method to add one line of debug info.
   */
  public void snap(String name, boolean value) {
    snap(name, Boolean.toString(value));
  }

  /**
   * Constructor with message.
   * @param message exception message.
   */
  public RunawayException(String message) {
    super(message);

    this.className = this.getClass().getName();
    this.threadId = Thread.currentThread().getId();
    // copy Java standard StackTraceElement[] into List<StackFrame>      
    StackTraceElement[] originalStack = this.getStackTrace();
    setStackFrames(originalStack);
    originalStack = null;
  }

  /**
   * Constructor, no message.
   */
  public RunawayException() {
    this.className = this.getClass().getName();
    threadId = Thread.currentThread().getId();
    // copy Java standard StackTraceElement[] into List<StackFrame>
    StackTraceElement[] originalStack = this.getStackTrace();
    setStackFrames(originalStack);
    originalStack = null;
  }

  /**
   * Constructor of RunawayException from another throwable.
   * This method leaves only one last message in the message stack,
   * as we already have stack trace in cause exception.
   * Do NOT use this method if argument could be another instance of RunawayException,
   * use of(Throwable) instead.
   */
  public RunawayException(Throwable throwable) {
    this.className = this.getClass().getName();
    this.threadId = Thread.currentThread().getId();

    // assert exception: we should not be here
    if (throwable == null) {
      return;
    }

    // if we keep original source every time,
    // we might have too many stack traces - at least 2.
    // so we just take ORIGINAL stack trace and keep it here.
    this.causeExceptionName = throwable.getClass().getName();
    this.causeExceptionMessage = throwable.getMessage();

    // copy original cause complete stack trace into our custom stackFrames
    StackTraceElement[] originalStack = throwable.getStackTrace();
    setStackFrames(originalStack);
  }

  /**
   * Constructor of RunawayException from another RunawayException of another thread
   * and having different stack trace that we also want to keep.
   * @param different instance of this exception.
   */

  private RunawayException(RunawayException another) {
    // Throwable has no setMessage() :o(
    // set this message as serialized bug from another thread
    super("[Cause bug: " + another.toString() + "]");

    this.className = this.getClass().getName();
    this.threadId = Thread.currentThread().getId();

    // get all info from another RunawayException including stack trace
    // as this exception message
    causeExceptionName = another.causeExceptionName;
    causeExceptionMessage = another.causeExceptionMessage;

    // copy Java standard StackTraceElement[] into List<StackFrame>,
    // skip the last element
    StackTraceElement[] originalStack = this.getStackTrace();
    setStackFrames(originalStack);
    originalStack = null;

    TraceNumber appTraceNumber = TraceNumber.determineCurrentFrame(this.className);

    if (!appTraceNumber.isUndefined()) {
      // unlikely but possible, add snapshots to the first (the most recent) frame
      // as the oldest frame most likely will be cut off
      while (!stackFrames.isEmpty()) {
        if (stackFrames.get(0).getTraceNumber().equals(appTraceNumber)) {
          break;
        }
        stackFrames.remove(0);
      } //-- while
    } //-- if
  }

  /**
   * Convert source exception to RunawayException.
   * If exception is RunawayException itself and from the same thread
   * (i.e. having the same stack trace) just return it back,
   * otherwise consider it as different exception.
   * @param throwable source exception to convert to this one.
   * @return RunawayException
   */

  public static RunawayException of(final Throwable throwable) {
    // assert exception: we should not be here
    if (throwable == null) {
      return new RunawayException();
    }

    // if it's already RunawayException and from the same thread
    // (i.e. having the same stack trace), just return it back
    if (throwable instanceof RunawayException) {
      long currentThreadId = Thread.currentThread().getId();
      RunawayException sourceBug = (RunawayException) throwable;
      if (currentThreadId == sourceBug.threadId) {
        return sourceBug;
      }
      return new RunawayException(sourceBug);
    }

    // have some other exception
    return new RunawayException(throwable);
  }

  /**
   * Copy Java standard StackTraceElement[] into List of StackFrames.
   * @param sourceStack  Java standard original StackTraceElement[].
   */

  private void setStackFrames(StackTraceElement[] sourceStack) {
    if (sourceStack == null) {
      //-- should not be here
      throw new IllegalArgumentException("sourceStack is null");
    }

    int stackLength = sourceStack.length;
    if (stackLength == 0) {
      stackFrames = Collections.emptyList();
      return;
    }

    stackFrames = new ArrayList<>(stackLength);
    int stackFrameNumber = stackLength;

    for (StackTraceElement traceElement: sourceStack) {
      TraceNumber frameNumber = new TraceNumber(--stackFrameNumber);
      TraceFrame frame = new TraceFrame(frameNumber, traceElement);
      stackFrames.add(frame);
    }
  }

  /**
   * Overridden getMessage() to provide more info (except stack trace).
   */
  @Override
  public String getMessage() {
    StringBuilder builder;

    try {
      builder = new StringBuilder(BUFFER_SIZE);
      builder.append("-:[").append(getTechSupportCode()).append("]:- ");
      builder.append("Thread id: ").append(threadId).append(". ");

      if (super.getMessage() != null) {
        builder.append(super.getMessage()).append(". ");
      }

      if (causeExceptionName != null) {
        builder.append("Cause: ").append(causeExceptionName).append(". Msg: ");
        builder.append(causeExceptionMessage).append(". ");
      }

      return builder.toString();
    } catch (RuntimeException e) { /* should not be here */
      return "Failed to stringify message. Msg: " + e.toString();
    }
  }

  /**
   * Provides complete debug info - class name, message and stack frames.
   */

  @Override
  public String toString() {
    StringBuilder builder;

    try {
      builder = new StringBuilder(BUFFER_SIZE);

      // because of inheritance, use the class of instance
      builder.append(this.getClass().getName()).append(": ").append(getMessage()).append("\n");
      // StackTrace can NOT be null but might be empty.
      builder.append(framesToString());
      return builder.toString();
    } catch (RuntimeException e) { /* should not be here */
      return "failed to stringify exception. Msg: " + e.toString();
    }
  }

  /**
   * Convert stack trace frames to String.
   * @return string
   */
  private String framesToString() {
    List<TraceFrame> printStackFrames;
    StringBuilder buffer = new StringBuilder(BUFFER_SIZE);

    // StackTrace can NOT be null but might be empty.
    int traceLength = stackFrames.size();
    if (traceLength == 0) {
      return "";
    }

    int printLimit = (traceLength < MAX_STACK_TRACE_SIZE) ? traceLength : MAX_STACK_TRACE_SIZE;
    printStackFrames = stackFrames.subList(0, printLimit);

    for (TraceFrame frame: printStackFrames) {
      //-- reverse stack frame index
      buffer.append("->> ");
      buffer.append(frame.getTraceNumber().getValue());
      buffer.append(":");

      // --- customize if needed
      StackTraceElement trace = frame.getStackTraceElement();
      buffer.append(trace.getClassName());
      buffer.append(".");
      buffer.append(trace.getMethodName());
      buffer.append("[");
      buffer.append(trace.getLineNumber());
      buffer.append("]");

      //-- snapshots could be empty but never null
      List<Snapshot> snapshots = frame.getSnapshots();

      if (!snapshots.isEmpty()) {
        buffer.append(": ");
        for (Snapshot snapshot: snapshots) {
          buffer.append("[");
          buffer.append(snapshot.getName());
          buffer.append("=");
          buffer.append(snapshot.getValue());
          buffer.append("]");
        }
      }

      buffer.append("\n");
    }

    return buffer.toString();
  }


  /**
   * Return bug id (hashcode of UUID) used in getMessage() when error is logged
   * and this same code could be returned to front-end user as tech support code.
   * @return "Tech support code" which could be shown to end user
   */

  public int getTechSupportCode() {
    int hashCode = errorGuid.hashCode();
    if (hashCode == Integer.MIN_VALUE) {
      hashCode = Integer.MAX_VALUE;
    }
    return Math.abs(hashCode);
  }

  /**
   * Get stack frames to allow subclasses override toString().
   * @return stackFrames
   */

  public List<TraceFrame> getStackFrames() {
    return stackFrames;
  }

}