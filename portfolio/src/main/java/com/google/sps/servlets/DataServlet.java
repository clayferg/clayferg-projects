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

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String username = getStringParameter(request, "username");
    String comment = getStringParameter(request, "comment");
    long timestamp = System.currentTimeMillis();

    Entity newComment = new Entity("Comment");
    newComment.setProperty("Username", username);
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
  /*
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      response.setContentType("application/json;");
      response.getWriter().println(convertToJsonUsingGson(newComment));
    }
  */
  private String convertToJsonUsingGson(ArrayList input) {
    Gson gson = new Gson();
    String json = gson.toJson(input);
    return json;
  }
}
