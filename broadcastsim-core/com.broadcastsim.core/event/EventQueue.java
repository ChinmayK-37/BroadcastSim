package com.broadcastsim.core.event;

import com.broadcastsim.core.common.enums.EventType;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/** Stores pending simulation event types in first-in, first-out order. */
public final class EventQueue {

  private final Deque<EventType> events = new ArrayDeque<>();

  /**
   * Adds an event to the end of the pending event queue.
   *
   * @param eventType the event type to enqueue
   */
  public void enqueue(EventType eventType) {
    events.addLast(eventType);
  }

  /**
   * Removes and returns the next pending event.
   *
   * @return the next event, if one is pending
   */
  public Optional<EventType> dequeue() {
    return Optional.ofNullable(events.pollFirst());
  }

  /**
   * Returns the next pending event without removing it.
   *
   * @return the next event, if one is pending
   */
  public Optional<EventType> peek() {
    return Optional.ofNullable(events.peekFirst());
  }

  /**
   * Returns whether no events are waiting to be processed.
   *
   * @return {@code true} if the queue has no pending events
   */
  public boolean isEmpty() {
    return events.isEmpty();
  }

  /** Removes every pending event. */
  public void clear() {
    events.clear();
  }
}
