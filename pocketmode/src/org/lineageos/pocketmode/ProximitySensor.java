/*
 * Copyright (c) 2016 The CyanogenMod Project
 *               2018 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.pocketmode;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class ProximitySensor implements SensorEventListener {

    private static final boolean DEBUG = false;
    private static final String TAG = "PocketModeProximity";

    private static final String FPC_FILE = "/sys/devices/soc/soc:fpc_fpc1020/proximity_state";

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Context mContext;

    public ProximitySensor(Context context) {
        mContext = context;
        mSensorManager = mContext.getSystemService(SensorManager.class);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        boolean isNear = event.values[0] < mSensor.getMaximumRange();
        if (isFileWritable(FPC_FILE)) {
            writeLine(FPC_FILE, isNear ? "1" : "0");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        /* Empty */
    }

    protected void enable() {
        if (DEBUG) Log.d(TAG, "Enabling");
        mSensorManager.registerListener(this, mSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void disable() {
        if (DEBUG) Log.d(TAG, "Disabling");
        mSensorManager.unregisterListener(this, mSensor);
    }

     /**
     * Checks whether the given file is writable
     *
      * @return true if writable, false if not
      */
     public static boolean isFileWritable(String fileName) {
         final File file = new File(fileName);
         return file.exists() && file.canWrite();
     }

     /**
      * Writes the given value into the given file
      *
      * @return true on success, false on failure
      */
     public static boolean writeLine(String fileName, String value) {
         BufferedWriter writer = null;
          try {
             writer = new BufferedWriter(new FileWriter(fileName));
             writer.write(value);
         } catch (FileNotFoundException e) {
             Log.w(TAG, "No such file " + fileName + " for writing", e);
             return false;
         } catch (IOException e) {
             Log.e(TAG, "Could not write to file " + fileName, e);
             return false;
         } finally {
             try {
                 if (writer != null) {
                     writer.close();
                 }
             } catch (IOException e) {
                 // Ignored, not much we can do anyway
             }
         }
          return true;
     }
}
