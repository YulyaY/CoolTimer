package com.example.android.cooltimer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private TextView textView;
    private SeekBar seekBar;
    private Button button;
    MediaPlayer mediaPlayer;
    private boolean isTimerOn;
    private CountDownTimer countDownTimer;
    private int defaultInterval;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

          textView = findViewById(R.id.textView);
          seekBar = findViewById(R.id.seekBar);
          button = findViewById(R.id.button);
          isTimerOn = false;
          sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

          seekBar.setMax(600);
          setIntervalFromSharedPreferences(sharedPreferences);

          seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
              @Override
              public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                  long progress = i*1000;
                  updateTimer(progress);
              }

              @Override
              public void onStartTrackingTouch(SeekBar seekBar) {

              }

              @Override
              public void onStopTrackingTouch(SeekBar seekBar) {

              }
          });

          sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isTimerOn) {
                    button.setText("STOP");
                    seekBar.setEnabled(false);
                    isTimerOn = true;
                    countDownTimer = new CountDownTimer(seekBar.getProgress() * 1000,
                            1000) {

                        @Override
                        public void onTick(long l) {
                            updateTimer(l);
                        }

                        @Override
                        public void onFinish() {

                            SharedPreferences sharedPreferences =
                                    PreferenceManager.
                                            getDefaultSharedPreferences(getApplicationContext());
                            if (sharedPreferences.getBoolean("enable_sound", true)) {
                                String melodyName = sharedPreferences.getString("timer_melody",
                                        "bell");
                                if (melodyName.equals("bell")) {
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(),
                                            R.raw.bell_sound);
                                } else  if (melodyName.equals("alarm_siren")) {
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(),
                                            R.raw.alarm_siren_sound);
                                }  else if (melodyName.equals("bip")) {
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(),
                                            R.raw.bip_sound);
                                }

                                mediaPlayer.start();
                            }
                            resetTimer();
                        }
                    }.start();
                } else {
                    resetTimer();
                }
            }
        };

        button.setOnClickListener(clickListener);
    }



    private void updateTimer (long l) {
        int minutes = (int) (l/1000/60);
        int seconds = (int) (l/1000 - (minutes*60));

        String minutesString;
        String secondsString;

        if (minutes < 10) {
            minutesString = "0" + minutes;
        } else {
            minutesString = String.valueOf(minutes);
        }

        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = String.valueOf(seconds);
        }

        textView.setText(minutesString + ":" + secondsString);
    }

    private void resetTimer () {
        button.setText("START");
        setIntervalFromSharedPreferences(sharedPreferences);
        countDownTimer.cancel();
        seekBar.setEnabled(true);
        isTimerOn = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.timer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent openSettings = new Intent(this, SettingsActivity.class);
            startActivity(openSettings);
            return true;
        } else if (id == R.id.action_about) {
            Intent openAbout = new Intent(this, AboutActivity.class);
            startActivity(openAbout);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void setIntervalFromSharedPreferences (SharedPreferences sharedPreferences) {

        defaultInterval = Integer.parseInt(sharedPreferences.getString("default_interval",
                "30"));
        long defaultIntervalInMillis = defaultInterval*1000;
        updateTimer(defaultIntervalInMillis);
        seekBar.setProgress(defaultInterval);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals("default_interval")) {
            setIntervalFromSharedPreferences(this.sharedPreferences);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }
}