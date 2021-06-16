package com.dk.platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	public Main() {
		new com.dk.platform.eventTasker.Application();
		System.out.println("TASKER");
		new com.dk.platform.eventManager.Application();

	}
	
	public static void main(String[] args) {

//		new Main();

		Logger logger = LoggerFactory.getLogger(Main.class);
		logger.info("[{}] Hello Worlds", "hi");
	}

}
