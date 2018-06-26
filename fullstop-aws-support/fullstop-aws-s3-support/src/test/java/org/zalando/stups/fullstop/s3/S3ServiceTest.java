package org.zalando.stups.fullstop.s3;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Ignore
public class S3ServiceTest {

    private final Logger log = getLogger(getClass());

    private S3Service service;

    @Before
    public void setUp() {
        service = new S3Service(null);
    }

    @Test
    public void testS3ServiceListObject() {
        final List<String> result = service.listCommonPrefixesS3Objects(
                "an-s3-bucket",
                "111222333444/eu-west-1/2016/01/15/");
        log.info("{}", result);
    }

    @Test
    public void testListObjects() throws Exception {
        final List<String> result = service.listS3Objects("an-s3-bucket", "111222333444/eu-west-1/2016/01/15/i-62f41fea-2016-01-15T15:59:54.000Z/");
        log.info("{}", result);
    }
}
