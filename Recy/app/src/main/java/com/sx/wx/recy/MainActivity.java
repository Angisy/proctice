package com.sx.wx.recy;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.tv_text)
    TextView mTvText;
    @BindView(R.id.tv_start)
    TextView mTvStart;
    @BindView(R.id.tv_stop)
    TextView mTvStop;
    @BindView(R.id.bind)
    Button mBind;
    @BindView(R.id.unbind)
    Button mUnbind;
    @BindView(R.id.btn_db)
    Button mCreateDb;
    @BindView(R.id.btn_phone)
    Button mPhone;
    @BindView(R.id.content)
    Button mContent;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private static final int UPDATE_UI = 1;
    private static final String TAG = "MainActivity";
    private MyService.DownloadBinder mDownloadBinder;
    private MyDatabaseHelper mMyDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Log.d(TAG, "onCreate: mTvText: " + mTvText);
        mMyDatabaseHelper = new MyDatabaseHelper(this, "BookStore.db", null, 1);
        mTvText.setSelected(true);
    }

    Handler mhandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_UI:
                    mTvText.setText("nihao");
                    break;

                default:
                    break;
            }
            return true;
        }
    });

    @OnClick({
            R.id.tv_text,
            R.id.tv_start,
            R.id.tv_stop,
            R.id.bind,
            R.id.unbind,
            R.id.btn_db,
            R.id.btn_phone,
            R.id.content
    })
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_text:
                Log.d(TAG, "on click1 ");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "on click2   ");
                        Message message = new Message();
                        message.what = UPDATE_UI;
                        mhandler.sendMessage(message);
                    }
                }).start();
                break;
            case R.id.tv_start:
                Intent intent = new Intent(this, MyService.class);
                startService(intent);
                break;
            case R.id.tv_stop:
                Intent intent1 = new Intent(this, MyService.class);
                stopService(intent1);
                break;
            case R.id.bind:
                Intent bindIntent = new Intent(this, MyService.class);
                bindService(bindIntent, connection, BIND_AUTO_CREATE);
                break;
            case R.id.unbind:
                unbindService(connection);
                Log.d(TAG, "unbind click");
                break;
            case R.id.btn_db:
                SQLiteDatabase writableDatabase = mMyDatabaseHelper.getWritableDatabase();
                break;
            case R.id.btn_phone:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);

                } else {
                    call();
                }
                break;
            case R.id.content:
                startActivity(new Intent(this,ContentResoverActivity.class));
            default:
                break;
        }
    }

    private void call() {
        try {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:10086"));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDownloadBinder = (MyService.DownloadBinder) service;
            mDownloadBinder.startDownload();
            mDownloadBinder.getProgress();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    call();
                } else {
                    Toast.makeText(this, "you denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
    }
}
