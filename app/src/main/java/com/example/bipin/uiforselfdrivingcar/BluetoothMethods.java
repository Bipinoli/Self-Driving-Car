package com.example.bipin.uiforselfdrivingcar;

import android.util.Log;
import java.io.IOException;

/**
 * Created by bipin on 1/28/2018.
 */

public class BluetoothMethods {

    /**
     *
     * @param s String of single character is sent via bluetooth to arduino
     * @throws IOException
     */
    public static void bluetoothWrite(String s) throws IOException {
        MainActivity.outputStream.write(s.getBytes());
    }


    /**
     * Sends the path of the robot found from GameView to arduino via bluetooth
     */
    public static void bluetoothWriteMessage(String s) {
        try {
            for (int i=0; i<s.length(); i++) {
                Thread.sleep(100);
                String str = s.substring(i,i+1);
                bluetoothWrite(str);
            }
        }
        catch (IOException e) {
            Log.e("EXCEPTION", "failed to write data during bluetooth comm.");
        }
        catch (Exception e) {
            Log.e("EXCEPTION", e.toString());
            Log.e("EXCEPTION", "failed to write data during bluetooth comm. due to exception while doing Thread.sleep");
        }
    }
}
