package com.android.mikechenmj.myapplication;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import com.android.mikechenmj.myapplication.aidl.ICallbackAidl;
import com.android.mikechenmj.myapplication.aidl.IMusicServiceAidl;

import java.io.File;
import java.io.IOException;

public class MusicService extends Service {

    private static final boolean DEBUG = false;

    private MediaPlayer mMediaPlayer;

    private ICallbackAidl mCallback;

    private IMusicServiceAidl mIMusicServiceAidl = new IMusicServiceAidl.Stub() {

        @Override
        public void startMusic() throws RemoteException {
            mMediaPlayer.start();
        }

        @Override
        public void pauseMusic() throws RemoteException {
            mMediaPlayer.pause();
        }

        @Override
        public void switchMusic() throws RemoteException {
            String path = mCallback.getMusicPath();
            initMediaPlayer(path);

            if (DEBUG) {
                mMediaPlayer.seekTo((int) (mMediaPlayer.getDuration() * 0.9f));
            }

            mMediaPlayer.start();
        }

        @Override
        public void setCallback(ICallbackAidl callback) throws RemoteException {
            mCallback = callback;
        }
    };
	
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mMediaPlayer.start();
        return super.onStartCommand(intent, flags, startId);
    }
	
    @Override
    public IBinder onBind(Intent intent) {
        initMediaPlayer(intent.getStringExtra("data"));
        return mIMusicServiceAidl.asBinder();
    }


    private void initMediaPlayer(final String path) {
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
            }
            File file = new File(path);
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
            }
            mMediaPlayer.setDataSource(file.getPath());
            mMediaPlayer.prepare();
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    try {
                        mCallback.update();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
