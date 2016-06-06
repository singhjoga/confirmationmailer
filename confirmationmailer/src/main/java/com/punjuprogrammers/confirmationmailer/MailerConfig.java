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

/**
 * {@link ConfirmationMailer} configuration options.
 * 
 * @author Joga Singh <joga.singh@gmail.com>
 *
 */
public class MailerConfig {
	private String smtpServer;
	private String serverUserName;
	private String serverPassowrd;

	/**
	 * Default constructor.
	 * 
	 * @param smtpServer
	 *            SMTP server host name or IP Address.
	 * @param serverUserName
	 *            SMTP Server needed user name. It can be null if SMTP Server does not require authentication.
	 * @param serverPassowrd
	 *            Authentication password needed by SMTP Server. Null if authentication is not required.
	 */
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
