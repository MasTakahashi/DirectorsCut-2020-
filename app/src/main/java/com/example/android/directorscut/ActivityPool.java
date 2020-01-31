package com.example.android.directorscut;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

public class ActivityPool extends AppCompatActivity implements View.OnClickListener {

    // RecyclerView ScoreRow
    private RecyclerView mScoreRowRV;
    private RecyclerView.Adapter mSRRVAdapter;
    private RecyclerView.LayoutManager mSRRVLayoutManager;

    // INTENT STRINGS
    private static final String INTENT_BOUT_INDEX = "bout_index";
    private static final String INTENT_NUM_FENCERS = "number_fencers";
    private static final String INTENT_FENCER_LIST = "fencer_list";
    private static final String INTENT_SCORE_LIMIT = "score_limit";
    private static final String INTENT_BOUT = "bout_object";

    // ADAPTERS
//    private AdapterScores adapterScores;
    private AdapterFencerResult resultAdapter;
    // RecyclerView
    private ArrayList<Bout> bouts = new ArrayList<>();
    private ArrayList<ScoreRow> scoreRowList;
    private RecyclerView mRecyclerView;
    private AdapterBoutRV mBoutsAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    // SETTING VALUES
    // Constant
    private static final int REQCODE_TO_SK = 1;
    // Variable
    private int NUM_FEN = 5;
    private int NUM_BOUTS = (NUM_FEN * (NUM_FEN - 1))/2;
    private int GRID_SIZE = NUM_FEN*NUM_FEN;
    private int SCORE_LIMIT = 5;
    private ScoreBox[] scores = new ScoreBox[GRID_SIZE];

    // TRACKING VALUES
    private static int activeBout = 0;
    private boolean scoreChanged = false;
    Bout oldBout = null;
    Bout changedBout = null;

    private Fencer[] fencerTemplate = {
        new Fencer("Alpha"),
        new Fencer("Bravo"),
        new Fencer("Charlie"),
        new Fencer("Delta"),
        new Fencer("Echo"),
        new Fencer("Foxtrot"),
        new Fencer("Golf"),
        new Fencer("Hotel")
    };

    private Fencer[] fencers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(getSupportActionBar()!= null) getSupportActionBar()
                .setBackgroundDrawable(getResources().getDrawable(R.drawable.background_501));
        super.onCreate(savedInstanceState);
        activeBout = 0;
        setContentView(R.layout.activity_pool);
        parseIntent();
        buildBoutRV();

        initializeScoreRows();
        initializeScoreBoxes();

        mScoreRowRV = findViewById(R.id.gv_main_scores);
        mScoreRowRV.setHasFixedSize(true);
        mSRRVLayoutManager = new LinearLayoutManager(this);
        mSRRVAdapter = new AdapterScoreRow(scoreRowList, scoreRowList.size());

        mScoreRowRV.setLayoutManager(mSRRVLayoutManager);
        mScoreRowRV.setAdapter(mSRRVAdapter);

        ListView resultList = findViewById(R.id.lv_fencer_results);
        Button btnNextBout = findViewById(R.id.PL_btn_next_bout);
        btnNextBout.setOnClickListener(this);
        Button btnPrevBout = findViewById(R.id.PL_btn_redo);
        btnPrevBout.setOnClickListener(this);

        assignBoutNames();
        resultAdapter = new AdapterFencerResult(this, fencers);
        resultList.setAdapter(resultAdapter);
        int numBouts = (NUM_FEN * (NUM_FEN-1)) / 2;
        setTitle("" + NUM_FEN + " Fencers (" + numBouts + " Bouts)");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQCODE_TO_SK) {
            if (resultCode == RESULT_OK) {
                scoreChanged = true;
                placeBoutInArray(data);
                markPlaces();
                updateAdapters();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.PL_btn_next_bout:
                if (activeBout < NUM_BOUTS) {
                    System.out.println("" +activeBout + ","+ NUM_BOUTS);
                    sendToScorekeeper();
                }
                break;
            case R.id.PL_btn_redo:
                if (activeBout > 0) {
                    prevToScorekeeper();
                }
        }
    }

    // Process Changes
    public void placeBoutInArray(Intent data) {
        Bout bout = data.getParcelableExtra(INTENT_BOUT);
        changedBout = bout;
        int boutIndex = data.getIntExtra(INTENT_BOUT_INDEX, 0);
        bouts.set(boutIndex, bout);
        fillScores(bout);

        mBoutsAdapter.notifyItemChanged(boutIndex);

        if (activeBout == boutIndex && activeBout < NUM_BOUTS-1) {
            while (activeBout < NUM_BOUTS && bouts.get(activeBout).isComplete()) {
                activeBout++;
            }
        }
        if (activeBout == NUM_BOUTS-1 && bouts.get(activeBout).isComplete()) {
            activeBout++;
        }
        updateScores();
    }

    private void fillScores(Bout bout) {
        int myNumber = bout.getMyNumber();
        int opNumber = bout.getOpNumber();

        int myScore = bout.getMyScore();
        int opScore = bout.getOpScore();

        ScoreRow mySRow = scoreRowList.get(myNumber);
        ScoreRow opSRow = scoreRowList.get(opNumber);
        mySRow.setScore(opNumber, myScore);
        mySRow.getScoreBox(opNumber).setShow();
        opSRow.setScore(myNumber, opScore);
        opSRow.getScoreBox(myNumber).setShow();

        if (myScore < opScore) {
            mySRow.getScoreBox(opNumber).setVictory(false);
            opSRow.getScoreBox(myNumber).setVictory(true);
        } else if (myScore > opScore) {
            mySRow.getScoreBox(opNumber).setVictory(true);
            opSRow.getScoreBox(myNumber).setVictory(false);
        }
    }

    private void addScores(@org.jetbrains.annotations.NotNull Bout bout, boolean positive) {
        int sign = positive ? 1 : -1;
        int myNumber = bout.getMyNumber();
        int opNumber = bout.getOpNumber();

        int myScore = sign * bout.getMyScore();
        int opScore = sign * bout.getOpScore();

        fencers[myNumber].addTS(myScore);
        fencers[myNumber].addTR(opScore);
        fencers[opNumber].addTS(opScore);
        fencers[opNumber].addTR(myScore);

        ScoreRow mySRow = scoreRowList.get(myNumber);
        ScoreRow opSRow = scoreRowList.get(opNumber);
        // How to deal with victories?
        if (!positive) {
            mySRow.getScoreBox(opNumber).setVictory(false);
            opSRow.getScoreBox(myNumber).setVictory(false);
        }
        if (bout.isComplete()) { // Subtracts "changed" bout only if bout had non-zero values
            if (bout.isMyVictory()) {
                fencers[myNumber].addVictory(sign);
                if (positive) mySRow.getScoreBox(opNumber).setVictory(true);
            } else {
                fencers[opNumber].addVictory(sign);
                if (positive) opSRow.getScoreBox(myNumber).setVictory(true);
            }
        }

    }
    private void addScores(Bout bout) {
        addScores(bout, true);
    }

    private void updateScores() {
        if (scoreChanged) {
            addScores(oldBout,false);
            scoreChanged = false;
        }
        addScores(changedBout);
    }

    private void markPlaces() {
        //sort fencers by victories, else indicator, descending
        Arrays.sort(fencers, Fencer.fencerPlacementComparator);
        int place = 0;
        int prevVic = -1;
        int prevInd = -1;
        for (Fencer fencer : fencers) {
            if (prevVic != fencer.getVic() || prevInd != fencer.getInd()){
                place++;
            }
            fencer.setPlace(place);
            prevVic = fencer.getVic();
            prevInd = fencer.getInd();
        }
        // Sort by default (localIndex)
        Arrays.sort(fencers);
    }
    private void rebuildBouts() {
        int[] list = new int[NUM_BOUTS];
        switch (NUM_FEN) {
            case 2:
                list = new int[]{12};
                break;
            case 3:
                list = new int[]{12,13,23};
                break;
            case 4:
                list = new int[]{14,23,13,24,34,12};
                break;
            case 5:
                list = new int[]{12,34,51,23,54,13,25,41,35,42};
                break;
            case 6:
                list = new int[]{12,45,23,56,31,64,25,14,53,16,42,36,51,34,62};
                break;
            case 7:
                list = new int[]{14,25,36,71,54,23,67,51,43,62,57,31,46,72,35,16,24,73,65,12,47};
                break;
            case 8:
                list = new int[]{23,15,74,68,12,34,56,87,41,52,83,67,42,81,75,36,28,74,61,37,48,26,35,17,46,85,72,13};
                break;
        }
        int myNum;
        int opNum;
        for (int k=0; k<NUM_BOUTS; k++){
            myNum = (list[k]/10)-1;
            opNum = (list[k]%10)-1;
            bouts.add(new Bout(myNum, opNum));
        }

    }



    // Initialize
    private void initializeScoreRows() {
        scoreRowList = new ArrayList<>();
        for (Fencer fencer : fencers) {
            scoreRowList.add(new ScoreRow(fencer.getLastName()));
        }
    }
    private void initializeScoreBoxes() {
        int rowIndex = 0;
        for (ScoreRow sRow : scoreRowList) {
            sRow.initScoreBoxes(NUM_FEN - 1);
            sRow.getScoreBox(rowIndex).setBlack();
            if (rowIndex < NUM_FEN-1) rowIndex++;
        }
    }
    private void assignBoutNames() {
        for (Bout bout : bouts) {
            bout.setMyName(fencers[bout.getMyNumber()].getLastName());
            bout.setOpName(fencers[bout.getOpNumber()].getLastName());
        }
    }

    // Refresh
    public void updateAdapters() {
        resultAdapter.notifyDataSetChanged();
        mSRRVAdapter.notifyDataSetChanged();
    }

    // Other Activities
    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(INTENT_FENCER_LIST)) {
            ArrayList<Fencer> alFencers = intent.getParcelableArrayListExtra(INTENT_FENCER_LIST);
            fencers = new Fencer[alFencers.size()];
            fencers = alFencers.toArray(fencers);
        }
        if (intent.hasExtra(INTENT_NUM_FENCERS)) {
            int numFencersInt = intent.getIntExtra(INTENT_NUM_FENCERS, 5);
            if (numFencersInt > 0) {
                NUM_FEN = numFencersInt;
                NUM_BOUTS = (NUM_FEN * (NUM_FEN - 1)) / 2;
                GRID_SIZE = NUM_FEN * NUM_FEN;
                rebuildBouts();
            }
        }
        if (intent.hasExtra(INTENT_SCORE_LIMIT)) {
            int scoreLimitInt = intent.getIntExtra(INTENT_SCORE_LIMIT, 5);
            if (scoreLimitInt > 0) {
                SCORE_LIMIT = scoreLimitInt;
            } else {
                SCORE_LIMIT = 5;
            }
        }
    }
    public void sendToScorekeeper(int boutNumber) {
        Bout activeBout = bouts.get(boutNumber);
        oldBout = activeBout;
        Intent intent = new Intent(ActivityPool.this, ActivityScorekeeper.class);
        intent.putExtra(INTENT_BOUT, activeBout);
        intent.putExtra(INTENT_BOUT_INDEX, boutNumber);
        startActivityForResult(intent, REQCODE_TO_SK);
    }
    public void sendToScorekeeper() {
        sendToScorekeeper(activeBout);
    }
    public void prevToScorekeeper() {
        Intent intent = new Intent(ActivityPool.this, ActivityScorekeeper.class);
        intent.putExtra(INTENT_BOUT, bouts.get(--activeBout));
        startActivityForResult(intent, REQCODE_TO_SK);
    }
    public void buildBoutRV() {
        mRecyclerView = findViewById(R.id.rv_bout_list);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mBoutsAdapter = new AdapterBoutRV(bouts);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mBoutsAdapter);

        mBoutsAdapter.setOnItemClickListener(new AdapterBoutRV.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                sendToScorekeeper(position);
            }
        });
    }


}
