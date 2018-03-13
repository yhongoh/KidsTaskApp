package jp.happyandsmile.kidstaskapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

import static io.realm.Sort.ASCENDING;


public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_TASK = "jp.happyandsmile.kidstaskapp.TASK";

    private Toolbar mToolbar;
    private Realm mRealm;
    private RealmChangeListener mRealmListener = new RealmChangeListener() {
        @Override
        public void onChange(Object element) {
        }
    };
    private ListView mListView;
    private TaskAdapter mTaskAdapter;
    private String mStrDate;
    private Calendar calendar, today, calToday, calmDate, cal, calnewDate;
    static Date mDate;
    private Date nextDate,defaltLastDate;
    private String mStrTodo;
    private int mDayOfWeek,todayYear,todayMonth,todayDay;
    private RealmResults<Task> taskRealmResults;
    private long long_mDate;
    private ArrayList checkedItems;
    private Task task;
    private Integer nextDay, nextAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        //MainActivityのタイトル変更
        setTitle(R.string.app_todo);

        calendar = Calendar.getInstance();
        int mDateYear = calendar.get(Calendar.YEAR);
        int mDateMonth = calendar.get(Calendar.MONTH);
        int mDateday = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(mDateYear, mDateMonth, mDateday, 0, 0, 0);
        mDate = calendar.getTime();
        mStrDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(mDate);


        //PreviousDay
        ImageView previousDay = (ImageView) findViewById(R.id.previousDay);
        previousDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 日時を加算する
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                mDate = calendar.getTime();
                mDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                mStrDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(mDate);
                mStrTodo = new SimpleDateFormat("MM月dd日(E)のやること", Locale.getDefault()).format(mDate);

                TextView tv = (TextView) findViewById(R.id.date);
                tv.setText(mStrTodo);
                reloadListView();
            }
        });

        //NextDay
        ImageView nextDay = (ImageView) findViewById(R.id.nextDay);
        nextDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 日時を加算する
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                mDate = calendar.getTime();
                mDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

                mStrDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(mDate);

                mStrTodo = new SimpleDateFormat("MM月dd日(E)のやること", Locale.getDefault()).format(mDate);
                TextView tv = (TextView) findViewById(R.id.date);
                tv.setText(mStrTodo);

                reloadListView();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                startActivity(intent);
            }
        });

        // Realmの設定
        mRealm = Realm.getDefaultInstance();
        mRealm.addChangeListener(mRealmListener);

        // TextViewの設定
        TextView tv = (TextView) findViewById(R.id.date);
        String mTodo = new SimpleDateFormat("MM月dd日(E)のやること", Locale.getDefault()).format(mDate);
        tv.setText(mTodo);

        // ListViewの設定
        mTaskAdapter = new TaskAdapter(MainActivity.this);

        mListView = (ListView) findViewById(R.id.listView1);

        // ListViewをタップしたときの処理
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()

                                         {
                                             @Override
                                             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                 // 入力・編集する画面に遷移させる
                                                 Task task = (Task) parent.getAdapter().getItem(position);

                                                 Intent intent = new Intent(MainActivity.this, InputActivity.class);
                                                 intent.putExtra(EXTRA_TASK, task.getId());
                                                 intent.putExtra("long_mDate", long_mDate);
                                                 startActivity(intent);
                                             }
                                         }

        );

        //ListViewを長押ししたときの処理
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()

                                             {
                                                 @Override
                                                 public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
                                                                                long id) {


                                                     // タスクを削除する
                                                     task = (Task) parent.getAdapter().getItem(position);

                                                     final RealmResults<Task> results = mRealm.where(Task.class).equalTo("id", task.getId()).findAll();

                                                     //繰り返し設定がある場合、個別削除か一括削除か確認するDialogを表示する
                                                     if (task.getSunday() || task.getMonday() || task.getTuesday() || task.getWednesday() || task.getThursday() || task.getFriday() || task.getSaturday()) {
                                                         showConfirmDialog();
                                                     } else {
                                                         //ダイアログを表示する
                                                         AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                                                         builder.setTitle("削除");
                                                         builder.setMessage(task.getTitle() + "を削除しますか");
                                                         builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                             @Override
                                                             public void onClick(DialogInterface dialog, int which) {

                                                                 RealmResults<ToDo> todoResults = mRealm.where(ToDo.class).equalTo("taskId", task.getId()).findAll();

                                                                 if (BuildConfig.DEBUG) {
                                                                     Log.d("todoResults", String.valueOf(todoResults));
                                                                 }

                                                                 mRealm.beginTransaction();
                                                                 results.deleteAllFromRealm();
                                                                 todoResults.deleteAllFromRealm();
                                                                 mRealm.commitTransaction();

                                                                 reloadListView();

                                                             }
                                                         });
                                                         builder.setNegativeButton("CANCEL", null);

                                                         AlertDialog dialog = builder.create();
                                                         dialog.show();

                                                         AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                                                         Intent intent = new Intent(getApplicationContext(), TaskAlarmReceiver.class);
                                                         PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), task.getId(), intent, 0);

                                                         pendingIntent.cancel();
                                                         alarmManager.cancel(pendingIntent);


                                                     } return true;
                                                 }
                                             }


        );
    }


    private void reloadListView() {
        mDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        calendar.set(Calendar.MILLISECOND,000);
        mDate = calendar.getTime();
        long_mDate = mDate.getTime();
        if (BuildConfig.DEBUG) {
            Log.d("long_mDate", String.valueOf(long_mDate));
        }

        //今日の日付を同日比較をできるように変換
        today = Calendar.getInstance();
        calToday = Calendar.getInstance();
        todayYear = today.get(Calendar.YEAR);
        todayMonth = today.get(Calendar.MONTH) + 1;
        todayDay = today.get(Calendar.DAY_OF_MONTH);
        calToday.set(todayYear, todayMonth, todayDay, 0, 0);

        //選択日時を同日比較できるように変換
        cal = Calendar.getInstance();
        calmDate = Calendar.getInstance();
        cal.setTime(mDate);
        int mDateYear = cal.get(Calendar.YEAR);
        int mDateMonth = cal.get(Calendar.MONTH) + 1;
        int mDateDay = cal.get(Calendar.DAY_OF_MONTH);
        calmDate.set(mDateYear, mDateMonth, mDateDay, 0, 0);

        mStrDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(calendar.getTime());
        if (BuildConfig.DEBUG) {
            Log.d("reloadListView_mdate", mStrDate);
        }

        //選択日時+1
        calnewDate = Calendar.getInstance();
        calnewDate.setTime(mDate);
        calnewDate.add(Calendar.DAY_OF_MONTH, +1);
        int calnewDateYear = calnewDate.get(Calendar.YEAR);
        int calnewDateMonth = calnewDate.get(Calendar.MONTH);
        int calnewDateday = calnewDate.get(Calendar.DAY_OF_MONTH);
        calnewDate.set(calnewDateYear, calnewDateMonth, calnewDateday, 0, 0, 0);
        nextDate = calnewDate.getTime();

        if (BuildConfig.DEBUG) {
            Log.d("reloadListView_mdate", String.valueOf(mDate));
        }

        // Realmデータベースから、「指定した日のデータを取得して新しい時間順に並べた結果」を取得
        switch (mDayOfWeek) {
            case 1:
                taskRealmResults = mRealm.where(Task.class).equalTo("datestring", mStrDate).greaterThan("lastDate", mDate).or().beginGroup().equalTo("sunday", true).lessThan("date", nextDate).greaterThan("lastDate", mDate).endGroup().findAll();
                break;
            case 2:
                taskRealmResults = mRealm.where(Task.class).equalTo("datestring", mStrDate).greaterThan("lastDate", mDate).or().beginGroup().equalTo("monday", true).lessThan("date", nextDate).greaterThan("lastDate", mDate).endGroup().findAll();
                break;
            case 3:
                taskRealmResults = mRealm.where(Task.class).equalTo("datestring", mStrDate).greaterThan("lastDate", mDate).or().beginGroup().equalTo("tuesday", true).lessThan("date", nextDate).greaterThan("lastDate", mDate).endGroup().findAll();
                break;
            case 4:
                taskRealmResults = mRealm.where(Task.class).equalTo("datestring", mStrDate).greaterThan("lastDate", mDate).or().beginGroup().equalTo("wednesday", true).lessThan("date", nextDate).greaterThan("lastDate", mDate).endGroup().findAll();
                break;
            case 5:
                taskRealmResults = mRealm.where(Task.class).equalTo("datestring", mStrDate).greaterThan("lastDate", mDate).or().beginGroup().equalTo("thursday", true).lessThan("date", nextDate).greaterThan("lastDate", mDate).endGroup().findAll();
                break;
            case 6:
                taskRealmResults = mRealm.where(Task.class).equalTo("datestring", mStrDate).greaterThan("lastDate", mDate).or().beginGroup().equalTo("friday", true).lessThan("date", nextDate).greaterThan("lastDate", mDate).endGroup().findAll();
                break;
            case 7:
                taskRealmResults = mRealm.where(Task.class).equalTo("datestring", mStrDate).greaterThan("lastDate", mDate).or().beginGroup().equalTo("saturday", true).lessThan("date", nextDate).greaterThan("lastDate", mDate).endGroup().findAll();
                break;
            default:
        }

        taskRealmResults = taskRealmResults.sort("displayTime", ASCENDING);
        if (BuildConfig.DEBUG) {
            Log.d("taskRealmResults", String.valueOf(taskRealmResults));
        }


        // 上記の結果を、TaskList としてセットする
        mTaskAdapter.setTaskList(mRealm.copyFromRealm(taskRealmResults));

        // TaskのListView用のアダプタに渡す
        mListView.setAdapter(mTaskAdapter);

        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged();
    }

    //繰り返し設定があった場合にその日のみ変更するか、以降の予定を変更するか確認する
    private void showConfirmDialog() {
        if (BuildConfig.DEBUG) {
            Log.d("MA_showConfirm", "MA_showConfirm");
        }
        final String[] items = {"この予定のみ削除", "これ以降の全てを削除"};
        int defaultItem = 0; // デフォルトでチェックされているアイテム
        checkedItems = new ArrayList<>();
        checkedItems.add(defaultItem);
        new android.app.AlertDialog.Builder(MainActivity.this)
                .setTitle("確認")
                .setSingleChoiceItems(items, defaultItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkedItems.clear();
                        checkedItems.add(which);
                    }
                })
                .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!checkedItems.isEmpty()) {
                            if (checkedItems.get(0).toString().equals("0")) {
                                deleteTask();
                                if (BuildConfig.DEBUG) {
                                    Log.d("checkedItem0", String.valueOf(checkedItems.get(0)));
                                }

                            } else {
                                deleteTask();
                                if (BuildConfig.DEBUG) {
                                    Log.d("checkedItem1", String.valueOf(checkedItems.get(0)));
                                }
                            }
                        }
                    }
                })
                .setNegativeButton("キャンセル", null)
                .show();
    }

    // checkedItems.get(0) = 0 単発予定を削除
    // checkedItems.get(0) = 0 以降の予定を削除
    private void deleteTask(){
        if (checkedItems.get(0).toString().equals("0")) {
            //単発のタスクを削除

            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();

            //削除日以降の次のタスクを新規作成
            Task mTask1 = new Task();
            ToDo mToDo1 = new ToDo();

            //IDを設定
            RealmResults<Task> taskRealmResults = realm.where(Task.class).findAll();
            RealmResults<ToDo> todoResults = realm.where(ToDo.class).findAll();

            int task_id1 = taskRealmResults.max("id").intValue() + 1;
            mTask1.setId(task_id1);

            int todo_id1;
            todo_id1 = todoResults.max("id").intValue() + 1;
            mToDo1.setId(todo_id1);

            //タイトル、繰り返し設定、画像、Timeは変更前のタスクの情報を引き継ぐ
            mTask1.setTitle(task.getTitle());

            Calendar cal1 = Calendar.getInstance();
            //タスク一覧で時刻でソートするために年月日は固定とする
            cal1.setTime(task.getDate());
            int mHour = cal1.get(Calendar.HOUR);
            int mMinute = cal1.get(Calendar.MINUTE);
            cal1.set(2000, 1, 1, mHour, mMinute);
            Date time = cal1.getTime();
            mTask1.setDisplayTime(time);

            //繰り返し設定の登録
            mTask1.setMonday(task.getMonday());
            mTask1.setTuesday(task.getTuesday());
            mTask1.setWednesday(task.getWednesday());
            mTask1.setThursday(task.getThursday());
            mTask1.setFriday(task.getFriday());
            mTask1.setSaturday(task.getSaturday());
            mTask1.setSunday(task.getSunday());

            //画像の登録
            if (task.getImageBytes() != null) {
                mTask1.setImageBytes(task.getImageBytes());
            }

            //変更対象の次のタスクを設定する
            Calendar cal = Calendar.getInstance();
            cal.setTime(mDate);
            cal.get(Calendar.DAY_OF_WEEK);
            if (BuildConfig.DEBUG) {
                Log.d("mDate_DOW", String.valueOf(cal.get(Calendar.DAY_OF_WEEK)));
            }

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

            //次回のタスク実行時刻を設定
            Calendar ex_task = Calendar.getInstance();
            ex_task.setTime(task.getDate());
            int ex_task_hour = ex_task.get(Calendar.HOUR_OF_DAY);
            int ex_task_minute = ex_task.get(Calendar.MINUTE);
            Calendar taskTime = Calendar.getInstance();
            Calendar calDate = Calendar.getInstance();
            calDate.setTime(mDate);
            int mYear = calDate.get(Calendar.YEAR);
            int mMonth = calDate.get(Calendar.MONTH);
            int mDay = calDate.get(Calendar.DAY_OF_MONTH);
            taskTime.set(mYear, mMonth, mDay, ex_task_hour, ex_task_minute);
            taskTime.add(Calendar.DAY_OF_MONTH, nextAlarm);
            Date taskDate = taskTime.getTime();

            mTask1.setDate(taskDate);

            SimpleDateFormat strdate = new SimpleDateFormat("yyyy/MM/dd");
            String datestring = strdate.format(taskDate);
            mTask1.setDatestring(datestring);

            //LastDateは初期値を設定
            Calendar cal2 = Calendar.getInstance();
            cal2.set(2099, 11, 31, 23, 59, 59);
            cal2.set(Calendar.MILLISECOND,000);
            defaltLastDate = cal2.getTime();
            mTask1.setLastDate(defaltLastDate);

            //mToDoの更新
            mToDo1.setDate(taskDate);
            mToDo1.setDatestring(datestring);
            mToDo1.setStatus(0);
            mToDo1.setTaskId(mTask1.getId());


            //変更前のタスクに終了日時を設定
            task.setLastDate(mDate);

            realm.copyToRealmOrUpdate(task);
            realm.copyToRealmOrUpdate(mTask1);
            realm.copyToRealmOrUpdate(mToDo1);
            realm.commitTransaction();

            realm.close();

            Intent resultIntent = new Intent(MainActivity.this, TaskAlarmReceiver.class);
            resultIntent.putExtra(MainActivity.EXTRA_TASK, mTask1.getId());
            PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                    this,
                    mTask1.getId(),
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );


            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(taskTime.getTimeInMillis(), null), resultPendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, taskTime.getTimeInMillis(), resultPendingIntent);
            }

            reloadListView();
        }
        else {
            //以降の予定を全て削除
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();

            //変更前のタスクに終了日時を設定
            task.setLastDate(mDate);
            realm.copyToRealmOrUpdate(task);
            realm.commitTransaction();
            realm.close();

            reloadListView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (BuildConfig.DEBUG) {
            Log.d("MA_onResume", "onResume");
        }

        // Realmの設定
        mRealm=Realm.getDefaultInstance();
        mRealm.addChangeListener(mRealmListener);

        // TextViewの設定
        TextView tv = (TextView)findViewById(R.id.date);
        mStrTodo = new SimpleDateFormat("MM月dd日(E)のやること", Locale.getDefault()).format(mDate);
        tv.setText(mStrTodo);

        reloadListView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }
}