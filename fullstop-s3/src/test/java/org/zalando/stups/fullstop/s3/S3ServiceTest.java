/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.s3;

import org.junit.Test;

import java.util.List;

public class S3ServiceTest {

    @Test
    public void testS3ServiceListObject() {
        S3Service service = new S3Service();
        List<String> result = service.listS3Objects("zalando-fullstop", "12094567/eu-central-1/2015/5/22/");
        System.out.println(result.toString());
    }

}
