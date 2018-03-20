package com.android.mikechenmj.myapplication;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.mikechenmj.myapplication.aidl.ICallbackAidl;
import com.android.mikechenmj.myapplication.aidl.IMusicServiceAidl;
import com.android.mikechenmj.myapplication.bean.Music;
import com.android.mikechenmj.myapplication.util.PermissionHelper;

import java.util.ArrayList;
import java.util.List;

public class MusicListActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_MUSIC_PERMISSION = 1;

    private List<Music> mList;//保存音乐列表

    private ListView mListView;//展示的listView
    private TextView mNameText;//mini条中展示当前音乐名字以及状态的TextView
    private ImageView mPreviousButton;//上一首按钮
    private ImageView mControlButton;//播放或暂停按钮
    private ImageView mNextButton;//下一首

    private boolean mIsPlay;//是否播放的标记
    private int mCurrentPosition;//当前选中的音乐在列表中的位置

    private IMusicServiceAidl mIMusicServiceAidl;//远程服务接口


    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            String className = service.getClass().getName();
            Log.e("MCJ", "className: " + className);
            mIMusicServiceAidl = IMusicServiceAidl.Stub.asInterface(service);
            if (mIMusicServiceAidl == null || !service.isBinderAlive()) {
                Toast.makeText(MusicListActivity.this,"连接服务失败", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                mIMusicServiceAidl.setCallback(new ICallbackAidl.Stub() {
                    @Override
                    public void update() throws RemoteException {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                nextMusic();
                            }
                        });
                    }

                    @Override
                    public String getMusicPath() throws RemoteException {
                        return mList.get(mCurrentPosition).getData();
                    }
                });
                mIMusicServiceAidl.startMusic();
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.list_view);
        mNameText = (TextView) findViewById(R.id.music_name);
        mNameText.requestFocus();
        mPreviousButton = (ImageView) findViewById(R.id.previous);
        mPreviousButton.setOnClickListener(this);
        mControlButton = (ImageView) findViewById(R.id.control);
        mControlButton.setOnClickListener(this);
        mNextButton = (ImageView) findViewById(R.id.next);
        mNextButton.setOnClickListener(this);

        requestMusicList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIMusicServiceAidl != null) {
            unbindService(mConnection);
        }
    }

    private void initList() {
        mList = createMusicList();
        mListView.setAdapter(new ArrayAdapter<>(MusicListActivity.this,
                android.R.layout.simple_list_item_1, mList));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCurrentPosition = position;
                mNameText.setText((mList.get(position)).getTitle());
                if (mIMusicServiceAidl == null) {
                    controlMusic();
                } else {
                    switchMusic();
                }
            }
        });
    }

    /**
     * 请求获取音乐列表
     */
    private void requestMusicList() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionHelper.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_MUSIC_PERMISSION, new PermissionHelper.PermissionCallback() {
                        @Override
                        public boolean onPermissionGrantedStates(String permission, boolean isGranted,
                                                                 boolean shouldShowRationale) {
                            if (isGranted) {
                                initList();
                            }
                            return false;
                        }

                        @Override
                        public void onAllGranted(boolean isAllGranted) {
                        }
                    });
        } else {
            initList();
        }
    }

    private List<Music> createMusicList() {
        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA
        };
        List<Music> list = new ArrayList<>();
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, null, null,
                new StringBuilder(MediaStore.Audio.Media.TITLE).append(" ASC").toString());

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                Music music = new Music(id, name, artist, data);
                list.add(music);
            } while (cursor.moveToNext());
        }
        return list;
    }

    /**
     * 控制音乐播放或暂停
     */
    private void controlMusic() {
        if (mIsPlay) {
            mIsPlay = false;
            mControlButton.setImageResource(R.drawable.ic_media_play);
            mNameText.setText(getString(R.string.music_pause));
            if (mIMusicServiceAidl != null && mIMusicServiceAidl.asBinder().isBinderAlive()) {
                try {
                    mIMusicServiceAidl.pauseMusic();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else {
            mIsPlay = true;
            mControlButton.setImageResource(R.drawable.ic_media_pause);
            mNameText.setText(mList.get(mCurrentPosition).getTitle());
            if (mIMusicServiceAidl == null) {
                Intent intent = new Intent(this, MusicService.class);
                intent.putExtra("data", mList.get(mCurrentPosition).getData());
                bindService(intent, mConnection, BIND_AUTO_CREATE);
            } else if (mIMusicServiceAidl.asBinder().isBinderAlive()) {
                try {
                    mIMusicServiceAidl.startMusic();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.onRequestPermissionsResult(this, permissions, grantResults,
                new PermissionHelper.PermissionCallback() {
                    @Override
                    public boolean onPermissionGrantedStates(String permission, boolean isGranted, boolean shouldShowRationale) {
                        if (requestCode == REQUEST_MUSIC_PERMISSION) {
                            initList();
                        }
                        return false;
                    }

                    @Override
                    public void onAllGranted(boolean isAllGranted) {

                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next:
                if (mIMusicServiceAidl != null && mIMusicServiceAidl.asBinder().isBinderAlive()) {
                    nextMusic();
                }
                break;
            case R.id.previous:
                if (mIMusicServiceAidl != null && mIMusicServiceAidl.asBinder().isBinderAlive()) {
                    previousMusic();
                }
                break;
            case R.id.control:
                controlMusic();
                break;
            default:
                break;
        }
    }


    /**
     * 下一首
     */
    private void nextMusic() {
        if (mCurrentPosition == mList.size() - 1) {
            mCurrentPosition = 0;
        } else {
            mCurrentPosition++;
        }
        switchMusic();
    }

    /**
     * 上一首
     */
    private void previousMusic() {
        if (mCurrentPosition == 0) {
            mCurrentPosition = mList.size() - 1;
        } else {
            mCurrentPosition--;
        }
        switchMusic();
    }

    /**
     * 切换音乐
     */
    private void switchMusic() {
        try {
            mIMusicServiceAidl.switchMusic();
            mControlButton.setImageResource(R.drawable.ic_media_pause);
            mNameText.setText(mList.get(mCurrentPosition).getTitle());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
