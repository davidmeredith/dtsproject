package org.dataminx.dts.batch;

import java.util.List;

public interface DtsJobStep {

    List<DtsDataTransferUnit> getDataTransferUnits();

    int getStepId();

    boolean addDataTransferUnit(DtsDataTransferUnit dataTransferUnit);

    int getCurrentTotalFileNum();

    long getCurrentTotalByteSize();

    String getSourceRootFileObjectString();

    String getTargetRootFileObjectString();

    String getJobStepFilename();

    void setJobStepFilename(String filename);

}