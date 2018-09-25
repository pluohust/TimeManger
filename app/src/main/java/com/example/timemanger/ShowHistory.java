package com.example.timemanger;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class ShowHistory extends AppCompatActivity {

    private List<String> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_history);

        //隐藏标题
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        data = new ArrayList<>();
        LoadInformation();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                ShowHistory.this, android.R.layout.simple_list_item_1, data
        );
        ListView listView = (ListView) findViewById(R.id.allhistory);
        listView.setAdapter(adapter);
    }

    private boolean IsExist() {
        try {
            File f = new File("data");
            if (!f.exists()) {
                return  false;
            }
        } catch (Exception e) {
            return false;
        }
        return  true;
    }

    public void LoadInformation() {
        FileInputStream in = null;
        BufferedReader reader = null;

        if (!IsExist()) {
            return;
        }

        data.clear();

        try {
            in = openFileInput("data");
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                AllDataInformation tmpalldata = new AllDataInformation();
                String[] splitstr = line.split(" ");
                if (splitstr.length > 5) {
                    String tmpStr = "";
                    tmpStr += (splitstr[0]).trim() + "    ";
                    Long tmpLong = Long.parseLong(splitstr[1]);
                    Double tmpDouble = tmpLong * 1.0 / 3600;
                    tmpStr += String.format("0.0", tmpDouble) + "    ";
                    tmpLong = Long.parseLong(splitstr[2]);
                    tmpDouble = tmpLong * 1.0 / 3600;
                    tmpStr += String.format("0.0", tmpDouble) + "    ";
                    tmpLong = Long.parseLong(splitstr[3]);
                    tmpDouble = tmpLong * 1.0 / 3600;
                    tmpStr += String.format("0.0", tmpDouble) + "    ";
                    tmpLong = Long.parseLong(splitstr[4]);
                    tmpDouble = tmpLong * 1.0 / 3600;
                    tmpStr += String.format("0.0", tmpDouble) + "    ";
                    tmpLong = Long.parseLong(splitstr[5]);
                    tmpDouble = tmpLong * 1.0 / 3600;
                    tmpStr += String.format("0.0", tmpDouble);
                    data.add(tmpStr);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
