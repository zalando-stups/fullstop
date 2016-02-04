package org.zalando.stups.fullstop.whitelist;

import org.zalando.stups.fullstop.violation.entity.ViolationEntity;

public class WhitelistRules {

    public boolean execute(ViolationEntity entity) {

        final boolean result = false;

        if (entity != null){
            //doSomething();
        }

        return result;
    }

}
