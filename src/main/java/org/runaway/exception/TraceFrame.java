package org.runaway.exception;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * TraceFrame represents one frame in stack trace - it contains both
 * standard Java StackTraceElement representing the specified execution point
 * as well as snapshots - [name x value] pairs, representing some variables at that
 * execution point.
 */

class TraceFrame implements Serializable {
  private static final long serialVersionUID = 20030101L;

  /**
   * Stack frame number (used in reverse order - [length-1 .. 0]).
   * E.g., stack size is 76, with numbers in reverse order [75..0]
   * where 0 is program start frame and 75 - most recent frame
   * (where exception was thrown).
   * Note: traceNumber is not list index in corresponding TraceFrames
   * which is [0..length-1].
   */

  private final TraceNumber traceNumber;

  /**
   * Standard StackTraceElement representing the specified execution point.
   * StackTraceElement class is final, so we just wrap it here "as is".
   */

  private final StackTraceElement stackTraceElement;

  /**
   * Snapshots - [name x value] pairs of this stack frame.
   */

  private final List<Snapshot> snapshots;

  /**
   * Constructor.
   * @param traceNumber frame number.
   * @param stackTraceElement Java stack trace element.
   */

  public TraceFrame(TraceNumber traceNumber, StackTraceElement stackTraceElement) {
    if (traceNumber == null) {
      throw new IllegalArgumentException("traceNumber==null");
    }
    if (stackTraceElement == null) {
      throw new IllegalArgumentException("stackTraceElement==null");
    }

    this.traceNumber = traceNumber;
    this.stackTraceElement = stackTraceElement;
    this.snapshots = new ArrayList<>();
  }

  /**
   * Get frame number.
   * @return traceNumber, never null
   */

  public TraceNumber getTraceNumber() {
    return traceNumber;
  }

  /**
   * Get Java StackTraceElement representing the specified execution point.
   * @return StackTraceElement
   */

  public StackTraceElement getStackTraceElement() {
    return stackTraceElement;
  }

  /**
   * Get snapshots.
   * @return list of snapshots, never null but could be empty.
   */

  public List<Snapshot> getSnapshots() {
    return snapshots;
  }

  /**
   * Add snapshot if not null.
   * @param snapshot snapshot to add
   */

  public void addSnapshot(Snapshot snapshot) {
    if (snapshot == null) {
      return;
    }
    snapshots.add(snapshot);
  }

}
