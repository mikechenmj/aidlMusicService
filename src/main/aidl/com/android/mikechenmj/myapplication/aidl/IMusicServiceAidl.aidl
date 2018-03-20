// IMusicServiceAidl.aidl
package com.android.mikechenmj.myapplication.aidl;

import com.android.mikechenmj.myapplication.aidl.ICallbackAidl;

interface IMusicServiceAidl {

    void startMusic();

    void pauseMusic();

    void switchMusic();

    oneway void setCallback(ICallbackAidl callback);
}
