package jp.happyandsmile.kidstaskapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
            //reloadListView();
        }
    };
    private ListView mListView;
    private TaskAdapter mTaskAdapter;
    private String mStrDate;
    private String StrToday;
    private Calendar calendar, today, calToday, calmDate, cal, calnewDate;
    static Date mDate;
    private Date newDate;
    private ToDo mToDo;
    private String mStrTodo;
    private int mDayOfWeek,todayYear,todayMonth,todayDay,identifier;
    private RealmResults<Task> taskRealmResults;
    private long long_mDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        //MainActivityのタイトル変更
        setTitle(R.string.app_todo);

        calendar = Calendar.getInstance();
        mDate = calendar.getTime();
        mStrDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(mDate);


        //PreviousDay
        ImageView previousDay = (ImageView) findViewById(R.id.previousDay);
        previousDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                // 日時を加算する
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                mDate = calendar.getTime();
                mDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                mStrDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(mDate);
                mStrTodo = new SimpleDateFormat("MM月dd日(E)のやること", Locale.getDefault()).format(mDate);

                TextView tv = (TextView)findViewById(R.id.date);
                tv.setText(mStrTodo);
                reloadListView();
            }
        });

        //NextDay
        ImageView nextDay = (ImageView) findViewById(R.id.nextDay);
        nextDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                // 日時を加算する
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                mDate = calendar.getTime();
                mDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

                mStrDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(mDate);

                mStrTodo = new SimpleDateFormat("MM月dd日(E)のやること", Locale.getDefault()).format(mDate);
                TextView tv = (TextView)findViewById(R.id.date);
                tv.setText(mStrTodo);

                reloadListView();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view){
                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                startActivity(intent);
            }
        });

        // Realmの設定
        mRealm=Realm.getDefaultInstance();
        mRealm.addChangeListener(mRealmListener);

        // TextViewの設定
        TextView tv = (TextView)findViewById(R.id.date);
        String mTodo = new SimpleDateFormat("MM月dd日(E)のやること", Locale.getDefault()).format(mDate);
        tv.setText(mTodo);

        // ListViewの設定
        mTaskAdapter=new TaskAdapter(MainActivity.this);

        mListView=(ListView) findViewById(R.id.listView1);

        // ListViewをタップしたときの処理
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()

                                         {
                                             @Override
                                             public void onItemClick (AdapterView < ? > parent, View view,int position, long id){
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
                                                 public boolean onItemLongClick (AdapterView < ? > parent, View view,int position,
                                                                                 long id){
                                                     // タスクを削除する

                                                     final Task task = (Task) parent.getAdapter().getItem(position);

                                                     //ダイアログを表示する
                                                     AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                                                     builder.setTitle("削除");
                                                     builder.setMessage(task.getTitle() + "を削除しますか");
                                                     builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                         @Override
                                                         public void onClick(DialogInterface dialog, int which) {
                                                             RealmResults<Task> results = mRealm.where(Task.class).equalTo("id", task.getId()).findAll();
                                                             RealmResults<ToDo> todoResults = mRealm.where(ToDo.class).equalTo("taskId", task.getId()).findAll();
                                                             //Log.d("todoResults",String.valueOf(todoResults));

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

                                                     return true;
                                                 }
                                             }


        );

    }

    private void reloadListView() {
        mDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        mDate = calendar.getTime();
        long_mDate = mDate.getTime();

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
        //Log.d("reloadListView_mdate", mStrDate);

        //選択日時+1
        calnewDate = Calendar.getInstance();
        calnewDate.setTime(mDate);
        calnewDate.add(Calendar.DAY_OF_MONTH, +1);
        int calnewDateYear = calnewDate.get(Calendar.YEAR);
        int calnewDateMonth = calnewDate.get(Calendar.MONTH);
        int calnewDateday = calnewDate.get(Calendar.DAY_OF_MONTH);
        calnewDate.set(calnewDateYear, calnewDateMonth, calnewDateday, 0, 0, 0);
        newDate = calnewDate.getTime();


        // Realmデータベースから、「指定した日のデータを取得して新しい時間順に並べた結果」を取得
        switch (mDayOfWeek) {
            case 1:
                taskRealmResults = mRealm.where(Task.class).equalTo("datestring", mStrDate).or().beginGroup().equalTo("sunday", true).lessThan("date", newDate).endGroup().findAll();
                break;
            case 2:
                taskRealmResults = mRealm.where(Task.class).equalTo("datestring", mStrDate).or().beginGroup().equalTo("monday", true).lessThan("date", newDate).endGroup().findAll();
                break;
            case 3:
                taskRealmResults = mRealm.where(Task.class).equalTo("datestring", mStrDate).or().beginGroup().equalTo("tuesday", true).lessThan("date", newDate).endGroup().findAll();
                break;
            case 4:
                taskRealmResults = mRealm.where(Task.class).equalTo("datestring", mStrDate).or().beginGroup().equalTo("wednesday", true).lessThan("date", newDate).endGroup().findAll();
                break;
            case 5:
                taskRealmResults = mRealm.where(Task.class).equalTo("datestring", mStrDate).or().beginGroup().equalTo("thursday", true).lessThan("date", newDate).endGroup().findAll();
                break;
            case 6:
                taskRealmResults = mRealm.where(Task.class).equalTo("datestring", mStrDate).or().beginGroup().equalTo("friday", true).lessThan("date", newDate).endGroup().findAll();
                break;
            case 7:
                taskRealmResults = mRealm.where(Task.class).equalTo("datestring", mStrDate).or().beginGroup().equalTo("saturday", true).lessThan("date", newDate).endGroup().findAll();
                break;
            default:
        }

        taskRealmResults = taskRealmResults.sort("displayTime", ASCENDING);
        //Log.d("taskRealmResults", String.valueOf(taskRealmResults));

        // 上記の結果を、TaskList としてセットする
        mTaskAdapter.setTaskList(mRealm.copyFromRealm(taskRealmResults));

        // TaskのListView用のアダプタに渡す
        mListView.setAdapter(mTaskAdapter);

        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();

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