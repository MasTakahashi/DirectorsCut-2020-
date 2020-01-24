package com.example.android.directorscut;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

public class ActivityPoolSetup extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, DialogFencer.DialogFencerListener {
    private static final String INTENT_NUM_FENCERS = "number_fencers";
    private static final String INTENT_SCORE_LIMIT = "score_limit";
    private static final String INTENT_FENCER_LIST = "fencer_list";
    private static final String DIALOG_FENCER = "dialog_fencer";
    private static final int MAX_POOL_SIZE = 8;
    private int mFencerModifyIndex = 0;

    // Buttons
    private Button btnGenerate;
    private Button btnInsert;
    private Button btnBoutOnly;
    private Button btnStartPool;

    // Inputs
    private EditText etNumFencers;
    private EditText etScoreLimit;

    // Variables
    private ArrayList<Fencer> psFencers;
    private int numFencers = 5;
    private int scoreLimit = 0;

    //RecyclerView Members
    private RecyclerView mFencerRV;
    private FencerPoolSetupAdapter mFRVAdapter;
    private RecyclerView.LayoutManager mFRVLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pool_setup);

        createFencers();
        setupButtons();
        setupViews();
        updateHintText();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.bt_mid_insert:
                insertFencer(psFencers.size());
                updateHintText();
                break;
            case R.id.bt_mid_generate:
                int setCount;
                if (etNumFencers.getText().toString().length() > 0) {
                    setCount = Integer.parseInt(etNumFencers.getText().toString());
                    generateFencers(setCount);
                    updateHintText();
                }
                break;
            case R.id.btn_bout_only:
                startActivityBoutOnly();
                break;
            case R.id.btn_start_pool:
                startActivityPool();
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch(view.getId()) {
            case R.id.bt_mid_generate:
                insertFencer(2);
        }
        return true;
    }

    public void insertFencer(int position) {
        if (position > psFencers.size()) {
            position = psFencers.size();
        }

        if (psFencers.size() < MAX_POOL_SIZE) {
            psFencers.add(position, new Fencer("New Fencer"));
            mFRVAdapter.notifyItemInserted(position);
        }
    }

    public void insertFencer() {
        insertFencer(psFencers.size());
    }

    public void removeFencer(int position) {
        psFencers.remove(position);
        mFRVAdapter.notifyItemRemoved(position);
    }

    public void generateFencers(int newCount) {
        if (newCount < 2) {
            return;
        }
        int size = psFencers.size();
        int diff = size - newCount;
        if (diff < 0) {
            for (int i = 0; i < Math.abs(diff); i++) {
                insertFencer();
            }
        } else if (diff > 0) {
            for (int i = 0; i < diff; i++) {
                removeFencer(psFencers.size() - 1);
            }
        }
    }

    private void updateHintText() {
        etNumFencers.setHint(String.valueOf(psFencers.size()));
        etNumFencers.setText("");
    }

    private void setupViews() {
        etNumFencers = findViewById(R.id.et_num_fencers);
        etScoreLimit = findViewById(R.id.et_score_limit);

        setupFencerRV();
    }

    private void setupFencerRV() {
        mFencerRV = findViewById(R.id.rv_fencer_list);
        mFencerRV.setHasFixedSize(true);
        mFRVLayoutManager = new LinearLayoutManager(this);
        mFRVAdapter = new FencerPoolSetupAdapter(psFencers);

        mFencerRV.setLayoutManager(mFRVLayoutManager);
        mFencerRV.setAdapter(mFRVAdapter);

        mFRVAdapter.setOnItemClickListener(new FencerPoolSetupAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // Send to Dialog for fencer edit prompt
                mFencerModifyIndex = position;
                openFencerDialog();
            }
        });
    }

    public void openFencerDialog() {
        DialogFencer dialogFencer = new DialogFencer();
        dialogFencer.show(getSupportFragmentManager(), DIALOG_FENCER);
    }

    private void setupButtons() {
        btnInsert = findViewById(R.id.bt_mid_insert);
        btnGenerate = findViewById(R.id.bt_mid_generate);
        btnBoutOnly = findViewById(R.id.btn_bout_only);
        btnStartPool = findViewById(R.id.btn_start_pool);

        btnInsert.setOnClickListener(this);
        btnInsert.setOnLongClickListener(this);
        btnGenerate.setOnClickListener(this);
        btnBoutOnly.setOnClickListener(this);
        btnStartPool.setOnClickListener(this);
    }

    private void createFencers() {
        psFencers = new ArrayList<Fencer>(){{
            add(new Fencer("Alpha"));
            add(new Fencer("Beta"));
            add(new Fencer("Gamma"));
        }};

        psFencers.get(1).setRating(FencerRating.Rating.C, 2016);
        psFencers.get(0).setClub("EO66");

        for (Fencer fencer : psFencers) {
            System.out.println(fencer);
        }
    }

   private void startActivityBoutOnly() {
       Intent skIntent = new Intent(ActivityPoolSetup.this, ActivityScorekeeper.class);
       String scoreLimitText = etScoreLimit.getText().toString();
       if (!scoreLimitText.equals("")) {
           int scoreLimitInt = Integer.parseInt(scoreLimitText);
           if (scoreLimitInt > 0) {
               scoreLimit = scoreLimitInt;
           } else {
               scoreLimit = 0;
           }
       }
       skIntent.putExtra(INTENT_SCORE_LIMIT, scoreLimit);

       startActivity(skIntent);
   }

   private void startActivityPool() {
        Intent plIntent = new Intent(ActivityPoolSetup.this, ActivityPool.class);
        String numFencersText = etNumFencers.getText().toString();
        if (!numFencersText.equals("")) {
            int numFencersInt = Integer.parseInt(numFencersText);
            if (numFencersInt > 0) {
                numFencers = numFencersInt;
            } else {
                numFencers = 5;
            }
        } else {
            numFencers = psFencers.size();
        }
       String scoreLimitText = etScoreLimit.getText().toString();
       if (!scoreLimitText.equals("")) {
           int scoreLimitInt = Integer.parseInt(scoreLimitText);
           if (scoreLimitInt > 0) {
               scoreLimit = scoreLimitInt;
           } else {
               scoreLimit = 5;
           }
       } else {
           scoreLimit = 5;
       }
//       plIntent.putParcelableArrayListExtra(INTENT_FENCER_LIST, psFencers);
       plIntent.putExtra(INTENT_NUM_FENCERS, numFencers);
       plIntent.putExtra(INTENT_SCORE_LIMIT, scoreLimit);
       startActivity(plIntent);
   }

    @Override
    public void applyChanges(String name, String club, FencerRating.Rating ratingLet, int ratingYear) {
        Fencer modifyFencer = psFencers.get(mFencerModifyIndex);
        if (name.length() > 0) modifyFencer.setLastName(name);
        if (club.length() > 0) modifyFencer.setClub(club);
        mFRVAdapter.notifyItemChanged(mFencerModifyIndex);
    }

    @Override
    public void applyChanges(String name, String club) {
        applyChanges(name, club, FencerRating.Rating.U, 0);
    }
}
