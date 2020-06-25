/**
 * Prompts questions about me and updates the score
 */

 function triviaQuestions(wannaPlay) {
 if (wannaPlay) {
     alert("Welcome to the game");
     answer1 = prompt("This is Q1", ''); 
     answer2 = prompt("This is Q2", ''); 
     answer3 = prompt("This is Q3", ''); 
     answer4 = prompt("This is Q4", ''); 
     answer5 = prompt("This is Q5", ''); 
     answer6 = prompt("This is Q6", ''); 
     answer7 = prompt("This is Q7", ''); 
     answer8 = prompt("This is Q8", ''); 
     answer9 = prompt("This is Q9", ''); 
     answer10 = prompt("This is Q10", ''); 
     document.getElementById("pointTracker").innerHTML = "Final Score = 0 / 0";


 } else {
     alert("Hm...your loss"); 
     document.getElementById("pointTracker").innerHTML = "Final Score = 0 / 10"; 
 }
 }