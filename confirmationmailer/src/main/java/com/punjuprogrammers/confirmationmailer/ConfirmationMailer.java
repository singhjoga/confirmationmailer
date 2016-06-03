/*******************************************************************************
 *  This file is part of free utilities created by Joga Singh <joga.singh@gmail.com>.
 *  You are free to copy/modify/distribute the files to use it in any way you like.
 *  However as a credit, author's name should be mentioned in the file header.
 *  
 *  See the complete license terms (MIT License) in LICENSE.TXT included in the package.
 *  
 *******************************************************************************/
package com.punjuprogrammers.confirmationmailer;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.punjuprogrammers.confirmationmailer.utils.MailUtil;
import com.punjuprogrammers.confirmationmailer.utils.Util;

public class ConfirmationMailer {
	public static final int ERROR_TOKEN_NOT_FOUND = 10001;
	public static final int ERROR_TOKEN_EXPIRED = 10002;
	public static final int ERROR_TOKEN_ALREADY_USED = 10002;
	public static String TOKEN_STATUS_USED = "U";
	private Map<String, MailerTemplate> templatesMap;
	private MailerConfig mailConfig;
	private MailerPersistentProvider persistentProvider;

	public ConfirmationMailer(MailerConfigProvier configProvider, MailerPersistentProvider persistentProvider) {
		Util.assertNotNull(configProvider, "configProvider");
		Util.assertNotNull(persistentProvider, "persistentProvider");
		this.mailConfig = configProvider.getMailConfig();
		this.persistentProvider = persistentProvider;
		templatesMap = new HashMap<String, MailerTemplate>();
		for (MailerTemplate mt : configProvider.getMailerTemplates()) {
			templatesMap.put(mt.getMailType(), mt);
		}
	}

	public void sendMail(String mailId, String mailType, Map<String, String> fields) throws ConfirmationMailerException {
		sendMail(mailId, mailType, fields, null, false);
	}

	public String sendMailWithToken(String mailId, String mailType, Map<String, String> fields, String userData) throws ConfirmationMailerException {
		return sendMail(mailId, mailType, fields, userData, true);
	}

	private String sendMail(String mailId, String mailType, Map<String, String> fields, String userData, boolean genToken) throws ConfirmationMailerException {
		Util.assertNotNull(mailId, "mailId");
		Util.assertNotNull(mailType, "mailType");
		Util.assertNotNull(fields, "fields");

		MailerTemplate mailTemplate = templatesMap.get(mailType);

		if (mailTemplate == null) {
			throw new IllegalArgumentException("Template not found for mail type: " + mailType);
		}
		Map<String, String> mailFields = fields;
		String token = null;
		if (genToken) {
			mailFields = new HashMap<String, String>(fields);
			token = genToken();
			mailFields.put("TOKEN", token);
		}

		String subject = replaceTokens(mailTemplate.getSubject(), mailFields);
		String msgText = replaceTokens(mailTemplate.getText(), mailFields);
		if (genToken) {
			saveToken(token, mailTemplate, mailId, mailType, userData);
		}
		try {
			MailUtil.sendMail(mailId, subject, msgText, mailTemplate.getFromEmail(), mailConfig.getSmtpServer(), mailConfig.getServerUserName(), mailConfig.getServerPassowrd());
		} catch (MessagingException e) {
			if (genToken) {
				deleteToken(token);
			}
			throw new ConfirmationMailerException(e.getMessage(), e);
		}
		return token;
	}

	private String replaceTokens(String template, Map<String, String> fields) {
		Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}");
		Matcher matcher = pattern.matcher(template);
		StringBuffer buffer = new StringBuffer();
		while (matcher.find()) {
			String replacement = fields.get(matcher.group(1));
			if (replacement != null) {
				// if replacement contains $ sing or backspaces, appendReplacement can have problem therefore append is split into two lines.
				matcher.appendReplacement(buffer, "");
				buffer.append(replacement);
			} else {
				throw new IllegalStateException("Token not found in template: " + matcher.group(1));
			}
		}
		matcher.appendTail(buffer);
		return buffer.toString();
	}

	private String genToken() {
		return UUID.randomUUID().toString();
	}

	private String saveToken(String token, MailerTemplate mailTemplate, String mailId, String mailType, String userData) throws ConfirmationMailerException {
		Date expiryDate = new Date((new Date().getTime()) + (mailTemplate.getValidityMinutes() * 60 * 1000));
		MailToken mailToken = new MailToken(token, mailType, expiryDate, mailId, userData);
		try {
			persistentProvider.getEntityManager().getTransaction().begin();
			persistentProvider.getEntityManager().persist(mailToken);
			persistentProvider.getEntityManager().getTransaction().commit();
		} catch (Throwable e) {
			throw new ConfirmationMailerException(e.getMessage(), e);
		}
		return token;
	}

	public MailToken getTokenDetails(String token) {
		Util.assertNotNull(token, "token");
		MailToken mailToken = persistentProvider.getEntityManager().find(MailToken.class, token);

		return mailToken;
	}

	private void validateToken(String token, MailToken mailToken) throws ConfirmationMailerException {
		if (mailToken == null) {
			throw new ConfirmationMailerException("Token not found: " + token, ERROR_TOKEN_NOT_FOUND);
		}
		if (TOKEN_STATUS_USED.equals(mailToken.getStatus())) {
			throw new ConfirmationMailerException("Token is already used: " + token, ERROR_TOKEN_ALREADY_USED);
		}
		if (mailToken.getExpiryDate().before(new Date())) {
			throw new ConfirmationMailerException("Token is expired: " + token, ERROR_TOKEN_EXPIRED);
		}
	}

	public MailToken validateToken(String token) throws ConfirmationMailerException {
		Util.assertNotNull(token, "token");
		MailToken mailToken = persistentProvider.getEntityManager().find(MailToken.class, token);
		validateToken(token, mailToken);
		return mailToken;
	}

	public MailToken validateAndMarkUsedToken(String token) throws ConfirmationMailerException {
		Util.assertNotNull(token, "token");
		MailToken mailToken = persistentProvider.getEntityManager().find(MailToken.class, token);
		validateToken(token, mailToken);
		mailToken.setStatus(TOKEN_STATUS_USED);
		mailToken.setUsedDate(new Date());
		persistentProvider.getEntityManager().getTransaction().begin();
		persistentProvider.getEntityManager().merge(mailToken);
		persistentProvider.getEntityManager().getTransaction().commit();

		return mailToken;
	}

	public MailToken validateAndDeleteToken(String token) throws ConfirmationMailerException {
		Util.assertNotNull(token, "token");
		MailToken mailToken = persistentProvider.getEntityManager().find(MailToken.class, token);
		validateToken(token, mailToken);
		mailToken.setStatus(TOKEN_STATUS_USED);
		persistentProvider.getEntityManager().getTransaction().begin();
		persistentProvider.getEntityManager().remove(mailToken);
		persistentProvider.getEntityManager().getTransaction().commit();

		return mailToken;
	}

	public MailToken deleteToken(String token) {
		Util.assertNotNull(token, "token");
		MailToken mailToken = persistentProvider.getEntityManager().find(MailToken.class, token);
		if (mailToken != null) {
			persistentProvider.getEntityManager().getTransaction().begin();
			persistentProvider.getEntityManager().remove(mailToken);
			persistentProvider.getEntityManager().getTransaction().commit();
		}
		return mailToken;
	}

	public int deleteAll(Date expiryDateTill) {
		Util.assertNotNull(expiryDateTill, "expiryDateTill");
		EntityManager em = persistentProvider.getEntityManager();
		Query q = em.createQuery(" DELETE FROM MailToken m WHERE m.expiryDate <= :expiryDateTill");
		q.setParameter("expiryDateTill", expiryDateTill);
		em.getTransaction().begin();
		int count = q.executeUpdate();
		em.getTransaction().commit();

		return count;
	}

	public int deleteAllUsed() {
		EntityManager em = persistentProvider.getEntityManager();
		Query q = em.createQuery(" DELETE FROM MailToken m WHERE m.status <= :status");
		q.setParameter("status", TOKEN_STATUS_USED);
		em.getTransaction().begin();
		int count = q.executeUpdate();
		em.getTransaction().commit();

		return count;
	}

	public List<MailToken> getAllExpired(Date expiryDateTill) {
		Util.assertNotNull(expiryDateTill, "expiryDateTill");
		EntityManager em = persistentProvider.getEntityManager();
		TypedQuery<MailToken> q = em.createNamedQuery("MailToken_Expired", MailToken.class);
		q.setParameter("expiryDateTill", expiryDateTill);
		return q.getResultList();
	}

	public List<MailToken> getAllUsed() {
		EntityManager em = persistentProvider.getEntityManager();
		TypedQuery<MailToken> q = em.createNamedQuery("MailToken_Status", MailToken.class);
		q.setParameter("status", TOKEN_STATUS_USED);
		return q.getResultList();
	}

	public List<MailToken> getAllUsed(Date usedDateFrom, Date usedDateTill) {
		Util.assertNotNull(usedDateFrom, "usedDateFrom");
		Util.assertNotNull(usedDateTill, "usedDateTill");
		EntityManager em = persistentProvider.getEntityManager();
		TypedQuery<MailToken> q = em.createNamedQuery("MailToken_Used", MailToken.class);
		q.setParameter("usedDateFrom", usedDateFrom);
		q.setParameter("usedDateTill", usedDateTill);
		return q.getResultList();
	}
}
