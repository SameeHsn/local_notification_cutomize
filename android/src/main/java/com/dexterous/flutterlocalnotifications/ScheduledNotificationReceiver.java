package com.dexterous.flutterlocalnotifications;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.core.app.NotificationManagerCompat;

import com.dexterous.flutterlocalnotifications.models.NotificationDetails;
import com.dexterous.flutterlocalnotifications.utils.StringUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import com.google.firebase.analytics.FirebaseAnalytics;
import java.util.Date;
import java.text.SimpleDateFormat;
import android.app.NotificationManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import com.rudderstack.android.sdk.core.*;
import com.rudderstack.android.sdk.core.RudderClient;
import com.google.firebase.FirebaseApp;
/** Created by michaelbui on 24/3/18. */
@Keep
public class ScheduledNotificationReceiver extends BroadcastReceiver {

  private static final String TAG = "ScheduledNotificationReceiver";
  private FirebaseAnalytics mFirebaseAnalytics;
  @Override
  @SuppressWarnings("deprecation")
  public void onReceive(final Context context, Intent intent) {
    FirebaseApp.initializeApp(context);
//    RudderClient rudderClient = RudderClient.getInstance(
//            context,
//            "2UeJCPdPLAKDVVepD86HWjwfxf2",
//            new RudderConfig.Builder()
//                    .withDataPlaneUrl("https://mawaqitsagskez.dataplane.rudderstack.com")
//                    .withTrackLifecycleEvents(true)
//                    .withRecordScreenViews(true)
//                    .withSleepCount(1)
////                    .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
//                    .build()
//    );


    // Obtain the FirebaseAnalytics instance.
    mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);

    // mFirebaseAnalytics.registerCustomDimension(1,"currentDateTime");
    // mFirebaseAnalytics.registerCustomDimension(2,"scheduledDateTime");
    // mFirebaseAnalytics.registerCustomDimension(3,"isPowerSavingModeOn");
    // mFirebaseAnalytics.registerCustomDimension(4,"isDoNotDisturbOn");
    // mFirebaseAnalytics.registerCustomDimension(5,"isBatteryOptimizationEnabled");
    // mFirebaseAnalytics.registerCustomDimension(6,"noitification_title");

    
    String notificationDetailsJson =
        intent.getStringExtra(FlutterLocalNotificationsPlugin.NOTIFICATION_DETAILS);

    
    if (StringUtils.isNullOrEmpty(notificationDetailsJson)) {
      // This logic is needed for apps that used the plugin prior to 0.3.4

      Notification notification;
      int notificationId = intent.getIntExtra("notification_id", 0);

      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        notification = intent.getParcelableExtra("notification", Notification.class);
      } else {
        notification = intent.getParcelableExtra("notification");
      }

      if (notification == null) {
        // This means the notification is corrupt
        FlutterLocalNotificationsPlugin.removeNotificationFromCache(context, notificationId);
        Log.e(TAG, "Failed to parse a notification from  Intent. ID: " + notificationId);
        return;
      }

      notification.when = System.currentTimeMillis();
      NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
      notificationManager.notify(notificationId, notification);
      notificationManager.notify(notificationId, notification);
      boolean repeat = intent.getBooleanExtra("repeat", false);
      if (!repeat) {
        FlutterLocalNotificationsPlugin.removeNotificationFromCache(context, notificationId);
      }
    }
    else {
      Gson gson = FlutterLocalNotificationsPlugin.buildGson();
      Type type = new TypeToken<NotificationDetails>() {}.getType();
      NotificationDetails notificationDetails = gson.fromJson(notificationDetailsJson, type);

      FlutterLocalNotificationsPlugin.showNotification(context, notificationDetails);
      FlutterLocalNotificationsPlugin.scheduleNextNotification(context, notificationDetails);


      String isPowerSavingModeOn="";
      String isDoNotDisturbOn="";
      String isBatteryOptimizationEnabled="";
      Date date = new Date();
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
      String formattedDate = dateFormat.format(date);
      
      Log.d("notificationDetailsJson:",notificationDetailsJson.toString());
      Log.d("currentDateTime:",formattedDate.toString());
      Log.d("scheduledDateTime:",notificationDetails.scheduledDateTime.toString());
      Log.d("adhanTitle:",notificationDetails.title.toString());
//      Log.d("adhanTitle:",notificationDetails.styleInformation("title").toString());
      if (isPowerSavingModeOn(context)) {
        Log.d("isPowerSavingModeOn?:", "True");
        isPowerSavingModeOn="True";
      } else {
        Log.d("isPowerSavingModeOn?:", "False");
        isPowerSavingModeOn="False";
      }
      if (isDoNotDisturbOn(context)) {
        Log.d("isDoNotDisturbOn?:", "True");
        isDoNotDisturbOn="True";
      } else {
        Log.d("isDoNotDisturbOn?:", "False");
        isDoNotDisturbOn="False";
      }
      if (isBatteryOptimizationEnabled(context)) {
        Log.d("isBatteryOptimizationEnabled?:", "True");
        isBatteryOptimizationEnabled="True";

      } else {
        Log.d("isBatteryOptimizationEnabled?:", "False");
        isBatteryOptimizationEnabled="False";
      }

//      RudderTraits traits = new RudderTraits();
//      traits.put("currentDateTime", formattedDate.toString());
//      traits.put("scheduledDateTime", notificationDetails.scheduledDateTime.toString());
//      traits.put("adhanTitle:",notificationDetails.title.toString());
//      traits.put("isPowerSavingModeOn", isPowerSavingModeOn);
//      traits.put("isDoNotDisturbOn", isDoNotDisturbOn);
//      traits.put("isBatteryOptimizationEnabled", isBatteryOptimizationEnabled);
//
//      rudderClient.identify("test_notifcation_delay", traits, null);

 
      Bundle bundle = new Bundle();
      bundle.putString("currentDateTime", formattedDate.toString());
      bundle.putString("scheduledDateTime", notificationDetails.scheduledDateTime.toString());
      bundle.putString("isPowerSavingModeOn", isPowerSavingModeOn);
      bundle.putString("isDoNotDisturbOn", isDoNotDisturbOn);
      bundle.putString("isBatteryOptimizationEnabled", isBatteryOptimizationEnabled);
      bundle.putString("noitification_title", notificationDetails.title.toString());
      Log.d("-------- :event logged Bundle",bundle.toString());
      // bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, formattedDate.toString());
      // bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, notificationDetails.scheduledDateTime.toString());
      // bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, isPowerSavingModeOn);
      // bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, isDoNotDisturbOn);
      // bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, isBatteryOptimizationEnabled);
      // bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, notificationDetails.title.toString());

      
      mFirebaseAnalytics.logEvent("delay_notification_test_demo", bundle);
      // mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, bundle);

    }
  }
  public boolean isPowerSavingModeOn(Context context) {
    PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    return powerManager != null && powerManager.isPowerSaveMode();
  }
  public static boolean isDoNotDisturbOn(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
      if (notificationManager != null) {
        int currentInterruptionFilter = notificationManager.getCurrentInterruptionFilter();
        return currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALARMS ||
                currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_PRIORITY;
      }
    }
    return false;
  }

  public static boolean isBatteryOptimizationEnabled(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      String packageName = context.getPackageName();
      PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

      if (powerManager != null) {
        return !powerManager.isIgnoringBatteryOptimizations(packageName);
      }
    }
    return false;
  }
}
