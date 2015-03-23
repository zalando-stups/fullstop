/*
 * Copyright 2015 Zalando SE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zalando.stups.fullstop.aws;

import com.amazonaws.regions.Region;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.route53.AmazonRoute53Client;

/**
 * @author  mrandi
 */
public interface ClientProvider {

    AmazonEC2Client getEC2Client(String accountId, Region region);

    AmazonRoute53Client getRoute53Client(String accountId, Region region);
}
