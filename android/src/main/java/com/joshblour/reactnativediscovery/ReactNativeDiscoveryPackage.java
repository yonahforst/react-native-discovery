package com.joshblour.reactnativediscovery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import com.joshblour.reactnativediscovery.ReactNativeDiscoveryModule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ReactNativeDiscoveryPackage implements ReactPackage {

    private Activity mActivity;

    public ReactNativeDiscoveryPackage(Activity activityContext) {
        mActivity = activityContext;
    }

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<>();
        modules.add(new ReactNativeDiscoveryModule(reactContext, mActivity));
        return modules;    }

    @Override
    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Arrays.asList();
    }
}
