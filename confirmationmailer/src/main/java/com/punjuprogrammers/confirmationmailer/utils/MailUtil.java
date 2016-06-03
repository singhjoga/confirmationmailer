/*******************************************************************************
 *  This file is part of free utilities created by Joga Singh <joga.singh@gmail.com>.
 *  You are free to copy/modify/distribute the files to use it in any way you like.
 *  However as a credit, author's name should be mentioned in the file header.
 *  
 *  See the complete license terms (MIT License) in LICENSE.TXT included in the package.
 *  
 *******************************************************************************/
package com.punjuprogrammers.confirmationmailer.utils;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailUtil {

	public static void sendMail(String toEmailId, String subject, String msgText, String from, String smtpServer, String userName, String password) throws MessagingException {

		Properties properties = new Properties();
		properties.setProperty("mail.smtp.host", smtpServer);
		Session session = Session.getDefaultInstance(properties);
		MimeMessage message = new MimeMessage(session);

		message.setFrom(new InternetAddress(from));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmailId));
		message.setSubject(subject);
		message.setContent(msgText, "text/html; charset=utf-8");
		message.setSentDate(new Date());
		Transport.send(message);
	}
}
