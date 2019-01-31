package com.example.deepakhaldar.application;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.nbsp.materialfilepicker.MaterialFilePicker;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends Activity {
    LineGraphSeries<DataPoint> series1 = new LineGraphSeries<>(); //Graph Series for X values of the Accelerometer
    LineGraphSeries<DataPoint> series2 = new LineGraphSeries<>();//Graph Series for Y values of the Accelerometer
    LineGraphSeries<DataPoint> series3 = new LineGraphSeries<>();//Graph Series for Z values of the Accelerometer
    private int last_x1,Entry;
    Boolean stop_pressed = false;
    Sensor mySensor;
    SensorManager SM;
    DownloadManager dm;
    SQLiteDatabase db;
    double time;
    Button upload, download, run_button, stop_button;
    ProgressDialog uploading, downloading;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SM = (SensorManager) getSystemService(SENSOR_SERVICE);
        mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        upload = (Button) findViewById(R.id.button_upload);
        download=(Button) findViewById(R.id.button_download);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},100);
                return;
            }
        }

        enable_button();
        SimpleDateFormat s = new SimpleDateFormat("hhmmss"); //Date Format
        String format = s.format(new Date());
        time=Double.parseDouble(format);
        stop_button = (Button) findViewById(R.id.button_stop);

        //Stop Button Functionality
        stop_button.setOnClickListener(new View.OnClickListener() { //Adding Listener for Stop Button.
            public void onClick(View v) {
                run_button = (Button) findViewById(R.id.button_run);
                run_button.setClickable(true);
                upload.setClickable(true);
                download.setClickable(true);
                stop_pressed = true;    //Updating the Boolean value to true once the Stop Button is pressed
                GraphView graph = (GraphView) findViewById(R.id.graph);
                graph.removeAllSeries();
                SM.unregisterListener(mSensorListener, mySensor);
            }
        });

        RadioGroup Gender = (RadioGroup) findViewById(R.id.radioGroup);
        //Listening the Change in Radio Button Selection
        Gender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                GraphView graph = (GraphView) findViewById(R.id.graph);
                graphSet(graph);
                Button run_button = (Button) findViewById(R.id.button_run);
                run_button.setClickable(true);
                SM.unregisterListener(mSensorListener, mySensor);
                series1.resetData(new DataPoint[]{
                        new DataPoint(last_x1, Math.random() * 100d)
                });
                series2.resetData(new DataPoint[]{
                        new DataPoint(last_x1, Math.random() * 100d)
                });
                series3.resetData(new DataPoint[]{
                        new DataPoint(last_x1, Math.random() * 100d)
                });
            }
        });
        GraphView graph = (GraphView) findViewById(R.id.graph);
        graphSet(graph);
    }

    private final SensorEventListener mSensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            EditText ID_Field = (EditText) findViewById(R.id.ID);
            EditText Age_Field = (EditText) findViewById(R.id.AGE);
            EditText Name_Field = (EditText) findViewById(R.id.NAME);
            RadioButton r = (RadioButton) findViewById(R.id.male);
            String id = ID_Field.getText().toString();
            String age = Age_Field.getText().toString();
            String name = Name_Field.getText().toString();
            String gender;
            if (r.isChecked())
                gender = "Male";
            else
                gender = "Female";
            String tableName = name + "_" + id + "_" + age + "_" + gender;

            try {
                db.beginTransaction();
                try {
                    //Inserting the Values in the database
                    db.execSQL("Insert into " + tableName + " values(" + "'" + Entry++ + "','" + time + "','" + event.values[0] + "','" + event.values[1] + "','" + event.values[2] + "');");
                    db.setTransactionSuccessful();
                    series1.setColor(Color.GREEN);
                    series2.setColor(Color.RED);
                    series3.setColor(Color.BLUE);
                    //Plotting the Data on the Graph
                    series1.appendData(new DataPoint(time, event.values[0]), true, 100);
                    series2.appendData(new DataPoint(time, event.values[1]), true, 100);
                    series3.appendData(new DataPoint(time, event.values[2]), true, 100);
                    time++;//Incrementing the time in order to avoid conflicts

                } catch (SQLiteException e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

                } finally {
                   db.endTransaction();
                }
            } catch (SQLiteException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    // Setting the parameters for the Graph
    public void graphSet(GraphView graph) {
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(time);
        graph.getViewport().setMaxX(time+10);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(-10);
        graph.getViewport().setMaxY(10);
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().setGridColor(Color.LTGRAY);
        graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.BOTH);
        graph.setTitle("HEARTBEAT");
        graph.setTitleColor(Color.WHITE);
        graph.addSeries(series1);
        graph.addSeries(series2);
        graph.addSeries(series3);
        graph.getViewport().setScrollable(true);
    }

    //Run Button Functionality
    public int onRunClick(final View v) {
        run_button = (Button) findViewById(R.id.button_run);
        final GraphView graph = (GraphView) findViewById(R.id.graph);
        EditText ID_Field = (EditText) findViewById(R.id.ID);
        EditText Age_Field = (EditText) findViewById(R.id.AGE);
        EditText Name_Field = (EditText) findViewById(R.id.NAME);
        RadioButton r = (RadioButton) findViewById(R.id.male);
        RadioGroup Gender = (RadioGroup) findViewById(R.id.radioGroup);
        upload=(Button)findViewById(R.id.button_upload);
        download=(Button)findViewById(R.id.button_download);
        upload.setClickable(false);
        download.setClickable(false);

        //Adding a Text Watcher to check the changes in the text field
        TextWatcher check_text = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                GraphView graph = (GraphView) findViewById(R.id.graph);
                graphSet(graph);
                Button run_button = (Button) findViewById(R.id.button_run);
                run_button.setClickable(true);
                last_x1 = 0;
                SM.unregisterListener(mSensorListener, mySensor);
                //Resetting the Data Series once the Text is changed.
                series1.resetData(new DataPoint[]{
                        new DataPoint(time,0)
                });
                series2.resetData(new DataPoint[]{
                        new DataPoint(time,0)
                });
                series3.resetData(new DataPoint[]{
                        new DataPoint(time,0)
                });
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

            }
        };

        //Retrieving the Data from the Text Fields
        String id = ID_Field.getText().toString();
        String age = Age_Field.getText().toString();
        String name = Name_Field.getText().toString();
        String gender;
        if (r.isChecked())
            gender = "Male";
        else
            gender = "Female";

        //Adding Text Change Listener for the Fields
        ID_Field.addTextChangedListener(check_text);
        Age_Field.addTextChangedListener(check_text);
        Name_Field.addTextChangedListener(check_text);

        // Validating the Entries in the Field
        if (id.matches("")) {
            Toast.makeText(getApplicationContext(), "Enter ID", Toast.LENGTH_SHORT).show();
            return -1;
        }
        if (age.matches("")) {
            Toast.makeText(getApplicationContext(), "Enter AGE", Toast.LENGTH_SHORT).show();
            return -1;
        }
        if (name.matches("")) {
            Toast.makeText(getApplicationContext(), "Enter NAME", Toast.LENGTH_SHORT).show();
            return -1;
        }
        if (Gender.getCheckedRadioButtonId() == -1) {
            Toast.makeText(getApplicationContext(), "Select GENDER", Toast.LENGTH_SHORT).show();
            return -1;
        }

        // Plotting the Graph on the Graph View
        else {
            run_button.setClickable(false);
            String tableName = name + "_" + id + "_" + age + "_" + gender;
            try {
                db = openOrCreateDatabase(Environment.getExternalStorageDirectory() + "/Android/Data/CSE535_ASSIGNMENT2/Assignment2_7.db", Context.MODE_PRIVATE, null);
                db.beginTransaction();
                try {
                    //Creating the Table in the Database for a particular user
                    db.execSQL("create table if not exists " + tableName + "(" + "Entry INT PRIMARY KEY, Time DOUBLE ,x REAL, y REAL, z REAL);");
                    Cursor c = db.rawQuery("Select * from " + tableName + "", null); //Reading data from the Table
                    Entry=c.getCount();
                    c.close();
                    db.setTransactionSuccessful();
                }
                catch (SQLiteException e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                finally {
                    db.endTransaction();
                }
            } catch (SQLiteException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            graph.addSeries(series1);
            graph.addSeries(series2);
            graph.addSeries(series3);
            stop_pressed = false;
            db = openOrCreateDatabase(Environment.getExternalStorageDirectory() + "/Android/Data/CSE535_ASSIGNMENT2/Assignment2_7.db", Context.MODE_PRIVATE, null);
            SM.registerListener(mSensorListener, mySensor, 1000000);
        }
        return 1;
    }


    private void enable_button() {
        //Download Button Functionality
        download.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                EditText ID_Field = (EditText) findViewById(R.id.ID);
                EditText Age_Field = (EditText) findViewById(R.id.AGE);
                EditText Name_Field = (EditText) findViewById(R.id.NAME);
                RadioGroup Gender = (RadioGroup) findViewById(R.id.radioGroup);
                ID_Field.setEnabled(false);
                Age_Field.setEnabled(false);
                Name_Field.setEnabled(false);
                Gender.setEnabled(false);
                //Deleting the file if it is already present
                File f = new File(Environment.getExternalStorageDirectory() + "/Android/Data/CSE535_ASSIGNMENT2-Extra/Assignment2_7.db");
                if (f.exists()) {
                    f.delete();
                }
                downloading = new ProgressDialog(MainActivity.this);
                downloading.setTitle("Downloading");
                downloading.setMessage("Please wait...");
                downloading.show();

                dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                Uri uri = Uri.parse("http://10.218.110.136/CSE535Fall17Folder/Assignment2_7.db");
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                //Enabling the application to access WIFI or the Mobile Network
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                            .setDestinationInExternalPublicDir("/Android/Data/CSE535_ASSIGNMENT2-Extra/", "Assignment2_7.db");
                    Long reference = dm.enqueue(request);

                //Setting 5 seconds delay for the Application to complete downloading
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onDownloadClick();
                        }
                    }, 5000);
                }


            public int onDownloadClick(){
                series1.setColor(Color.GREEN);
                series2.setColor(Color.RED);
                series3.setColor(Color.BLUE);

                SM.unregisterListener(mSensorListener, mySensor);
                downloading.dismiss();
                GraphView graph = (GraphView) findViewById(R.id.graph);
                graph.removeAllSeries();
                EditText ID_Field = (EditText) findViewById(R.id.ID);
                EditText Age_Field = (EditText) findViewById(R.id.AGE);
                EditText Name_Field = (EditText) findViewById(R.id.NAME);
                RadioGroup Gender = (RadioGroup) findViewById(R.id.radioGroup);
                RadioButton r = (RadioButton) findViewById(R.id.male);
                String id = ID_Field.getText().toString();
                String age = Age_Field.getText().toString();
                String name = Name_Field.getText().toString();

                if (id.matches("")) {
                    Toast.makeText(getApplicationContext(), "Enter ID", Toast.LENGTH_SHORT).show();
                    ID_Field.setEnabled(true);
                    Age_Field.setEnabled(true);
                    Name_Field.setEnabled(true);
                    Gender.setEnabled(true);
                    return -1;
                }
                if (age.matches("")) {
                    Toast.makeText(getApplicationContext(), "Enter AGE", Toast.LENGTH_SHORT).show();
                    ID_Field.setEnabled(true);
                    Age_Field.setEnabled(true);
                    Name_Field.setEnabled(true);
                    Gender.setEnabled(true);
                    return -1;
                }
                if (name.matches("")) {
                    Toast.makeText(getApplicationContext(), "Enter NAME", Toast.LENGTH_SHORT).show();
                    ID_Field.setEnabled(true);
                    Age_Field.setEnabled(true);
                    Name_Field.setEnabled(true);
                    Gender.setEnabled(true);
                    return -1;
                }
                if (Gender.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(getApplicationContext(), "Select GENDER", Toast.LENGTH_SHORT).show();
                    ID_Field.setEnabled(true);
                    Age_Field.setEnabled(true);
                    Name_Field.setEnabled(true);
                    Gender.setEnabled(true);
                    return -1;
                }
                else {
                    String gender;
                    if (r.isChecked())
                        gender = "Male";
                    else
                        gender = "Female";
                    String tableName = name + "_" + id + "_" + age + "_" + gender;
                    try {
                        //Reading the Data from the DataBase
                        db = openOrCreateDatabase(Environment.getExternalStorageDirectory() + "/Android/Data/CSE535_ASSIGNMENT2-Extra/Assignment2_7.db", Context.MODE_PRIVATE, null);
                        Cursor c = db.rawQuery("Select * from " + tableName + " order by Time desc limit 10", null); //Stroing the Recent 10 second data
                        int time[] = new int[c.getCount()];
                        double x_values[] = new double[c.getCount()];
                        double y_values[] = new double[c.getCount()];
                        double z_values[] = new double[c.getCount()];
                        int i = 0;
                        if (c.getCount() > 0) {
                            c.moveToFirst();
                            do {
                                time[i] = c.getInt(c.getColumnIndex("Time"));
                                x_values[i] = c.getDouble(c.getColumnIndex("x"));
                                y_values[i] = c.getDouble(c.getColumnIndex("y"));
                                z_values[i] = c.getDouble(c.getColumnIndex("z"));
                                i++;
                            } while (c.moveToNext() && i < 10);
                            c.close();
                        }
                        series1.resetData(new DataPoint[]{
                                new DataPoint(time[c.getCount()-1], x_values[c.getCount()-1])
                        });
                        series2.resetData(new DataPoint[]{
                                new DataPoint(time[c.getCount()-1], x_values[c.getCount()-1])
                        });
                        series3.resetData(new DataPoint[]{
                                new DataPoint(time[c.getCount()-1], x_values[c.getCount()-1])
                        });
                        graph.addSeries(series1);
                        graph.addSeries(series2);
                        graph.addSeries(series3);
                        //Plotting the data on the Graph
                        for (i = (c.getCount()-2); i >= 0; i--) {
                            System.out.println(i + " " + time[i] + " " + x_values[i] + " " +  y_values[i] + " " + z_values[i]);
                            series1.appendData(new DataPoint(time[i], x_values[i]), true, 100);
                            series2.appendData(new DataPoint(time[i], y_values[i]), true, 100);
                            series3.appendData(new DataPoint(time[i], z_values[i]), true, 100);
                        }
                    } catch (SQLException e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                ID_Field.setEnabled(true);
                Age_Field.setEnabled(true);
                Name_Field.setEnabled(true);
                Gender.setEnabled(true);
                return 1;
            }

        });

        //Upload button Functionality
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialFilePicker()
                        .withActivity(MainActivity.this)
                        .withRequestCode(10)
                        .withFilter(Pattern.compile(".*\\.db$"))//Showing only files with extension DB.
                        .start();
                //  button.setText("Changed");

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 100 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)){
            enable_button();
        }else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},100);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {


        if(requestCode == 10 && resultCode == RESULT_OK){

            //Creating a Progress Bar
            uploading = new ProgressDialog(MainActivity.this);
            uploading.setTitle("Uploading");
            uploading.setMessage("Please wait...");
            uploading.show();

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {

                    File f  = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/Data/CSE535_ASSIGNMENT2/Assignment2_7.db");
                    String content_type  = "application/x-sqlite3";

                    String file_path = f.getAbsolutePath();
                    OkHttpClient client = new OkHttpClient();
                    RequestBody file_body = RequestBody.create(MediaType.parse(content_type),f);

                    RequestBody request_body = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("type",content_type)
                            .addFormDataPart("uploaded_file",file_path.substring(file_path.lastIndexOf("/")+1), file_body)
                            .build();


                    Request request = new Request.Builder()
                            .url("http://10.218.110.136/CSE535Fall17Folder/UploadToServer.php")
                            .post(request_body)
                            .build();

                    try {
                        Response response = client.newCall(request).execute();

                        if(!response.isSuccessful()){
                            throw new IOException("Error : "+response);
                        }

                        uploading.dismiss();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            t.start();

        }
    }
}

