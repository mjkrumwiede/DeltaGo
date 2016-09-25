package com.hackgt.alligator.airways;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class DisplayTime extends AppCompatActivity{
    private static String message;
    private String airportCode;
    private final static String apikey = "WISwm1hTrfWfZGTDxULy1csrxNQddEd4";
    private final static String baseUrl = "https://demo30-test.apigee.net/v1/hack/";
    private String terminal;
    private String airportLat;
    private String airportLong;
    private String myLat;
    private String myLong;
    private Integer hoursToAirport;
    private Integer minToAirport;
    private Integer secToAirport;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_time);

        Intent intent = getIntent();
        message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        getPermissions();

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_LOW);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);


        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        String provider=locationManager.getBestProvider(criteria, true);
        if (provider == null) {
            Log.i("INFO", "Provider is null");
            return;
        } else {
            Log.i("INFO", "Provider: " + provider);
        }
        MyCurrentLocationListener locationListener = new MyCurrentLocationListener();
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            Log.i("INFO","You're in!");
        }catch(SecurityException e){
            Log.i("INFO","Didn't Work!");
        }

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        AsyncTask task = new RetrieveFeedTask().execute();
//        if (task.getStatus().equals(AsyncTask.Status.FINISHED)) {
            new TsaTask().execute();
//        }
        new GoogleTask().execute();

    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("DisplayTime Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    class RetrieveFeedTask extends AsyncTask<Void, Void, String> {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        TextView responseView = (TextView) findViewById(R.id.responseView);
        String urlString;
        String apikey = "WISwm1hTrfWfZGTDxULy1csrxNQddEd4";
        String baseUrl = "https://demo30-test.apigee.net/v1/hack/";

        private Exception exception;

        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            responseView.setText("");
        }

        protected String doInBackground(Void... urls) {
            Calendar c = Calendar.getInstance();
            System.out.println("Current time => " + c.getTime());

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = df.format(c.getTime());
            // Do some validation here

            try {
                URL url = new URL(baseUrl + "status?flightNumber=" + message + "&flightOriginDate=" +
                        formattedDate + "&apikey=" + apikey);


                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            if (response == null) {
                response = "THERE WAS AN ERROR";
            }
            progressBar.setVisibility(View.GONE);
            Log.i("INFO", response);
//            responseView.setText(response);
            try {
                JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
                JSONObject check = object.getJSONObject("flightStatusResponse");
                check = check.getJSONObject("statusResponse");
                check = check.getJSONObject("flightStatusTO");
                JSONArray checkArr = check.getJSONArray("flightStatusLegTOList");
                check = checkArr.getJSONObject(1);
                String test = check.getString("departureAirportName");
                airportCode = check.getString("departureAirportCode");
                airportLat = check.getString("arrivalTsoagLatitudeDecimal");
                airportLong = check.getString("arrivalTsoagLongitudeDecimal");
                terminal = check.getString("departureTerminal");
                switch (terminal) {
                    case "Domestic Term-South":
                        terminal = "T South Checkpoint";
                        break;
                    case "International Term":
                        terminal = "International";
                        break;
                }
                responseView.setText(test);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class TsaTask extends AsyncTask<Void, Void, String> {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        TextView responseView = (TextView) findViewById(R.id.responseView);
        String urlString;
        String apikey = "WISwm1hTrfWfZGTDxULy1csrxNQddEd4";
        String baseUrl = "https://demo30-test.apigee.net/v1/hack/";

        private Exception exception;

        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            responseView.setText("");
        }

        protected String doInBackground(Void... urls) {
            try {
                URL url = new URL(baseUrl + "tsa?airport=" + "ATL" + "&apikey=" + apikey);
                urlString = url.toString();
                Log.i("INFO",urlString);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            }   catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            if (response == null) {
                response = "THERE WAS AN ERROR";
            }
            progressBar.setVisibility(View.GONE);
            Log.i("INFO", response);
//            responseView.setText(response);
            try {
                JSONObject tsaObject = (JSONObject) new JSONTokener(response).nextValue();
                JSONArray airport = tsaObject.getJSONArray("AirportResult");
                JSONObject airportCheck = airport.getJSONObject(0);
                airportCheck = airportCheck.getJSONObject("airport");
                airport = airportCheck.getJSONArray("checkpoints");
                int id = 0;
                for (int i = 0; i < airport.length(); i++) {
                    JSONObject checkpoint = airport.getJSONObject(i);
                    if (checkpoint.getString("longname").equals(terminal) ) {
                        id = checkpoint.getInt("id");
                    }
                }
                try {
                    JSONArray waitTimes = tsaObject.getJSONArray("WaitTimeResult");
                    String waitTime = "";
                    for (int i = 0; i < waitTimes.length(); i++) {
                        JSONObject timesCheck = waitTimes.getJSONObject(i);
                        if (id == timesCheck.getInt("checkpointID")) {
                            waitTime = timesCheck.getString("waitTime");
                            i += waitTimes.length();
                        }
                    }
                    responseView.setText("Your Wait Time is " + waitTime);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    class GoogleTask extends AsyncTask<Void, Void, String> {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        TextView responseView = (TextView) findViewById(R.id.responseView);
        String urlString;
        String baseUrl = "https://maps.googleapis.com/maps/api/directions/json?";
        String apikey = "AIzaSyDZl2HiMQF1L9eEwFoCkJ5oHBo5G_hJWvQ";

        private Exception exception;

        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            responseView.setText("");
        }

        protected String doInBackground(Void... urls) {
            try {
                while(myLat==null){

                }
                URL url = new URL(baseUrl + "origin="+myLat+","+myLong+"&destination="+airportLat+","+airportLong+
                "&key="+apikey);
                urlString = url.toString();
                Log.i("INFO",urlString);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            if (response == null) {
                response = "THERE WAS AN ERROR";
            }
            progressBar.setVisibility(View.GONE);
            Log.i("INFO", response);
            //responseView.setText(response);
            try {
                JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
                JSONArray checkARR = object.getJSONArray("routes");
                JSONObject check = checkARR.getJSONObject(0);
                JSONArray checkARR2 = check.getJSONArray("legs");
                check = checkARR2.getJSONObject(0);
                int timeToTravel=check.getJSONObject("duration").getInt("value");
                hoursToAirport = (int)Math.floor(timeToTravel/3600);
                timeToTravel = timeToTravel % 3600;
                minToAirport = (int)Math.floor(timeToTravel/60);
                timeToTravel = timeToTravel % 60;
                secToAirport = timeToTravel;
                responseView.setText("You are "+hoursToAirport+" hrs, "+minToAirport+" min, and " +
                        secToAirport+" sec from Airport!");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class MyCurrentLocationListener implements android.location.LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            myLat = Double.toString(location.getLatitude());
            myLong = Double.toString(location.getLongitude());
            Log.i("INFO","DICKS OUT FOR HARAMBE");
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }

    public void getPermissions(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }
}
