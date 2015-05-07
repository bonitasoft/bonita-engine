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

package org.bonitasoft.engine.business.data.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.bonitasoft.engine.bdm.Entity;

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
        aggregationEntities = new ArrayList<>();
        compositionEntities = new ArrayList<>();

    }

    public EntityPojo() {
        aggregationEntities = new ArrayList<>();
        compositionEntities = new ArrayList<>();
    }

    @Override
    public Long getPersistenceId() {
        return persistenceId;
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
