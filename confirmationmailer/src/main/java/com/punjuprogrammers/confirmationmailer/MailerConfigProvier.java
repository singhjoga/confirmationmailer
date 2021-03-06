/*******************************************************************************
 *  This file is part of free utilities created by Joga Singh <joga.singh@gmail.com>.
 *  You are free to copy/modify/distribute the files to use it in any way you like.
 *  However as a credit, author's name should be mentioned in the file header.
 *  
 *  See the complete license terms (MIT License) in LICENSE.TXT included in the package.
 *  
 *******************************************************************************/
package com.punjuprogrammers.confirmationmailer;

import java.util.List;

/**
 * {@link ConfirmationMailer} needed configuration provider.
 * 
 * @author Joga Singh - joga.singh@gmail.com
 *
 */
public interface MailerConfigProvier {
	/**
	 * {@link ConfirmationMailer} related configuration.
	 * 
	 * @return config object.
	 */
	public MailerConfig getMailConfig();

	/**
	 * List of templates which are used for sending mails.
	 * 
	 * @return list of templates.
	 */
	public List<MailerTemplate> getMailerTemplates();
}
