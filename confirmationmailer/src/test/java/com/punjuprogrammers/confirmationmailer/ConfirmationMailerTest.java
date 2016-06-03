/*******************************************************************************
 *  This file is part of free utilities created by Joga Singh <joga.singh@gmail.com>.
 *  You are free to copy/modify/distribute the files to use it in any way you like.
 *  However as a credit, author's name should be mentioned in the file header.
 *  
 *  See the complete license terms (MIT License) in LICENSE.TXT included in the package.
 *  
 *******************************************************************************/
package com.punjuprogrammers.confirmationmailer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.Assert;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Unit test for simple App.
 */
public class ConfirmationMailerTest extends TestCase {

	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public ConfirmationMailerTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(ConfirmationMailerTest.class);
	}

	/**
	 * Normal test case in which a mail is sent and token is deleted when it is validated when email link is clicked.
	 * 
	 * @throws ConfirmationMailerException
	 */
	public void testVaidateAndDelete() throws ConfirmationMailerException {
		ConfirmationMailer mailer = new ConfirmationMailer(new TestConfigProvider(), new TestEntityManagerProvider());
		Map<String, String> fields = getConfirmationMailFields();
		// Send a confirmation mail with generated token.
		String token = mailer.sendMailWithToken("jsingh@gmail.com", "confirmMail", fields, "");

		// Read the token
		MailToken mailToken = mailer.getTokenDetails(token);
		Assert.assertNotNull(mailToken);
		Assert.assertEquals(token, mailToken.getToken());
		
		//Validate of token that it is existing and not expired etc. 
		mailer.validateAndDeleteToken(token);
		mailToken = mailer.getTokenDetails(token);
		// Now it should return a null
		Assert.assertNull(mailToken);
	}
	
	/**
	 * This test tests the possible error conditions.
	 */
	public void testNegativeTestCases() {
		TestEntityManagerProvider entityManagerProvier= new TestEntityManagerProvider();
		ConfirmationMailer mailer = new ConfirmationMailer(new TestConfigProvider(), entityManagerProvier);
		Map<String, String> fields = getConfirmationMailFields();
		
		//Do not pass a required field in the template, it should throw a runtime exception  because it is a programming error
		
		fields.remove("TITLE");
		boolean error=false;
		try {
			mailer.sendMailWithToken("jsingh@gmail.com", "confirmMail", fields, "");
		} catch (ConfirmationMailerException e) {
		} catch (RuntimeException e) {
			error=true;			
		}
		Assert.assertTrue(error);
		
		//Give it a wrong Mail Type. It should throw a runtime exception because it is a programming error
		error=false;
		try {
			mailer.sendMailWithToken("jsingh@gmail.com", "confirmMailXX", fields, "");
		} catch (ConfirmationMailerException e) {
		} catch (RuntimeException e) {
			error=true;			
		}
		Assert.assertTrue(error);
		
		//Give it a wrong emailid. Should throw a checked exception
		fields = getConfirmationMailFields();
		error=false;
		try {
			mailer.sendMailWithToken("jsingh@", "confirmMail", fields, "");
		} catch (ConfirmationMailerException e) {
			error=true;
		}
		Assert.assertTrue(error);

		//Test the Token Not Found error
		error=false;
		String token=null;
		try {
			token = mailer.sendMailWithToken("jsingh@gmail.com", "confirmMail", fields, "");
			//Validate and delete the token.
			mailer.validateAndDeleteToken(token);
			//Try to validate again
			mailer.validateToken(token);
			Assert.fail("It should thow a Token not found error, not come here!");
		} catch (ConfirmationMailerException e) {
			Assert.assertEquals(ConfirmationMailer.ERROR_TOKEN_NOT_FOUND, e.getErrorCode());
		}
		
		//Test the Token Already Used error
		error=false;
		token=null;
		try {
			token = mailer.sendMailWithToken("jsingh@gmail.com", "confirmMail", fields, "");
			//Validate and mark the token as used.
			mailer.validateAndMarkUsedToken(token);
			//Try to validate again
			mailer.validateToken(token);
			Assert.fail("It should thow a Token not found error, not come here!");
		} catch (ConfirmationMailerException e) {
			Assert.assertEquals(ConfirmationMailer.ERROR_TOKEN_ALREADY_USED, e.getErrorCode());
			//Delete the token
			mailer.deleteToken(token);
		}

		//Test the Token Expired error
		error=false;
		token=null;
		try {
			token = mailer.sendMailWithToken("jsingh@gmail.com", "confirmMail", fields, "");
			//Manually change the token expiry date
			MailToken mailToken = mailer.getTokenDetails(token);
			mailToken.setExpiryDate(new Date(new Date().getTime()-100000000));
			entityManagerProvier.getEntityManager().getTransaction().begin();
			entityManagerProvier.getEntityManager().merge(mailToken);
			entityManagerProvier.getEntityManager().getTransaction().commit();
			//Try to validate again
			mailer.validateToken(token);
			Assert.fail("It should thow a Token Expired, not come here!");
		} catch (ConfirmationMailerException e) {
			Assert.assertEquals(ConfirmationMailer.ERROR_TOKEN_EXPIRED, e.getErrorCode());
			mailer.deleteToken(token);
		}
	}
	/**
	 * This test tests the operation deleteAll.
	 */
	public void testDeleteAll() {
		TestEntityManagerProvider entityManagerProvier= new TestEntityManagerProvider();
		ConfirmationMailer mailer = new ConfirmationMailer(new TestConfigProvider(), entityManagerProvier);
		Map<String, String> fields = getConfirmationMailFields();

		boolean error=false;
		try {
			Date expiryDateTill = new Date(new Date().getTime()+(24*60*60*1000)+1000);
			List<MailToken> list = mailer.getAllExpired(expiryDateTill);
			int existingCount = list.size();
			mailer.sendMailWithToken("jsingh@gmail.com", "confirmMail", fields, "");
			mailer.sendMailWithToken("jsingh@gmail.com", "confirmMail", fields, "");
			mailer.sendMailWithToken("jsingh@gmail.com", "confirmMail", fields, "");
			//Calculate an expiry date after 24 hours, because in test the token validity is 24 hours.
		
			list = mailer.getAllExpired(expiryDateTill);
			Assert.assertEquals(existingCount+3, list.size());
			mailer.deleteAll(expiryDateTill);
			list = mailer.getAllExpired(expiryDateTill);
			Assert.assertEquals(0,list.size());
			
		} catch (ConfirmationMailerException e) {
		} catch (RuntimeException e) {
			error=true;			
		}
		Assert.assertFalse(error);
	}

	/**
	 * This test tests the operation deleteAll.
	 */
	public void testDeleteAllUsed() {
		TestEntityManagerProvider entityManagerProvier= new TestEntityManagerProvider();
		ConfirmationMailer mailer = new ConfirmationMailer(new TestConfigProvider(), entityManagerProvier);
		Map<String, String> fields = getConfirmationMailFields();

		boolean error=false;
		try {
			List<MailToken> list = mailer.getAllUsed();
			int existingCount = list.size();
			String token = mailer.sendMailWithToken("jsingh@gmail.com", "confirmMail", fields, "");
			mailer.validateAndMarkUsedToken(token);
			token = mailer.sendMailWithToken("jsingh@gmail.com", "confirmMail", fields, "");
			mailer.validateAndMarkUsedToken(token);
			token = mailer.sendMailWithToken("jsingh@gmail.com", "confirmMail", fields, "");
			mailer.validateAndMarkUsedToken(token);
		
			list = mailer.getAllUsed();
			Assert.assertEquals(existingCount+3, list.size());
			mailer.deleteAllUsed();
			list = mailer.getAllUsed();
			Assert.assertEquals(0,list.size());
			
		} catch (ConfirmationMailerException e) {
		} catch (RuntimeException e) {
			error=true;			
		}
		Assert.assertFalse(error);
	}
	
	private Map<String, String> getConfirmationMailFields() {

		// Create the template specific fields. See below the template declaration.
		Map<String, String> fields = new HashMap<String, String>();
		fields.put("TITLE", "Mr.");
		fields.put("LAST_NAME", "Singh");
		fields.put("PHONE_NO", "015111032002");

		return fields;
	}
	private static class TestEntityManagerProvider implements MailerPersistentProvider {
		private EntityManager entityManager;

		public EntityManager getEntityManager() {
			if (entityManager == null) {
				EntityManagerFactory emf = Persistence.createEntityManagerFactory("mailerLocal");
				entityManager = emf.createEntityManager();
			}

			return entityManager;
		}
	}

	private static class TestConfigProvider implements MailerConfigProvier {

		public MailerConfig getMailConfig() {
			MailerConfig config = new MailerConfig("localhost", null, null);
			return config;
		}

		public List<MailerTemplate> getMailerTemplates() {
			List<MailerTemplate> templates = new ArrayList<MailerTemplate>();

			String subject = "Phone activation ${PHONE_NO}"; // field substitution in subject
			StringBuilder sb = new StringBuilder();
			sb.append("Dear ${TITLE} ${LAST_NAME} <br> ");
			sb.append("Thank your for choosing HelloPunju for your International calling.");
			sb.append("Please click on the below link to active your phone no. ${PHONE_NO} for calling. <br>");
			sb.append("<br><br><a href='http://www.hellopunju.com/activation?token=${TOKEN}'>Click here to activate</a>");
			sb.append("<br><br>HelloPunju Team");

			MailerTemplate confirmMail = new MailerTemplate("confirmMail", subject, sb.toString(), "NoReply@hellopunju.com", 60 * 24);

			templates.add(confirmMail);

			subject = "Phone activation successful";
			sb = new StringBuilder();
			sb.append("Dear ${TITLE} ${LAST_NAME} <br> ");
			sb.append("Thank your for choosing HelloPunju for your International calling.");
			sb.append("Your phone no. ${PHONE_NO} has been activated for international calling. <br>");
			sb.append("<br><br>HelloPunju Team");

			MailerTemplate notifyMail = new MailerTemplate("notifyMail", subject, sb.toString(), "NoReply@hellopunju.com", 0);

			templates.add(notifyMail);
			return templates;
		}

	}
}
