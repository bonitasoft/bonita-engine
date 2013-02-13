package org.bonitasoft.engine.local;

import java.net.URL;
import java.util.Map;

import javax.naming.NamingException;

import org.ow2.carol.util.configuration.ConfigurationException;
import org.ow2.carol.util.configuration.ConfigurationRepository;
import org.springframework.jndi.JndiTemplate;

public class CarolJNDISetup {

	private final JndiTemplate jndiTemplate;
	private final Map<String, Object> jndiMapping;
	
	public CarolJNDISetup(
			final JndiTemplate jndiTemplate,
			final Map<String, Object> jndiMapping,
			final String carolProperties
			) {
		super();
		this.jndiTemplate = jndiTemplate;
		this.jndiMapping = jndiMapping;
		try {
			final URL carolPropertiesURL = this.getClass().getClassLoader().getResource(carolProperties);

			ConfigurationRepository.init(carolPropertiesURL);
		} catch (final ConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void init() throws NamingException {
		for(final Map.Entry<String, Object> addToJndi: this.jndiMapping.entrySet()){
			this.jndiTemplate.bind(addToJndi.getKey(), addToJndi.getValue());
		}
	}
	
	public void clean() throws NamingException {
		for(final Map.Entry<String, Object> addToJndi: this.jndiMapping.entrySet()){
			this.jndiTemplate.unbind(addToJndi.getKey());
		}
	}

}
