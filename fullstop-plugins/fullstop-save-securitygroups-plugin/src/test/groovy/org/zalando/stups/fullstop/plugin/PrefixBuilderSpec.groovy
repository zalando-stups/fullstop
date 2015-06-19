package org.zalando.stups.fullstop.plugin

import org.joda.time.DateTime

import spock.lang.Specification
import spock.lang.Title

@Title("Unit test for PrefixBuilder")
class PrefixBuilderSpec extends Specification {

    def "An prefix for valid accountId and regionName"() {
        given:
        String accountId = "123456789"
        String region = "eu-west-1"
        DateTime dateTime = new DateTime();

        when: "the client invokes the build-method"
        String prefixBuild = PrefixBuilder.build(accountId, region, dateTime)

        then: "the prefix should contain the accountId in the prefix"
        prefixBuild.contains('123456789')
    }
}
