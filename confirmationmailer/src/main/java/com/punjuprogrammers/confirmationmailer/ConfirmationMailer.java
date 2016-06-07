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

/**
 * Main class providing the public interface for confirmationmailer library.
 * 
 * @author Joga Singh - joga.singh@gmail.com
 */
public class ConfirmationMailer {
	public static final int ERROR_TOKEN_NOT_FOUND = 10001;
	public static final int ERROR_TOKEN_EXPIRED = 10002;
	public static final int ERROR_TOKEN_ALREADY_USED = 10002;
	public static String TOKEN_STATUS_USED = "U";
	private Map<String, MailerTemplate> templatesMap;
	private MailerConfig mailConfig;
	private MailerPersistentProvider persistentProvider;

	/**
	 * Constructor. Some information need to be added to the 'persistence.xml' file. See the README.TXT for usage instructions.
	 * 
	 * @param configProvider
	 *            a non-null object which provides the required configuration values.
	 * @param persistentProvider
	 *            a non-null provider to get the EntityManager from.
	 * 
	 * @see MailerConfigProvier
	 * @see MailerPersistentProvider
	 */
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

	/**
	 * Sends mail.
	 * 
	 * This method sends a mail to <code>mailId</code> with the mail contents identified by template <code>mailType</code> after replacing the
	 * template fields with the values from <code>fields</code>.
	 * 
	 * @param mailId
	 *            mail address of the receiver. Null or empty value will cause an {@link IllegalArgumentException}
	 * @param mailType
	 *            name of the template from the list given in constructor (see {@link MailerConfigProvier}. Null, empty or invalid value will cause an
	 *            {@link IllegalArgumentException}.
	 * @param fields
	 *            map of field names and their values for the given <code>mailType</code>. Null value will cause an {@link IllegalArgumentException}.
	 *            If any of the template field is missing, an {@link IllegalStateException} is thrown.
	 * @throws ConfirmationMailerException
	 *             thrown if mail cannot be send due to any reason.
	 */
	public void sendMail(String mailId, String mailType, Map<String, String> fields) throws ConfirmationMailerException {
		sendMail(mailId, mailType, fields, null, false);
	}

	/**
	 * Sends a mail after generating a unique TOKEN and using the token value in email.
	 * 
	 * It generates a unique token and uses this value in the email to replace the TOKEN field. This token is saved along with the
	 * <code>mailType</code> and <code>userData</code> which is then used in validate methods. Token details can be retrieved with
	 * {@link #getTokenDetails(String)} method.
	 * 
	 * @param mailId
	 *            mail address of the receiver. Null or empty value will cause an {@link IllegalArgumentException}
	 * @param mailType
	 *            name of the template from the list given in constructor (see {@link MailerConfigProvier}. Null, empty or invalid value will cause an
	 *            {@link IllegalArgumentException}.
	 * @param fields
	 *            map of field names and their values for the given <code>mailType</code>. Null value will cause an {@link IllegalArgumentException}.
	 *            If any of the template field is missing, an {@link IllegalStateException} is thrown.
	 * @param userData
	 *            this optional string argument can be used to stored any related data i.e. User Id of the person the mail is sent to or any other
	 *            data which can be used later to identify the mail.
	 * @return generated token.
	 * @throws ConfirmationMailerException
	 *             thrown if mail cannot be send due to any reason.
	 */
	public String sendMailWithToken(String mailId, String mailType, Map<String, String> fields, String userData) throws ConfirmationMailerException {
		return sendMail(mailId, mailType, fields, userData, true);
	}

	private String sendMail(String mailId, String mailType, Map<String, String> fields, String userData, boolean genToken) throws ConfirmationMailerException {
		Util.assertNotNullNotEmpty(mailId, "mailId");
		Util.assertNotNullNotEmpty(mailType, "mailType");
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
				throw new IllegalStateException("Field not found in template: " + matcher.group(1));
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

	/**
	 * Returns the token details.
	 * 
	 * @param token
	 *            should be the token returned from the {@link #sendMailWithToken(String, String, Map, String)} method.
	 * @return token details.
	 */
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

	/**
	 * Validates the <code>token</code>. A valid token which is not used(activated) and not expired yet.
	 * 
	 * A token can be used only once and should be used (activated) within particular time. Token validity period is calculated from used template
	 * when sending the mail (see {@link MailerTemplate}. This method is used to validate of the given <code>token</code> is still valid.
	 * 
	 * @param token
	 *            token generated by {@link #sendMailWithToken(String, String, Map, String)}.
	 * @return if the <code>token</code> is valid, token details otherwise an exception is thrown.
	 * @throws ConfirmationMailerException
	 *             is thrown if the token is not valid. Error code can be used for the particular reason.
	 */
	public MailToken validateToken(String token) throws ConfirmationMailerException {
		Util.assertNotNull(token, "token");
		MailToken mailToken = persistentProvider.getEntityManager().find(MailToken.class, token);
		validateToken(token, mailToken);
		return mailToken;
	}

	/**
	 * Validates the <code>token</code> and mark itas used. See {@link #validateToken(String)} for more details.
	 * 
	 * @param token
	 *            token generated by {@link #sendMailWithToken(String, String, Map, String)}.
	 * @return if the <code>token</code> is valid, token details otherwise an exception is thrown.
	 * @throws ConfirmationMailerException
	 *             is thrown if the token is not valid. Error code can be used for the particular reason.
	 */
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

	/**
	 * Validates the <code>token</code> and deletes it. See {@link #validateToken(String)} for more details. A deleted token is permanently deleted
	 * from the database.
	 * 
	 * @param token
	 *            token generated by {@link #sendMailWithToken(String, String, Map, String)}.
	 * @return if the <code>token</code> is valid, token details otherwise an exception is thrown.
	 * @throws ConfirmationMailerException
	 *             is thrown if the token is not valid. Error code can be used for the particular reason.
	 */
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

	/**
	 * Deletes the <code>token</code> permanently from database.
	 * 
	 * @param token a valid token.
	 * @return token details.
	 */
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

	/**
	 * Deletes all tokens whose expiry date is till <code>expiryDateTill</code>.
	 * 
	 * @param expiryDateTill
	 *            timestamp till the tokens should be deleted.
	 * @return the number of records deleted.
	 */
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

	/**
	 * Deletes all used tokens.
	 * 
	 * @return the number of records deleted.
	 */
	public int deleteAllUsed() {
		EntityManager em = persistentProvider.getEntityManager();
		Query q = em.createQuery(" DELETE FROM MailToken m WHERE m.status <= :status");
		q.setParameter("status", TOKEN_STATUS_USED);
		em.getTransaction().begin();
		int count = q.executeUpdate();
		em.getTransaction().commit();

		return count;
	}

	/**
	 * Returns all tokens whose expiry date is till <code>expiryDateTill</code>.
	 * 
	 * @param expiryDateTill
	 *            timestamp.
	 * 
	 * @return list of token objects.
	 */
	public List<MailToken> getAllExpired(Date expiryDateTill) {
		Util.assertNotNull(expiryDateTill, "expiryDateTill");
		EntityManager em = persistentProvider.getEntityManager();
		TypedQuery<MailToken> q = em.createNamedQuery("MailToken_Expired", MailToken.class);
		q.setParameter("expiryDateTill", expiryDateTill);
		return q.getResultList();
	}

	/**
	 * Returns the list of all used tokens
	 * 
	 * @return list of objects
	 */
	public List<MailToken> getAllUsed() {
		EntityManager em = persistentProvider.getEntityManager();
		TypedQuery<MailToken> q = em.createNamedQuery("MailToken_Status", MailToken.class);
		q.setParameter("status", TOKEN_STATUS_USED);
		return q.getResultList();
	}

	/**
	 * Returns list of token objects who are used between <code>usedDateFrom</code> and <code>usedDateTill</code>.
	 * 
	 * @param usedDateFrom
	 *            start timestamp.
	 * @param usedDateTill
	 *            end timestamp.
	 * @return list of token objects.
	 */
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
