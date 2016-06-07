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
 * Mail template definition. Mails can be sent for different purposes, therefore different mail templates. Each mail template is identified by
 * <code>mailType</code> field. Mail template can contain the substitute variables which are replaced with actual values during runtime. Below is a
 * sample email template: <pre> {@code
 * Dear ${TITLE} ${LAST_NAME} <br>
 * Thank your for choosing HelloPunju for your International calling.
 * Please click on the below link to active your phone no. ${PHONE_NO} for calling. <br>
 * <br><br><a href='http://www.hellopunju.com/activation?token=$}{TOKEN}{@code '>Click here to activate</a>
 * <br><br>HelloPunju Team }
 * </pre>
 * 
 * 
 * @author Joga Singh - joga.singh@gmail.com
 *
 */
public class MailerTemplate {
	private String mailType;
	private String subject;
	private String text;
	private String fromEmail;
	private long validityMinutes;

	/**
	 * Default constructor.
	 * 
	 * @param mailType
	 *            a unique mail type identifier. This identifier is then used in
	 *            {@link ConfirmationMailer#sendMailWithToken(String, String, java.util.Map, String)} method to select the mail template. If the same
	 *            mail is sent in different languages, different mail identifier must be used.
	 * @param subject
	 *            mail subject. It can also contain substitute variables in the form of ${Variable Name}.
	 * @param text
	 *            mail content in HTML. It can also contain substitute variables in the form of ${Variable Name}. It can also a contain a ${TOKEN}
	 *            substitute variable is generated and saved by the system.
	 * @param fromEmail
	 *            name or email id which will be used as email sender.
	 * @param validityMinutes
	 *            If email template is used to send account activation email or for similar purpose, the generated token should be valid only for a
	 *            particular period i.e. a few hours, days etc. This must be specified in minutes. If it is just a simple mail notification, a zero
	 *            value can be used.
	 */
	public MailerTemplate(String mailType, String subject, String text, String fromEmail, long validityMinutes) {
		super();
		Util.assertNotNullNotEmpty(mailType, "mailType");
		Util.assertNotNullNotEmpty(subject, "subject");
		Util.assertNotNullNotEmpty(text, "text");
		Util.assertNotNullNotEmpty(fromEmail, "fromEmail");
		this.mailType = mailType;
		this.subject = subject;
		this.text = text;
		this.fromEmail = fromEmail;
		this.validityMinutes = validityMinutes;
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
