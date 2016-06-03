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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name="MAIL_TOKEN", indexes={@Index(name="idx_expiry_date",columnList="EXPIRY_DATE")})
@NamedQueries(value={
		@NamedQuery(name="MailToken_Expired",query="FROM MailToken m WHERE m.expiryDate <= :expiryDateTill"),
		@NamedQuery(name="MailToken_Status",query="FROM MailToken m WHERE m.status <= :status"),
		@NamedQuery(name="MailToken_Used",query="FROM MailToken m WHERE m.usedDate >= :useDateFrom AND m.usedDate <= :useDateTill"),
		
})
public class MailToken {
	
	@Id
	private String token;
	
	@Column(name="MAIL_TYPE",nullable=false)
	private String mailType;
	
	@Column(name="MAIL_ID",nullable=false)
	private String mailId;

	@Column(name="USER_DATA")
	private String userData;
	
	@Column(name="EXPIRY_DATE",nullable=false)
	private Date expiryDate;
	
	@Column(name="STATUS")
	private String status;

	@Column(name="USED_DATE")
	private Date usedDate;
	
	public MailToken() {

	}
	public MailToken(String token, String mailType, Date expiryDate, String mailId, String userData) {
		super();
		this.token = token;
		this.mailType = mailType;
		this.expiryDate = expiryDate;
		this.mailId=mailId;
		this.userData=userData;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public Date getExpiryDate() {
		return expiryDate;
	}
	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}
	public String getUserData() {
		return userData;
	}
	public void setUserData(String userData) {
		this.userData = userData;
	}
	public String getMailType() {
		return mailType;
	}
	public void setMailType(String mailType) {
		this.mailType = mailType;
	}
	public String getMailId() {
		return mailId;
	}
	public void setMailId(String mailId) {
		this.mailId = mailId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Date getUsedDate() {
		return usedDate;
	}
	public void setUsedDate(Date usedDate) {
		this.usedDate = usedDate;
	}
	
	
}
