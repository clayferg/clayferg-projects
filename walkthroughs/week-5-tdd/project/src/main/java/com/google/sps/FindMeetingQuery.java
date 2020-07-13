// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

public final class FindMeetingQuery {
  private Collection<TimeRange> result; 
  private Iterator neededGuests;   
  private final int END_OF_DAY = 24 * 60; 

  // Checks if anyone who must be at the requested meeting is at the given event
  private boolean isEventImportant(Event event, MeetingRequest request) {
    for (String person : request.getAttendees()) {
        if (event.getAttendees().contains(person)) return true;
      }
      return false; 
  }

     /** A comparator for sorting events by their start time in ascending order. */
  private static final Comparator<Event> ORDER_EVENT_BY_START =
      new Comparator<Event>() {
        @Override
        public int compare(Event a, Event b) {
          return Long.compare(a.getWhen().start(), b.getWhen().start());
        }
      };

  /** A comparator for sorting events by their end time in ascending order. */
  private static final Comparator<Event> ORDER_EVENT_BY_END =
      new Comparator<Event>() {
        @Override
        public int compare(Event a, Event b) {
          return Long.compare(a.getWhen().end(), b.getWhen().end());
        }
      };
  
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    
    // Find out who has to be at the meeting
    neededGuests = request.getAttendees().iterator();
    // Find out how long they have to be at the meeting
    long duration = request.getDuration(); 
    // Set the collection of possible times to be empty
    result = new ArrayList<TimeRange>(); 

    // window referring to the opening and closing of the window of time where the meeting could be done
    int windowStart = 0; 
    int windowClose = 0; 

    Object[] objectArray = events.toArray(); 
    Event[] eventsOrderedByStart = new Event[objectArray.length]; 
    Event[] eventsOrderedByEnd = new Event[objectArray.length];

    for (int i = 0; i < events.toArray().length; i++) {
      eventsOrderedByStart[i] = (Event) objectArray[i];  
      eventsOrderedByEnd[i] = (Event) objectArray[i]; 
    }

    Arrays.sort(eventsOrderedByStart, ORDER_EVENT_BY_START);
    Arrays.sort(eventsOrderedByEnd, ORDER_EVENT_BY_END);
    int startPointer = 0; 
    int endPointer = 0; 
    int nextMeetingEnd = 0; 
    int nextMeetingStart = 0; 
    Set<Event> problemEvents = new HashSet<>();



    // Loop until all meetings have ended
    while(endPointer < eventsOrderedByEnd.length) {
    // What comes first, the end of a meeting or the start of one
      nextMeetingEnd = eventsOrderedByEnd[endPointer].getWhen().end(); 
      if (startPointer < eventsOrderedByStart.length) {
      nextMeetingStart = eventsOrderedByStart[startPointer].getWhen().start(); 
      }
      if (startPointer == eventsOrderedByStart.length || nextMeetingEnd <= nextMeetingStart) {
        if (problemEvents.contains(eventsOrderedByEnd[endPointer])) {
          windowStart = nextMeetingEnd; 
          problemEvents.remove(eventsOrderedByEnd[endPointer]); 
        }
        endPointer++; 
      } else {
          if (isEventImportant(eventsOrderedByStart[startPointer], request)) {
            windowClose = nextMeetingStart; 
            if (problemEvents.isEmpty()) {
              if ((windowClose - windowStart) >= duration) {
                result.add(TimeRange.fromStartEnd(windowStart, windowClose, false)); 
              }
            }
            problemEvents.add(eventsOrderedByStart[startPointer]); 
          }
          startPointer++; 
      }
  }
  if (windowStart < END_OF_DAY && (END_OF_DAY - windowStart) >= duration) {
    result.add(TimeRange.fromStartEnd(windowStart, END_OF_DAY, false)); 
  }

  return result; 
  }
}