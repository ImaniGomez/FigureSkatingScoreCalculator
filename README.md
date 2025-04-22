# FigureSkatingScoreCalculator

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
2. Enter the competition name and date
3. Choose between **Short** and **Free** program using the radio buttons at the top of the table
4. Fill out the TES and PCS tables (Click **Add Row to TES** or **Add Row to PCS** if more elements are needed)
5. Click **"Save Competition"** to save the scores to the database
6. View all competition scores in the **Scores By Season** page or compare two competition scores in the **Compare Scores** page. 

## Author
** Imani Gomez**
Graduate Student, Computer Enginnering, NYU
[Github] (https://github.com/ImaniGomez)

---
