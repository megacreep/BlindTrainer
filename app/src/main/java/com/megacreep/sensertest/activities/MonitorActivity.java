package com.megacreep.sensertest.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.megacreep.sensertest.R;
import com.megacreep.sensertest.sensor.TrainingController;

public class MonitorActivity extends AppCompatActivity implements TrainingController.OnTrainingEventController {

    TextView mTvGait;

    TextView mTvDirection;
    ImageView mImageLeft;
    ImageView mImageRight;

    TextView mTvBalance;

    TrainingController mController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        prepareViews();
        startCountDown();
    }

    private void prepareViews() {
        mTvGait = (TextView) this.findViewById(R.id.tvGait);
        mTvDirection = (TextView) this.findViewById(R.id.tvDirection);
        mImageLeft = (ImageView) this.findViewById(R.id.arrLeft);
        mImageRight = (ImageView) this.findViewById(R.id.arrRight);
        mTvBalance = (TextView) this.findViewById(R.id.tvBalance);
    }

    private void startCountDown() {
        MediaPlayer player = MediaPlayer.create(this, R.raw.audio_start);
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                startTraining();
            }
        });
        player.start();
    }

    private void startTraining() {
        mController = TrainingController.getInstance(this, this);
        mController.start(true);
    }

    @Override
    public void onGaitScoreChanged(float score) {
        mTvDirection.setText(String.format("%.2f", score));
    }

    @Override
    public void onDirectionalScoreChanged(float score, boolean isLeft) {
        if (isLeft) {
            mImageLeft.setVisibility(View.VISIBLE);
            mImageRight.setVisibility(View.INVISIBLE);
        } else {
            mImageLeft.setVisibility(View.INVISIBLE);
            mImageRight.setVisibility(View.VISIBLE);
        }
        mTvDirection.setText(String.format("%.2f", score));
    }

    @Override
    public void onBalanceScoreChanged(float score) {
        mTvBalance.setText(String.format("%.2f", score));
    }

    @Override
    public void onTrainingFinished(boolean success) {
        if (success) {
            Intent intent = new Intent(this, ResultActivity.class);
            this.startActivity(intent);
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mController != null) {
            mController.abort();
        }
    }
}

