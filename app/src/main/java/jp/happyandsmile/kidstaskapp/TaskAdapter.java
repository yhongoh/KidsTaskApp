package jp.happyandsmile.kidstaskapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;

import static jp.happyandsmile.kidstaskapp.MainActivity.mDate;

/**
 * Created by hongoyuka on 2018/02/15.
 */

public class TaskAdapter extends BaseAdapter {
    private LayoutInflater mLayoutInflater = null;
    private List<Task> mTaskList;
    private int diff;
    private Date finishDate;
    private ImageView checkTask;
    private String mstrdate, strFinishDate;
    private Calendar cal, calTaskDate;
    private int finishDate1, taskDate1, identifier;
    private int todayYear, todayMonth, todayDay, todayHour, todayMinute, mTaskHour, mTaskMinute, finishYear, finishMonth, finishDay, finishHour, finishMinute;


    public TaskAdapter(Context context) {
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setTaskList(List<Task> taskList) {
        mTaskList = taskList;
    }

    @Override
    public int getCount() {
        return mTaskList.size();
    }

    @Override
    public Object getItem(int position) {
        return mTaskList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mTaskList.get(position).getId();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_task, null);
        }

        TextView textView1 = (TextView) convertView.findViewById(R.id.timeTextView);
        TextView textView2 = (TextView) convertView.findViewById(R.id.taskTextView);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
        checkTask = (ImageView) convertView.findViewById(R.id.checkTask);
        //Log.d("TA_ImageView", "TA_ImageView");

        //今日の日付を同日比較をできるように変換
        final Calendar today = Calendar.getInstance();
        final Calendar calToday = Calendar.getInstance();
        todayYear = today.get(Calendar.YEAR);
        todayMonth = today.get(Calendar.MONTH) + 1;
        todayDay = today.get(Calendar.DAY_OF_MONTH);
        todayHour = today.get(Calendar.HOUR);
        todayMinute = today.get(Calendar.MINUTE);
        calToday.set(todayYear, todayMonth, todayDay, todayHour, todayMinute);

        mstrdate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(mDate);
        //Log.d("TA_mstrdate", mstrdate);

        final Realm realm = Realm.getDefaultInstance();
        //Log.d("TA_onCre_taskId", String.valueOf(mTaskList.get(position).getId()));

        RealmResults<ToDo> todoRealmResults = realm.where(ToDo.class).equalTo("datestring", mstrdate).findAll();
        RealmResults<ToDo> todoResults = realm.where(ToDo.class).findAll();
        ToDo mToDo = realm.where(ToDo.class).equalTo("datestring", mstrdate).equalTo("taskId", mTaskList.get(position).getId()).findFirst();

        if (mToDo == null) {
            mToDo = new ToDo();
            mToDo.setDate(mDate);
            mToDo.setTaskId(mTaskList.get(position).getId());
            mToDo.setStatus(0);
            mToDo.setDatestring(mstrdate);


            if (todoResults.max("id") != null) {
                identifier = todoResults.max("id").intValue() + 1;
            } else {
                identifier = 0;
            }
            mToDo.setId(identifier);

            realm.beginTransaction();
            realm.copyToRealmOrUpdate(mToDo);
            realm.commitTransaction();
        }

        //Log.d("TA_taskId", String.valueOf(mTaskList.get(position).getId()));
        //Log.d("TA_todoRealmResults", String.valueOf(todoRealmResults));

                // int 0 check
                // int 1 smile
                // int 2 soso
                // int 3 ng

        if (mToDo.getStatus() == 0) {
            checkTask.setImageResource(R.drawable.checkbox);
            //Log.d("mToDo_status","0");
        } else if (mToDo.getStatus() == 1) {
            checkTask.setImageResource(R.drawable.ok);
            checkTask.setColorFilter(Color.RED);
            //Log.d("mToDo_status","1");
        } else if (mToDo.getStatus() == 2) {
            checkTask.setImageResource(R.drawable.soso);
            checkTask.setColorFilter(Color.GREEN);
            //Log.d("mToDo_status","2");
        } else {
            checkTask.setImageResource(R.drawable.ng);
            checkTask.setColorFilter(Color.BLUE);
            //Log.d("mToDo_status","3");
        }

        checkTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d("TA_onClick", "TA_onClick");
                int taskId1 = mTaskList.get(position).getId();
                //Log.d("TA_onClick_taskID", String.valueOf(taskId1));

                ToDo mToDo1 = realm.where(ToDo.class).equalTo("datestring", mstrdate).equalTo("taskId", mTaskList.get(position).getId()).findFirst();

                // タスク時刻より3分以内ならsmile
                // タスク時刻より5分以内ならsoso
                // タスク時刻を5分過ぎてもタスク未完了ならng

                //タスク日時を同日比較できるように変換
                cal = Calendar.getInstance();
                calTaskDate = cal.getInstance();
                cal.setTime(mTaskList.get(position).getDate());
                mTaskHour = cal.get(Calendar.HOUR);
                mTaskMinute = cal.get(Calendar.MINUTE);
                calTaskDate.set(todayYear, todayMonth, todayDay, mTaskHour, mTaskMinute);
                taskDate1 = mTaskHour * 60 + mTaskMinute;

                // 完了時刻は現在時刻
                finishDate = new Date();
                Calendar calendar = Calendar.getInstance();
                Calendar calFinishDate = Calendar.getInstance();
                calendar.setTime(finishDate);
                finishYear = calendar.get(Calendar.YEAR);
                finishMonth = calendar.get(Calendar.MONTH) + 1;
                finishDay = calendar.get(Calendar.DAY_OF_MONTH);
                finishHour = calendar.get(Calendar.HOUR);
                finishMinute = calendar.get(Calendar.MINUTE);
                calFinishDate.set(finishYear, finishMonth, finishDay, finishHour, finishMinute);
                strFinishDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(finishDate);
                finishDate1 = finishHour * 60 + finishMinute;

                diff = finishDate1 - taskDate1;
                //Log.d("diff", String.valueOf(diff));

                // 今日のタスクのみ変更可能とする
                Integer nextDay_Day_OF_MONTH = calTaskDate.get(Calendar.DAY_OF_MONTH)+1;
                Integer nextDay_MONTH = calTaskDate.get(Calendar.MONTH);
                Integer priorDay_Day_OF_MONTH = calTaskDate.get(Calendar.DAY_OF_MONTH)-1;
                Integer priorDay_MONTH = calTaskDate.get(Calendar.MONTH);

                GregorianCalendar nextDay = new GregorianCalendar(todayYear,nextDay_MONTH,nextDay_Day_OF_MONTH,0,0);
                GregorianCalendar priorDay = new GregorianCalendar(todayYear,priorDay_MONTH,priorDay_Day_OF_MONTH,0,0);


                if (calFinishDate.compareTo(nextDay)==-1 && calFinishDate.compareTo(priorDay) == 1) {
                    realm.beginTransaction();

                    // タスク時刻
                    if (diff <= 3) {
                        //Log.d("smile", "smile");
                        mToDo1.setStatus(1);
                        //Log.d("changeStatus", "1");
                    } else if (diff <= 5 && diff > 3) {
                        //Log.d("soso", "soso");
                        mToDo1.setStatus(2);
                        //Log.d("changeStatus", "2");
                    } else if (diff > 5) {
                        mToDo1.setStatus(3);
                        //Log.d("ng", "ng");
                        //Log.d("changeStatus", "3");
                    }
                    mToDo1.setDate(finishDate);
                    mToDo1.setDatestring(strFinishDate);
                    mToDo1.setTaskId(taskId1);

                    realm.copyToRealmOrUpdate(mToDo1);
                    realm.commitTransaction();
                    realm.close();

                    notifyDataSetChanged();
                }
            }
        });

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.JAPANESE);
            Date date = mTaskList.get(position).getDate();
            textView1.setText(simpleDateFormat.format(date));

            textView2.setText(mTaskList.get(position).getTitle());

            byte[] bytes = mTaskList.get(position).getImageBytes();
            if (bytes != null) {
                Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length).copy(Bitmap.Config.ARGB_8888, true);
                imageView.setImageBitmap(image);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setAdjustViewBounds(true);
            }
        return convertView;
    }
}