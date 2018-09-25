package com.example.timemanger;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Message;
import android.service.wallpaper.WallpaperService;
import android.support.annotation.IntegerRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.Buffer;
import java.nio.DoubleBuffer;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.widget.Toast;

import java.util.logging.LogRecord;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static List<Button> allButtons = new ArrayList<>();

    public Button currentButton = null;

    public TextView statusTextView = null;
    public TextView allinforTextView = null;

    public Date startData = null;

    public static final int UPDATE_TIME = 1;

    public LinearLayout treeGroup = null;

    //文件存储
    public TimeInformation timeInformation = null;

    //文件权限
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };

    public void ShowTree (long numTree) {
        treeGroup.removeAllViews();
        for (long i = 0; i < numTree; i++) {
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(R.drawable.tree); //图片资源
            treeGroup.addView(imageView);
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TIME:
                    Date nowData = new Date(System.currentTimeMillis());
                    if (null != currentButton) {
                        long diff = nowData.getTime() - startData.getTime();
                        long numTree = diff / (1000 * 60 * 30);
                        if (numTree > 12)
                            numTree = 12;
                        ShowTree(numTree);
                        /*Long hour = diff / (1000 * 60 * 60);
                        Long min = diff / (1000 * 60) - hour * 60;
                        Long second = diff / 1000 - hour * 60 * 60 - min * 60;
                        if (null != statusTextView) {
                            String oldText = "正在" + currentButton.getText().toString() + "：";
                            if (hour > 0)
                                oldText = oldText + hour + "小时";
                            if (hour > 0 || min > 0)
                                oldText = oldText + min + "分";
                            oldText = oldText + second + "秒";
                            statusTextView.setText(oldText);
                        }*/
                    }
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private Timer timer = new Timer(true);
    private TimerTask task = new TimerTask() {
        @Override
        public void run() {
            try {
                Thread.sleep(1000 * 60);
                Message message = new Message();
                message.what = UPDATE_TIME;
                handler.sendMessage(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    public void ShowAllinforTextView(TimeInformation timeInformation) {
        String allline = "";
        allline += timeInformation.returnTodayData() + "\n";
        allline += Integer.toString(timeInformation.retureFlagStatus()) + "\n";
        allline += Long.toString(timeInformation.returnStartTime()) + "\n";
        allline += timeInformation.returnTotalTime() + "\n";
        allline += Long.toString(timeInformation.returnComputerTime()) + "\n";
        allline += Long.toString(timeInformation.returnEnglishTime()) + "\n";
        allline += Long.toString(timeInformation.returnWritingTime()) + "\n";
        allline += Long.toString(timeInformation.returnReadTime()) + "\n";
        allinforTextView.setText(allline);
    }

    public void ShowAllinforTextView() {
        String showTime = timeInformation.returnTotalTime() + "小时";
        allinforTextView.setText(showTime);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //申请文件权限
        verifyStoragePermissions(MainActivity.this);

        //隐藏标题
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        Button computerButton = (Button) findViewById(R.id.computer); //按钮操作
        computerButton.setOnClickListener(this);
        allButtons.add(computerButton);
        Button englishButon = (Button) findViewById(R.id.english);
        englishButon.setOnClickListener(this);
        allButtons.add(englishButon);
        Button writingButton = (Button) findViewById(R.id.writing);
        writingButton.setOnClickListener(this);
        allButtons.add(writingButton);
        Button readingButton = (Button) findViewById(R.id.reading);
        readingButton.setOnClickListener(this);
        allButtons.add(readingButton);

        statusTextView = (TextView) findViewById(R.id.status);
        allinforTextView = (TextView) findViewById(R.id.all_info);
        //测试日期显示代码
        /*Date nowData = new Date(System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        simpleDateFormat.setTimeZone(TimeZone.getDefault());
        statusTextView.setText(simpleDateFormat.format(nowData));*/

        Button historyButton = (Button) findViewById(R.id.history);
        historyButton.setOnClickListener(this);
        Button exitButton = (Button) findViewById(R.id.exit);
        exitButton.setOnClickListener(this);

        treeGroup = (LinearLayout) findViewById(R.id.tree_group); //显示时间

        timer.schedule(task, 0, 1000);

        //读取存储信息
        timeInformation = new TimeInformation();
        timeInformation.LoadInformation();

        ShowAllinforTextView();

        switch (timeInformation.retureFlagStatus()) {
            case TimeInformation.FLAGSTATUS_NOTASK:
                startData = new Date(System.currentTimeMillis()); //获取当前时间
                currentButton = null;
                for (Button button : allButtons) {
                    button.setBackgroundColor(Color.parseColor("#4876FF"));
                    button.setEnabled(true);
                }
                statusTextView.setText("正在休息");
                statusTextView.setTextColor(Color.parseColor("#FF0000"));
                break;
            case TimeInformation.FLAGSTATUS_COMPUTER:
                currentButton = (Button) findViewById(R.id.computer);
                for (Button button : allButtons) {
                    if (button != currentButton) {
                        button.setBackgroundColor(Color.parseColor("#5B5B5B"));
                        button.setEnabled(false);
                    }
                }
                currentButton.setBackgroundColor(Color.parseColor("#00CD00"));
                setStatus(R.id.computer);
                startData = new Date(timeInformation.returnStartTime());
                break;
            case TimeInformation.FLAGSTATUS_ENGLISH:
                currentButton = (Button) findViewById(R.id.english);
                for (Button button : allButtons) {
                    if (button != currentButton) {
                        button.setBackgroundColor(Color.parseColor("#5B5B5B"));
                        button.setEnabled(false);
                    }
                }
                currentButton.setBackgroundColor(Color.parseColor("#00CD00"));
                setStatus(R.id.english);
                startData = new Date(timeInformation.returnStartTime());
                break;
            case TimeInformation.FLAGSTATUS_WRITING:
                currentButton = (Button) findViewById(R.id.writing);
                for (Button button : allButtons) {
                    if (button != currentButton) {
                        button.setBackgroundColor(Color.parseColor("#5B5B5B"));
                        button.setEnabled(false);
                    }
                }
                currentButton.setBackgroundColor(Color.parseColor("#00CD00"));
                setStatus(R.id.writing);
                startData = new Date(timeInformation.returnStartTime());
                break;
            case TimeInformation.FLAGSTATUS_READING:
                currentButton = (Button) findViewById(R.id.reading);
                for (Button button : allButtons) {
                    if (button != currentButton) {
                        button.setBackgroundColor(Color.parseColor("#5B5B5B"));
                        button.setEnabled(false);
                    }
                }
                currentButton.setBackgroundColor(Color.parseColor("#00CD00"));
                setStatus(R.id.reading);
                startData = new Date(timeInformation.returnStartTime());
                break;
        }

        /*new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    Message message = new Message();
                    message.what = UPDATE_TIME;
                    handler.sendMessage(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();*/
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.computer:
                if (null == currentButton) {
                    currentButton = (Button) findViewById(v.getId());
                    for (Button button : allButtons) {
                        if (button != currentButton) {
                            button.setBackgroundColor(Color.parseColor("#5B5B5B"));
                            button.setEnabled(false);
                        }
                    }
                    currentButton.setBackgroundColor(Color.parseColor("#00CD00"));
                    setStatus(v.getId());
                    startData.setTime(System.currentTimeMillis());
                    timeInformation.storeStartTime(System.currentTimeMillis());
                    timeInformation.storeFlagStatus(TimeInformation.FLAGSTATUS_COMPUTER);
                } else if ((Button) findViewById(v.getId()) == currentButton) {
                    currentButton = null;
                    for (Button button : allButtons) {
                        button.setBackgroundColor(Color.parseColor("#4876FF"));
                        button.setEnabled(true);
                    }
                    statusTextView.setText("正在休息");
                    statusTextView.setTextColor(Color.parseColor("#FF0000"));

                    timeInformation.storeFlagStatus(TimeInformation.FLAGSTATUS_NOTASK);
                    Date nowData = new Date(System.currentTimeMillis());
                    long diff_second = (nowData.getTime() - startData.getTime()) / 1000;
                    timeInformation.storeComputerTime(diff_second);
                    timeInformation.caculateTotalTime();
                    timeInformation.UpdateInformation();
                    timeInformation.StoreInformation();
                }
                timeInformation.StoreInformation();
                break;
            case R.id.english:
                if (null == currentButton) {
                    currentButton = (Button) findViewById(v.getId());
                    for (Button button : allButtons) {
                        if (button != currentButton) {
                            button.setBackgroundColor(Color.parseColor("#5B5B5B"));
                            button.setEnabled(false);
                        }
                    }
                    currentButton.setBackgroundColor(Color.parseColor("#00CD00"));
                    setStatus(v.getId());
                    startData.setTime(System.currentTimeMillis());
                    timeInformation.storeStartTime(System.currentTimeMillis());
                    timeInformation.storeFlagStatus(TimeInformation.FLAGSTATUS_ENGLISH);
                } else if ((Button) findViewById(v.getId()) == currentButton) {
                    currentButton = null;
                    for (Button button : allButtons) {
                        button.setBackgroundColor(Color.parseColor("#4876FF"));
                        button.setEnabled(true);
                    }
                    statusTextView.setText("正在休息");
                    statusTextView.setTextColor(Color.parseColor("#FF0000"));

                    timeInformation.storeFlagStatus(TimeInformation.FLAGSTATUS_NOTASK);
                    Date nowData = new Date(System.currentTimeMillis());
                    long diff_second = (nowData.getTime() - startData.getTime()) / 1000;
                    timeInformation.storeEnglishTime(diff_second);
                    timeInformation.caculateTotalTime();
                    timeInformation.UpdateInformation();
                    timeInformation.StoreInformation();
                }
                timeInformation.StoreInformation();
                break;
            case R.id.writing:
                if (null == currentButton) {
                    currentButton = (Button) findViewById(v.getId());
                    for (Button button : allButtons) {
                        if (button != currentButton) {
                            button.setBackgroundColor(Color.parseColor("#5B5B5B"));
                            button.setEnabled(false);
                        }
                    }
                    currentButton.setBackgroundColor(Color.parseColor("#00CD00"));
                    setStatus(v.getId());
                    startData.setTime(System.currentTimeMillis());
                    timeInformation.storeStartTime(System.currentTimeMillis());
                    timeInformation.storeFlagStatus(TimeInformation.FLAGSTATUS_WRITING);
                } else if ((Button) findViewById(v.getId()) == currentButton) {
                    currentButton = null;
                    for (Button button : allButtons) {
                        button.setBackgroundColor(Color.parseColor("#4876FF"));
                        button.setEnabled(true);
                    }
                    statusTextView.setText("正在休息");
                    statusTextView.setTextColor(Color.parseColor("#FF0000"));

                    timeInformation.storeFlagStatus(TimeInformation.FLAGSTATUS_NOTASK);
                    Date nowData = new Date(System.currentTimeMillis());
                    long diff_second = (nowData.getTime() - startData.getTime()) / 1000;
                    timeInformation.storeWritingTime(diff_second);
                    timeInformation.caculateTotalTime();
                    timeInformation.UpdateInformation();
                    timeInformation.StoreInformation();
                }
                timeInformation.StoreInformation();
                break;
            case R.id.reading:
                if (null == currentButton) {
                    currentButton = (Button) findViewById(v.getId());
                    for (Button button : allButtons) {
                        if (button != currentButton) {
                            button.setBackgroundColor(Color.parseColor("#5B5B5B"));
                            button.setEnabled(false);
                        }
                    }
                    currentButton.setBackgroundColor(Color.parseColor("#00CD00"));
                    setStatus(v.getId());
                    startData.setTime(System.currentTimeMillis());
                    timeInformation.storeStartTime(System.currentTimeMillis());
                    timeInformation.storeFlagStatus(TimeInformation.FLAGSTATUS_READING);
                } else if ((Button) findViewById(v.getId()) == currentButton) {
                    currentButton = null;
                    for (Button button : allButtons) {
                        button.setBackgroundColor(Color.parseColor("#4876FF"));
                        button.setEnabled(true);
                    }
                    statusTextView.setText("正在休息");
                    statusTextView.setTextColor(Color.parseColor("#FF0000"));

                    timeInformation.storeFlagStatus(TimeInformation.FLAGSTATUS_NOTASK);
                    Date nowData = new Date(System.currentTimeMillis());
                    long diff_second = (nowData.getTime() - startData.getTime()) / 1000;
                    timeInformation.storeReadingTime(diff_second);
                    timeInformation.caculateTotalTime();
                    timeInformation.UpdateInformation();
                    timeInformation.StoreInformation();
                }
                timeInformation.StoreInformation();
                break;
            case R.id.exit:
                finish();
                break;
            case R.id.history:
                Intent intent = new Intent("com.example.timemanger.ACTION_START");
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    public void setStatus (int id) {
        switch (id) {
            case R.id.computer:
                statusTextView.setText("学习计算机");
                statusTextView.setTextColor(Color.parseColor("#00FF00"));
                break;
            case R.id.english:
                statusTextView.setText("学习英语");
                statusTextView.setTextColor(Color.parseColor("#00FF00"));
                break;
            case R.id.writing:
                statusTextView.setText("写作");
                statusTextView.setTextColor(Color.parseColor("#00FF00"));
                break;
            case R.id.reading:
                statusTextView.setText("读书");
                statusTextView.setTextColor(Color.parseColor("#00FF00"));
                break;
            default:
                break;
        }
    }

    //文件权限
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return ;
        }
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    public class TimeInformation {

        public static final int FLAGSTATUS_NOTASK = 0;
        public static final int FLAGSTATUS_COMPUTER = 1;
        public static final int FLAGSTATUS_ENGLISH =2;
        public static final int FLAGSTATUS_WRITING = 3;
        public static final int FLAGSTATUS_READING = 4;

        private String todayData;
        private int flagStatus;
        private long startTime;
        private long totalTime;
        private long computerTime;
        private long englishTime;
        private long writingTime;
        private long readingTime;
        private List<AllDataInformation> detailInformation;

        public TimeInformation() {
            Date nowData = new Date(System.currentTimeMillis());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            simpleDateFormat.setTimeZone(TimeZone.getDefault());
            todayData = simpleDateFormat.format(nowData);
            flagStatus = 0;
            startTime = 0;
            totalTime = 0;
            computerTime = 0;
            englishTime = 0;
            writingTime = 0;
            readingTime = 0;
            detailInformation = new ArrayList<>();
        }

        public String returnTodayData() {
            return todayData;
        }

        public void storeFlagStatus(int flagStatus) {
            this.flagStatus = flagStatus;
        }

        public int retureFlagStatus() {
            return flagStatus;
        }

        public void storeStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long returnStartTime() {
            return startTime;
        }

        public void caculateTotalTime() {
            totalTime = computerTime + englishTime + writingTime + readingTime;
        }

        public String returnTotalTime() {
            long hour = totalTime / (60 * 60);
            long other = totalTime - hour * 60 * 60;
            double last = other * 1.0 / totalTime;
            String outTotalTime = Long.toString(hour);
            outTotalTime += String.format("0.0", last);
            return outTotalTime;
        }

        public void storeComputerTime(long computerTime) {
            if (computerTime > 1) {
                this.computerTime += computerTime;
            }
        }

        public long returnComputerTime() {
            return computerTime;
        }

        public void storeEnglishTime(long englishTime) {
            if (englishTime > 1) {
                this.englishTime += englishTime;
            }
        }

        public long returnEnglishTime() {
            return englishTime;
        }

        public void storeWritingTime(long writingTime) {
            if (writingTime > 1) {
                this.writingTime += writingTime;
            }
        }

        public long returnWritingTime() {
            return writingTime;
        }

        public void storeReadingTime(long readingTime) {
            if (readingTime > 1) {
                this.readingTime += readingTime;
            }
        }

        public long returnReadTime() {
            return readingTime;
        }

        public boolean IsExistToday() {
            try
            {
                File f=new File("data");
                if(!f.exists())
                {
                    return false;
                }
            }
            catch (Exception e)
            {
                return false;
            }
            return true;
        }

        public void LoadInformation() {
            FileInputStream in = null;
            BufferedReader reader = null;

           if (!IsExistToday())
               return;
            try {
                in = openFileInput("data");
                reader = new BufferedReader(new InputStreamReader(in));
                String line = "";
                line = reader.readLine();
                line = line.trim();
                if (!todayData.equals(line)) {
                    Toast.makeText(MainActivity.this, "日期读取错误！",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
                line = reader.readLine();
                flagStatus = Integer.parseInt(line);
                line = reader.readLine();
                startTime = Long.parseLong(line);
                line = reader.readLine();
                totalTime = Long.parseLong(line);
                line = reader.readLine();
                computerTime = Long.parseLong(line);
                line = reader.readLine();
                englishTime = Long.parseLong(line);
                line = reader.readLine();
                writingTime = Long.parseLong(line);
                line = reader.readLine();
                readingTime = Long.parseLong(line);
                detailInformation.clear();
                while ((line = reader.readLine()) != null) {
                    AllDataInformation tmpdatail = new AllDataInformation();
                    String[] splitstr = line.split(" ");
                    if (splitstr.length > 5) {
                        tmpdatail.Date = (splitstr[0]).trim();
                        tmpdatail.total = Long.parseLong(splitstr[1]);
                        tmpdatail.computer = Long.parseLong(splitstr[2]);
                        tmpdatail.english = Long.parseLong(splitstr[3]);
                        tmpdatail.write = Long.parseLong(splitstr[4]);
                        tmpdatail.read = Long.parseLong(splitstr[5]);
                        detailInformation.add(tmpdatail);
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

        public void UpdateInformation() {
            AllDataInformation tmpToday = new AllDataInformation();
            tmpToday.Date = todayData;
            tmpToday.total = totalTime;
            tmpToday.computer = computerTime;
            tmpToday.english = englishTime;
            tmpToday.write = writingTime;
            tmpToday.read = readingTime;
            for (AllDataInformation eachone : detailInformation) {
                if (eachone.Date.equals(todayData)) {
                    detailInformation.remove(eachone);
                }
            }
            detailInformation.add(tmpToday);
        }

        public void StoreInformation() {
            FileOutputStream out = null;
            BufferedWriter writer = null;
            try {
                out = openFileOutput("data", Context.MODE_PRIVATE);
                writer = new BufferedWriter(new OutputStreamWriter(out));
                writer.write(todayData);
                writer.newLine();
                writer.write(Integer.toString(flagStatus));
                writer.newLine();
                writer.write(Long.toString(startTime));
                writer.newLine();
                writer.write(Long.toString(totalTime));
                writer.newLine();
                writer.write(Long.toString(computerTime));
                writer.newLine();
                writer.write(Long.toString(englishTime));
                writer.newLine();
                writer.write(Long.toString(writingTime));
                writer.newLine();
                writer.write(Long.toString(readingTime));
                writer.newLine();
                for (AllDataInformation eachone : detailInformation) {
                    String line = "";
                    line += eachone.Date + " ";
                    line += Long.toString(eachone.total) + " ";
                    line += Long.toString(eachone.computer) + " ";
                    line += Long.toString(eachone.english) + " ";
                    line += Long.toString(eachone.write) + " ";
                    line += Long.toString(eachone.read);
                    writer.write(line);
                    writer.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            ShowAllinforTextView();
        }
    }
}
