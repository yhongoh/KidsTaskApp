package jp.happyandsmile.kidstaskapp;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.realm.Realm;
import io.realm.RealmResults;

public class InputActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final int CHOOSER_REQUEST_CODE = 100;

    private int mYear, mMonth, mDay, mHour, mMinute,displayYear,displayMonth,displayDay,displayHour,displayMinute,dayOfWeek;
    private int errorCause;
    private boolean mMonday, mTuesday, mWednesday, mThursday, mFriday, mSaturday, mSunday;
    private Button mDoneButton;
    private TextView mImageButton, mImageTextView, mIterateTextView;
    private Button mDateButton, mTimeButton;
    private EditText mTitleEdit;
    private Uri mPictureUri;
    private Task mTask;
    private ToDo mToDo;
    private ImageView mImageView;
    private Date date, time, mDate;
    private Calendar cal, calendar;
    private Long long_mDate;
    //private List<Integer> checkedItems;
    private String dateString, timeString;
    final String[] items = {"繰り返さない", "毎日", "毎週（曜日指定）"};

    private View.OnClickListener mOnDateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DatePickerDialog datePickerDialog = new DatePickerDialog(InputActivity.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            mYear = year;
                            mMonth = monthOfYear;
                            mDay = dayOfMonth;
                            dateString = mYear + "/" + String.format("%02d", (mMonth + 1)) + "/" + String.format("%02d", mDay);
                            mDateButton.setText(dateString);
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }
    };

    private View.OnClickListener mOnTimeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TimePickerDialog timePickerDialog = new TimePickerDialog(InputActivity.this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            mHour = hourOfDay;
                            mMinute = minute;
                            timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute);
                            mTimeButton.setText(timeString);
                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();
        }
    };


    private View.OnClickListener mOnDoneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // タスク設定時刻の取得
            GregorianCalendar calendar = new GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute);
            date = calendar.getTime();

            // 現在時刻の取得
            Date currentDate = new Date();

            // タイトル、日付と時間がない場合にDialogを表示するする
            // タスク設定時刻が現在時刻より前の場合にはDialogを表示する
            if (mTitleEdit.getText().toString().equals("") == true || dateString == null || timeString == null) {
                errorCause = 1;
                showWarningDiaog();
            } else if (date.compareTo(currentDate) < 0) {
                errorCause = 2;
                showWarningDiaog();
            } else {
                addTask();
                finish();
            }
            //繰り返し設定のあるタスクを更新する場合にはDialogを表示し、単発の変更か、以降の予定全ての変更かを確認する
            //else if (mTask != null) {
            //    if (mTask.getSunday() || mTask.getMonday() || mTask.getTuesday() || mTask.getWednesday() || mTask.getThursday() || mTask.getFriday() || mTask.getSaturday()) {
            //        showConfirmDialog();
             }
    };

    private View.OnClickListener mOnImageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // パーミッションの許可状態を確認する
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // 許可されている
                    showChooser();
                } else {
                    // 許可されていないので許可ダイアログを表示する
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
                    return;
                }
            } else {
                showChooser();
            }
        }
    };

    private View.OnClickListener mOnImageViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            BitmapDrawable drawable = (BitmapDrawable) mImageView.getDrawable();
            if (drawable != null) {
                // アラートダイアログ
                AlertDialog.Builder builder = new AlertDialog.Builder(InputActivity.this);

                builder.setTitle("確認");
                builder.setMessage("画像を削除しますか？");
                builder.setCancelable(false);
                builder.setPositiveButton("OK" +
                        "", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //画像を削除する
                        mImageView.setImageDrawable(null);
                        mImageTextView.setVisibility(View.VISIBLE);
                    }
                });
                builder.setNegativeButton("キャンセル", null);

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    };

    private View.OnClickListener mOnIterateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(InputActivity.this);

            builder.setTitle("繰り返し設定");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    //繰り返しなし
                    if (item == 0) {
                        setMassage("繰り返さない");
                        mMonday = false;
                        mTuesday = false;
                        mWednesday = false;
                        mThursday = false;
                        mFriday = false;
                        mSaturday = false;
                        mSunday = false;
                    }
                    // 毎日
                    else if (item == 1) {
                        setMassage("毎日");
                        mMonday = true;
                        mTuesday = true;
                        mWednesday = true;
                        mThursday = true;
                        mFriday = true;
                        mSaturday = true;
                        mSunday = true;
                    }
                    // 毎週
                    else if (item == 2) {
                        showSelectTheDayOfWeek();
                    }
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

        }

        private void setMassage(String message) {
            mIterateTextView.setText(message);
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHOOSER_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                if (mPictureUri != null) {
                    getContentResolver().delete(mPictureUri, null, null);
                    mPictureUri = null;
                }
                return;
            }

            //画像を取得
            Uri uri = (data == null || data.getData() == null) ? mPictureUri : data.getData();


            //URIからBitmapを取得
            Bitmap image;
            try {
                ContentResolver contentResolver = getContentResolver();
                InputStream inputStream = contentResolver.openInputStream(uri);
                image = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
            } catch (Exception e) {
                return;
            }

            //取得したBitmapの長辺を500pxにリサイズする
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            float scale = Math.min((float) 500 / imageWidth, (float) 500 / imageHeight);

            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);

            Bitmap resizedImage = Bitmap.createBitmap(image, 0, 0, imageWidth, imageHeight, matrix, true);

            mImageView.setImageBitmap(resizedImage);
            mImageTextView.setVisibility(View.GONE);

            mPictureUri = null;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        // ActionBarを設定する
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // UI部品の設定
        mDateButton = (Button) findViewById(R.id.date_button);
        mDateButton.setOnClickListener(mOnDateClickListener);
        mTimeButton = (Button) findViewById(R.id.times_button);
        mTimeButton.setOnClickListener(mOnTimeClickListener);
        mDoneButton = (Button) findViewById(R.id.addTask_button);
        mDoneButton.setOnClickListener(mOnDoneClickListener);
        mTitleEdit = (EditText) findViewById(R.id.title_edit_text);
        mImageButton = (TextView) findViewById(R.id.imageTextView);
        mImageButton.setOnClickListener(mOnImageClickListener);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mImageView.setOnClickListener(mOnImageViewClickListener);
        mImageTextView = (TextView) findViewById(R.id.imageTextView);
        mIterateTextView = (TextView) findViewById(R.id.iterateTextView);
        mIterateTextView.setOnClickListener(mOnIterateClickListener);

        // EXTRA_TASK から Task の id を取得して、 id から Task のインスタンスを取得する
        Intent intent = getIntent();
        int taskId = intent.getIntExtra(MainActivity.EXTRA_TASK, -1);
        Realm realm = Realm.getDefaultInstance();
        mTask = realm.where(Task.class).equalTo("id", taskId).findFirst();

        if (mTask != null){
        mToDo = realm.where(ToDo.class).equalTo("taskId", mTask.getId()).findFirst();}

        // mDateの取得とCalendar型への変換
        long_mDate = intent.getLongExtra("long_mDate", 0);

        realm.close();

        if (mTask == null) {
            // 新規作成の場合
            Calendar calendar = Calendar.getInstance();
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH);
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);
        } else {
            // 更新の場合
            mTitleEdit.setText(mTask.getTitle());

            //年月日は選択日の年月日を設定
            cal = Calendar.getInstance();
            cal.setTimeInMillis(long_mDate);
            mYear = cal.get(Calendar.YEAR);
            mMonth =cal.get(Calendar.MONTH);
            mDay = cal.get(Calendar.DAY_OF_MONTH);

            //表示日時の0時0分
            calendar = Calendar.getInstance();
            calendar.setTimeInMillis(long_mDate);
            mYear = calendar.get(Calendar.YEAR);
            mMonth =calendar.get(Calendar.MONTH);
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
            calendar.set(mYear,mMonth,mDay,0,0);
            mDate = calendar.getTime();


            //時分はタスクの時分を設定
            cal.setTime(mTask.getDate());
            mHour = cal.get(Calendar.HOUR_OF_DAY);
            mMinute = cal.get(Calendar.MINUTE);

            dateString = mYear + "/" + String.format("%02d", (mMonth + 1)) + "/" + String.format("%02d", mDay);
            timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute);
            mDateButton.setText(dateString);
            mTimeButton.setText(timeString);

            mMonday = mTask.getMonday();
            mTuesday = mTask.getTuesday();
            mWednesday = mTask.getWednesday();
            mThursday = mTask.getThursday();
            mFriday = mTask.getFriday();
            mSaturday = mTask.getSaturday();
            mSunday = mTask.getSunday();

            if (mMonday && mTuesday && mWednesday && mThursday && mFriday && mSaturday && mSunday) {
                mIterateTextView.setText("毎日");
            } else if (mMonday || mTuesday || mWednesday || mThursday || mFriday || mSaturday || mSunday) {
                StringBuilder sb = new StringBuilder();
                String[] theDayOfWeek = {"月", "火", "水", "木", "金", "土", "日"};
                Boolean theDOW[] = {mMonday,mTuesday,mWednesday,mThursday,mFriday,mSaturday,mSunday};

                for (int i = 0; i < theDOW.length; i++) {
                    if (theDOW[i]) {
                        sb.append(theDayOfWeek[i] + ",");
                    }
                }

                String value = "";
                if (sb.length() > 0) {
                    value = sb.substring(0, sb.length() - 1);
                    mIterateTextView.setText("毎週（" + value
                            + "）");
                } else {
                    mIterateTextView.setText("繰り返さない");
                }
            }
            byte[] bytes = mTask.getImageBytes();
            if (bytes != null) {
                Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length).copy(Bitmap.Config.ARGB_8888, true);
                mImageView.setImageBitmap(image);
                mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                mImageView.setAdjustViewBounds(true);
                mImageTextView.setVisibility(View.GONE);
            }
        }
    }

    private void addTask() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        if (mTask == null) {
            // 新規作成の場合
            mTask = new Task();
            mToDo = new ToDo();

            RealmResults<Task> taskRealmResults = realm.where(Task.class).findAll();
            RealmResults<ToDo> todoResults = realm.where(ToDo.class).findAll();

            int identifier;
            if (taskRealmResults.max("id") != null) {
                identifier = taskRealmResults.max("id").intValue() + 1;
            } else {
                identifier = 0;
            }
            mTask.setId(identifier);

            int identifier1;
            if (todoResults.max("id") != null) {
                identifier1 = todoResults.max("id").intValue() + 1;
            } else {
                identifier1 = 0;
            }
            mToDo.setId(identifier1);

        }

            String title = mTitleEdit.getText().toString();
            mTask.setTitle(title);

            GregorianCalendar calendar = new GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute);
            date = calendar.getTime();
            mTask.setDate(date);

            Calendar cal1 = Calendar.getInstance();
            //タスク一覧で時刻でソートするために年月日は固定とする
            cal1.set(2000, 1, 1, mHour, mMinute);
            time = cal1.getTime();
            mTask.setDisplayTime(time);

            SimpleDateFormat strdate = new SimpleDateFormat("yyyy/MM/dd");
            String datestring = strdate.format(calendar.getTime());
            mTask.setDatestring(datestring);

            //繰り返し設定の登録
            mTask.setMonday(mMonday);
            mTask.setTuesday(mTuesday);
            mTask.setWednesday(mWednesday);
            mTask.setThursday(mThursday);
            mTask.setFriday(mFriday);
            mTask.setSaturday(mSaturday);
            mTask.setSunday(mSunday);

            //mToDoの更新
            mToDo.setDate(date);
            mToDo.setDatestring(datestring);
            mToDo.setStatus(0);
            mToDo.setTaskId(mTask.getId());

            //画像の登録
            BitmapDrawable drawable = (BitmapDrawable) mImageView.getDrawable();

            if (drawable != null) {
                Bitmap bitmap = drawable.getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                byte[] mBitmapArray = baos.toByteArray();

                mTask.setImageBytes(mBitmapArray);
            }

            realm.copyToRealmOrUpdate(mTask);
            realm.copyToRealmOrUpdate(mToDo);
            realm.commitTransaction();

            realm.close();

            //AlarmManagerの設定
            Intent resultIntent = new Intent(InputActivity.this, TaskAlarmReceiver.class);
            resultIntent.putExtra(MainActivity.EXTRA_TASK, mTask.getId());
            PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                    this,
                    mTask.getId(),
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );


            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), null), resultPendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), resultPendingIntent);
            }
        }



    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //ユーザが許可した時
                    showChooser();
                }
                return;
            }
        }
    }


    private void showChooser() {
        //ギャラリーから選択するIntent
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);

        // カメラで撮影するIntent
        String filename = System.currentTimeMillis() + ".jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, filename);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        mPictureUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPictureUri);

        //ギャラリー選択のIntentを与えてcreateChooserを呼ぶ
        Intent chooserIntent = Intent.createChooser(galleryIntent, "画像を取得");

        //EXTRA_INITIAL_INTENTSにカメラの撮影を追加する
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});
        startActivityForResult(chooserIntent, CHOOSER_REQUEST_CODE);
    }

    private void showSelectTheDayOfWeek() {
        final CharSequence[] list = new CharSequence[7];
        list[0] = "月";
        list[1] = "火";
        list[2] = "水";
        list[3] = "木";
        list[4] = "金";
        list[5] = "土";
        list[6] = "日";

        //新規追加の場合には今日の曜日をデフォルトで設定

        if (mTask==null) {
            Calendar c = Calendar.getInstance();
            dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == 1) {
                mSunday = true;
            } else if (dayOfWeek == 2) {
                mMonday = true;
            } else if (dayOfWeek == 3) {
                mTuesday = true;
            } else if (dayOfWeek == 4) {
                mWednesday = true;
            } else if (dayOfWeek == 5) {
                mThursday = true;
            } else if (dayOfWeek == 6) {
                mFriday = true;
            } else if (dayOfWeek == 7) {
                mSaturday = true;
            }
        }

        final boolean[]checkedItems = {mMonday, mTuesday, mWednesday, mThursday, mFriday, mSaturday, mSunday};

        final AlertDialog.Builder checkDlg = new AlertDialog.Builder(this);
        checkDlg.setTitle("選択");
        checkDlg.setMultiChoiceItems(list, checkedItems,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which, boolean isChecked) {
                        checkedItems[which] = isChecked;
                    }
                });

        checkDlg.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // mMonday~mSundayへ登録とiterateTextViewのテキスト更新

                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < list.length; i++) {
                            if (checkedItems[i]) {
                                sb.append(list[i] + ",");
                            }
                        }
                        String value = "";
                        if (sb.length() > 0) {
                            value = sb.substring(0, sb.length() - 1);
                            mIterateTextView.setText("毎週（" + value
                                    + "）");

                            for (int i = 0; i < list.length; i++) {
                                if (checkedItems[i]) {
                                    if (i == 0) {
                                        mMonday = true;
                                    } else {
                                        mMonday = false;
                                    }

                                    if (i == 1) {
                                        mTuesday = true;
                                    } else {
                                        mTuesday = false;
                                    }

                                    if (i == 2) {
                                        mWednesday = true;
                                    } else {
                                        mWednesday = false;
                                    }

                                    if (i == 3) {
                                        mThursday = true;
                                    } else {
                                        mThursday = false;
                                    }

                                    if (i == 4) {
                                        mFriday = true;
                                    } else {
                                        mFriday = false;
                                    }

                                    if (i == 5) {
                                        mSaturday = true;
                                    } else {
                                        mSaturday = false;
                                    }

                                    if (i == 6) {
                                        mSunday = true;
                                    } else {
                                        mSunday = false;
                                    }
                                }
                            }
                        }

                        for (int i = 0; i < 7; i++) {
                            if (checkedItems[i]) {
                                if (i == 0) {
                                    mMonday = true;
                                }

                                if (i == 1) {
                                    mTuesday = true;
                                }

                                if (i == 2) {
                                    mWednesday = true;
                                }

                                if (i == 3) {
                                    mThursday = true;
                                }

                                if (i == 4) {
                                    mFriday = true;
                                }

                                if (i == 5) {
                                    mSaturday = true;
                                }

                                if (i == 6) {
                                    mSunday = true;
                                }
                            }
                        }
                        }
                    });

                    // 表示
                        checkDlg.create().show();
                }

    //入力チェックでNGとなった旨のダイアログを表示させる
    private void showWarningDiaog() {
        Log.d("showWarning","showWarning");
        AlertDialog.Builder warningDlg = new AlertDialog.Builder(InputActivity.this);
        if (errorCause ==1 ){
            warningDlg.setMessage("件名、日時を入力してください");
        } else if (errorCause == 2) {
            warningDlg.setMessage("現在時刻以降の日時を設定してください");
        }
        warningDlg.setCancelable(false);
        warningDlg.setPositiveButton("OK" +
                "", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = warningDlg.create();
        dialog.show();
    }

    /*
    //繰り返し設定があった場合にその日のみ変更するか、以降の予定を変更するか確認する
    private void showConfirmDialog() {
        Log.d("showConfirm","showConfirm");
        final String[] items = {"この予定のみ変更", "これ以降の全てを変更"};
        int defaultItem = 0; // デフォルトでチェックされているアイテム
        checkedItems = new ArrayList<>();
        checkedItems.add(defaultItem);
        new AlertDialog.Builder(InputActivity.this)
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
                            if (checkedItems.get(0) == 0) {
                                changeTask();
                                Log.d("checkedItem:", "" + checkedItems.get(0));
                            } else {
                                changeTask();
                                Log.d("checkedItem:", "" + checkedItems.get(0));
                                //元の予定を更新する
                            }
                        }
                    }
                })
                .setNegativeButton("キャンセル", null)
                .show();
        }

        */


    /*
    // checkedItems.get(0) == 0 単発予定を変更
    // checkedItems.get(0) == 0 以降の予定を変更
    private void changeTask(){
        if (checkedItems.get(0) == 0){
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();

            //IDを設定
            RealmResults<Task> taskRealmResults = realm.where(Task.class).findAll();
            RealmResults<ToDo> todoResults = realm.where(ToDo.class).findAll();

                int identifier;
                identifier = taskRealmResults.max("id").intValue() + 1;
                mTask.setId(identifier);

                int identifier1;
                identifier1 = todoResults.max("id").intValue() + 1;
                mToDo.setId(identifier1);


            String title = mTitleEdit.getText().toString();
            mTask.setTitle(title);

            GregorianCalendar calendar = new GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute);
            date = calendar.getTime();
            mTask.setDate(date);

            Calendar cal1 = Calendar.getInstance();
            //タスク一覧で時刻でソートするために年月日は固定とする
            cal1.set(2000, 1, 1, mHour, mMinute);
            time = cal1.getTime();
            mTask.setDisplayTime(time);

            SimpleDateFormat strdate = new SimpleDateFormat("yyyy/MM/dd");
            String datestring = strdate.format(calendar.getTime());
            mTask.setDatestring(datestring);

            //繰り返し設定の登録
            mTask.setMonday(mMonday);
            mTask.setTuesday(mTuesday);
            mTask.setWednesday(mWednesday);
            mTask.setThursday(mThursday);
            mTask.setFriday(mFriday);
            mTask.setSaturday(mSaturday);
            mTask.setSunday(mSunday);

            //mToDoの更新
            mToDo.setDate(date);
            mToDo.setDatestring(datestring);
            mToDo.setStatus(0);
            mToDo.setTaskId(mTask.getId());

            //画像の登録
            BitmapDrawable drawable = (BitmapDrawable) mImageView.getDrawable();

            if (drawable != null) {
                Bitmap bitmap = drawable.getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                byte[] mBitmapArray = baos.toByteArray();

                mTask.setImageBytes(mBitmapArray);
            }

            realm.copyToRealmOrUpdate(mTask);
            realm.copyToRealmOrUpdate(mToDo);
            realm.commitTransaction();

            realm.close();

            //AlarmManagerの設定
            Intent resultIntent = new Intent(InputActivity.this, TaskAlarmReceiver.class);
            resultIntent.putExtra(MainActivity.EXTRA_TASK, mTask.getId());
            PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                    this,
                    mTask.getId(),
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );


            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), null), resultPendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), resultPendingIntent);
            }

        }else {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();

            //変更前のタスクを削除
            RealmResults<Task> beforeChangeTaskResults = realm.where(Task.class).equalTo("id", mTask.getId()).lessThan("date", mDate).findAll();
            RealmResults<ToDo> beforeChangeToDoResults = realm.where(ToDo.class).equalTo("taskId", mTask.getId()).lessThan("date", mDate).findAll();

            beforeChangeTaskResults.deleteAllFromRealm();
            beforeChangeToDoResults.deleteAllFromRealm();
            realm.commitTransaction();

            //変更後のタスクを新規作成
            Task mTask1 = new Task();
            ToDo mToDo1 = new ToDo();

            //IDを設定
            RealmResults<Task> taskRealmResults = realm.where(Task.class).findAll();
            RealmResults<ToDo> todoResults = realm.where(ToDo.class).findAll();

            int identifier = taskRealmResults.max("id").intValue() + 1;
            mTask1.setId(identifier);

            int identifier1;
            identifier1 = todoResults.max("id").intValue() + 1;
            mToDo1.setId(identifier1);


            String title = mTitleEdit.getText().toString();
            mTask1.setTitle(title);

            GregorianCalendar calendar = new GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute);
            date = calendar.getTime();
            mTask1.setDate(date);

            Calendar cal1 = Calendar.getInstance();
            //タスク一覧で時刻でソートするために年月日は固定とする
            cal1.set(2000, 1, 1, mHour, mMinute);
            time = cal1.getTime();
            mTask1.setDisplayTime(time);

            SimpleDateFormat strdate = new SimpleDateFormat("yyyy/MM/dd");
            String datestring = strdate.format(calendar.getTime());
            mTask1.setDatestring(datestring);

            //繰り返し設定の登録
            mTask1.setMonday(mMonday);
            mTask1.setTuesday(mTuesday);
            mTask1.setWednesday(mWednesday);
            mTask1.setThursday(mThursday);
            mTask1.setFriday(mFriday);
            mTask1.setSaturday(mSaturday);
            mTask1.setSunday(mSunday);

            //mToDoの更新
            mToDo1.setDate(date);
            mToDo1.setDatestring(datestring);
            mToDo1.setStatus(0);
            mToDo1.setTaskId(mTask1.getId());

            //画像の登録
            BitmapDrawable drawable = (BitmapDrawable) mImageView.getDrawable();

            if (drawable != null) {
                Bitmap bitmap = drawable.getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                byte[] mBitmapArray = baos.toByteArray();

                mTask1.setImageBytes(mBitmapArray);
            }

            realm.copyToRealmOrUpdate(mTask1);
            realm.copyToRealmOrUpdate(mToDo1);
            realm.commitTransaction();

            realm.close();

            //AlarmManagerの設定
            Intent resultIntent = new Intent(InputActivity.this, TaskAlarmReceiver.class);
            resultIntent.putExtra(MainActivity.EXTRA_TASK, mTask1.getId());
            PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                    this,
                    mTask1.getId(),
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );


            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), null), resultPendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), resultPendingIntent);
            }
        }
        finish();
    }
    */


        // BACKボタンが押された時の処理
    //ToDo toolbarのバックキー押下時も表示

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode== KeyEvent.KEYCODE_BACK){
            // アラートダイアログ
            AlertDialog.Builder builder = new AlertDialog.Builder(InputActivity.this);

            builder.setTitle("確認");
            builder.setMessage("この編集を破棄してもよろしいですか？");
            builder.setCancelable(false);
            builder.setPositiveButton("OK" +
                    "", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    InputActivity.this.finish();
                }
            });
            builder.setNegativeButton("キャンセル", null);

            AlertDialog dialog = builder.create();
            dialog.show();

        }return false;
     }
}
