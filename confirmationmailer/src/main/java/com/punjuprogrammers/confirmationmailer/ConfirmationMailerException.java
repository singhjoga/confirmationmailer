/*******************************************************************************
 *  This file is part of free utilities created by Joga Singh <joga.singh@gmail.com>.
 *  You are free to copy/modify/distribute the files to use it in any way you like.
 *  However as a credit, author's name should be mentioned in the file header.
 *  
 *  See the complete license terms (MIT License) in LICENSE.TXT included in the package.
 *  
 *******************************************************************************/
package com.punjuprogrammers.confirmationmailer;

public class ConfirmationMailerException extends Exception {
	
	private int errorCode;
	
	public ConfirmationMailerException() {
		super();
	}

	public ConfirmationMailerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ConfirmationMailerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfirmationMailerException(String message) {
		super(message);
	}
	public ConfirmationMailerException(String message, int errorCode) {
		super(message);
		this.errorCode=errorCode;
	}

	public ConfirmationMailerException(Throwable cause) {
		super(cause);
	}

	public int getErrorCode() {
		return errorCode;
	}

}
