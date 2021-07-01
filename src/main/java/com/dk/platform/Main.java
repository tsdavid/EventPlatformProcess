package com.dk.platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	/*****************************************************************************************
	 **************************************  Logger ******************************************
	 ****************************************************************************************/

	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	public Main() {
//		new com.dk.platform.eventTasker.Application();
//		logger.info("Event Tasker Process ara Running.");
		new com.dk.platform.eventManager.Application();
		logger.info("Event Manager Process ara Running.");

	}
	
	public static void main(String[] args) {

		new Main();

	}

}
