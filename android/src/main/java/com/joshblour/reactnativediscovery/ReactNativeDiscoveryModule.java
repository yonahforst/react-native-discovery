package com.yonahforst.discoveryreact;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;


import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.NativeModule;
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


public class DiscoveryReactModule extends ReactContextBaseJavaModule implements Discovery.DiscoveryCallback, Application.ActivityLifecycleCallbacks {

    private static Discovery mDiscovery;
    private static ParcelUuid mDiscoveryUUID;
    private static Activity mActivity;

    public DiscoveryReactModule(ReactApplicationContext reactContext, Activity activity) {
        super(reactContext);
        mActivity = activity;
    }

    @Override
    public String getName() {
        return "DiscoveryReact";
    }


    /**
     * Initialize the Discovery object with a UUID specific to your app, and a username specific to your device.
     */
    @ReactMethod
    public void initialize(String uuid, String username) {
        mDiscoveryUUID = ParcelUuid.fromString(uuid);
        mDiscovery =  new Discovery(getReactApplicationContext(), mDiscoveryUUID, username, Discovery.DIStartOptions.DIStartNone, this);
        this.mActivity.getApplication().registerActivityLifecycleCallbacks(this);
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

//
//
////commented because i dont know how to return values for exported methods
/////**
//// * Returns the user user from our user dictionary according to its peripheralId.
//// */
////RCT_EXPORT_METHOD(userWithPeripheralId:(NSString *)peripheralId) {
////    BLEUser *user = [self.discovery userWithPeripheralId:peripheralId];
////    return [self convertBLEUserToDict:user];
////}
//
//


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


//    THIS DOESNT EXIST FOR ANDROID. ONLY IOS
//    /*
//        * Update interval is the interval that your usersBlock gets triggered.
//        */
//        RCT_EXPORT_METHOD(setUpdateInterval:(int)updateInterval)
//        {
//            [self.discovery setUpdateInterval:updateInterval];
//    }

    @ReactMethod
    public void setScanForSeconds(int scanForSeconds) {
        mDiscovery.setScanForSeconds(scanForSeconds);
    }

    @ReactMethod
    public void setWaitForSeconds(int waitForSeconds) {
        mDiscovery.setWaitForSeconds(waitForSeconds);
    }

    /**
     * Set this to YES, if your app will disappear, or set to NO when it will appear.
     */
    @ReactMethod
    public void setPaused(Boolean paused) {
        mDiscovery.setPaused(paused);
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

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.e("TAG", "ACTIVITY DESTROYED");
        mDiscovery.setShouldAdvertise(false);
        mDiscovery.setShouldDiscover(false);
    }
}
