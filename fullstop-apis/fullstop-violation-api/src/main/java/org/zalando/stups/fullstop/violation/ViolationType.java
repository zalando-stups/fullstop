package org.zalando.stups.fullstop.violation;

/**
 * Created by mrandi.
 */
public class ViolationType {

    public static final String EC2_WITH_KEYPAIR = "EC2_WITH_KEYPAIR";

    public static final String WRONG_REGION = "WRONG_REGION";

    public static final String MODIFIED_ROLE_OR_SERVICE = "MODIFIED_ROLE_OR_SERVICE";

    public static final String EC2_RUN_IN_PUBLIC_SUBNET = "EC2_RUN_IN_PUBLIC_SUBNET";

    public static final String ACTIVE_KEY_TOO_OLD = "ACTIVE_KEY_TOO_OLD";

    public static final String PASSWORD_USED = "PASSWORD_USED";

    public static final String WRONG_AMI = "WRONG_AMI";

    public static final String MISSING_USER_DATA = "MISSING_USER_DATA";

    public static final String MISSING_SOURCE_IN_USER_DATA = "MISSING_SOURCE_IN_USER_DATA";

    public static final String EC2_WITH_A_SNAPSHOT_IMAGE = "EC2_WITH_A_SNAPSHOT_IMAGE";

    public static final String SCM_URL_IS_MISSING_IN_KIO = "SCM_URL_IS_MISSING_IN_KIO";

    public static final String SCM_URL_IS_MISSING_IN_SCM_SOURCE_JSON = "SCM_URL_IS_MISSING_IN_SCM_SOURCE_JSON";

    public static final String SCM_URL_NOT_MATCH_WITH_KIO = "SCM_URL_NOT_MATCH_WITH_KIO";

    public static final String IMAGE_IN_PIERONE_NOT_FOUND = "IMAGE_IN_PIERONE_NOT_FOUND";

    public static final String SCM_SOURCE_JSON_MISSING = "SCM_SOURCE_JSON_MISSING";

    public static final String APPLICATION_VERSION_DOES_NOT_HAVE_A_VALID_ARTIFACT = "APPLICATION_VERSION_DOES_NOT_HAVE_A_VALID_ARTIFACT";

    public static final String APPLICATION_NOT_PRESENT_IN_KIO = "APPLICATION_NOT_PRESENT_IN_KIO";

    public static final String APPLICATION_VERSION_NOT_PRESENT_IN_KIO = "APPLICATION_VERSION_NOT_PRESENT_IN_KIO";

    public static final String UNSECURED_PUBLIC_ENDPOINT = "UNSECURED_PUBLIC_ENDPOINT";

    public static final String SPEC_URL_IS_MISSING_IN_KIO = "SPEC_URL_IS_MISSING_IN_KIO";

    public static final String MISSING_APPLICATION_ID_IN_USER_DATA = "MISSING_APPLICATION_ID_IN_USER_DATA";

    public static final String MISSING_APPLICATION_VERSION_IN_USER_DATA = "MISSING_APPLICATION_VERSION_IN_USER_DATA";

    public static final String OUTDATED_TAUPAGE = "OUTDATED_TAUPAGE";

    public static final String UNSECURED_ROOT_USER = "UNSECURED_ROOT_USER";

    public static final String ILLEGAL_SCM_REPOSITORY = "ILLEGAL_SCM_REPOSITORY";

    public static final String CROSS_ACCOUNT_ROLE = "CROSS_ACCOUNT_ROLE";

    public static final String ARTIFACT_BUILT_FROM_DIRTY_REPOSITORY = "ARTIFACT_BUILT_FROM_DIRTY_REPOSITORY";

}
