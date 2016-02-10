/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.synchro;

import java.io.Serializable;
import java.util.Map;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;

public class JMSProducer {

    public static final String TOPIC_CONNECTION_FACTORY = "bonita/jms/TopicConnectionFactory";

    public static final String TOPIC_NAME = "synchroServiceTopic";

    private final TopicConnectionFactory topicConnectionFactory;

    private final TopicConnection topicConnection;

    private final Session session;

    private final Topic topic;

    private final MessageProducer producer;

    private final long timeout;

    private static JMSProducer jmsProducer;

    private JMSProducer(final long timeout, String brokerURL) throws JMSException {

        // if brokerURL not defined before, get property
        if (brokerURL == null) {
            brokerURL = System.getProperty("broker.url");
        }

        // Create a ConnectionFactory
        topicConnectionFactory = new ActiveMQConnectionFactory(brokerURL);
        topicConnection = topicConnectionFactory.createTopicConnection();
        session = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        topic = session.createTopic(TOPIC_NAME);

        topicConnection.start();

        producer = session.createProducer(topic);

        this.timeout = timeout;
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                try {
                    topicConnection.stop();
                    producer.close();
                    session.close();
                    topicConnection.close();

                } catch (final JMSException e) {
                    System.err.println("Cannot stop the synchro service, probably already stopped?");
                }
            }
        });

    }

    public synchronized static JMSProducer getInstance(final long messageTimeout, final String brokerURL) {
        // TODO : add map by tenant
        if (jmsProducer == null) {
            try {
                jmsProducer = new JMSProducer(messageTimeout, brokerURL);
            } catch (final Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return jmsProducer;
    }

    public synchronized static void resetInstance() {
        if (jmsProducer != null) {
            if (jmsProducer.topicConnection != null) {
                try {
                    jmsProducer.topicConnection.stop();
                } catch (JMSException ex) {
                    ex.printStackTrace();
                }
            }
            if (jmsProducer.producer != null) {
                try {
                    jmsProducer.producer.close();
                } catch (JMSException ex) {
                    ex.printStackTrace();
                }
            }
            if (jmsProducer.session != null) {
                try {
                    jmsProducer.session.close();
                } catch (JMSException ex) {
                    ex.printStackTrace();
                }
            }
            if (jmsProducer.topicConnection != null) {
                try {
                    jmsProducer.topicConnection.close();
                } catch (JMSException ex) {
                    ex.printStackTrace();
                }
            }
        }
        jmsProducer = null;
    }

    public void sendMessage(final Map<String, Serializable> properties, final String bodyId) throws JMSException {
        final MapMessage message = session.createMapMessage();

        for (final Map.Entry<String, Serializable> property : properties.entrySet()) {
            message.setObjectProperty(property.getKey(), property.getValue());
        }
        message.setString("body-id", bodyId);
        message.setJMSExpiration(System.currentTimeMillis() + timeout);

        producer.send(message);
    }
}
