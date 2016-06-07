/*******************************************************************************
 *  This file is part of free utilities created by Joga Singh <joga.singh@gmail.com>.
 *  You are free to copy/modify/distribute the files to use it in any way you like.
 *  However as a credit, author's name should be mentioned in the file header.
 *  
 *  See the complete license terms (MIT License) in LICENSE.TXT included in the package.
 *  
 *******************************************************************************/
package com.punjuprogrammers.confirmationmailer;

import javax.persistence.EntityManager;

/**
 * Provider interface to inject JPA EntityManager into {@link ConfirmationMailer}.
 * 
 * @author Joga Singh - joga.singh@gmail.com
 *
 */
public interface MailerPersistentProvider {
	/**
	 * Returns JPA EntityManager.
	 * 
	 * @return
	 */
	public EntityManager getEntityManager();
}
