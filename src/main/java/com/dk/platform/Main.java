package com.dk.platform;

import java.util.Arrays;

public class Main {

	public Main() {
		new com.dk.platform.eventTasker.Application();
		System.out.println("TASKER");
		new com.dk.platform.eventManager.Application();

	}
	
	public static void main(String[] args) {

		new Main();

	}

}
