package com.h.game.jni;

public class GameJNI {

    public native void registerHotkeyCallback(HotkeyCallback callback);

    public native  int getDesktopIcon();

    public native void moveDesktopIcon(int index,int x,int y);
}
