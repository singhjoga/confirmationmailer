# ConfirmationMailer
ConfirmationMailer is a Java library which can be used to send confirmation mails such as Account Activation, 
Password Change or similar use casses. In these kind of mails, generally a link is sent to the user which he/she must click to
to carry out the related task. Concerned system must validate the url and perform the task. This library takes care of generating 
the necessary URL token and persisting the token and related details into the database.

# Using the library
Library can be used as a source or by including the jar in `bin` folder into the classpath. It uses JPA 2.1 version for persistence, therefore can be used with any Persistence Provider such as Hibernate, EclipseLink, OpenJPA etc. At the moment it is tested with only Hibernate, but should work with other providers also. To use the library, some changes are needed in your existing persistence.xml file as shown below:

```xml

<persistence version="2.1" xmlns="http://xmlns.jcp.org/xml/ns/persistence" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence http://xmlns.jcp.org/xml/ns/persistence/persistence_2_1.xsd">
	<persistence-unit name="mailerLocal" transaction-type="RESOURCE_LOCAL">
		<class>com.punjuprogrammers.confirmationmailer.MailToken</class>
		<properties>
			<property name="javax.persistence.jdbc.url" value="jdbc:hsqldb:hsql://localhost/voctrainer"/>
			<property name="javax.persistence.jdbc.user" value="SA"/>
			<property name="javax.persistence.jdbc.password" value=""/>
			<property name="javax.persistence.jdbc.driver" value="org.hsqldb.jdbcDriver"/>
			<property name="hibernate.hbm2ddl.auto" value="update"/>
		</properties>
	</persistence-unit>
</persistence>

```
`class` element should be specified as for `com.punjuprogrammers.confirmationmailer.MailToken`, the only persistence entity. 
Also the property `hibernate.hbm2ddl.auto` should be set to `update` to create the necessary database tables automatically.

Library is well documented
and sample use cases can be found in the test class.


