package com.hackgt.alligator.airways;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        flightNumberEntered();
    }

    public void flightNumberEntered(){
        final EditText flightNum = (EditText) findViewById(R.id.editText);
        flightNum.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    showDisplayTime(flightNum);
                }
                return false;
            }
        });
    }

    public void showDisplayTime(EditText flightNum){
        Intent intent = new Intent(this, DisplayTime.class);
        String flightNumber = flightNum.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, flightNumber);
        startActivity(intent);
    }
}

