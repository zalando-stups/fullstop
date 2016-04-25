[![Hex.pm](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Build Status](https://travis-ci.org/zalando-stups/fullstop.svg?branch=master)](https://travis-ci.org/zalando-stups/fullstop)
[![codecov.io](https://codecov.io/github/zalando-stups/fullstop/coverage.svg?branch=master)](https://codecov.io/github/zalando-stups/fullstop?branch=master)
[![swagger-editor](https://img.shields.io/badge/swagger-editor-brightgreen.svg)](http://editor.swagger.io/#/?import=https://raw.githubusercontent.com/zalando-stups/fullstop/master/fullstop-api.yaml#/)
[![Issues in progress](https://badge.waffle.io/zalando-stups/fullstop.svg?label=In%20Progress&title=In%20Progress)](http://waffle.io/zalando-stups/fullstop)
[![Join the chat at https://gitter.im/zalando-stups/fullstop](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/zalando-stups/fullstop?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

![swagger-validator](http://online.swagger.io/validator/?url=https://raw.githubusercontent.com/zalando-stups/fullstop/master/fullstop-api.yaml)

#Fullstop - Audit reporting


###Fullstop AWS overview
![Fullstop](images/fullstop.png)
###Fullstop Architecture overview
![Fullstop Architecture](images/fullstop-architecture.png)

Aim of the project is to enrich CloudTrail log events.

In our scenario we have multiple AWS accounts that need to be handled.

Each of this account has CloudTrail activated and is configured to write
in a bucket that resides in the account where also fullstop is running.
(Right now in AWS it's not possible to read CloudTrail logs from a different account)

Fullstop will then process events collected from CloudTrail.

To enrich CloudTrail log events with information that comes
from other systems than AWS, we should only configure fullstop to do so.

Fullstop can even call the AWS API of a different account, by using a [cross-account role](http://docs.aws.amazon.com/IAM/latest/UserGuide/roles-walkthrough-crossacct.html).
The account that is running fullstop should therefore be trusted
by all other accounts in order to perform this operations.

![Fullstop-Cross-Account-Role](images/fullstop-cross-account-role.png)

##Plugins

* [fullstop-ami-plugin](fullstop-plugins/fullstop-ami-plugin)
* [fullstop-application-lifecycle-plugin] (fullstop-plugins/fullstop-application-lifecycle-plugin)
* [fullstop-application-masterdata-plugin] (fullstop-plugins/fullstop-application-masterdata-plugin)
* [fullstop-count-events-plugin] (fullstop-plugins/fullstop-count-events-plugin)
* [fullstop-hello-event-plugin](fullstop-plugins/fullstop-hello-event-plugin)
* [fullstop-keypair-plugin](fullstop-plugins/fullstop-keypair-plugin)
* [fullstop-region-plugin](fullstop-plugins/fullstop-region-plugin)
* [fullstop-registry-plugin](fullstop-plugins/fullstop-registry-plugin)
* [fullstop-save-security-group-plugin](fullstop-plugins/fullstop-save-security-group-plugin)
* [fullstop-scm-repository-plugin] (fullstop-plugins/fullstop-scm-repository-plugin)
* [fullstop-snapshot-source-plugin] (fullstop-plugins/fullstop-snapshot-source-plugin)
* [fullstop-subnet-plugin](fullstop-plugins/fullstop-subnet-plugin)
* [fullstop-taupage-yaml-plugin] (fullstop-plugins/fullstop-taupage-yaml-plugin)
* [fullstop-unapproved-services-and-role-plugin] (fullstop-plugins/fullstop-unapproved-services-and-role-plugin)

##Jobs

In addition to listening on cloudtrail events, Fullstop. runs jobs as well. A list of jobs can be
found [here](fullstop-jobs).

##Application lifecycle
Fullstop. also keeps record of your application lifecycles. for more information, see the
[fullstop-application-lifecycle-plugin] (fullstop-plugins/fullstop-application-lifecycle-plugin) and the
[Application lifecycle API](fullstop-web/fullstop-lifecycle-api)


##Configuration

This environment variables should be set:

    FULLSTOP_LOGS
    FULLSTOP_SQS_URL
    FULLSTOP_SQS_REGION
    FULLSTOP_S3_REGION
    FULLSTOP_TAUPAGE_OWNERS
    FULLSTOP_TAUPAGE_NAME_PREFIX
    FULLSTOP_S3_BUCKET
    FULLSTOP_KIO_URL
    FULLSTOP_PIERONE_URL
    FULLSTOP_TEAM_SERVICE_URL
    FULLSTOP_KONTROLLETTI_URL
    DATABASE_URL
    DATABASE_USER
    DATABASE_PASSWORD
    DATABASE_DRIVER
    INSTANCE_LOGS_S3_BUCKET
    ACCESS_TOKEN_URI
    CREDENTIALS_DIR
    TOKEN_INFO_URI
    FULLSTOP_UNAPPROVED_SERVICES_AND_ROLE_BUCKET_NAME
    FULLSTOP_UNAPPROVED_SERVICES_AND_ROLE_PREFIX

Example:

    $ export FULLSTOP_LOGS=/fullstop_logs_dir
    $ export FULLSTOP_SQS_URL=https://sqs.eu-central-1.amazonaws.com/ACCOUNT_ID/fullstop
    $ export FULLSTOP_SQS_REGION=eu-central-1
    $ export FULLSTOP_S3_REGION=eu-west-1
    $ export FULLSTOP_TAUPAGE_OWNERS=999999999999
    $ export FULLSTOP_TAUPAGE_NAME_PREFIX=Taupage
    $ export FULLSTOP_S3_BUCKET=fullstop-bucket
    $ export FULLSTOP_KIO_URL: https://application.registry.address
    $ export FULLSTOP_PIERONE_URL: https://docker.repository.address
    $ export FULLSTOP_TEAM_SERVICE_URL: https://team.service.address
    $ export FULLSTOP_KONTROLLETTI_URL: https://kontrolletti.address
    $ export DATABASE_URL='jdbc:postgresql://localhost:5432/fullstop'
    $ export DATABASE_USER=postgres
    $ export DATABASE_PASSWORD='{cipher}234laksnfdlF83NHALF'
    $ export DATABASE_DRIVER=org.postgresql.Driver
    $ export INSTANCE_LOGS_S3_BUCKET=my-s3-bucket
    $ export ACCESS_TOKEN_URI=accessTokenUri
    $ export CREDENTIALS_DIR=/location/credentials
    $ export TOKEN_INFO_URI=tokenInfoUri
    $ export FULLSTOP_UNAPPROVED_SERVICES_AND_ROLE_BUCKET_NAME=fullstop-bucket-policy
    $ export FULLSTOP_UNAPPROVED_SERVICES_AND_ROLE_PREFIX=folder_containing_templates_files

### Disable CloudTrail Processing

Set the parameter `fullstop.container.autoStart=false` either as program argument, or as system property to start
Fullstop without CloudTrail processing.

##Database setup
Fullstop will store the violations in a RDBMS. Once you start Fullstop, it will create the necessary schema and tables
for you. The database itself, however, has to be created by you.
Your database password is encrypted with [AWS KMS](https://docs.aws.amazon.com/kms/latest/developerguide/overview.html).
We are using [Taupage](http://docs.stups.io/en/latest/components/taupage.html#environment) to decrypt the password on the fly.
To use Amazons KMS for de/encryption, you need to to provide a region and the key id for your key. In Fullstop, both
will be provided via environment variables.

The password should be already encrypted, when you store it in the ```DATABASE_PASSWORD``` environment variable. An
encrypted password always starts with ```aws:kms:```. You can use our [CLI tool](https://github.com/zalando/spring-cloud-config-aws-kms/tree/master/encryption-cli)
for encryption or you use Amazons [AWS CLI](http://docs.aws.amazon.com/cli/latest/reference/kms/encrypt.html#examples).

##Propose API changes

**Important** all changes should be swagger 2.0 spec compliant.

* Click here [![swagger-editor](https://img.shields.io/badge/swagger-editor-brightgreen.svg)](http://editor.swagger.io/#/?import=https://raw.githubusercontent.com/zalando-stups/fullstop/master/fullstop-api.yaml#/) to edit the API definition.
* Copy your changes and paste it in the [Github web editor: fullstop-api.yaml](https://github.com/zalando-stups/fullstop/edit/master/fullstop-api.yaml).
* Create a pull request with the new swagger 2.0 spec.

##How to build

    $ mvn clean install

##How to run

You need a locally running PostgreSQL 9.4 server (database "fullstop", host "localhost", user "postgres", password "postgres").

    $ # set env variables first
    $ cd fullstop
    $ mvn spring-boot:run -Dfullstop.container.autoStart=false

##How to build a docker image

Build fullstop:

    $ mvn clean package -U

Build scm-source.json:

    $ ./scm-source.sh

Build docker image:

    $ docker build -t registry/fullstop:0.1 fullstop

Show images:

    $ docker images

Run with docker:

    $ docker run -it registry/fullstop:0.1

Push docker image:

    $ docker push registry/fullstop:0.1

## License

Copyright © 2015 Zalando SE

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

