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
package org.bonitasoft.engine.core.process.instance.model.archive.impl;

import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.TransitionState;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.archive.SATransitionInstance;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Hongwen Zang
 * @author Matthieu Chaffotte
 */
public class SATransitionInstanceImpl extends SAFlowElementInstanceImpl implements SATransitionInstance {

    private static final long serialVersionUID = 3514739741984232312L;

    private long source;

    private long target;

    private long sourceObjectId;

    private long archiveDate;

    private TransitionState state;

    private String description;

    private boolean terminal;

    private boolean stable;

    private SStateCategory stateCategory;

    public SATransitionInstanceImpl() {
        super();
    }

    public SATransitionInstanceImpl(final STransitionDefinition sTransitionDefinition, final SFlowNodeInstance sourceFlowNode) {
        super();
        setName(sTransitionDefinition.getName());
        setRootContainerId(sourceFlowNode.getRootContainerId());
        setParentContainerId(sourceFlowNode.getParentContainerId());
        setLogicalGroup(0, sourceFlowNode.getLogicalGroup(0));
        setLogicalGroup(1, sourceFlowNode.getLogicalGroup(1));
        setLogicalGroup(2, sourceFlowNode.getLogicalGroup(2));
        setLogicalGroup(3, sourceFlowNode.getLogicalGroup(3));
        source = sourceFlowNode.getId();
    }

    public void setSource(final long source) {
        this.source = source;
    }

    public void setTarget(final long target) {
        this.target = target;
    }

    public void setState(final TransitionState state) {
        this.state = state;
    }

    @Override
    public TransitionState getState() {
        return state;
    }

    @Override
    public long getSource() {
        return source;
    }

    @Override
    public long getTarget() {
        return target;
    }

    @Override
    public long getSourceObjectId() {
        return sourceObjectId;
    }

    @Override
    public void setSourceObjectId(final long sourceObjectId) {
        this.sourceObjectId = sourceObjectId;
    }

    @Override
    public long getArchiveDate() {
        return archiveDate;
    }

    @Override
    public void setArchiveDate(final long archiveDate) {
        this.archiveDate = archiveDate;
    }

    @Override
    public String getDiscriminator() {
        return SATransitionInstance.class.getName();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public boolean isTerminal() {
        return terminal;
    }

    public void setTerminal(final boolean terminal) {
        this.terminal = terminal;
    }

    public boolean isStable() {
        return stable;
    }

    public void setStable(final boolean stable) {
        this.stable = stable;
    }

    public SStateCategory getStateCategory() {
        return stateCategory;
    }

    public void setStateCategory(final SStateCategory stateCategory) {
        this.stateCategory = stateCategory;
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        // no source class, transitions are only archived
        return SATransitionInstance.class;
    }

}
