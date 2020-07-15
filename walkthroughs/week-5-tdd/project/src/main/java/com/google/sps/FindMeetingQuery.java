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
  private PotentialAttendeeList[] possibleGroupsOfOptionalAttendees;
  private Collection < TimeRange > optimalTimesForMeeting;
  private MeetingRequest request;
  private Collection < TimeRange > timesForRequiredGuests;
  
  // a representation of a group of potential optional attendees to the meeting
  // and the time at which they could come
  private static class PotentialAttendeeList {
    public int startTime;
    public int endTime;
    public Set < String > people;
    public PotentialAttendeeList(int startTime, Set people) {
      this.startTime = startTime;
      this.people = new HashSet(people);
      endTime = startTime;
    }
    public PotentialAttendeeList(PotentialAttendeeList other) {
      this.startTime = other.startTime;
      this.people = new HashSet(other.people);
      endTime = other.endTime;
    }
    public void setEndTime(int endTime) {
      this.endTime = endTime;
    }
    public int getDuration() {
      return endTime - startTime;
    }
    public String toString() {
      return "Size: " + people.size() + " Start: " + startTime + " End: " +
        endTime;
    }
  }

  // Checks if anyone who must be at the requested meeting is at the given event
  private boolean isEventImportant(Event event, MeetingRequest request) {
    for (String person: request.getAttendees()) {
      if (event.getAttendees().contains(person)) return true;
    }
    return false;
  }

  // Method for creating time range collection when there are only optional attendees 
  private void onlyOptionalAttendees() {
    int champ = 0;
    for (int i = 0; i < possibleGroupsOfOptionalAttendees.length; i++) {
      if (possibleGroupsOfOptionalAttendees[i].people.size() < champ) break;
      if (possibleGroupsOfOptionalAttendees[i].people.size() > champ) {
        optimalTimesForMeeting.clear();
        champ = possibleGroupsOfOptionalAttendees[i].people.size();
      }
      optimalTimesForMeeting.add(TimeRange.fromStartEnd(
        possibleGroupsOfOptionalAttendees[i].startTime,
        possibleGroupsOfOptionalAttendees[i].endTime, false));
      champ = possibleGroupsOfOptionalAttendees[i].people.size();
    }
  }

  // Creates Potential Attendee Lists to find times with most optional
  // guests. 
  private void findPotentialGroupsOfOptionalAttendees(
    Event[] eventsOrderedByStart, Event[] eventsOrderedByEnd, Collection <
    Event > events, ArrayList < PotentialAttendeeList > resultsArrayList) {
    int startPointer = 0;
    int endPointer = 0;
    int nextMeetingEndTime = 0;
    int nextMeetingStartTime = 0;
    Set < String > importantAttendees = new HashSet < > ();
    Set < PotentialAttendeeList > groups = new HashSet < > ();
    HashMap < String, Integer > numCurrentMeetings = new HashMap < String,
      Integer > ();
    for (String person: request.getOptionalAttendees()) {
      numCurrentMeetings.put(person, 0);
    }
    ArrayList < PotentialAttendeeList > groupsToRemove = new ArrayList < > ();
    groups.add(new PotentialAttendeeList(0, new HashSet(request
      .getOptionalAttendees())));
    while (endPointer < eventsOrderedByEnd.length) {
      nextMeetingEndTime = eventsOrderedByEnd[endPointer].getWhen().end();
      if (startPointer < eventsOrderedByStart.length) {
        nextMeetingStartTime = eventsOrderedByStart[startPointer].getWhen()
          .start();
      }
      if (startPointer == eventsOrderedByStart.length || nextMeetingEndTime <=
        nextMeetingStartTime) {
        boolean newPossibleAttendees = false;
        for (String attendee: eventsOrderedByEnd[endPointer].getAttendees()) {
          if (numCurrentMeetings.containsKey(attendee) && numCurrentMeetings
            .get(attendee) != 0) {
            numCurrentMeetings.put(attendee, numCurrentMeetings.get(
              attendee) - 1);
            if (numCurrentMeetings.get(attendee) == 0) newPossibleAttendees =
              true;
          }
        }
        if (newPossibleAttendees) {
          Set < String > availablePeople = new HashSet < > ();
          for (String attendee: request.getOptionalAttendees()) {
            if (numCurrentMeetings.get(attendee) == 0) availablePeople.add(
              attendee);
          }
          PotentialAttendeeList newGroup = new PotentialAttendeeList(
            nextMeetingEndTime, availablePeople);
          groups.add(newGroup);
        }
        endPointer++;
        continue;
      }
      for (String attendee: eventsOrderedByStart[startPointer]
        .getAttendees()) {
        if (request.getOptionalAttendees().contains(attendee)) {
          importantAttendees.add(attendee);
          numCurrentMeetings.put(attendee, numCurrentMeetings.get(attendee) +
            1);
        }
      }
      for (PotentialAttendeeList possibleGroup: groups) {
        possibleGroup.setEndTime(nextMeetingStartTime);
        PotentialAttendeeList pastVersion = new PotentialAttendeeList(
          possibleGroup);
        pastVersion.setEndTime(nextMeetingStartTime);
        for (String attendee: importantAttendees) {
          if (possibleGroup.people.contains(attendee)) possibleGroup.people
            .remove(attendee);
        }
        if (pastVersion.people.size() > possibleGroup.people.size() &&
          pastVersion.getDuration() >= request.getDuration()) {
          resultsArrayList.add(pastVersion);
        }
        if (possibleGroup.people.size() == 0) {
          groupsToRemove.add(possibleGroup);
        }
      }
      groups.removeAll(groupsToRemove);
      groupsToRemove.clear(); 
      importantAttendees.clear();
      startPointer++;
    }
    if (!groups.isEmpty()) {
      for (PotentialAttendeeList possibleGroup: groups) {
        possibleGroup.setEndTime(END_OF_DAY);
        if (possibleGroup.getDuration() > request.getDuration()) {
          resultsArrayList.add(possibleGroup);
        }
      }
    }
  }

  // Method for creating time range collection when there are a mix of attendees 
  private void mixOfAttendees() {
    int champ = 0;
    // For every time range
    for (TimeRange time: timesForRequiredGuests) {
      for (int i = 0; i < possibleGroupsOfOptionalAttendees.length; i++) {
        if (possibleGroupsOfOptionalAttendees[i].people.size() < champ) break;
        TimeRange groupTimeRange = TimeRange.fromStartEnd(
          possibleGroupsOfOptionalAttendees[i].startTime,
          possibleGroupsOfOptionalAttendees[i].endTime, false);
        if (groupTimeRange.overlaps(time)) {
          int startPoint = Math.max(possibleGroupsOfOptionalAttendees[i]
            .startTime, time.start());
          int endPoint = Math.min(possibleGroupsOfOptionalAttendees[i]
            .endTime, time.end());
          if (endPoint - startPoint >= request.getDuration()) {
            champ = possibleGroupsOfOptionalAttendees[i].people.size();
            optimalTimesForMeeting.add(TimeRange.fromStartEnd(startPoint,
              endPoint, false));
          }
        }
      }
    }
  }

  // Method for removing the overlapping times from our returned suggested times 
  private void removeOverlaps() {
    ArrayList < TimeRange > duplicatesToRemove = new ArrayList < > ();
    for (TimeRange time: optimalTimesForMeeting) {
      for (TimeRange otherTime: optimalTimesForMeeting) {
        if (time == otherTime) continue;
        if (time.contains(otherTime)) {
          duplicatesToRemove.add(otherTime);
        }
      }
    }
    optimalTimesForMeeting.removeAll(duplicatesToRemove);
  }

  private void findPotentialTimesForRequiredAttendees(
    Event[] eventsOrderedByStart, Event[] eventsOrderedByEnd, Collection <
    Event > events) {
    int windowStart = 0;
    int windowClose = 0;
    int startPointer = 0;
    int endPointer = 0;
    int nextMeetingEndTime = 0;
    int nextMeetingStartTime = 0;
    Set < Event > problemEvents = new HashSet < >
      (); // Keeps track of the events which have relevant guests
    while (endPointer < eventsOrderedByEnd.length) {
      nextMeetingEndTime = eventsOrderedByEnd[endPointer].getWhen().end();
      if (startPointer < eventsOrderedByStart.length) {
        nextMeetingStartTime = eventsOrderedByStart[startPointer].getWhen()
          .start();
      }
      if (startPointer == eventsOrderedByStart.length || nextMeetingEndTime <=
        nextMeetingStartTime) {
        if (problemEvents.contains(eventsOrderedByEnd[endPointer])) {
          windowStart = nextMeetingEndTime;
          problemEvents.remove(eventsOrderedByEnd[endPointer]);
        }
        endPointer++;
        continue;
      }
      if (isEventImportant(eventsOrderedByStart[startPointer], request)) {
        windowClose = nextMeetingStartTime;
        if (problemEvents.isEmpty()) {
          if ((windowClose - windowStart) >= request.getDuration()) {
            timesForRequiredGuests.add(TimeRange.fromStartEnd(windowStart,
              windowClose, false));
          }
        }
        problemEvents.add(eventsOrderedByStart[startPointer]);
      }
      startPointer++;
    }
    if (windowStart < END_OF_DAY && (END_OF_DAY - windowStart) >= request
      .getDuration()) {
      timesForRequiredGuests.add(TimeRange.fromStartEnd(windowStart,
        END_OF_DAY, false));
    }
  }

  // While using request.getAttendees().isEmpty() to find if there were any required attendees worked for the tests,
  // It caused problems when used on the development server, so I made this method to compensate. 
  private boolean noRequiredAttendees(MeetingRequest request) {
    boolean noRequiredAttendees = false;
    for (String person: request.getAttendees()) {
      if (person.equals(null) || person.equals("")) noRequiredAttendees =
        true;
      break;
    }
    return noRequiredAttendees;
  }

  /** A comparator for sorting events by their start time in ascending order. */
  private static final Comparator < Event > ORDER_EVENT_BY_START =
    new Comparator < Event > () {
      @Override
      public int compare(Event a, Event b) {
        return Long.compare(a.getWhen().start(), b.getWhen().start());
      }
    };

  /** A comparator for sorting events by their end time in ascending order. */
  private static final Comparator < Event > ORDER_EVENT_BY_END =
    new Comparator < Event > () {
      @Override
      public int compare(Event a, Event b) {
        return Long.compare(a.getWhen().end(), b.getWhen().end());
      }
    };

  /** A comparator for sorting potential attendee lists by their size (largest first). */
  private static final Comparator < PotentialAttendeeList > ORDER_BY_SIZE =
    new Comparator < PotentialAttendeeList > () {
      @Override
      public int compare(PotentialAttendeeList a, PotentialAttendeeList b) {
        return -Integer.compare(a.people.size(), b.people.size());
      }
    };

  public Collection < TimeRange > query(Collection < Event > events,
    MeetingRequest request) {
    
    this.request = request;
    timesForRequiredGuests = new ArrayList < TimeRange > ();
    
    Object[] objectArray = events.toArray();
    Event[] eventsOrderedByStart = new Event[objectArray.length];
    Event[] eventsOrderedByEnd = new Event[objectArray.length];
    for (int i = 0; i < events.toArray().length; i++) {
      eventsOrderedByStart[i] = (Event) objectArray[i];
      eventsOrderedByEnd[i] = (Event) objectArray[i];
    }
    Arrays.sort(eventsOrderedByStart,
      ORDER_EVENT_BY_START); // Events ordered by start time
    Arrays.sort(eventsOrderedByEnd,
      ORDER_EVENT_BY_END); // Events ordered by end time
    
    findPotentialTimesForRequiredAttendees(eventsOrderedByStart,
      eventsOrderedByEnd, events);
    possibleGroupsOfOptionalAttendees = findPotentialOptionalAttendees(events,
      request);
    optimalTimesForMeeting = new ArrayList < > ();
    if ((this.noRequiredAttendees(request) || request.getAttendees()
      .isEmpty()) && !request.getOptionalAttendees().isEmpty()) { 
      onlyOptionalAttendees();
    } else {
      mixOfAttendees();
    }
    // If no time works when considering optional attendees, just use required guests
    if (optimalTimesForMeeting.isEmpty()) optimalTimesForMeeting =
      timesForRequiredGuests;
    removeOverlaps();
    // return optimal times; 
    return optimalTimesForMeeting;
  }
  private PotentialAttendeeList[] findPotentialOptionalAttendees(Collection <
    Event > events, MeetingRequest request) {
    ArrayList < PotentialAttendeeList > resultsArrayList = new ArrayList < >
      ();
    Object[] objectArray = events.toArray();
    Event[] eventsOrderedByStart = new Event[objectArray.length];
    Event[] eventsOrderedByEnd = new Event[objectArray.length];
    for (int i = 0; i < events.toArray().length; i++) {
      eventsOrderedByStart[i] = (Event) objectArray[i];
      eventsOrderedByEnd[i] = (Event) objectArray[i];
    }
    Arrays.sort(eventsOrderedByStart,
      ORDER_EVENT_BY_START); // Events ordered by start time
    Arrays.sort(eventsOrderedByEnd,
      ORDER_EVENT_BY_END); // Events ordered by end time
    
    findPotentialGroupsOfOptionalAttendees(eventsOrderedByStart,
      eventsOrderedByEnd, events, resultsArrayList);
    
    Object[] resultObjectArray = resultsArrayList.toArray();
    PotentialAttendeeList[] result = new PotentialAttendeeList[
      resultObjectArray.length];
    for (int i = 0; i < resultObjectArray.length; i++) {
      result[i] = (PotentialAttendeeList) resultObjectArray[i];
    }
    Arrays.sort(result, ORDER_BY_SIZE);
    for (int i = 0; i < result.length; i++) {
      System.out.println(result[i]); 
    }
    return result;
  }
}
