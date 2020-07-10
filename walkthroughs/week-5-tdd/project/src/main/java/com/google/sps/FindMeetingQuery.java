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
import java.util.Queue; 
import java.util.LinkedList;
import java.lang.Math;
import java.io.*; 

public final class FindMeetingQuery {
  private final int END_OF_DAY = 24 * 60; 

  private static class OptionalTimeRange {
    private TimeRange time; 
    private int numPeopleAvailable; 

    public OptionalTimeRange(TimeRange time, int numPeopleAvailable) {
      this.time = time; 
      this.numPeopleAvailable = numPeopleAvailable; 
    }

    public TimeRange getTime() {
      return time; 
    }

    public int getNumPeopleAvailable() {
        return numPeopleAvailable; 
    }

  }

  // Checks if anyone who must be at the requested meeting is at the given event
  private boolean isEventImportant(Event event, MeetingRequest request) {
    for (String person : request.getAttendees()) {
        if (event.getAttendees().contains(person)) return true;
      }
      return false; 
  }

  // Checks if anyone who must be at the requested meeting is at the given event
  private boolean isEventImportantWithOptional(Event event, MeetingRequest request) {
    for (String person : request.getOptionalAttendees()) {
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
 System.out.println("New test"); 
 /*   
    Collection<TimeRange> resultWithOptionalAttendees = this.optionalQuery(events, request); 
    // If a meeting works with optional guests or there were only optional guests requested
    // return result considering optional guests
    if (!resultWithOptionalAttendees.isEmpty() || (request.getAttendees().isEmpty() && !request.getOptionalAttendees().isEmpty())) { 
        return resultWithOptionalAttendees; 
    }
*/ 

    // Find out how long they have to be at the meeting
    long duration = request.getDuration(); 
    // Set the collection of possible times to be empty
    Collection<TimeRange> result = new ArrayList<TimeRange>(); 

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

  Queue<OptionalTimeRange> optionalRanges = this.optionalQueryHelper(events, request); 
   for (OptionalTimeRange o : optionalRanges) {
    System.out.println(o.getTime() + " " + o.getNumPeopleAvailable()); 
  }

  int champ = 0; 
  Collection<TimeRange> resultAfterOptional = new ArrayList<>(); 
  OptionalTimeRange otr = optionalRanges.remove(); 
  int start = 0; 

 
  if (request.getAttendees().isEmpty() && !request.getOptionalAttendees().isEmpty()) {
    if (otr.getTime().duration() >= duration) {
      champ = otr.getNumPeopleAvailable(); 
      resultAfterOptional.add(otr.getTime()); 

      for (OptionalTimeRange possibleMeetTime : optionalRanges) {
        if (possibleMeetTime.getNumPeopleAvailable() > champ && possibleMeetTime.getTime().duration() >= duration) {
          champ = possibleMeetTime.getNumPeopleAvailable(); 
          resultAfterOptional.clear(); 
        }
        if (possibleMeetTime.getNumPeopleAvailable() >= champ && possibleMeetTime.getTime().duration() >= duration) {
          resultAfterOptional.add(possibleMeetTime.getTime()); 
        }
      }
    }
  } else {
  // While there are more possible meeting times
  for (TimeRange time : result) {
    // See if the start time of the current meeting time is contained in this OptionalTimeRange
    while (!otr.getTime().contains(time.start())) {
        otr = optionalRanges.remove(); 
        // Store the OptionalTimeRange this meeting time starts in
    }
    // Use "start" to store when this possible meeting time started 
    start = time.start(); 
    System.out.println(otr.getTime() + " " + time);
    while (start != time.end()) { 
    // What comes first? the end of the OptionalTimeRange or the end of the possible meeting time?
    if (otr.getTime().end() < time.end()) {    
    // If it's the OptionalTimeRange
        // Is there enough time between the possible start time and the end of the OptionalTimeRange to have a meeting
        if (duration <= otr.getTime().end() - start) {
            // If so, how does the number of guests compare to the champion
            if (otr.getNumPeopleAvailable() > champ) {
            // If more:
                // Clear out your current resultBin
                resultAfterOptional.clear(); 
                // Update the champion to this number
                champ = otr.getNumPeopleAvailable(); 
            }
            // If equal or more
            if(otr.getNumPeopleAvailable() >= champ) {
                // Add from the "start", to the end of the OptionalMeetingRange
                resultAfterOptional.add(TimeRange.fromStartEnd(start, otr.getTime().end(), true)); 
            }
        }
       // Set start to the end of the OptionalTimeRange
        start = otr.getTime().end(); 
        // dequeue next OptionalTimeRange
        otr = optionalRanges.remove(); 
        // Return to the top of this small loop
    } else {
        // If it's the end of the possible meeting time
        if (duration <= time.end() - start) {
        // Is the duration enough? If not, skip to next possible time step
            // how does the OptionalTimeRange compare to the champ
            // If more:
            if (otr.getNumPeopleAvailable() > champ) {
                // Clear out your current resultBin
                resultAfterOptional.clear();
                // Update the champion to this number
                champ = otr.getNumPeopleAvailable();
            }
            // If more or equal:
            if (otr.getNumPeopleAvailable() >= champ) {
                // Add from the "start", to the end of the possible meeting time
                resultAfterOptional.add(TimeRange.fromStartEnd(start, time.end(), false));
            }
        }
    // Regardless, move on to the next possible time (Whole loop restarts) (set start to time.end())
    start = time.end(); 
    }
    }
  }
    if (resultAfterOptional.isEmpty()) resultAfterOptional = result; 
  }


  // return result; 
  return resultAfterOptional; 
  }


  
/*
  private Collection<TimeRange> optionalQuery(Collection<Event> events, MeetingRequest request) {
    
    // Find out how long they have to be at the meeting
    long duration = request.getDuration(); 
    // Set the collection of possible times to be empty
    Collection<TimeRange> result = new ArrayList<TimeRange>(); 

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
          if (isEventImportantWithOptional(eventsOrderedByStart[startPointer], request)) {
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
*/ 
  private Queue<OptionalTimeRange> optionalQueryHelper(Collection<Event> events, MeetingRequest request) {
    
    // Set the collection of possible times to be empty
    Queue<OptionalTimeRange> result = new LinkedList<>(); 

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
    int lastMarker = 0; 
    Collection<String> optionalPeopleAvailable = new HashSet(request.getOptionalAttendees()); 
    Set<Event> problemEvents = new HashSet<>();



    // Loop until all meetings have ended
    while(endPointer < eventsOrderedByEnd.length) {
    // What comes first, the end of a meeting or the start of one
      nextMeetingEnd = eventsOrderedByEnd[endPointer].getWhen().end(); 
      if (startPointer < eventsOrderedByStart.length) {
      nextMeetingStart = eventsOrderedByStart[startPointer].getWhen().start(); 
      }
      if (startPointer == eventsOrderedByStart.length || nextMeetingEnd < nextMeetingStart) {
        if (problemEvents.contains(eventsOrderedByEnd[endPointer])) {
          result.add(new OptionalTimeRange(TimeRange.fromStartEnd(lastMarker, nextMeetingEnd, false), optionalPeopleAvailable.size()));
          for (String person : request.getOptionalAttendees()) {
            if (eventsOrderedByEnd[endPointer].getAttendees().contains(person)) optionalPeopleAvailable.add(person); 
          }
          problemEvents.remove(eventsOrderedByEnd[endPointer]);  
          lastMarker = nextMeetingEnd; 
        }
        endPointer++; 
      } else {
          if (isEventImportantWithOptional(eventsOrderedByStart[startPointer], request)) {
            result.add(new OptionalTimeRange(TimeRange.fromStartEnd(lastMarker, nextMeetingStart, false), optionalPeopleAvailable.size()));
            for (String person : request.getOptionalAttendees()) {
              if (eventsOrderedByStart[startPointer].getAttendees().contains(person)) optionalPeopleAvailable.remove(person); 
            }   
            problemEvents.add(eventsOrderedByStart[startPointer]); 
            lastMarker = nextMeetingStart;
          }
          startPointer++; 
      }
  }
  if (lastMarker != END_OF_DAY) {
  result.add(new OptionalTimeRange(TimeRange.fromStartEnd(lastMarker, END_OF_DAY, false), optionalPeopleAvailable.size())); 
  }

  return result; 
  }
}