/*******************************************************************************
 *  This file is part of free utilities created by Joga Singh <joga.singh@gmail.com>.
 *  You are free to copy/modify/distribute the files to use it in any way you like.
 *  However as a credit, author's name should be mentioned in the file header.
 *  
 *  See the complete license terms (MIT License) in LICENSE.TXT included in the package.
 *  
 *******************************************************************************/
package com.punjuprogrammers.confirmationmailer;

import com.punjuprogrammers.confirmationmailer.utils.Util;

public class MailerConfig {
	private String smtpServer;
	private String serverUserName;
	private String serverPassowrd;
	public MailerConfig(String smtpServer, String serverUserName, String serverPassowrd) {
		super();
		Util.assertNotNullNotEmpty(smtpServer, "smtpServer");
		this.smtpServer = smtpServer;
		this.serverUserName = serverUserName;
		this.serverPassowrd = serverPassowrd;
	}
	public String getSmtpServer() {
		return smtpServer;
	}
	public String getServerUserName() {
		return serverUserName;
	}
	public String getServerPassowrd() {
		return serverPassowrd;
	}

}
