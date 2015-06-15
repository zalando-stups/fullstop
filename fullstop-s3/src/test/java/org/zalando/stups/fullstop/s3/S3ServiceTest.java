package org.zalando.stups.fullstop.s3;

import java.util.List;

import org.junit.Test;

public class S3ServiceTest {

    @Test
    public void testS3ServiceListObject() {
        S3Service service = new S3Service();
        List<String> result = service.listS3Objects("zalando-fullstop", "12094567/eu-central-1/2015/5/22/");
        System.out.println(result.toString());
    }

}
