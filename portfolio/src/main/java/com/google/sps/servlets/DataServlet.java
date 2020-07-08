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

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private static class Comment {
    long id;
    long timestamp;
    String userName;
    String comment;

    public Comment(long id, long timestamp, String userName, String comment) {
      this.id = id;
      this.timestamp = timestamp;
      this.userName = userName;
      this.comment = comment;
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String userName = getStringParameter(request, "username");
    String comment = getStringParameter(request, "comment");
    long timestamp = System.currentTimeMillis();

    Entity newComment = new Entity("Comment");
    newComment.setProperty("Username", userName);
    newComment.setProperty("Comment", comment);
    newComment.setProperty("Timestamp", timestamp);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(newComment);
    response.sendRedirect("/index.html");
  }

  private String getStringParameter(HttpServletRequest request, String name) {
    String value = request.getParameter(name);
    if (value == null) {
      value = "";
    }
    return value;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment").addSort("Timestamp", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    // Convert the input to an int.
    int maxNumberComments;
    try {
      maxNumberComments = Integer.parseInt(request.getParameter("max-num-comments"));
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + request.getParameter("max-num-comments"));
      maxNumberComments = 3; 
    }

    ArrayList<Comment> comments = new ArrayList<>();

    int maxComments = Integer.parseInt(request.getParameter("max-num-comments"));

    for (Entity entity : results.asIterable()) {
      if (maxNumberComments == 0) break; 
      long id = entity.getKey().getId();
      long timestamp = (long) entity.getProperty("Timestamp");
      String userName = (String) entity.getProperty("Username");
      String commentText = (String) entity.getProperty("Comment");

      Comment comment = new Comment(id, timestamp, userName, commentText);
      comments.add(comment);
      maxNumberComments--; 
    }
    response.setContentType("application/json;");
    response.getWriter().println(convertToJsonUsingGson(comments));
  }

  private String convertToJsonUsingGson(ArrayList input) {
    Gson gson = new Gson();
    String json = gson.toJson(input);
    return json;
  }
}

