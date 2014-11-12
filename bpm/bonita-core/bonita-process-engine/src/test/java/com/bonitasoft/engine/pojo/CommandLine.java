package com.bonitasoft.engine.pojo;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.bdm.lazy.LazyLoaded;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class CommandLine implements Entity {

    private static final long serialVersionUID = -2128636663862820028L;

    @Id
    @GeneratedValue
    private final Long persistenceId;

    @Version
    private final Long persistenceVersion;

    @JsonIgnore
    private final Product product;

    private final long quantity;

    public CommandLine(final Long persistenceId, final Long persistenceVersion, final Product product, final long quantity) {
        super();
        this.persistenceId = persistenceId;
        this.persistenceVersion = persistenceVersion;
        this.product = product;
        this.quantity = quantity;
    }

    @Override
    public Long getPersistenceId() {
        return persistenceId;
    }

    @Override
    public Long getPersistenceVersion() {
        return persistenceVersion;
    }

    @LazyLoaded
    public Product getProduct() {
        return product;
    }

    public long getQuantity() {
        return quantity;
    }

}
