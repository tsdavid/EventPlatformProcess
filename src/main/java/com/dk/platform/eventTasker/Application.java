package com.dk.platform.eventTasker;

import com.dk.platform.eventTasker.process.Initialize;

public class Application implements com.dk.platform.Application {

    @Override
    public void initialize() {
        new Initialize();
    }
}
