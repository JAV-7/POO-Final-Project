package com.multi.driver.crud;

import java.util.HashMap;
import java.util.Map;

/**
 * DriverFactory is where all drivers are stored in a HashMap, 
 * having the path of the file as the key value and Object as 
 * the value.
 * This class is set apart from others, as it should keep all
 * the content only visible to the same packet and only be 
 * accessible by letting the user know how many drivers are 
 * being used in the project.
 */

public class DriverFactory{

	
	protected static final Map<String, Object> DRIVERS = new HashMap<>();
	
	protected DriverFactory() {
		
	}
	
	public static int getNumberOfDrivers() {
		return DRIVERS.size();
	}
	
	
}
