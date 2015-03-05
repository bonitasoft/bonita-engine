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
        if (brokerURL == null) 
            brokerURL = System.getProperty("broker.url");

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
    
   
   public static JMSProducer getInstance(final long messageTimeout, final String brokerURL) {
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
