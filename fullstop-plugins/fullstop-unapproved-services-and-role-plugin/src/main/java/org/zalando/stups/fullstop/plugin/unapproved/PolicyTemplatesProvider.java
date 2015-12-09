package org.zalando.stups.fullstop.plugin.unapproved;

import java.util.List;

/**
 * Created by mrandi.
 */
public interface PolicyTemplatesProvider {
    List<String> getPolicyTemplateNames();

    String getPolicyTemplate(String roleName);
}
