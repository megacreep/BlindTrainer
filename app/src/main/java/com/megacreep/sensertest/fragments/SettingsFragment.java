package com.megacreep.sensertest.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.megacreep.sensertest.R;
import com.megacreep.sensertest.media.BluetoothController;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private static final int[] SEEKBAR_IDS = new int[] {
            R.id.sb_vibrator_1,
            R.id.sb_vibrator_2,
            R.id.sb_vibrator_3,
            R.id.sb_vibrator_4,
            R.id.sb_vibrator_5,
            R.id.sb_vibrator_6,
            R.id.sb_vibrator_7,
            R.id.sb_vibrator_8,
    };

    private static final int[] TEXT_IDS = new int[] {
            R.id.tv_vibrator_1,
            R.id.tv_vibrator_2,
            R.id.tv_vibrator_3,
            R.id.tv_vibrator_4,
            R.id.tv_vibrator_5,
            R.id.tv_vibrator_6,
            R.id.tv_vibrator_7,
            R.id.tv_vibrator_8,
    };

    private List<SeekBar> mSeekbars = new ArrayList<>();
    private BluetoothController mBluetoothController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        initView(view);

        mBluetoothController = BluetoothController.getInstance(getActivity());
        return view;
    }

    private void initView(View view) {
        for (int i = 0; i < SEEKBAR_IDS.length; i++) {
            int id = SEEKBAR_IDS[i];
            SeekBar sb = (SeekBar) view.findViewById(id);
            final TextView tv = (TextView) view.findViewById(TEXT_IDS[i]);
            sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    tv.setText(String.valueOf(progress));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            mSeekbars.add(sb);
        }

        Button btnSend = (Button) view.findViewById(R.id.btn_command_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] values = new byte[8];
                for (int i = 0; i < mSeekbars.size(); i++) {
                    values[i] = (byte) mSeekbars.get(i).getProgress();
                    Log.d("megacreep", String.format("command %d: %02X", i, values[i]));
                }
                if (mBluetoothController != null) {
                    mBluetoothController.connect();
                }
            }
        });

        Button btnStop = (Button) view.findViewById(R.id.btn_command_stop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothController.stopVibrating();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBluetoothController.close();
    }
}
