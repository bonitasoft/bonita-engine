/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.business.data.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;


import com.bonitasoft.engine.bdm.Entity;

public class EntityPojo implements Entity {

        private static final long serialVersionUID = 1L;
        private String name;
        private Boolean bool;
        private Date date;
        private List<Long> numbers;

        @OneToOne(cascade = CascadeType.MERGE)
        private Entity aggregationEntity;

        @OneToOne(cascade = CascadeType.ALL)
        private Entity compositionEntity;

        @OneToOne(cascade = CascadeType.ALL)
        private Entity nullChildEntity;

        @OneToMany(cascade = CascadeType.MERGE)
        private List<Entity> aggregationEntities;

        @OneToMany(cascade = CascadeType.ALL)
        private List<Entity> compositionEntities;

        private Long persistenceId;

        public EntityPojo(final Long persistenceId) {
            this.persistenceId = persistenceId;
            aggregationEntities = new ArrayList<Entity>();
            compositionEntities = new ArrayList<Entity>();

        }

        public EntityPojo() {
            aggregationEntities = new ArrayList<Entity>();
            compositionEntities = new ArrayList<Entity>();
        }

        @Override
        public Long getPersistenceId() {
            return persistenceId;
        }

        public void setPersistenceId(final Long persistenceId) {
            this.persistenceId = persistenceId;
        }

        @Override
        public Long getPersistenceVersion() {
            return 2L;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(final Date date) {
            this.date = date;
        }

        public Boolean getBool() {
            return bool;
        }

        public void setBool(final Boolean bool) {
            this.bool = bool;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public List<Long> getNumbers() {
            return numbers;
        }

        public void setNumbers(final List<Long> numbers) {
            this.numbers = numbers;
        }

        public Entity getCompositionEntity() {
            return compositionEntity;
        }

        public void setCompositionEntity(final Entity compositionEntity) {
            this.compositionEntity = compositionEntity;
        }

        public Entity getAggregationEntity() {
            return aggregationEntity;
        }

        public void setAggregationEntity(final Entity aggregationEntity) {
            this.aggregationEntity = aggregationEntity;
        }

        public Entity getNullChildEntity() {
            return null;
        }

        public void setNullChildEntity(final Entity nullChildEntity) {
            this.nullChildEntity = nullChildEntity;
        }

        public List<Entity> getAggregationEntities() {
            return aggregationEntities;
        }

        public void setAggregationEntities(List<Entity> aggregationEntities) {
            this.aggregationEntities = aggregationEntities;
        }

        public List<Entity> getCompositionEntities() {
            return compositionEntities;
        }

        public void setCompositionEntities(List<Entity> compositionEntities) {
            this.compositionEntities = compositionEntities;
        }
    }