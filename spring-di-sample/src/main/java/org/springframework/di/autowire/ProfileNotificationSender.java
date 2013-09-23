/**
 * 
 */
package org.springframework.di.autowire;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.di.MessagingException;
import org.springframework.di.ProfileInfo;
import org.springframework.di.ProfileNotificationInterface;
import org.springframework.stereotype.Component;

/**
 * @author corneil
 */
@Component("notificationSender")
public class ProfileNotificationSender implements ProfileNotificationInterface {
    private Destination destination;

    @Autowired
    public ProfileNotificationSender(ConnectionFactory factory, Destination destination) {
        super();
        this.factory = factory;
        this.destination = destination;
    }

    private ConnectionFactory factory;

    public void notifyCreate(ProfileInfo profile) throws MessagingException {
        sendNotify(profile, "CREATE");
    }

    public void notifyDelete(ProfileInfo profile) throws MessagingException {
        sendNotify(profile, "DELETE");
    }

    public void notifyUpdate(ProfileInfo profile) throws MessagingException {
        sendNotify(profile, "UPDATE");
    }

    private void sendNotify(ProfileInfo profile, String notificationType) throws MessagingException {
        Connection connection;
        try {
            connection = factory.createConnection();
        } catch (JMSException e) {
            throw new MessagingException("Exception connecting:" + e.getMessage(), e);
        }
        Session session;
        try {
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException e) {
            throw new MessagingException("Exception creating session:" + e.getMessage(), e);
        }
        MessageProducer producer;
        try {
            producer = session.createProducer(destination);
        } catch (JMSException e) {
            throw new MessagingException("Exception creating message producer:" + e.getMessage(), e);
        }
        ObjectMessage message;
        try {
            message = session.createObjectMessage(profile);
        } catch (JMSException e) {
            throw new MessagingException("Exception creating message:" + e.getMessage(), e);
        }
        try {
            message.setStringProperty("EVENT.TYPE", notificationType);
        } catch (JMSException e) {
            throw new MessagingException("Exception setting message properties:" + e.getMessage(), e);
        }
        try {
            producer.send(message);
        } catch (JMSException e) {
            throw new MessagingException("Exception sending message:" + e.getMessage(), e);
        }
        try {
            session.close();
        } catch (JMSException e) {
            throw new MessagingException("Exception closing session:" + e.getMessage(), e);
        }
        try {
            connection.close();
        } catch (JMSException e) {
            throw new MessagingException("Exception closing connection:" + e.getMessage(), e);
        }
    }
}