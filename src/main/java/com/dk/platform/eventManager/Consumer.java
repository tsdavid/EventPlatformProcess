package com.dk.platform.eventManager;

public interface Consumer {

    void setActive();

    void setDeActive();

    void closeAllResources();
}
