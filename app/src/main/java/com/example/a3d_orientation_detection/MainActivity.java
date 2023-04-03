package com.example.a3d_orientation_detection;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    private SensorManager SM;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor magnetometer;
    float acclX =0, acclY=0,acclZ=0,gyrX =0, gyrY=0,gyrZ=0,magX =0, magY=0,magZ=0,pitchAcc=0,rollAcc =0,yawAcc =0
            ,pitchGyro=0,rollGyro=0,yawGyro=0;
    private TextView accX,accY,accZ,gyX,gyY,gyZ,mmX,mmY,mmZ;
    private Button resetBtn;

    private long deltaTime =0,lastRecordingTime=0;

    private ImageView image3d;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accX = findViewById(R.id.editTextAccX);
        accY = findViewById(R.id.editTextAccY);
        accZ = findViewById(R.id.editTextAccZ);
        gyX = findViewById(R.id.editTextGyrX);
        gyY = findViewById(R.id.editTextGyrY);
        gyZ = findViewById(R.id.editTextGyrZ);
        mmX = findViewById(R.id.editTextMmX);
        mmY = findViewById(R.id.editTextMmY);
        mmZ = findViewById(R.id.editTextMmZ);
        image3d = findViewById(R.id.imageView);
        resetBtn = findViewById(R.id.resetBtn);

        SM = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = SM.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magnetometer = SM.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        resetBtn.setOnClickListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SM.unregisterListener(this,accelerometer);
        SM.unregisterListener(this,gyroscope);
        SM.unregisterListener(this,magnetometer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SM.registerListener(MainActivity.this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
        SM.registerListener(MainActivity.this,gyroscope,SensorManager.SENSOR_DELAY_NORMAL);
        SM.registerListener(MainActivity.this,magnetometer,SensorManager.SENSOR_DELAY_NORMAL);
        pitchGyro =0;
        yawGyro =0;
        rollGyro =0;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                acclX = event.values[0];
                acclY = event.values[1];
                acclZ = event.values[2];
                accX.setText(String.valueOf(acclX));
                accY.setText(String.valueOf(acclY));
                accZ.setText(String.valueOf(acclZ));
                calculateAccRotation();
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyrX = event.values[0];
                gyrY = event.values[1];
                gyrZ = event.values[2];
                gyX.setText(String.valueOf(gyrX));
                gyY.setText(String.valueOf(gyrY));
                gyZ.setText(String.valueOf(gyrZ));
                calculateGyrRotation(event.timestamp);
                filter();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magX = event.values[0];
                magY = event.values[1];
                magZ = event.values[2];
                mmX.setText(String.valueOf(magX));
                mmY.setText(String.valueOf(magY));
                mmZ.setText(String.valueOf(magZ));
                break;
        }

    }

    private void filter() {
        double  alpha = 0.98;
        double result_pitch = pitchGyro * alpha + (1 - alpha) * pitchAcc;
        double result_roll = rollGyro * alpha + (1 - alpha) * rollAcc;
        double result_yaw = yawGyro * alpha + (1 - alpha) * yawAcc;
        mmX.setText(String.valueOf(result_pitch));
        mmY.setText(String.valueOf(result_roll));
        mmZ.setText(String.valueOf(yawGyro));
        image3d.setRotationX((float) result_pitch);
        image3d.setRotationY((float) result_roll);
        image3d.setRotation((float) result_yaw);

    }

    private void calculateGyrRotation(long eventTime) {
        deltaTime = eventTime - lastRecordingTime;
        lastRecordingTime = eventTime;
        pitchGyro = (float) (pitchGyro + ((-gyrX) * deltaTime * 0.000000001f * 57.3f))%360;
        rollGyro = (float) (rollGyro + ((gyrY) * deltaTime * 0.000000001f * 57.3f))%360;
        yawGyro = (float) (yawGyro + ((-gyrZ) * deltaTime * 0.000000001f * 57.3f))%360;
       // System.out.println(pitchGyro + "______" + rollGyro + "____________" + yawGyro);

    }

    private void calculateAccRotation() {
        double resultant = Math.sqrt(acclX*acclX + acclY*acclY + acclZ*acclZ);
        pitchAcc = (float) Math.toDegrees(Math.asin(-(acclY)/resultant));
        rollAcc = (float) Math.toDegrees(Math.asin(acclX/resultant));
        float temp1 = (float) ((-magX) * Math.cos(rollAcc) + magZ * Math.sin(rollAcc));
        float temp2 = (float) ((magX * Math.sin(pitchAcc) * Math.sin(rollAcc)) + (magY * Math.cos(pitchAcc)) +  (magZ * Math.sin(pitchAcc) * Math.cos(rollAcc)));
        yawAcc = (float) Math.toDegrees(Math.atan2(temp2,temp1));

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.resetBtn){
           pitchAcc=0;rollAcc =0;yawAcc =0;
           pitchGyro=0;rollGyro=0;yawGyro=0;
            image3d.setRotationX(pitchGyro);
            image3d.setRotationY(rollGyro);
            image3d.setRotation(yawGyro);
        }
    }
}