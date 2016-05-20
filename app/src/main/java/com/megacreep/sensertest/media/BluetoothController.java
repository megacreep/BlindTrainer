package com.megacreep.sensertest.media;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothController {

    public static final UUID MY_UUID = UUID.randomUUID();
    public static final int STATUS_OFF = 0;
    public static final int STATUS_ON = 1;

    private static final byte[] COMMAND_VIBRATE = new byte[] {0x00, (byte)0xAA, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f,};
    private static final byte[] COMMAND_STOP = new byte[] {0x00, (byte)0xAA, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    private static BluetoothController instance;

    private Handler mHandler = new Handler();

    private Context mContext;
    private int mCurrentStatus = STATUS_OFF;
    private BluetoothDevice mDevice;
    private BluetoothAdapter mBluetoothAdapter;

    private ConnectedThread mWriteThread;

    private BluetoothController(Context context) {
        mContext = context;
    }

    public static BluetoothController getInstance(Context context) {
        if (instance == null) {
            instance = new BluetoothController(context);
        }
        return instance;
    }

    public void connect() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            return;
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals("BT05")) {
                    mDevice = device;
                    break;
                }
            }
        }

        if (mDevice != null) {
            new ConnectThread(mDevice).start();
        }
    }

    public void vibrate(int seconds) {
        sendCommand(COMMAND_VIBRATE);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendCommand(COMMAND_STOP);
            }
        }, seconds * 1000);
    }

    public void stopVibrating() {
        sendCommand(COMMAND_STOP);
    }

    private void sendCommand(byte[] command) {
        if (mCurrentStatus == STATUS_OFF || mWriteThread == null) {
            return;
        }
        mWriteThread.write(command);
    }

    public void close() {
        mWriteThread.cancel();
    }


    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }
            mWriteThread = new ConnectedThread(mmSocket);
            mWriteThread.start();
            mCurrentStatus = STATUS_ON;
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
            mCurrentStatus = STATUS_OFF;
        }
    }
}
