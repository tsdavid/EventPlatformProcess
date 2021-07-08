package com.dk.platform.backup;

public class SingleTone {

    private static class SingletonHelper {
        private static final SingleTone INSTANCE = new SingleTone();
    }

    public static SingleTone getInstance() {
        return SingletonHelper.INSTANCE;
    }
}
