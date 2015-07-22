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
package org.zalando.stups.fullstop.violation;

/**
 * Created by mrandi.
 */
public class ViolationType {

    public static final String EC2_WITH_SSH_KEY = "EC2_WITH_SSH_KEY";
    public static final String WRONG_REGION = "WRONG_REGION";
    public static final String MODIFIED_ROLE_OR_SERVICE = "MODIFIED_ROLE_OR_SERVICE";
    public static final String EC2_WITHOUT_ROUTING_INFORMATION = "EC2_WITHOUT_ROUTING_INFORMATION";
    public static final String EC2_RUN_IN_PUBLIC_SUBNET = "EC2_RUN_IN_PUBLIC_SUBNET";
    public static final String ACTIVE_KEY_TO_OLD = "ACTIVE_KEY_TO_OLD";
    public static final String PASSWORD_USED = "PASSWORD_USED";
    public static final String MISSING_USER_DATA = "MISSING_USER_DATA";
    public static final String WRONG_AMI = "WRONG_AMI";
    public static final String SECURITY_GROUPS_PORT_NOT_ALLOWED = "SECURITY_GROUPS_PORT_NOT_ALLOWED";
    public static final String MISSING_SOURCE_IN_USER_DATA = "MISSING_SOURCE_IN_USER_DATA";
    public static final String EC2_WITH_A_SNAPSHOT_IMAGE = "EC2_WITH_A_SNAPSHOT_IMAGE";
    public static final String SCM_URL_IS_MISSING_IN_KIO = "SCM_URL_IS_MISSING_IN_KIO";
    public static final String SCM_URL_IS_MISSING_IN_SCM_SOURCE_JSON = "SCM_URL_IS_MISSING_IN_SCM_SOURCE_JSON";
    public static final String SCM_URL_NOT_MATCH_WITH_KIO = "SCM_URL_NOT_MATCH_WITH_KIO";
    public static final String VERSION_APPROVAL_NOT_ENOUGH = "VERSION_APPROVAL_NOT_ENOUGH";
    public static final String MISSING_VERSION_APPROVAL = "MISSING_VERSION_APPROVAL";
}
