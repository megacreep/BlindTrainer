package com.megacreep.sensertest.media;

import android.content.Context;
import android.media.MediaPlayer;

import com.megacreep.sensertest.R;

/**
 * This class provide the functionality of playing a
 * predefined sound file one note at a time
 * Created by muyang on 4/9/16.
 */
public class MusicPlayer {

    private static final int[] SOUND_MAPPING = new int[] {
            R.raw.note_0, R.raw.note_1, R.raw.note_2, R.raw.note_3, R.raw.note_4, R.raw.note_5,
            R.raw.note_6, R.raw.note_7, R.raw.note_8, R.raw.note_9, R.raw.note_10, R.raw.note_11,
            R.raw.note_12, R.raw.note_13, R.raw.note_14, R.raw.note_15, R.raw.note_16, R.raw.note_17,
            R.raw.note_18, R.raw.note_19, R.raw.note_20, R.raw.note_21, R.raw.note_22, R.raw.note_23,
            R.raw.note_24, R.raw.note_25, R.raw.note_26, R.raw.note_27, R.raw.note_28, R.raw.note_29,
            R.raw.note_30, R.raw.note_31, R.raw.note_32, R.raw.note_33, R.raw.note_34, R.raw.note_35,
    };

    private Context mContext;
    private int[] mNotes;
    private int mCurrentNote = 0;

    private MediaPlayer mPlayer;

    public MusicPlayer(Context context, int[] notes) {
        this.mContext = context;
        this.mNotes = notes;
    }


    public void next() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            return;
        }

        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
        }

        mPlayer = MediaPlayer.create(mContext, SOUND_MAPPING[mNotes[mCurrentNote]]);
        if (mPlayer != null) {
            mPlayer.start();
            mCurrentNote = (mCurrentNote + 1) % mNotes.length;
        }
    }

    public void reset() {
        this.mCurrentNote = 0;
    }
}
