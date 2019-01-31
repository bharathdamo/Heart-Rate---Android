HeartRate 2.0 is an application that plots the Accelerometer data in a 2 dimensional graph.

The input for the application is ID, AGE, NAME, and GENDER.
The X-axis label of the graph is the TimeStamp in HHMMSS format and the Y-axis label is the accelerometer values
ranging from -10 to +10. 

The app supports both uploading and downloading functionality. 

Initially, Run, Upload and Download buttons are enabled and Stop button is disabled.

Download requires all the data fields to be filled else it will generate a Toast Message stating Missing parameters.

Once the Run button is pressed, Only Stop button is enabled and rest all the buttons are disabled.

The data is stored in the database named Assignment2_7.db inside the stated folder. 

When clicking the download button, the database is stored in the stated folder and it plots the recent 10s data of the User specified in the data fields. 

Name of the Database is Assignment2_7.db.