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
const facts = [
  'My favorite TV shows are Atlanta (FX), Rick and Morty (AS), and Bojack' +
  'Horseman (Netflix). You do not have to watch them,' +
  'but you also kind of do.',
  ' My favorite albums are Fulfillingness\' First Finale (Stevie Wonder),' + 
  ' Becuase the Internet (Childish Gambino), To Pimp A Butterfly ' +
  ' (Kendrick Lamar), 2014 Forrest Hills Drive (J Cole),' +
  ' and Acid Rap (Chance The Rapper).',
  'My favorite movies are Get Out, The Artist, and Sorry To Bother You.',
  'Mixed Bag: favorite time - 9:30 A.M., favorite food - steak' +
  '(oops, you knew that already), favorite color - blue!',
];

function addFavoriteThing() {
  const factContainer = document.getElementById('greeting-container');
  if (facts.length != 0) {
    // Pick a random fact.
    const randomNumber = Math.floor(Math.random() * facts.length);
    const fact = facts[randomNumber];
    // Add it to the page and remove it from the list
    factContainer.innerText = fact;
    facts.splice(randomNumber, 1);
  } else {
    factContainer.innerText = 'That is all the info you get for now...';
  }
}

function getUserInfo() {
  fetch('login-check').then((response) => response.json()).then((loginInfo) => {
    if (loginInfo.isLoggedIn) {
      console.log(loginInfo.logoutLink);
      document.getElementById("comment-form").style.display = "block";
      document.getElementById("login-logout").href = loginInfo.logoutLink;
      document.getElementById("login-logout").innerText = "Logout Here"; 
      document.getElementById("username").value = loginInfo.userEmail; 
    } else {
      console.log(loginInfo.loginLink); 
      document.getElementById("comment-form").style.display = "none";
      document.getElementById("login-logout").href = loginInfo.loginLink;
      document.getElementById("login-logout").innerText = "Login Here"; 
    }
  })
}

// Retreive Messages from Servlet
function getMessage(maxNum) {
  if (maxNum == null || maxNum == "") {
    maxNum = 5;
  }
  fetch('/data?max-num-comments=' + maxNum).then((response) => response.json())
    .then((messages) => {
      const messageElement = document.getElementById('message-container');
      messageElement.innerHTML = '';
      messages.forEach(getMessageHelper);
    })
}

function getMessageHelper(singleComment) {
  const messageElement = document.getElementById('message-container');
  const userNameElement = document.createElement('p');
  const commentElement = document.createElement('p');
  userNameElement.innerText = "Username: " + singleComment.userName;
  commentElement.innerText = "Comment: " + singleComment.comment;
  messageElement.appendChild(userNameElement);
  messageElement.appendChild(commentElement);
}

function deleteAllMessages() {
  const request = new Request('/delete-data', {
    method: 'POST'
  });
  fetch(request).then(response => {
    getMessage(0);
  })
}

function createMap() {
  const map = new google.maps.Map(document.getElementById('map'), {
    center: {
      lat: 41.3492972,
      lng: -81.4174989,
    },
    zoom: 5,
    styles: [{
      "elementType": "geometry",
      "stylers": [{
        "color": "#1d2c4d"
      }]
    }, {
      "elementType": "labels.text.fill",
      "stylers": [{
        "color": "#8ec3b9"
      }]
    }, {
      "elementType": "labels.text.stroke",
      "stylers": [{
        "color": "#1a3646"
      }]
    }, {
      "featureType": "administrative.country",
      "elementType": "geometry.stroke",
      "stylers": [{
        "color": "#4b6878"
      }]
    }, {
      "featureType": "administrative.land_parcel",
      "elementType": "labels.text.fill",
      "stylers": [{
        "color": "#64779e"
      }]
    }, {
      "featureType": "administrative.province",
      "elementType": "geometry.stroke",
      "stylers": [{
        "color": "#4b6878"
      }]
    }, {
      "featureType": "landscape.man_made",
      "elementType": "geometry.stroke",
      "stylers": [{
        "color": "#334e87"
      }]
    }, {
      "featureType": "landscape.natural",
      "elementType": "geometry",
      "stylers": [{
        "color": "#023e58"
      }]
    }, {
      "featureType": "poi",
      "elementType": "geometry",
      "stylers": [{
        "color": "#283d6a"
      }]
    }, {
      "featureType": "poi",
      "elementType": "labels.text.fill",
      "stylers": [{
        "color": "#6f9ba5"
      }]
    }, {
      "featureType": "poi",
      "elementType": "labels.text.stroke",
      "stylers": [{
        "color": "#1d2c4d"
      }]
    }, {
      "featureType": "poi.park",
      "elementType": "geometry.fill",
      "stylers": [{
        "color": "#023e58"
      }]
    }, {
      "featureType": "poi.park",
      "elementType": "labels.text.fill",
      "stylers": [{
        "color": "#3C7680"
      }]
    }, {
      "featureType": "road",
      "elementType": "geometry",
      "stylers": [{
        "color": "#304a7d"
      }]
    }, {
      "featureType": "road",
      "elementType": "labels.text.fill",
      "stylers": [{
        "color": "#98a5be"
      }]
    }, {
      "featureType": "road",
      "elementType": "labels.text.stroke",
      "stylers": [{
        "color": "#1d2c4d"
      }]
    }, {
      "featureType": "road.arterial",
      "stylers": [{
        "visibility": "off"
      }]
    }, {
      "featureType": "road.highway",
      "elementType": "geometry",
      "stylers": [{
        "color": "#2c6675"
      }]
    }, {
      "featureType": "road.highway",
      "elementType": "geometry.stroke",
      "stylers": [{
        "color": "#255763"
      }]
    }, {
      "featureType": "road.highway",
      "elementType": "labels",
      "stylers": [{
        "visibility": "off"
      }]
    }, {
      "featureType": "road.highway",
      "elementType": "labels.text.fill",
      "stylers": [{
        "color": "#b0d5ce"
      }]
    }, {
      "featureType": "road.highway",
      "elementType": "labels.text.stroke",
      "stylers": [{
        "color": "#023e58"
      }]
    }, {
      "featureType": "road.local",
      "stylers": [{
        "visibility": "off"
      }]
    }, {
      "featureType": "transit",
      "elementType": "labels.text.fill",
      "stylers": [{
        "color": "#98a5be"
      }]
    }, {
      "featureType": "transit",
      "elementType": "labels.text.stroke",
      "stylers": [{
        "color": "#1d2c4d"
      }]
    }, {
      "featureType": "transit.line",
      "elementType": "geometry.fill",
      "stylers": [{
        "color": "#283d6a"
      }]
    }, {
      "featureType": "transit.station",
      "elementType": "geometry",
      "stylers": [{
        "color": "#3a4762"
      }]
    }, {
      "featureType": "water",
      "elementType": "geometry",
      "stylers": [{
        "color": "#0e1626"
      }]
    }, {
      "featureType": "water",
      "elementType": "geometry.fill",
      "stylers": [{
        "color": "#b3e2ff"
      }, {
        "saturation": -30
      }, {
        "weight": 1
      }]
    }, {
      "featureType": "water",
      "elementType": "labels.text.fill",
      "stylers": [{
        "color": "#4e6d70"
      }]
    }]
  });
  const homeMarker = new google.maps.Marker({
    position: {
      lat: 41.3492972,
      lng: -81.4174989,
    },
    title: "My Home"
  });
  homeMarker.setMap(map);
  const homeMarkerContent = '<div id="content">' + '<div id="siteNotice">' +
    '</div>' + '<h1 id="firstHeading" class="firstHeading">My Home</h1>' +
    '<div id="bodyContent">' + '<p> Here is info about my house </p>' +
    '<p>Here is where I may put a link: <a href="index.html">' +
    'Link</a> </p> </div> </div>';
  const homeInfoWindow = new google.maps.InfoWindow({
    content: homeMarkerContent
  });
  homeMarker.addListener('click', function() {
    homeInfoWindow.open(map, homeMarker);
  });
  const schoolMarker = new google.maps.Marker({
    position: {
      lat: 40.3430983,
      lng: -74.6572626,
    },
    title: "My University"
  });
  schoolMarker.setMap(map);
  const schoolMarkerContent = '<div id="content">' + '<div id="siteNotice">' +
    '</div>' + '<h1 id="firstHeading" class="firstHeading">My School</h1>' +
    '<div id="bodyContent">' + '<p> Here is info about Princeton </p>' +
    '<p>Here is where I may put a link: <a href="index.html">' +
    'Link</a> </p> </div> </div>';
  const schoolInfoWindow = new google.maps.InfoWindow({
    content: schoolMarkerContent
  });
  schoolMarker.addListener('click', function() {
    schoolInfoWindow.open(map, schoolMarker);
  });
  const highSchoolMarker = new google.maps.Marker({
    position: {
      lat: 41.5257359,
      lng: -81.3880294,
    },
    title: "My High School"
  });
  highSchoolMarker.setMap(map);
  const highSchoolMarkerContent = '<div id="content">' +
    '<div id="siteNotice">' + '</div>' +
    '<h1 id="firstHeading" class="firstHeading">My High School</h1>' +
    '<div id="bodyContent">' + '<p> Here is info about My High School</p>' +
    '<p>Here is where I may put a link: <a href="index.html">' +
    'Link</a> </p> </div> </div>';
  const highSchoolInfoWindow = new google.maps.InfoWindow({
    content: highSchoolMarkerContent
  });
  highSchoolMarker.addListener('click', function() {
    highSchoolInfoWindow.open(map, highSchoolMarker);
  });
  const fiveGuysMarker = new google.maps.Marker({
    position: {
      lat: 41.5257988,
      lng: -81.4208599
    },
    title: "My Favorite Restaurant (Five Guys)"
  });
  fiveGuysMarker.setMap(map);
  const fiveGuysMarkerContent = '<div id="content">' + '<div id="siteNotice">' +
    '</div>' +
    '<h1 id="firstHeading" class="firstHeading">My Favorite Restaurant</h1>' +
    '<div id="bodyContent">' + '<p> Here is info about Five Guys </p>' +
    '<p>Here is where I may put a link: <a href="index.html">' +
    'Link</a> </p> </div> </div>';
  const fiveGuysInfoWindow = new google.maps.InfoWindow({
    content: fiveGuysMarkerContent
  });
  fiveGuysMarker.addListener('click', function() {
    fiveGuysInfoWindow.open(map, fiveGuysMarker);
  });
  const rMFHMarker = new google.maps.Marker({
    position: {
      lat: 41.4964837,
      lng: -81.6904016,
    },
    title: "Clevleand Basektball Arena"
  });
  rMFHMarker.setMap(map);
  const rMFHMarkerContent = '<div id="content">' + '<div id="siteNotice">' +
    '</div>' +
    '<h1 id="firstHeading" class="firstHeading">Rocket Mortgage FieldHouse</h1>' +
    '<div id="bodyContent">' + '<p> Here is info about RMFH. </p>' +
    '<p>Here is where I may put a link: <a href="index.html">' +
    'Link</a> </p> </div> </div>';
  const rMFHInfoWindow = new google.maps.InfoWindow({
    content: rMFHMarkerContent
  });
  rMFHMarker.addListener('click', function() {
    rMFHInfoWindow.open(map, rMFHMarker);
  });
}