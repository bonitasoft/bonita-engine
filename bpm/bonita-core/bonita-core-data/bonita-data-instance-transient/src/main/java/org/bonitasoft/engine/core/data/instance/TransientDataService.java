package org.bonitasoft.engine.core.data.instance;

import java.util.List;

import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

public interface TransientDataService {

    /**
     * @param dataNames
     * @param containerId
     * @param containerType
     * @return
     * @throws SDataInstanceException
     */
    List<SDataInstance> getDataInstances(List<String> dataNames, long containerId, String containerType) throws SDataInstanceException;

    /**
     * @param dataInstance
     * @throws SDataInstanceException
     */
    void createDataInstance(SDataInstance dataInstance) throws SDataInstanceException;

    /**
     * @param dataInstance
     * @param descriptor
     * @throws SDataInstanceException
     */
    void updateDataInstance(SDataInstance dataInstance, EntityUpdateDescriptor descriptor) throws SDataInstanceException;

    /**
     * @param dataInstance
     * @throws SDataInstanceException
     */
    void deleteDataInstance(SDataInstance dataInstance) throws SDataInstanceException;

    /**
     * @param dataInstanceId
     * @return
     * @throws SDataInstanceException
     */
    SDataInstance getDataInstance(long dataInstanceId) throws SDataInstanceException;

    /**
     * @param dataName
     * @param containerId
     * @param containerType
     * @return
     * @throws SDataInstanceException
     */
    SDataInstance getDataInstance(String dataName, long containerId, String containerType) throws SDataInstanceException;

    /**
     * @param containerId
     * @param containerType
     * @param fromIndex
     * @param numberOfResults
     * @return
     * @throws SDataInstanceException
     */
    List<SDataInstance> getDataInstances(long containerId, String containerType, int fromIndex, int numberOfResults) throws SDataInstanceException;

    /**
     * @param dataInstanceIds
     * @return
     */
    List<SDataInstance> getDataInstances(List<Long> dataInstanceIds);

}
