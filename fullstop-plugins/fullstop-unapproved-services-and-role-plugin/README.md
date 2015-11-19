##Unapproved services and roles Plugin

###Aim of plugin
Find out if someone uses an unapproved service
and if someone changes roles that must not be changed. The plugin will compare the roles defined in IAM with a json file in `${FULLSTOP_UNAPPROVED_SERVICES_AND_ROLE_BUCKET_NAME}`.

###Reacts on

```
EVENT SOURCE = "iam.amazonaws.com"
EVENT NAME = see the configuration section
POLICY NAME = the name of the files contained in s3 bucket will be used to match the role (the content will then be checked for equality)
```

###Configuration

You can modify the plugin configuration here: [application.yml](../../fullstop/src/main/resources/config/application.yml)

```yml
fullstop:
    plugins:
        unapprovedServicesAndRole:
                    bucketName: ${FULLSTOP_UNAPPROVED_SERVICES_AND_ROLE_BUCKET_NAME}
                    prefix: ${FULLSTOP_UNAPPROVED_SERVICES_AND_ROLE_PREFIX}
                    # iam event names that activates the plugin
                    eventNames:
                      - 'CreateRole'
                      - 'DeleteRole'
                      - 'AttachRolePolicy'
                      - 'UpdateAssumeRolePolicy'
                      - 'PutRolePolicy'
```
If the list is empty, default will be used: 
* 'CreateRole'
* 'DeleteRole'
* 'AttachRolePolicy'
* 'UpdateAssumeRolePolicy'
* 'PutRolePolicy'

###Editing roles
If you want to add or remove a role without creating a violation, you have to add/remove the role in the template as well. The template a be found in the S3 bucket `${FULLSTOP_UNAPPROVED_SERVICES_AND_ROLE_BUCKET_NAME}`
