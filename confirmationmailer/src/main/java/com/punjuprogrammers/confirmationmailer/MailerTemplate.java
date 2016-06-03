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

public class MailerTemplate {
	private String mailType;
	private String subject;
	private String text;
	private String fromEmail;
	private long validityMinutes;
	
	public MailerTemplate(String mailType, String subject, String text, String fromEmail, long validityMinutes) {
		super();
		Util.assertNotNullNotEmpty(mailType, "mailType");
		Util.assertNotNullNotEmpty(subject, "subject");
		Util.assertNotNullNotEmpty(text, "text");
		Util.assertNotNullNotEmpty(fromEmail, "fromEmail");
		this.mailType = mailType;
		this.subject = subject;
		this.text = text;
		this.fromEmail=fromEmail;
		this.validityMinutes=validityMinutes;
	}

	public String getMailType() {
		return mailType;
	}

	public String getSubject() {
		return subject;
	}

	public String getText() {
		return text;
	}

	public String getFromEmail() {
		return fromEmail;
	}

	public long getValidityMinutes() {
		return validityMinutes;
	}

}
