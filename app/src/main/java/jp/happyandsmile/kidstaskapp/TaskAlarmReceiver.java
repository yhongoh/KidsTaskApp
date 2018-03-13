package jp.happyandsmile.kidstaskapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import io.realm.Realm;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by hongoyuka on 2018/02/24.
 */

public class TaskAlarmReceiver extends BroadcastReceiver {
    private Task task;
    private Integer nextAlarm, nextDay;
    private ToDo todo;
    private String strToday;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BuildConfig.DEBUG) {
            Log.d("onReceive", "onReceive");
        }

        // EXTRA_TASK から Task の id を取得して、 id から Task のインスタンスを取得する
        int taskId = intent.getIntExtra(MainActivity.EXTRA_TASK, -1);
        strToday = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date());

        Realm realm = Realm.getDefaultInstance();
        task = realm.where(Task.class).equalTo("id", taskId).findFirst();

        //LastDateが本日以降の場合のみ後続処理を実施する
        Calendar today = Calendar.getInstance();
        int today_year = today.get(Calendar.YEAR);
        int today_month = today.get(Calendar.MONTH);
        int today_day = today.get(Calendar.DAY_OF_MONTH);
        today.set(Calendar.MILLISECOND,000);
        today.set(today_year,today_month,today_day,0,0,0);
        Date currentDate = today.getTime();

        if (BuildConfig.DEBUG) {
            Log.d("task_LastDate", String.valueOf(task.getLastDate()));
            Log.d("currentDate", String.valueOf(currentDate));
        }

        if (task.getLastDate().compareTo(currentDate) > 0) {
            todo = realm.where(ToDo.class).equalTo("datestring", strToday).equalTo("taskId", taskId).findFirst();
            if (BuildConfig.DEBUG) {
                Log.d("TaskAlarmReceiver", String.valueOf(task));
                Log.d("TaskAlarmReceiver", String.valueOf(todo));
            }


            if (!task.getSunday() && task.getMonday() && task.getTuesday() && task.getWednesday() && task.getThursday() && task.getFriday() && task.getSaturday()) {
                //今日の曜日を取得
                Calendar cal = Calendar.getInstance();
                cal.get(Calendar.DAY_OF_WEEK);

                //タスクの繰り返し設定を確認

                ArrayList<Integer> weekday = new ArrayList<>();
                if (task.getSunday()) {
                    weekday.add(1);
                }
                if (task.getMonday()) {
                    weekday.add(2);
                }
                if (task.getTuesday()) {
                    weekday.add(3);
                }
                if (task.getWednesday()) {
                    weekday.add(4);
                }
                if (task.getThursday()) {
                    weekday.add(5);
                }
                if (task.getFriday()) {
                    weekday.add(6);
                }
                if (task.getSaturday()) {
                    weekday.add(7);
                }

                if (BuildConfig.DEBUG) {
                    Log.d("weekday", String.valueOf(weekday));
                }

                for (int i = 0; i < weekday.size(); i++) {
                    if (weekday.get(i) > cal.get(Calendar.DAY_OF_WEEK)) {
                        nextDay = weekday.get(i);
                        break;
                    }
                }

                if (nextDay == null) {
                    for (int i = 0; i < weekday.size(); i++) {
                        if (weekday.get(i) + 7 > cal.get(Calendar.DAY_OF_WEEK)) {
                            nextDay = weekday.get(i);
                            break;
                        }
                    }

                    if (BuildConfig.DEBUG) {
                        Log.d("nextDay", String.valueOf(nextDay));
                    }

                    nextAlarm = nextDay - cal.get(Calendar.DAY_OF_WEEK);

                    if (nextAlarm > 0) {
                        nextAlarm = nextAlarm;
                    } else {
                        nextAlarm = nextAlarm + 7;
                    }

                    if (BuildConfig.DEBUG) {
                        Log.d("nextAlarm", String.valueOf(nextAlarm));
                    }

                    //次回のタスク実行時刻
                    Calendar taskTime = Calendar.getInstance();
                    taskTime.setTime(task.getDate());
                    taskTime.add(Calendar.DAY_OF_MONTH, nextAlarm);
                    long nextAlarmTimer = taskTime.getTimeInMillis();
                    if (BuildConfig.DEBUG) {
                        Log.d("nextAlarmTimer", String.valueOf(nextAlarmTimer));
                    }

                    //次回Alarmのセット
                    Intent resultIntent = new Intent(context, TaskAlarmReceiver.class);

                    PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                            context,
                            taskId,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(nextAlarmTimer, null), resultPendingIntent);
                    } else {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextAlarmTimer, resultPendingIntent);
                    }
                }
            }

            //ToDoのステータスが0（未完了）の時のみAlertDialogを呼び出す
            if (todo.getStatus() == 0) {
                Intent intent1 = new Intent(context, AlertDialogActivity.class);
                intent1.putExtra(MainActivity.EXTRA_TASK, task.getId());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent1);
            }
        }
    }
}

