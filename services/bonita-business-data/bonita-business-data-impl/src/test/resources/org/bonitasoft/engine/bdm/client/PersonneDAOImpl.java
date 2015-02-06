
package com.test.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.bdm.dao.client.resources.BusinessObjectDeserializer;
import org.bonitasoft.engine.bdm.dao.client.resources.proxy.LazyLoader;
import org.bonitasoft.engine.bdm.dao.client.resources.proxy.Proxyfier;
import org.bonitasoft.engine.session.APISession;

public class PersonneDAOImpl
    implements PersonneDAO
{

    private APISession session;
    private BusinessObjectDeserializer deserializer;
    private Proxyfier proxyfier;

    public PersonneDAOImpl(APISession session) {
        this.session = session;
        this.deserializer = new BusinessObjectDeserializer();
        LazyLoader lazyLoader = new LazyLoader(session);
        this.proxyfier = new Proxyfier(lazyLoader);
    }

    public com.test.model.Personne findByPrenomAndNomAndBirthDate(String prenom, String nom, Date birthDate) {
        try {
            CommandAPI commandApi = org.bonitasoft.engine.api.TenantAPIAccessor.getCommandAPI(session);
            Map<String, Serializable> commandParameters = new HashMap<String, Serializable>();
            commandParameters.put("queryName", "Personne.findByPrenomAndNomAndBirthDate");
            commandParameters.put("returnsList", false);
            commandParameters.put("returnType", "com.test.model.Personne");
            Map<String, Serializable> queryParameters = new HashMap<String, Serializable>();
            queryParameters.put("prenom", prenom);
            queryParameters.put("nom", nom);
            queryParameters.put("birthDate", birthDate);
            commandParameters.put("queryParameters", ((Serializable) queryParameters));
            return proxyfier.proxify(deserializer.deserialize(((byte[]) commandApi.execute("executeBDMQuery", commandParameters)), com.test.model.Personne.class));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public List<com.test.model.Personne> findByPrenom(String prenom, int startIndex, int maxResults) {
        try {
            CommandAPI commandApi = org.bonitasoft.engine.api.TenantAPIAccessor.getCommandAPI(session);
            Map<String, Serializable> commandParameters = new HashMap<String, Serializable>();
            commandParameters.put("queryName", "Personne.findByPrenom");
            commandParameters.put("returnsList", true);
            commandParameters.put("returnType", "com.test.model.Personne");
            commandParameters.put("startIndex", startIndex);
            commandParameters.put("maxResults", maxResults);
            Map<String, Serializable> queryParameters = new HashMap<String, Serializable>();
            queryParameters.put("prenom", prenom);
            commandParameters.put("queryParameters", ((Serializable) queryParameters));
            return proxyfier.proxify(deserializer.deserializeList(((byte[]) commandApi.execute("executeBDMQuery", commandParameters)), com.test.model.Personne.class));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public List<com.test.model.Personne> findByNom(String nom, int startIndex, int maxResults) {
        try {
            CommandAPI commandApi = org.bonitasoft.engine.api.TenantAPIAccessor.getCommandAPI(session);
            Map<String, Serializable> commandParameters = new HashMap<String, Serializable>();
            commandParameters.put("queryName", "Personne.findByNom");
            commandParameters.put("returnsList", true);
            commandParameters.put("returnType", "com.test.model.Personne");
            commandParameters.put("startIndex", startIndex);
            commandParameters.put("maxResults", maxResults);
            Map<String, Serializable> queryParameters = new HashMap<String, Serializable>();
            queryParameters.put("nom", nom);
            commandParameters.put("queryParameters", ((Serializable) queryParameters));
            return proxyfier.proxify(deserializer.deserializeList(((byte[]) commandApi.execute("executeBDMQuery", commandParameters)), com.test.model.Personne.class));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public List<com.test.model.Personne> findByBirthDate(Date birthDate, int startIndex, int maxResults) {
        try {
            CommandAPI commandApi = org.bonitasoft.engine.api.TenantAPIAccessor.getCommandAPI(session);
            Map<String, Serializable> commandParameters = new HashMap<String, Serializable>();
            commandParameters.put("queryName", "Personne.findByBirthDate");
            commandParameters.put("returnsList", true);
            commandParameters.put("returnType", "com.test.model.Personne");
            commandParameters.put("startIndex", startIndex);
            commandParameters.put("maxResults", maxResults);
            Map<String, Serializable> queryParameters = new HashMap<String, Serializable>();
            queryParameters.put("birthDate", birthDate);
            commandParameters.put("queryParameters", ((Serializable) queryParameters));
            return proxyfier.proxify(deserializer.deserializeList(((byte[]) commandApi.execute("executeBDMQuery", commandParameters)), com.test.model.Personne.class));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public List<com.test.model.Personne> find(int startIndex, int maxResults) {
        try {
            CommandAPI commandApi = org.bonitasoft.engine.api.TenantAPIAccessor.getCommandAPI(session);
            Map<String, Serializable> commandParameters = new HashMap<String, Serializable>();
            commandParameters.put("queryName", "Personne.find");
            commandParameters.put("returnsList", true);
            commandParameters.put("returnType", "com.test.model.Personne");
            commandParameters.put("startIndex", startIndex);
            commandParameters.put("maxResults", maxResults);
            Map<String, Serializable> queryParameters = new HashMap<String, Serializable>();
            commandParameters.put("queryParameters", ((Serializable) queryParameters));
            return proxyfier.proxify(deserializer.deserializeList(((byte[]) commandApi.execute("executeBDMQuery", commandParameters)), com.test.model.Personne.class));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public com.test.model.Personne newInstance(String prenom, String nom, Date birthDate, Adresse adressePersonne) {
        if (prenom == null) {
            throw new IllegalArgumentException("prenom cannot be null");
        }
        if (nom == null) {
            throw new IllegalArgumentException("nom cannot be null");
        }
        if (birthDate == null) {
            throw new IllegalArgumentException("birthDate cannot be null");
        }
        if (adressePersonne == null) {
            throw new IllegalArgumentException("adressePersonne cannot be null");
        }
        com.test.model.Personne instance = new com.test.model.Personne();
        instance.setPrenom(prenom);
        instance.setNom(nom);
        instance.setBirthDate(birthDate);
        instance.setAdressePersonne(adressePersonne);
        return instance;
    }

}
