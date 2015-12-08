package org.zalando.stups.fullstop.s3;

import org.junit.Test;

import java.util.List;

public class S3ServiceTest {

    @Test
    public void testS3ServiceListObject() {
        S3Service service = new S3Service();
        List<String> result = service.listCommonPrefixesS3Objects(
                "zalando-fullstop",
                "12094567/eu-central-1/2015/5/22/");
        System.out.println(result.toString());
    }

}
