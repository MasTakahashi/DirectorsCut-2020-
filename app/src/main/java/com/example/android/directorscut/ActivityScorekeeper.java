package com.example.android.directorscut;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.Nullable;

public class ActivityScorekeeper extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, PopupMenu.OnMenuItemClickListener {
    // INTENTS
    // Bout
    private static final String INTENT_BOUT_INDEX = "bout_index";
    private static final String INTENT_BOUT = "bout_object";
    // Card
    private static final String INTENT_CARD_NAME = "card_name";
    private static final String INTENT_CARD_SIDE = "card_side";
    private static final String INTENT_CARD_TYPE = "card_type";

    private String cardName = "";
    private String cardSide = "";

    private static final int REQCODE_CARD = 1;

    private TextView countdownText;
    private Bout bout;
    private int activeBout;

    private Button boutReset;
    private Button boutMinuteBreak;
    private Button boutSubmit;

    private Button nameFOTL;
    private Button nameFOTR;

    private Button doubleTouch;
    private Button countdownButton;

    private TextView scoreTextViewL;
    private TextView scoreTextViewR;

    private final int TIME_INTERVAL_MIL = 1;
    private final long TIME_MIN_THREE = 180000;
    private final long TIME_MIN_ONE = 60000;
    private long TIME_DURATION = TIME_MIN_THREE;

    private CountDownTimer countDownTimer;
    private int timeUpdateInterval = TIME_INTERVAL_MIL;
    private long timeLeftInMilliseconds = TIME_DURATION; //3 mins
    private boolean timerRunning;
    private boolean timerDone = false;
    private boolean timerMinute = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scorekeeper);
        String FOTR = "RIGHT";
        String FOTL = "LEFT";

        Intent intent = getIntent();
        if (intent.hasExtra(INTENT_BOUT)) {
            bout = intent.getParcelableExtra(INTENT_BOUT);
            FOTR = bout.getMyName();
            FOTL = bout.getOpName();
        } else {
            bout = new Bout(0, 0);
        }
        if (intent.hasExtra(INTENT_BOUT_INDEX)) {
            activeBout = intent.getIntExtra(INTENT_BOUT_INDEX, 0);
        }

        scoreTextViewL = (TextView) findViewById(R.id.score_FOTL);
        scoreTextViewR = (TextView) findViewById(R.id.score_FOTR);

        boutReset = (Button) findViewById(R.id.bout_reset);
        boutMinuteBreak = (Button) findViewById(R.id.bout_minuteBreak);
        boutSubmit = (Button) findViewById(R.id.bout_submit);
        countdownText = (TextView) findViewById(R.id.clock_timer);
        countdownButton = (Button) findViewById(R.id.time_toggle);
        doubleTouch = (Button) findViewById(R.id.double_touch);
        nameFOTL = (Button) findViewById(R.id.btn_name_FOTL);
        nameFOTR = (Button) findViewById(R.id.btn_name_FOTR);

        // BUTTONS
        // Bout Status
        boutReset.setOnClickListener(this);
        boutReset.setOnLongClickListener(this);
        boutMinuteBreak.setOnClickListener(this);
        boutSubmit.setOnClickListener(this);
        // Countdown
        countdownText.setOnClickListener(this);
        countdownButton.setOnClickListener(this);
        countdownButton.setOnLongClickListener(this);
        // Score
        nameFOTL.setOnClickListener(this);
        nameFOTL.setText(FOTL);
        nameFOTR.setOnClickListener(this);
        nameFOTR.setText(FOTR);
        Button plusLeft = (Button) findViewById(R.id.plus_FOTL);
        plusLeft.setOnClickListener(this);
        Button plusRight = (Button) findViewById(R.id.plus_FOTR);
        plusRight.setOnClickListener(this);
        doubleTouch.setOnClickListener(this);
        doubleTouch.setOnLongClickListener(this);
        Button minusLeft = (Button) findViewById(R.id.minus_FOTL);
        minusLeft.setOnClickListener(this);
        Button minusRight = (Button) findViewById(R.id.minus_FOTR);
        minusRight.setOnClickListener(this);
        scoreTextViewL.setText(bout.getOpScore().toString());
        scoreTextViewR.setText(bout.getMyScore().toString());
    }

    @Override
    public void onClick(View view) {
        // Get score from TextView
        switch(view.getId()) {
            case R.id.clock_timer:
            case R.id.time_toggle:
                if (timeLeftInMilliseconds > 0) {
                    if (timerRunning) {
                        stopTimer();
                    } else {
                        startTimer();
                    }
                }
                break;
            // Bout
            case R.id.bout_reset:
                makeToast("Hold to Reset Bout");
                // TODO: convert to OnTouch (press, instead of release)
                break;
        }
        if (!timerRunning) {
            switch (view.getId()) {
                case R.id.bout_submit:
                    submit();
                    break;
                // Minute
                case R.id.bout_minuteBreak:
                    if (!timerMinute) {
                        breakEnter();
                    } else {
                        breakExit();
                    }
                    break;
                // Scores
                case R.id.double_touch:
                    bout.addDouble();
                    break;
                case R.id.btn_name_FOTL:
                case R.id.plus_FOTL:
                    bout.addOpScore();
                    break;
                case R.id.btn_name_FOTR:
                case R.id.plus_FOTR:
                    bout.addMyScore();
                    break;
                case R.id.minus_FOTL:
                    if (bout.getOpScore() > 0) bout.subOpScore();
                    break;
                case R.id.minus_FOTR:
                    if (bout.getMyScore() > 0) bout.subMyScore();
                    break;
            }
        }
        // send to TextView
        scoreTextViewL.setText(bout.getOpScore().toString());
        scoreTextViewR.setText(bout.getMyScore().toString());
    }

    @Override
    public boolean onLongClick(View view){
        // Get score from TextView
        switch(view.getId()) {
            case R.id.time_toggle:
                if (!timerDone) break;
            case R.id.bout_reset:
                if (!timerRunning) {
                    bout.setOpScore(0);
                    bout.setMyScore(0);
                    resetBout();
                    makeToast("Resetting Bout");
                } else {
                    makeToast("Cannot Reset Active Bout!");
                }
                break;
            case R.id.double_touch:
                bout.subDouble();
                break;
            case R.id.bout_submit:
                break;
        }
        // send to TextView
        scoreTextViewL.setText(bout.getOpScore().toString());
        scoreTextViewR.setText(bout.getMyScore().toString());
        return true;
    }

    public void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMilliseconds, timeUpdateInterval) {
            @Override
            public void onTick(long l) {
                timeLeftInMilliseconds = l;
                updateTimer();
            }

            @Override
            public void onFinish() {
                countdownText.setBackgroundColor(getResources().getColor(R.color.timeFinal));
                countdownButton.setText(R.string.timeBtnFinal);
                countdownButton.setTextSize(30);
                countdownButton.setTextColor(getResources().getColor(R.color.timeTextFin));
                countdownButton.setBackgroundColor(getResources().getColor(R.color.timeBtnFinal));
                timerRunning = false;
                timerDone = true;
                if (!timerMinute) {
                    timeLeftInMilliseconds = 0;
                    makeToast("Time's up!");
                } else {
                    timeLeftInMilliseconds = TIME_DURATION;
                    breakExit();
                }
                updateTimer();
            }
        }.start();
        countdownButton.setText(R.string.timeBtnStop);
        countdownButton.setTextColor(getResources().getColor(R.color.timeTextRes));
        if (timerMinute){
            countdownButton.setBackgroundColor(getResources().getColor(R.color.timeBtnResBreak));
            countdownText.setBackgroundColor(getResources().getColor(R.color.timeBtnResBreak));
        } else {
            countdownButton.setBackgroundColor(getResources().getColor(R.color.timeBtnResumed));
            countdownText.setBackgroundColor(getResources().getColor(R.color.timeResumed));
        }

        countdownButton.setTextSize(36);
        timerRunning = true;
    }

    public void stopTimer() {
        countDownTimer.cancel();
        countdownButton.setText(R.string.timeBtnStart);
        countdownButton.setTextColor(getResources().getColor(R.color.timeTextStop));
        if (timerMinute){
            countdownButton.setBackgroundColor(getResources().getColor(R.color.timeBtnStopBreak));
            countdownText.setBackgroundColor(getResources().getColor(R.color.timeBtnStopBreak));
        } else {
            countdownButton.setBackgroundColor(getResources().getColor(R.color.timeBtnStopped));
            countdownText.setBackgroundColor(getResources().getColor(R.color.timeStopped));
        }
        timerRunning = false;
    }

    public void resetTimer() {
        if (!timerRunning) {
            timeLeftInMilliseconds = TIME_DURATION;
            countdownButton.setText(R.string.timerBtnInit);
            countdownButton.setTextColor(getResources().getColor(R.color.timeTextInit));
            countdownButton.setTextSize(32);
            countdownText.setBackgroundColor(getResources().getColor(R.color.timeInitial));
            timerDone = false;
            updateTimer();
        }
    }

    public void updateTimer() {

        int minutes = (int) timeLeftInMilliseconds / 60000;
        int seconds = (int) (timeLeftInMilliseconds % 60000) / 1000;
        int milsecs = (int) (timeLeftInMilliseconds % 1000) / 10;

        String timeLeftText;
        timeLeftText = "";
        timeLeftText += minutes;
        timeLeftText += ":";
        if (seconds < 10) timeLeftText += '0';
        timeLeftText += seconds;
        if (minutes <= 0) {
            countdownText.setTextSize(120);
            timeLeftText += ".";
            if (milsecs < 10) timeLeftText += '0';
            timeLeftText += milsecs;
        }
        countdownText.setText(timeLeftText);

    }

    public void makeToast(String message) {
        Toast aToast =
                Toast.makeText(this, message, Toast.LENGTH_SHORT);
        aToast.show();
    }

    public void resetBout() {
        countdownButton.setBackgroundColor(getResources().getColor(R.color.timeBtnInit));
        breakExit();
        resetTimer();
    }

    public void breakEnter() {
        timerMinute = true;
        timeLeftInMilliseconds = TIME_MIN_ONE;
        boutMinuteBreak.setText(R.string.btnMinuteActive);
        startTimer();
        makeToast("One Minute Break");
    }

    public void breakExit() {
        timerMinute = false;
        boutMinuteBreak.setText(R.string.btnMinuteInactive);
        countdownButton.setBackgroundColor(getResources().getColor(R.color.timeBtnInit));
        resetTimer();
        makeToast("Break is over!");
    }

    public void submit() {
        if (bout.getMyScore() != bout.getOpScore()) {
            Intent resultSK = new Intent(this, ActivityPool.class);
            bout.endBout();
            System.out.println(bout);
            resultSK.putExtra(INTENT_BOUT, bout);
            resultSK.putExtra(INTENT_BOUT_INDEX, activeBout);
            setResult(RESULT_OK, resultSK);
            finish();
        } else {
            makeToast("Cannot End on a Tie!");
        }
    }

    public void cardPopup(View v) {
        PopupMenu cardMenu = new PopupMenu(this, v);
        switch (v.getId()) {
            case R.id.radial_card_left:
                cardName = bout.getOpName();
                cardSide = "Left";
                break;
            case R.id.radial_card_right:
                cardName = bout.getMyName();
                cardSide = "Right";
                break;
        }
        cardMenu.setOnMenuItemClickListener(this);
        cardMenu.inflate(R.menu.card_popup);
        cardMenu.show();
    }

    public void createCard(int color) {
        Intent cardIntent = new Intent(ActivityScorekeeper.this, ActivityCard.class);
        cardIntent.putExtra(INTENT_CARD_TYPE, color);
        cardIntent.putExtra(INTENT_CARD_NAME, cardName);
        cardIntent.putExtra(INTENT_CARD_SIDE, cardSide);
        startActivityForResult(cardIntent, REQCODE_CARD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.item_card_yellow:
                createCard(0);
                return true;
            case R.id.item_card_red:
                createCard(1);
                return true;
            case R.id.item_card_black:
                createCard(2);
                return true;
        }
        return false;
    }
}