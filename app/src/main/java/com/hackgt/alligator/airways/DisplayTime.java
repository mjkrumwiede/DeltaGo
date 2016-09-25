package com.hackgt.alligator.airways;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class DisplayTime extends AppCompatActivity {
    private static String message;
    private String airportCode;
    private final static String apikey = "WISwm1hTrfWfZGTDxULy1csrxNQddEd4";
    private final static String baseUrl = "https://demo30-test.apigee.net/v1/hack/";
    private String terminal;
    private Date timeToLeave;
    private Calendar currentDate = Calendar.getInstance();
    private Calendar originDate = currentDate;
    private boolean differentDays = false;
    private int step = 0;
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
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        step = 0;
        AsyncTask task = new RetrieveFeedTask().execute();
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
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = df.format(currentDate.getTime());
            String formattedDate2 = sf.format(originDate.getTime());
            formattedDate = formattedDate2;
            // Do some validation here

            try {
                URL url = new URL(baseUrl + "status?flightNumber=" + message + "&flightOriginDate=" +
                        formattedDate + "&apikey=" + apikey);
                urlString = url.toString();

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
                check = checkArr.getJSONObject(0);
                String test = check.getString("departureAirportName");
                airportCode = check.getString("departureAirportCode");
                if (airportCode.equals("ATL")) {
                    terminal = check.getString("departureTerminal");
                } else {
                    check = checkArr.getJSONObject(1);
                    test = check.getString("departureAirportName");
                    airportCode = check.getString("departureAirportCode");
                    terminal = check.getString("departureTerminal");
                }

                String startTime = "";
                if (check.has("departureLocalTimeEstimatedActual")) {
                    startTime = check.getString("departureLocalTimeEstimatedActual");
                }   else {
                    startTime = check.getString("departureLocalTimeScheduled");
                }
                String[] args = startTime.split("T");
                String testingDate = args[0];
                SimpleDateFormat testF = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                String testDate = testF.format(originDate.getTime());
                String[] args2 = testDate.split("-");
                if ((testDate != testingDate) && (!differentDays)) {
                    String day_of_month = args2[2];
                    originDate.set(Calendar.DAY_OF_MONTH, (Integer.parseInt(day_of_month) -1));
                    differentDays = true;
                } else {
                    startTime = args[1];
                    args = startTime.split("-");
                    startTime = args[0]; // HH:MM:SS
                    args = startTime.split(":");
                    String hours = args[0];
                    String mins = args[1];
                    SimpleDateFormat df = new java.text.SimpleDateFormat("HH:mm", Locale.US);
                    String time = hours + ":" + mins;
                    try {
                        timeToLeave = df.parse(time);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    switch (terminal) {
                        case "Domestic Term-South":
                            terminal = "T South Checkpoint";
                            break;
                        case "International Term":
                            terminal = "International";
                            break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (step == 0) {
                step = 1;
                new RetrieveFeedTask().execute();
            } else if (step == 1) {
                step = 2;
                new TsaTask().execute();
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
                    String[] times = waitTime.split("-");
                    waitTime = times[1];
                    times = waitTime.split("min");
                    waitTime = times[0];
                    SimpleDateFormat df = new java.text.SimpleDateFormat("mm", Locale.US);
                    Date subtract = df.parse(waitTime);
                    long diff = timeToLeave.getTime() - subtract.getTime();
                    SimpleDateFormat df2 = new java.text.SimpleDateFormat("HH:mm", Locale.US);
                    long seconds = diff / 1000;
                    long minutes = (seconds / 60);
                    long hours = minutes / 60;
                    minutes = minutes%60;
                    waitTime = hours + ":" + minutes;
                    timeToLeave = df.parse(waitTime);
                    responseView.setText("You should leave by " + waitTime);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
