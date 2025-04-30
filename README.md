# Figure Skating Score Calculator

This is a Java Swing-based application that allows users to input and calculate scores for figure skating competitions. The program supports scoring for both **Short Program** and **Free Skate**, following the ISU judging system structure.

## Features
 - Add and calculate **Technical Element Scores (TES)** including Base Value (BV) and Grade of Execution (GOE).
 - Add and calculate **Program Component Scores (PCS)** based on factors and judges' marks
 - Separate scoring tabs for **Short Program** and **Free Skate** events for indivdual competitions
 - Real-time score updates as values are entered
 - Dynamic UI using `JTabel`, `JRadioButton`, and `CardLayout`. 
 - SQLite Database structure ready for storing competition data 

## UI Preview
 **Coming Soon**

## Tech Stack
 - Java (Swing)
 - SQLite 
 - Git for version control

## How to use
1. Run the `ScoreTrackerController.java` inside an IDE
2. Once prompted with the login screen, enter a username and password and click 'register'.
    or enter the username 'imani' and password '1234' to use an already populated account. 
3. Once registerd or logged in, the **add competition** page will prompt 
4. Enter the competition name and date
5. Choose between **Short** and **Free** program using the radio buttons at the top of the table
6. Fill out the TES and PCS tables (Click **"Add Row to TES"** or **"Add Row to PCS"** if more elements are needed)
7. Click **"Save Competition"** to save the scores to the database
8. View all competition scores in the **"Scores By Season"** page or compare two competition scores in the **"Compare Scores"** page. 

## Project Requirements
This project was used as a final project for NYU CS-GY 9053.
The project required the use of 3 'advanced' topics: 
### GUI
- The project runs compeletly based off the GUI, which supports: 
	- Account creation
	- Competition creation
	- Viewing and managing the user's competitions
	- Comparing different entries. 
- SQLite is used to manage the database of user accounts and each of their competition scores. 
- Networking is used allow communication between the client and server. The server accepts plain text when creating an account and sends back a response if that creation was successful. For example, if the user creates an account with an already exsiting username, the server will send back "username already exists"


## Author

 ** Imani Gomez**
Graduate Student, Computer Enginnering, NYU
[Github] (https://github.com/ImaniGomez)

---
