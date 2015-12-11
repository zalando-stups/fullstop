package org.zalando.stups.fullstop.whitelist;

import java.util.Collection;
import java.util.Iterator;

import org.drools.template.DataProvider;
import org.zalando.stups.fullstop.rule.entity.RuleEntity;

public class RuleDataProvider implements DataProvider {

    private final Iterator<RuleEntity> iterator;

    public RuleDataProvider(final Collection<RuleEntity> rules) {
        iterator = rules.iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public String[] next() {

        final RuleEntity next = iterator.next();
        return new String[] {next.getRuleName(), next.getAccountId(), next.getViolationTypeEntity().getId()};
    }
}
