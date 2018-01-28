package com.example.bipin.uiforselfdrivingcar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Color;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button block, start, finish, clear, go;
    public static Button activeButton;
    public static boolean clearScreen, readyToFindPath;

    //----------------- Bluetooth related attributes --------------------------
    public static  OutputStream outputStream;
    //-------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        block = findViewById(R.id.block);
        start = findViewById(R.id.start);
        finish = findViewById(R.id.finish);
        go = findViewById(R.id.go);
        clear = findViewById(R.id.clear);

        block.setBackgroundColor(Color.WHITE);
        start.setBackgroundColor(Color.WHITE);
        finish.setBackgroundColor(Color.WHITE);
        go.setBackgroundColor(Color.GREEN);
        go.setTextColor(Color.WHITE);
        clear.setBackgroundColor(Color.WHITE);


        outputStream = null;
        activeButton = null;
        clearScreen = readyToFindPath = false;

        block.setOnClickListener(buttonClickListener);
        start.setOnClickListener(buttonClickListener);
        finish.setOnClickListener(buttonClickListener);
        go.setOnClickListener(buttonClickListener);
        clear.setOnClickListener(buttonClickListener);


        // ----------------------- Bluetooth related works ----------------------------------

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Log.e("BLUETOOTH_ERROR", "Device doesn't support bluetooth");
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }



        try {
            initBluetooth();
        }
        catch (IOException e) {
            Log.e("EXCEPTION", "cannot initialize output stream for bluetooth comm.");
        }

    }



    /**
     * Buton Click Linsteners that specifies the instructions for different button press events
     */
    View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (((Button) view).getId() == R.id.clear) {
                clearScreen = true;
                return;
            }
            if (((Button) view).getId() == R.id.go) {
                readyToFindPath = true;
                return;
            }
            if ( !((Button) view).isActivated() ) {
                view.setBackgroundColor(Color.GREEN);
                ((Button) view).setTextColor(Color.WHITE);

                // deactivate already active button before activating it
                if (activeButton != null)
                    activeButton.performClick();
                ((Button) view).setActivated(true);
                activeButton = (Button) view;
                clearScreen = false;
            }
            else {
                view.setBackgroundColor(Color.WHITE);
                ((Button) view).setTextColor(Color.BLACK);
                ((Button) view).setActivated(false);
                activeButton = null;
            }
        }
    };




    //---------------------- Bluetooth related function -------------------------------------

    /**
     * Initialize socket and outputstream to communicate via bluetooth
     * @throws IOException
     */
    private void initBluetooth() throws IOException {
        BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter();
        if (blueAdapter != null) {
            if (blueAdapter.isEnabled()) {
                Set<BluetoothDevice> bondedDevices = blueAdapter.getBondedDevices();

                if(bondedDevices.size() > 0) {

//                    Object[] devices = (Object []) bondedDevices.toArray();
//                    BluetoothDevice device = (BluetoothDevice) devices[0];
//                    ParcelUuid[] uuids = device.getUuids();
//                    Log.e("MAC_ADDRESS", uuids[0].getUuid().toString());

                    // I used above commented code to identify the MAC address of the
                    // arduino bluetooth module
                    // for which i unpaired all the devices from my phone
                    // and connected only arduino bluetooth module
                    // this way it was guaranteed that devices[0] was arduino bluetooth module


                    // find the Arduino Bluetooth module device
                    final String MAC_ADDRESS = "00001101-0000-1000-8000-00805f9b34fb";
                    Object[] devices = (Object[]) bondedDevices.toArray();
                    BluetoothDevice device = (BluetoothDevice) devices[0];

                    for (int i=0; i<devices.length; i++) {
                        if (((BluetoothDevice)devices[i]).getUuids()[0].getUuid().toString().equals(MAC_ADDRESS)) {
                            device = (BluetoothDevice) devices[i];
                            break;
                        }
                    }

                    ParcelUuid[] uuids = device.getUuids();

                    BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
                    socket.connect();
                    outputStream = socket.getOutputStream();
                }

                Log.e("error", "No appropriate paired devices.");
            } else {
                Log.e("error", "Bluetooth is disabled.");
            }
        }
    }


}
