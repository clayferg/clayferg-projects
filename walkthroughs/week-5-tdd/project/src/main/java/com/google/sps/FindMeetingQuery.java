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
import java.util.HashMap;
import java.util.Map;
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
  private final int END_OF_DAY = 24 * 60; // Total number of minutes in a day

  // I represented the amount of available optional people with a set of "Optional Time Ranges" spanning the entire day.
  // Each Optional Time Range has an associated number of people available. For example, if there were 5 total optional
  // attendees for a request and 3 are busy from 8-9. Then the OTR containing the time range from 8-9 has 2 as its number
  // of people available. 
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

  // Checks if anyone who is optional for the requested meeting is at the given event
  private boolean isEventImportantWithOptional(Event event, MeetingRequest request) {
    for (String person : request.getOptionalAttendees()) {
        if (event.getAttendees().contains(person)) return true;
    }
      return false; 
  }

  // While using request.getAttendees().isEmpty() to find if there were any required attendees worked for the tests,
  // It caused problems when used on the development server, so I made this method to compensate. 
  private boolean noRequiredAttendees(MeetingRequest request) {
    boolean noRequiredAttendees = false; 
    for (String person : request.getAttendees()) {
      String check = person; 
      if (check.equals(null) || check.equals("")) noRequiredAttendees = true; 
      break; 
    }
    return noRequiredAttendees; 
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

    long requestedMeetingDuration = request.getDuration(); 

    Collection<TimeRange> timesForRequiredGuests = new ArrayList<TimeRange>(); 

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

    Arrays.sort(eventsOrderedByStart, ORDER_EVENT_BY_START); // Events ordered by start time
    Arrays.sort(eventsOrderedByEnd, ORDER_EVENT_BY_END); // Events ordered by end time
    int startPointer = 0; // startPointer will help move through each Event in the eventsOrderedByStart array
    int endPointer = 0; // endPointer will help move through each Event in the eventsOrderedByEnd array
    int nextMeetingEndTime = 0; 
    int nextMeetingStartTime = 0; 
    Set<Event> problemEvents = new HashSet<>(); // Keeps track of the events which have relevant guests

    // While there are still meetings which haven't ended, this loop adds and removes
    // events with required guests for the meeting to a list of problem events. 
    // If the list of problem events is empty for a long enough time, the window will
    // be recorded as a possible meeting time. 
    while(endPointer < eventsOrderedByEnd.length) {
      nextMeetingEndTime = eventsOrderedByEnd[endPointer].getWhen().end(); 
      if (startPointer < eventsOrderedByStart.length) {
      nextMeetingStartTime = eventsOrderedByStart[startPointer].getWhen().start(); 
      }
      if (startPointer == eventsOrderedByStart.length || nextMeetingEndTime <= nextMeetingStartTime) {
        if (problemEvents.contains(eventsOrderedByEnd[endPointer])) {
          windowStart = nextMeetingEndTime; 
          problemEvents.remove(eventsOrderedByEnd[endPointer]); 
        }
        endPointer++; 
      } else {
          if (isEventImportant(eventsOrderedByStart[startPointer], request)) {
            windowClose = nextMeetingStartTime; 
            if (problemEvents.isEmpty()) {
              if ((windowClose - windowStart) >= requestedMeetingDuration) {
                timesForRequiredGuests.add(TimeRange.fromStartEnd(windowStart, windowClose, false)); 
              }
            }
            problemEvents.add(eventsOrderedByStart[startPointer]); 
          }
          startPointer++; 
      }
  }
  if (windowStart < END_OF_DAY && (END_OF_DAY - windowStart) >= requestedMeetingDuration) {
    timesForRequiredGuests.add(TimeRange.fromStartEnd(windowStart, END_OF_DAY, false)); 
  }

  Queue<OptionalTimeRange> optionalRanges = this.optionalQueryHelper(events, request); // Collection of "Optional Time Ranges" (Explained near top of class)

  int champ = 0; // highest amount of optional guests avaialable 
  Collection<TimeRange> optimalTimesForMeeting = new ArrayList<>();  
  
  if ((this.noRequiredAttendees(request) || request.getAttendees().isEmpty()) && !request.getOptionalAttendees().isEmpty()) { // All requested attendees are optional
    for (OptionalTimeRange possibleMeetTime : optionalRanges) {
      if (possibleMeetTime.getNumPeopleAvailable() > champ && possibleMeetTime.getTime().duration() >= requestedMeetingDuration) {
        champ = possibleMeetTime.getNumPeopleAvailable(); 
        optimalTimesForMeeting.clear(); 
      }
      if (possibleMeetTime.getNumPeopleAvailable() >= champ && possibleMeetTime.getTime().duration() >= requestedMeetingDuration) {
        optimalTimesForMeeting.add(possibleMeetTime.getTime()); 
      }
    }
  } else { // Any case other than all requested attendees are optional
    
    windowStart = 0; // still representing the beginning of a window for a meeting
    OptionalTimeRange otr = optionalRanges.remove();

  for (TimeRange time : timesForRequiredGuests) {
    // Finds the "optional time range" this potential meeting time range (based on the required guests) starts in
    while (!otr.getTime().contains(time.start())) {
        otr = optionalRanges.remove(); 
    }
    windowStart = time.start(); 

    // Looping until there's no time left in this potential meeting time range, find times
    // Where the most ammount of optional guests can come
    while (windowStart != time.end()) { 
      if (otr.getTime().end() < time.end()) { // If the number of free optional attendees changes before the end of the meeting
        if (requestedMeetingDuration <= otr.getTime().end() - windowStart) { // Is there enough time for a meeting?
          if (otr.getNumPeopleAvailable() > champ) { // Would this meeting time increase the amount of total attendees? 
            optimalTimesForMeeting.clear(); 
            champ = otr.getNumPeopleAvailable(); 
          }
          if(otr.getNumPeopleAvailable() >= champ) { // Is this an optimal time? 
            optimalTimesForMeeting.add(TimeRange.fromStartEnd(windowStart, otr.getTime().end(), false)); 
          }
        }
        windowStart = otr.getTime().end(); 
        otr = optionalRanges.remove(); 
      } else { // If the end of this possible meeting time comes before the next change in the number of optional attendees
          if (requestedMeetingDuration <= time.end() - windowStart) { // Is there enough time for a meeting?
            if (otr.getNumPeopleAvailable() > champ) { // Would this meeting time increase the amount of total attendees?
              optimalTimesForMeeting.clear();
              champ = otr.getNumPeopleAvailable();
            }
            if (otr.getNumPeopleAvailable() >= champ) { // Is this an optimal time? 
                optimalTimesForMeeting.add(TimeRange.fromStartEnd(windowStart, time.end(), false));
            }
          }
          windowStart = time.end(); 
        }
    }
  }
  // If no time works when considering optional attendees, just use required guests
  if (optimalTimesForMeeting.isEmpty()) optimalTimesForMeeting = timesForRequiredGuests; 
  }

  // return optimal times; 
  return optimalTimesForMeeting; 
  }

  private Queue<OptionalTimeRange> optionalQueryHelper(Collection<Event> events, MeetingRequest request) {
    
    Queue<OptionalTimeRange> timesForOptionalGuests = new LinkedList<>(); 

    Object[] objectArray = events.toArray(); 
    Event[] eventsOrderedByStart = new Event[objectArray.length]; 
    Event[] eventsOrderedByEnd = new Event[objectArray.length];

    for (int i = 0; i < events.toArray().length; i++) {
      eventsOrderedByStart[i] = (Event) objectArray[i];  
      eventsOrderedByEnd[i] = (Event) objectArray[i]; 
    }

    Arrays.sort(eventsOrderedByStart, ORDER_EVENT_BY_START); // Events ordered by start time
    Arrays.sort(eventsOrderedByEnd, ORDER_EVENT_BY_END); // Events ordered by end time
    int startPointer = 0; // startPointer will help move through each Event in the eventsOrderedByStart array
    int endPointer = 0; // endPointer will help move through each Event in the eventsOrderedByEnd array
    int nextMeetingEndTime = 0; 
    int nextMeetingStartTime = 0; 
    int lastMarker = 0; // Functions like windowStart above
    Collection<String> optionalPeopleAvailable = new HashSet(request.getOptionalAttendees()); 
    Set<Event> problemEvents = new HashSet<>(); // Keeps track of the events which have relevant guests
    HashMap<String, Integer> numCurrentMeetings = new HashMap<String, Integer>(); // Stores the number of meetings each attendee is currently at
    for (String person : request.getOptionalAttendees()) {
      numCurrentMeetings.put(person, 0); 
    }

    // While there are meetings which haven't ended, this loop creates the optional time ranges 
    // which are explained near the top of the class. 
    while(endPointer < eventsOrderedByEnd.length) {
      nextMeetingEndTime = eventsOrderedByEnd[endPointer].getWhen().end(); 
      if (startPointer < eventsOrderedByStart.length) {
      nextMeetingStartTime = eventsOrderedByStart[startPointer].getWhen().start(); 
      }
      if (startPointer == eventsOrderedByStart.length || nextMeetingEndTime <= nextMeetingStartTime) {
        if (problemEvents.contains(eventsOrderedByEnd[endPointer])) { // If the event which just ended had any of our requested optional attendees
          timesForOptionalGuests.add(new OptionalTimeRange(TimeRange.fromStartEnd(lastMarker, nextMeetingEndTime, false), optionalPeopleAvailable.size()));
          for (String person : request.getOptionalAttendees()) {
            if (eventsOrderedByEnd[endPointer].getAttendees().contains(person)) { 
              numCurrentMeetings.replace(person, numCurrentMeetings.get(person) - 1);
              if (numCurrentMeetings.get(person) == 0) { 
                  optionalPeopleAvailable.add(person); 
                }
            }
          }
          problemEvents.remove(eventsOrderedByEnd[endPointer]);  
          lastMarker = nextMeetingEndTime; 
        }
        endPointer++; 
      } else {
          if (isEventImportantWithOptional(eventsOrderedByStart[startPointer], request)) { // If this event has any of our requested attendees
            if (lastMarker != nextMeetingStartTime) timesForOptionalGuests.add(new OptionalTimeRange(TimeRange.fromStartEnd(lastMarker, nextMeetingStartTime, false), optionalPeopleAvailable.size()));
            for (String person : request.getOptionalAttendees()) {
              if (eventsOrderedByStart[startPointer].getAttendees().contains(person)) {
                optionalPeopleAvailable.remove(person); 
                numCurrentMeetings.replace(person, numCurrentMeetings.get(person) + 1);
              }
            }   
            problemEvents.add(eventsOrderedByStart[startPointer]); 
            lastMarker = nextMeetingStartTime;
          }
          startPointer++; 
      }
  }
  if (lastMarker != END_OF_DAY) {
  timesForOptionalGuests.add(new OptionalTimeRange(TimeRange.fromStartEnd(lastMarker, END_OF_DAY, false), optionalPeopleAvailable.size())); 
  }

  return timesForOptionalGuests; 
  }
}
