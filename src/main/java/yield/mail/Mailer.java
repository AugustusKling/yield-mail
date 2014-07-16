package yield.mail;

import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import yield.core.BaseControlQueueProvider;
import yield.core.EventListener;
import yield.input.ListenerExceutionFailed;

public class Mailer extends BaseControlQueueProvider implements
		EventListener<String> {
	private Address[] to;
	private Address[] cc;
	private String subjectPrefix;
	private Session session;

	/**
	 * @param serverConfig
	 *            Settings for javax.mail.
	 * @param to
	 *            Recipients TO field.
	 * @param cc
	 *            Recipients CC field.
	 * @param subjectPrefix
	 *            Prefix for subject. Can be {@code null} to be ignored. The
	 *            rest of the subject will be formed by the first line of the
	 *            event.
	 * @param username
	 *            User to authenticate with mail server. Can be {@code null} if
	 *            no user-name-password authentication is desired.
	 * @param password
	 *            Password to authenticate with mail server. Can be {@code null}
	 *            if no user-name-password authentication is desired.
	 */
	public Mailer(Properties serverConfig, List<String> to, List<String> cc,
			String subjectPrefix, final String username, final String password)
			throws AddressException {
		this.to = toAddresses(to);
		this.cc = toAddresses(cc);
		this.subjectPrefix = subjectPrefix;

		Logger.getLogger(getClass()).debug(
				"Creating mail session with options: " + serverConfig);
		Authenticator authenticator = null;
		if (username != null || password != null) {
			authenticator = new javax.mail.Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			};
		}
		session = Session.getInstance(serverConfig, authenticator);
	}

	private Address[] toAddresses(List<String> to) throws AddressException {
		Address[] addresses = new Address[to.size()];
		for (int i = 0; i < to.size(); i++) {
			String address = to.get(i);
			addresses[i] = new InternetAddress(address);
		}
		return addresses;
	}

	@Override
	public void feed(String e) {
		MimeMessage message = new MimeMessage(session);
		try {
			// the "from" address may be set in code, or set in the
			// config file under "mail.from" ; here, the latter style is used
			// message.setFrom(new InternetAddress(aFromEmailAddr));
			message.addRecipients(Message.RecipientType.TO, this.to);
			message.addRecipients(Message.RecipientType.CC, this.cc);

			String subject = e.split("\\r?\\n|\\r", 2)[0];
			if (this.subjectPrefix != null) {
				subject = subjectPrefix + subject;
			}
			message.setSubject(subject);

			message.setText(e);

			Transport.send(message);
		} catch (MessagingException ex) {
			this.getControlQueue().feed(
					new ListenerExceutionFailed<String>(e, ex));
			Logger.getLogger(getClass()).error("Cannot send email. ", ex);
		}
	}

}
