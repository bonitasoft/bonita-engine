package org.bonitasoft.engine.synchro.jms;

import java.io.Serializable;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class JMSProducer {

	public static final String TOPIC_CONNECTION_FACTORY = "bonita/jms/TopicConnectionFactory";
	public static final String TOPIC_NAME = "synchroServiceTopic";

	private final MessageProducer producer;
	private final Session session;

	private final long timeout;

	public JMSProducer(final long timeout) throws NamingException, JMSException {
		final InitialContext context = new InitialContext();
		final TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) context.lookup(TOPIC_CONNECTION_FACTORY);
		final Topic topic = (Topic) context.lookup(TOPIC_NAME);
		
		final TopicConnection topicConnection = topicConnectionFactory.createTopicConnection();
		topicConnection.start();

		this.session = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		this.producer = session.createProducer(topic);

		this.timeout = timeout;
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					topicConnection.stop();
					topicConnection.close();
					producer.close();
					session.close();
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void sendMessage(final Map<String, Serializable> properties, final long body) throws JMSException {
		final MapMessage message = session.createMapMessage();

		for (final Map.Entry<String, Serializable> property : properties.entrySet()) {
			message.setObjectProperty(property.getKey(), property.getValue());;
		}
		message.setLong("body-id", body);
		message.setJMSExpiration(System.currentTimeMillis() + timeout);

		producer.send(message);
		//System.err.println("Message sent= " + message);
	}
}
