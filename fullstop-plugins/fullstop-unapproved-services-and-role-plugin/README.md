##Unapproves services and role Plugin

###Aim of plugin
Find out if someone use an unapproved service
and if someone change roles that are from policy unchangeable.

###Reacts on

```
EVENT SOURCE = "iam.amazonaws.com"
EVENT NAME = see the configuration section
POLICY TEMPLATE = the name of the files contained in s3 bucket will be used to match the role (the content will be than match for equality)
```

###Configuration

You can modify here the plugin configuration: [application.yml](../../fullstop/src/main/resources/config/application.yml)

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