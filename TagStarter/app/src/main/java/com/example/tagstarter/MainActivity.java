package com.example.tagstarter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fridgetag.FTObject;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_TREE_PARSE = 101;
    private final int PARSE_TREE_ERROR = 467;
    private final int REQUEST_PERMISSION = 206;
    private final int SUCCESS = 200;
    private final int CANCEL = 406;
    private final int OBJECT = 300;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void launchIntent(View view) {
        // hardcode real quick
        String fileName = "test.txt";
        String destinationFile = "/storage/emulated/0/opendatakit/";

        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.example.fridgetag");
        launchIntent.putExtra("fileName", fileName);
        launchIntent.putExtra("destinationFile", destinationFile);
        launchIntent.setFlags(0);
        if (launchIntent != null) {
            //null pointer check in case package name was not found
            startActivityForResult(launchIntent, REQUEST_TREE_PARSE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == PARSE_TREE_ERROR) {
                Toast.makeText(this, "ERROR: Could not create a filesystem.", Toast.LENGTH_LONG).show();
            } else if (resultCode == 401) {
                Toast.makeText(this, "ERROR: Permission denied.", Toast.LENGTH_LONG).show();
            } else if (resultCode == SUCCESS) {
                Toast.makeText(this, "SUCCESS: Fridge tag data downloaded.", Toast.LENGTH_LONG).show();
            } else if (resultCode == CANCEL) {
                Toast.makeText(this, "CANCEL: The operation has been canceled.", Toast.LENGTH_LONG).show();
            } else if (resultCode == OBJECT) {
                // parse the object we just got back
                if (data != null) {
                    FTObject temp = (FTObject) data.getSerializableExtra("dates");
                    if (temp != null) {
                        Toast.makeText(this, "" + temp.size(), Toast.LENGTH_LONG).show();
                        TextView textView = findViewById(R.id.infoBox);
                        String info = "Information from the past " + temp.size() + " days:\n" +
                                "Number of alarms: " + totalAlarms(temp) + "\n" +
                                "Total time out of range: " + timeOut(temp) + "\n" +
                                "Average Temp: " + avgTemp(temp);
                        textView.setText(info);
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public String timeOut(FTObject temp) {
        double res = 0;
        for (FTObject.FTDate day : temp.lastMonth) {
            res += day.totalTimeOutRange;
        }
        return (res / 60) + "hours";
    }

    public int totalAlarms(FTObject temp) {
        int res = 0;
        for (FTObject.FTDate day : temp.lastMonth) {
            if (day.lowerAlarm == day.upperAlarm) {
                res += day.lowerAlarm ? 0 : 2;
            } else {
                res += 1;
            }
        }
        return res;
    }

    public float avgTemp(FTObject temp) {
        float res = 0;
        for (FTObject.FTDate day : temp.lastMonth) {
            res += day.avgTemp;
        }
        return res / temp.size();
    }
}
