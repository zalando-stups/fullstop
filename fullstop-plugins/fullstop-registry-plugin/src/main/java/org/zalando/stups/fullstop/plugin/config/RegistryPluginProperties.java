package org.zalando.stups.fullstop.plugin.config;

import com.google.common.collect.Lists;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author npiccolotto
 */
@ConfigurationProperties(prefix = "fullstop.plugins.registry")
public class RegistryPluginProperties {


    private List<String> mandatoryApprovals = Lists.newArrayList(
            "CODE_CHANGE",
            "TEST",
            "SPECIFICATION",
            "DEPLOY");


    private List<String> approvalsFromMany = Lists.newArrayList(
            "TEST",
            "CODE_CHANGE",
            "DEPLOY");

    public List<String> getMandatoryApprovals() {
        return mandatoryApprovals;
    }

    public void setMandatoryApprovals(List<String> defaultApprovals) {
        this.mandatoryApprovals = defaultApprovals;
    }


    public List<String> getApprovalsFromMany() {

        return approvalsFromMany;
    }

    public void setApprovalsFromMany(List<String> approvalsFromMany) {
        this.approvalsFromMany = approvalsFromMany;
    }

}
