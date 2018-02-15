package com.vonovak;

import android.app.Activity;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class AddCalendarEventModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    public final String ADD_EVENT_MODULE_NAME = "AddCalendarEvent";
    public final int ADD_EVENT_REQUEST_CODE = 11;
    public static final String DATE_PARSING_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private Promise promise;


    public AddCalendarEventModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addActivityEventListener(this);
        resetMembers();
    }

    private void resetMembers() {
        promise = null;
    }

    @Override
    public String getName() {
        return ADD_EVENT_MODULE_NAME;
    }

    private static long getTimestamp(String dateAsString) throws ParseException {
        SimpleDateFormat datetimeFormatter = new SimpleDateFormat(DATE_PARSING_FORMAT);
        datetimeFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return datetimeFormatter.parse(dateAsString).getTime();
    }

    @ReactMethod
    public void presentNewEventDialog(ReadableMap config, Promise eventPromise) {
        promise = eventPromise;

        try {
            final Intent calendarIntent = new Intent(Intent.ACTION_EDIT);
            calendarIntent
                    .setType("vnd.android.cursor.item/event")
                    .putExtra("title", config.getString("title"));

            if (config.hasKey("startDate")) {
                calendarIntent.putExtra("beginTime", getTimestamp(config.getString("startDate")));
            }

            if (config.hasKey("endDate")) {
                calendarIntent.putExtra("endTime", getTimestamp(config.getString("endDate")));
            }

            if (config.hasKey("location")
                    && config.getString("location") != null) {
                calendarIntent.putExtra("eventLocation", config.getString("location"));
            }

            if (config.hasKey("description")
                    && config.getString("description") != null) {
                calendarIntent.putExtra("description", config.getString("description"));
            }
            getReactApplicationContext().startActivityForResult(calendarIntent, ADD_EVENT_REQUEST_CODE, Bundle.EMPTY);
        } catch (ParseException e) {
            promise.reject(ADD_EVENT_MODULE_NAME, e);
        }
    }


    @Override
    public void onActivityResult(Activity activity, final int requestCode, final int resultCode, final Intent intent) {
        if (requestCode != ADD_EVENT_REQUEST_CODE || promise == null) {
            return;
        }
        resolvePromise();
    }


    private void resolvePromise() {
        if (promise == null) {
            Log.e(ADD_EVENT_MODULE_NAME, "promise is null");
            return;
        }


        promise.resolve(null);

        resetMembers();
    }

    private void destroyLoader(Loader loader) {
        // if loader isn't destroyed, onLoadFinished() gets called multiple times for some reason
        Activity activity = getCurrentActivity();
        if (activity != null) {
            activity.getLoaderManager().destroyLoader(loader.getId());
        } else {
            Log.d(ADD_EVENT_MODULE_NAME, "warning: activity was null when attempting to destroy the loader");
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
    }
}