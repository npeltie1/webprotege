package edu.stanford.bmir.protege.web.server.mail;

import edu.stanford.bmir.protege.web.server.inject.ApplicationHost;
import edu.stanford.bmir.protege.web.server.inject.ApplicationName;
import edu.stanford.bmir.protege.web.server.inject.MailProperties;
import edu.stanford.bmir.protege.web.shared.app.WebProtegePropertyName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 05/11/2013
 */
public class MailManager implements SendMail {

    public static final String MAIL_SMTP_AUTH = "mail.smtp.auth";

    public static final String MAIL_SMTP_USER = "mail.smtp.user";

    public static final String MAIL_SMTP_PASSWORD = "mail.smtp.wp.password";

    public static final String MAIL_SMTP_FROM = "mail.smtp.from";

    public static final String MAIL_SMTP_FROM_PERSONALNAME = "mail.smtp.from.wp.personalName";

    public static final String UTF_8 = "utf-8";

    public static final String DEFAULT_FROM_VALUE_PREFIX = "no-reply@";

    private static final Logger logger = LoggerFactory.getLogger(MailManager.class.getName());

    public static final String MAIL_SMTP_HOST = "mail.smtp.host";

    public static final String MAIL_SMTP_PORT = "mail.smtp.port";

    private final Properties properties;

    private final MessagingExceptionHandler messagingExceptionHandler;

    private final String applicationName;

    private final String applicationHost;


    /**
     * Constructs a {@code MailManager} using the specified {@link Properties} object and the specified exception handler.
     * The {@link Properties} object should contain java mail properties e.g. mail.smtp.host etc.  See
     * <a href="https://javamail.java.net/nonav/docs/api/com/sun/mail/smtp/package-summary.html">https://javamail.java.net/nonav/docs/api/com/sun/mail/smtp/package-summary.html</a>
     * for more information.    Note
     * modification of values in the {@link Properties} object will not modify mail settings after construction.
     *
     * @param applicationName           The name of the application (e.g. WebProtege).  Not {@code null}.
     * @param applicationHost           The host name that the application is running on (e.g. webprotege.stanford.edu).
     *                                  Not {@code null}.
     * @param properties                The mail properties.  Not {@code null}. These are the same properties as specified in the
     *                                  Java mail spec.  Note that an additional property "mail.smtp.from.personalName" can be
     *                                  used to specify a personal name in the from field of sent messages.  Also, if authentication
     *                                  is required then the "mail.smtp.password" property should be set.
     * @param messagingExceptionHandler An exception handler that handles {@link MessagingException}s.  Not {@code null}.
     * @throws NullPointerException if any parameters are {@code null}.
     */
    @Inject
    public MailManager(@ApplicationName String applicationName,
                       @ApplicationHost String applicationHost,
                       @MailProperties Properties properties,
                       MessagingExceptionHandler messagingExceptionHandler) {
        this.applicationName = checkNotNull(applicationName);
        this.applicationHost = checkNotNull(applicationHost);
        this.properties = new Properties(checkNotNull(properties));
        this.messagingExceptionHandler = checkNotNull(messagingExceptionHandler);
    }

    /**
     * Sends an email to the specified recipient.  The email will have the specified subject and specified content.
     *
     * @param recipientEmailAddresses The email address of the recipient.  Not {@code null}.
     * @param subject                 The subject of the email.  Not {@code null}.
     * @param text                    The content of the email.  Not {@code null}.
     * @throws NullPointerException if any parameters are {@code null}.
     */
    public void sendMail(@Nonnull final List<String> recipientEmailAddresses,
                         @Nonnull final String subject,
                         @Nonnull final String text) {
        sendMail(recipientEmailAddresses, subject, text, messagingExceptionHandler);
    }

    /**
     * Sends an email to the specified recipients.  The email will have the specified subject and specified content.
     *
     * @param recipientEmailAddresses The email addresses of the recipients.  Not {@code null}.
     * @param subject                 The subject of the email.  Not {@code null}.
     * @param text                    The content of the email.  Not {@code null}.
     * @param exceptionHandler        An exception handler for handling {@link MessagingException}s.  Not {@code null}.
     * @throws NullPointerException if any parameters are {@code null}.
     */
    public void sendMail(@Nonnull final List<String> recipientEmailAddresses,
                         @Nonnull final String subject,
                         @Nonnull final String text,
                         @Nonnull MessagingExceptionHandler exceptionHandler) {
        try {
            final Session session = createMailSession();
            MimeMessage msg = new MimeMessage(session);
            Optional<String> fromAddress = getPropertyValue(MAIL_SMTP_FROM);
            if (fromAddress.isPresent()) {
                Optional<InternetAddress> fromInternetAddress = toInternetAddress(fromAddress.get());
                if(fromInternetAddress.isPresent()) {
                    msg.setFrom(fromInternetAddress.get());
                }
            }
            Address [] recipients = checkNotNull(recipientEmailAddresses).stream()
                                                 .map(MailManager::toInternetAddress)
                                                 .toArray(Address[]::new);

            msg.setRecipients(Message.RecipientType.TO, recipients);
            msg.setSubject(checkNotNull(subject));
            msg.setText(checkNotNull(text), UTF_8);
            msg.setHeader("Content-Type" , String.format("text/html; charset=\"%s\"" , UTF_8));
            msg.setHeader("Content-Transfer-Encoding" , "quoted-printable" );
            InternetAddress from = getFromAddress();
            msg.setFrom(from);
            Transport.send(msg);
            logger.info(String.format(
                    "Sent email with subject \"%s\" to %s (mail.smtp.host: %s, mail.smtp.port: %s, mail.smtp.auth: %s, mail.smtp.from: %s)" ,
                    subject,
                    Arrays.toString(recipients),
                    session.getProperty(MAIL_SMTP_HOST),
                    session.getProperty(MAIL_SMTP_PORT),
                    session.getProperty(MAIL_SMTP_AUTH),
                    session.getProperty(MAIL_SMTP_FROM))
            );
        } catch (MessagingException e) {
            logger.info("There was a problem sending mail: " + e.getMessage());
            exceptionHandler.handleMessagingException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static Optional<InternetAddress> toInternetAddress(String address) {
        try {
            return Optional.of(new InternetAddress(address));
        } catch (AddressException e) {
            logger.warn("Cannot send email to address: {} (Cause {})", address, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Determines whether or not the property mail.smtp.auth is set to "true".
     *
     * @return {@code true} if the mail.smtp.auth is set to "true", otherwise {@code false}.
     */
    private boolean isSmtpAuth() {
        String value = properties.getProperty(MAIL_SMTP_AUTH);
        return value != null && "true".equals(value);
    }

    private Optional<String> getSmtpUser() {
        return getPropertyValue(MAIL_SMTP_USER);
    }

    private Optional<String> getSmtpPassword() {
        return getPropertyValue(MAIL_SMTP_PASSWORD);
    }

    /**
     * Creates a {@link Session} using the values stored in {@link MailManager#properties}.  If mail.smtp.auth is set
     * to {@code true} then an authentication handler will be set up to authenticate against the given user name
     * (specified by mail.smtp.user) and password (specified by mail.smtp.password).
     *
     * @return A session with the settings in {@link MailManager#properties}.  Not {@code null}.
     */
    private Session createMailSession() {
        final Session session;
        if (isSmtpAuth()) {
            session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(getSmtpUser().get(), getSmtpPassword().get());
                }
            });
        }
        else {
            session = Session.getInstance(properties);
        }
        return session;
    }

    /**
     * Gets the from address as an {@link InternetAddress}.  The personal name will be set to either the value supplied
     * by the {@code mail.smtp.from} property, or by the default value, which is specified by
     * concatenating the default prefix (@link MailManager#DEFAULT_FROM_VALUE_PREFIX) with the application host name.
     *
     * @return The from address.
     * @throws UnsupportedEncodingException
     */
    private InternetAddress getFromAddress() throws UnsupportedEncodingException {
        final String defaultFromValue = DEFAULT_FROM_VALUE_PREFIX + applicationHost;
        String from = getPropertyValue(MAIL_SMTP_FROM, defaultFromValue);
        final String defaultPersonalName = getPropertyValue(WebProtegePropertyName.APPLICATION_NAME, applicationName);
        String personalName = getPropertyValue(MAIL_SMTP_FROM_PERSONALNAME, defaultPersonalName);
        return new InternetAddress(from, personalName, UTF_8);
    }

    private String getPropertyValue(WebProtegePropertyName propertyName, String defaultValue) {
        String value = properties.getProperty(propertyName.getPropertyName());
        if (value != null) {
            return value;
        }
        else {
            return propertyName.getDefaultValue().or(defaultValue);
        }
    }

    /**
     * Gets the property value for the specified property name.
     *
     * @param propertyName The property name. Not {@code null}.
     * @return The property value for the specified name.  If the property value is {@code null} or is not set
     * then {@link com.google.common.base.Optional#absent()} will be returned.
     * @throws NullPointerException if {@code propertyName} is {@code null}.
     */
    private Optional<String> getPropertyValue(String propertyName) {
        return Optional.ofNullable(properties.getProperty(checkNotNull(propertyName)));
    }

    /**
     * Gets the value (or the default value) for the specified property name.
     *
     * @param propertyName The property name.  Not {@code null}.
     * @param defaultValue The default value.  May be {@code null}.
     * @return The value for the property if it exists, or the specified default value if the property does not exist.
     * @throws NullPointerException if any parameters are {@code null}.
     */
    private String getPropertyValue(String propertyName, String defaultValue) {
        String value = properties.getProperty(checkNotNull(propertyName));
        if (value != null) {
            return value;
        }
        else {
            return checkNotNull(defaultValue);
        }
    }
}
