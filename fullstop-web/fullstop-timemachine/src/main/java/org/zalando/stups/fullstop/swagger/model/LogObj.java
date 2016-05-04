package org.zalando.stups.fullstop.swagger.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.zalando.stups.fullstop.s3.LogType;

import java.util.Date;

@ApiModel(description = "")
public class LogObj {

    private LogType logType = null;

    private String instanceId = null;

    private String accountId = null;

    private String region = null;

    private Date instanceBootTime = null;

    private String logData = null;

    /**
     */
    @ApiModelProperty(value = "")
    public LogType getLogType() {
        return logType;
    }

    public void setLogType(final LogType logType) {
        this.logType = logType;
    }

    /**
     */
    @ApiModelProperty(value = "")
    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(final String instanceId) {
        this.instanceId = instanceId;
    }

    /**
     */
    @ApiModelProperty(value = "")
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(final String accountId) {
        this.accountId = accountId;
    }

    /**
     */
    @ApiModelProperty(value = "")
    public String getRegion() {
        return region;
    }

    public void setRegion(final String region) {
        this.region = region;
    }

    /**
     */
    @ApiModelProperty(value = "")
    public Date getInstanceBootTime() {
        return instanceBootTime;
    }

    public void setInstanceBootTime(final Date instanceBootTime) {
        this.instanceBootTime = instanceBootTime;
    }

    /**
     */
    @ApiModelProperty(value = "")
    public String getLogData() {
        return logData;
    }

    public void setLogData(final String logData) {
        this.logData = logData;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("class LogObj {\n");

        sb.append("  logType: ").append(logType).append("\n");
        sb.append("  instanceId: ").append(instanceId).append("\n");
        sb.append("  accountId: ").append(accountId).append("\n");
        sb.append("  region: ").append(region).append("\n");
        sb.append("  instanceBootTime: ").append(instanceBootTime).append("\n");
        sb.append("  logData: ").append(logData).append("\n");
        sb.append("}\n");
        return sb.toString();
    }
}
