/*******************************************************************************
 *  This file is part of free utilities created by Joga Singh <joga.singh@gmail.com>.
 *  You are free to copy/modify/distribute the files to use it in any way you like.
 *  However as a credit, author's name should be mentioned in the file header.
 *  
 *  See the complete license terms (MIT License) in LICENSE.TXT included in the package.
 *  
 *******************************************************************************/
package com.punjuprogrammers.confirmationmailer.utils;
import java.util.Properties;

public class Util {

	public static void assertNotNullNotEmpty(String obj, String paramName) {
		assertNotNull(obj,paramName);
		if (obj.isEmpty()) {
			throw new IllegalArgumentException(paramName+" cannot be empty");
		}
	}
	
	public static void assertNotNull(Object obj, String paramName) {
		if (obj == null) {
			throw new IllegalArgumentException(paramName+" cannot be null");
		}
	}
}
