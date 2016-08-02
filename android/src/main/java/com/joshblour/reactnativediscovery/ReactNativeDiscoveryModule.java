package com.joshblour.reactnativediscovery;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;


import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.joshblour.discovery.BLEUser;
import com.joshblour.discovery.Discovery;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class ReactNativeDiscoveryModule extends ReactContextBaseJavaModule implements Discovery.DiscoveryCallback, LifecycleEventListener {

    private static Discovery mDiscovery;
    private static ParcelUuid mDiscoveryUUID;
    private int mScanForSeconds;
    private int mWaitForSeconds;

    private final BroadcastReceiver mBleStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                handleStateChange(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR));
            }
        }
    };


    public ReactNativeDiscoveryModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "ReactNativeDiscovery";
    }


    /**
     * Initialize the Discovery object with a UUID specific to your app, and a username specific to your device.
     */
    @ReactMethod
    public void initialize(String uuid, String username, Promise promise) {
        // Register for broadcasts on BluetoothAdapter state change
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        getReactApplicationContext().registerReceiver(mBleStateReceiver, filter);

        mDiscoveryUUID = ParcelUuid.fromString(uuid);
        mDiscovery =  new Discovery(getReactApplicationContext(), mDiscoveryUUID, username, Discovery.DIStartOptions.DIStartNone, this);

        getReactApplicationContext().addLifecycleEventListener(this);

        if (BluetoothAdapter.getDefaultAdapter() != null) {
            handleStateChange(BluetoothAdapter.getDefaultAdapter().getState());
        }
        promise.resolve(uuid);
    }


    public void handleStateChange(int state) {
        WritableMap params = Arguments.createMap();

        switch (state) {
            case BluetoothAdapter.STATE_OFF:
                params.putBoolean("isOn", false);
                break;

            case BluetoothAdapter.STATE_ON:
                params.putBoolean("isOn", true);
                break;

        }

        if (params.hasKey("isOn")) {
            getReactApplicationContext()
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("bleStateChanged", params);
        }
    }

    @Override
    public void didUpdateUsers(ArrayList<BLEUser> users, Boolean usersChanged) {
        WritableArray usersArray = Arguments.createArray();
        for (BLEUser user : users) {
            usersArray.pushMap(convertBLEUserToMap(user));
        }

        WritableMap params = Arguments.createMap();
        params.putArray("users", usersArray);
        params.putBoolean("usersChanged", usersChanged);
        params.putString("uuid", mDiscoveryUUID.toString());

        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("discoveredUsers", params);
    }

    private WritableMap convertBLEUserToMap(BLEUser bleUser) {
        WritableMap params = Arguments.createMap();
        params.putString("peripheralId", bleUser.getDeviceAddress());
        params.putString("username", bleUser.getUsername());
        params.putBoolean("identified", bleUser.isIdentified());
        params.putInt("rssi", bleUser.getRssi());

        if (bleUser.getProximity() != null)
            params.putInt("proximity", bleUser.getProximity());

        params.putString("updateTime", getISO8601StringForDate(new Date(bleUser.getUpdateTime())));
        return params;
    }


    /**
     * Changing these properties will start/stop advertising/discovery
     */
    @ReactMethod
    public void setShouldAdvertise(Boolean shouldAdvertise) {
        mDiscovery.setShouldAdvertise(shouldAdvertise);
    }

    @ReactMethod
    public void setShouldDiscover(Boolean shouldDiscover) {
        mDiscovery.setShouldDiscover(shouldDiscover);
    }

    /*
     * Discovery removes the users if can not re-see them after some amount of time, assuming the device-user is gone.
     * The default value is 3 seconds. You can set your own values.
     */
    @ReactMethod
    public void setUserTimeoutInterval(int userTimeoutInterval) {
        mDiscovery.setUserTimeoutInterval(userTimeoutInterval);
    }

    @ReactMethod
    public void setScanForSeconds(int scanForSeconds) {
        mScanForSeconds = scanForSeconds;
        mDiscovery.setScanForSeconds(mScanForSeconds);
    }

    @ReactMethod
    public void setWaitForSeconds(int waitForSeconds) {
        mWaitForSeconds = waitForSeconds;
        mDiscovery.setWaitForSeconds(mWaitForSeconds);
    }

    /**
     * Set this to YES, if your app will disappear, or set to NO when it will appear.
     */
    @ReactMethod
    public void setPaused(Boolean paused) {
        mDiscovery.setPaused(paused);
    }


    @Override
    public void onHostResume() {
        mDiscovery.setWaitForSeconds(mWaitForSeconds);
    }

    @Override
    public void onHostPause() {
        mDiscovery.setWaitForSeconds(mWaitForSeconds * 6);
    }

    @Override
    public void onHostDestroy() {
        Log.e("TAG", "ACTIVITY DESTROYED");
        mDiscovery.setShouldAdvertise(false);
        mDiscovery.setShouldDiscover(false);
        getReactApplicationContext().unregisterReceiver(mBleStateReceiver);
    }
    /**
     * Return an ISO 8601 combined date and time string for specified date/time
     *
     * @param date
     *            Date
     * @return String with format "yyyy-MM-dd'T'HH:mm:ss'Z'"
     */
    private static String getISO8601StringForDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }
}
