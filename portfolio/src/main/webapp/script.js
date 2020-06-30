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

/**
 * Shows random facts about me until there are none left
 */
const facts =
  ['My favorite TV shows are Atlanta (FX), Rick and Morty (AS), and Bojack Horseman (Netflix). You do not have to watch them, but you also kind of do.',  
  "My favorite albums are Fulfillingness' First Finale (Stevie Wonder), Because the Internet (Childish Gambino), To Pimp A Butterfly (Kendrick Lamar), 2014 Forrest Hills Drive (J Cole), \
  and Acid Rap (Chance The Rapper).",
  'My favorite movies are Get Out, The Artist, and Sorry To Bother You.',
  'Mixed Bag: favorite time - 9:30 A.M., favorite food - steak (oops, you knew that already), favorite color - blue!'];
function addFavoriteThing() {
  const factContainer = document.getElementById('greeting-container');
  if (facts.length!=0) {
  // Pick a random fact.
  var randomNumber = Math.floor(Math.random() * facts.length); 
  const fact = facts[randomNumber];
  // Add it to the page and remove it from the list
  factContainer.innerText = fact;
  facts.splice(randomNumber,1); 
  } else {
    factContainer.innerText = "That's all the info you get for now...";
  }
}
// Retreive Messages from Servlet
function getMessage() {
  fetch('/data').then(response => response.json()).then(comments => {
    const messageElement = document.getElementById('message-container');
    messageElement.innerHTML = ''; 
    comments.forEach(getMessageHelper);
  })
}

function getMessageHelper(singleComment) {
  const messageElement = document.getElementById('message-container');
  const usernameElement = document.createElement('p'); 
  const commentElement = document.createElement('p');   
  usernameElement.innerText = "Username: " + singleComment.username;  
  commentElement.innerText = "Comment: " + singleComment.comment; 
  messageElement.appendChild(usernameElement);
  messageElement.appendChild(commentElement);
}