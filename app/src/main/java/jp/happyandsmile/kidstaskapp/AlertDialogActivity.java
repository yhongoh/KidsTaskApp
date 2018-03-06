package jp.happyandsmile.kidstaskapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;

public class AlertDialogActivity extends AppCompatActivity {
    private Task mTask;
    private Date finishDate;
    private int taskHour,taskMinute,finishHour,finishMinute,finishDate1;
    private String strFinishDate;
    private int diff,taskDate1;
    private Vibrator vib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_dialog);
        //Log.d("AlertDialog", "AlertDialog");

        vib = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        vib.vibrate(4000);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);


        LayoutInflater inflater = LayoutInflater.from(this);
        final View view = inflater.inflate(R.layout.alert_layout, null);
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setPositiveButton("できた！", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    //タスク時刻
                    Calendar taskDate = Calendar.getInstance();
                    taskDate.setTime(mTask.getDate());
                    taskHour = taskDate.get(Calendar.HOUR);
                    taskMinute = taskDate.get(Calendar.MINUTE);
                    taskDate1 = taskHour * 60 + taskMinute;


                    // 完了時刻は現在時刻
                    finishDate = new Date();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(finishDate);
                    finishHour = calendar.get(Calendar.HOUR);
                    finishMinute = calendar.get(Calendar.MINUTE);
                    strFinishDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(finishDate);
                    finishDate1 = finishHour * 60 + finishMinute;

                    //Log.d("finishDate", String.valueOf(finishDate));

                    diff = finishDate1 - taskDate1;
                    //Log.d("diff", String.valueOf(diff));

                    Realm realm1 = Realm.getDefaultInstance();
                    ToDo mToDo1 = realm1.where(ToDo.class).equalTo("datestring", strFinishDate).equalTo("taskId", mTask.getId()).findFirst();


                    realm1.beginTransaction();

                    // タスク時刻
                    if (diff <= 3) {
                        mToDo1.setStatus(1);
                    } else if (diff <= 5 && diff > 3) {
                        mToDo1.setStatus(2);
                    } else if (diff > 5) {
                        mToDo1.setStatus(3);
                    }
                    mToDo1.setDate(finishDate);
                    mToDo1.setDatestring(strFinishDate);
                    mToDo1.setTaskId(mTask.getId());


                    realm1.copyToRealmOrUpdate(mToDo1);
                    realm1.commitTransaction();
                    realm1.close();
                    vib.cancel();

                    AlertDialogActivity.this.finish();
                }
            });

            alertDialog.setView(view);
            alertDialog.show();

            Intent intent = getIntent();
            int taskId = intent.getIntExtra(MainActivity.EXTRA_TASK, -1);
            //Log.d("taskId", String.valueOf(taskId));
            Realm realm2 = Realm.getDefaultInstance();
            mTask = realm2.where(Task.class).equalTo("id", taskId).findFirst();
            realm2.close();

            byte[] bytes = mTask.getImageBytes();
            if (bytes != null) {
                Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length).copy(Bitmap.Config.ARGB_8888, true);

                ImageView taskImageView = (ImageView) view.findViewById(R.id.taskImageView);
                taskImageView.setImageBitmap(image);
            }

            TextView taskTextView = (TextView) view.findViewById(R.id.taskTextView);
            taskTextView.setText(mTask.getTitle());

        }
    }
