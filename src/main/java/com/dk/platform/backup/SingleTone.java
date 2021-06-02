package com.dk.platform.backup;

import com.dk.platform.eventManager.util.ManagerUtil;

public class SingleTone {

    private static class SingletonHelper {
        private static final SingleTone INSTANCE = new SingleTone();
    }

    public static SingleTone getInstance() {
        return SingleTone.SingletonHelper.INSTANCE;
    }
}
