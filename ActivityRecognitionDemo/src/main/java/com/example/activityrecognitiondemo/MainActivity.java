package com.example.activityrecognitiondemo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class MainActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "ActivityRecognitionDemo";
    private static final String ACTIVITY_RECOGNITION_ACTION = "activityrecognitiondemo.ActivityDetected";

    private GoogleApiClient activityRecognitionClient;
    private PendingIntent pendingIntent;

    private ActivityDetectedReceiver activityDetectedReceiver = new ActivityDetectedReceiver();
    private View container;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        container = findViewById(R.id.container);
        textView = (TextView) findViewById(R.id.display);

        activityRecognitionClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        activityRecognitionClient.connect();
    }

    @Override
    protected void onStop() {
        if(activityRecognitionClient.isConnected()){
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(activityRecognitionClient, pendingIntent);
        }
        activityRecognitionClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onResume() {
        registerReceiver(activityDetectedReceiver, new IntentFilter(ACTIVITY_RECOGNITION_ACTION));
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(activityDetectedReceiver);
        super.onPause();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Connected");
        Intent intent = new Intent(ACTIVITY_RECOGNITION_ACTION);
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(activityRecognitionClient, 0, pendingIntent);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public class ActivityDetectedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (ActivityRecognitionResult.hasResult(intent)) {
                ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
                DetectedActivity mostProbableActivity = result.getMostProbableActivity();
                int confidence = mostProbableActivity.getConfidence();
                final int activityType = mostProbableActivity.getType();
                Log.d(TAG, "Activity type:"+activityType);

                int color = Color.WHITE;
                String text = "";
                switch (activityType) {
                    case DetectedActivity.STILL:
                        color = Color.GREEN;
                        text = "STILL";
                        break;
                    case DetectedActivity.WALKING:
                    case DetectedActivity.RUNNING:
                    case DetectedActivity.ON_FOOT:
                        color = Color.RED;
                        text = "ON_FOOT";
                        break;
                    case DetectedActivity.ON_BICYCLE:
                        color = Color.BLUE;
                        text = "ON_BICYCLE";
                        break;
                    case DetectedActivity.IN_VEHICLE:
                        color = Color.YELLOW;
                        text = "IN_VEHICLE";
                        break;
                    case DetectedActivity.TILTING:
                        color = Color.CYAN;
                        text = "TILTING";
                        break;
                    case DetectedActivity.UNKNOWN:
                        color = Color.GRAY;
                        text = "UNKNOWN";
                        break;
                }
                container.setBackgroundColor(color);
                textView.setText(text);
            }
        }
    }

}
