[![Build Status](https://travis-ci.org/zalando-stups/fullstop.svg?branch=master)](https://travis-ci.org/zalando-stups/fullstop)
[![Coverage Status](https://coveralls.io/repos/zalando-stups/fullstop/badge.svg)](https://coveralls.io/r/zalando-stups/fullstop)


#Fullstop - Audit reporting

![Fullstop](images/fullstop.png)

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

* [fullstop-hello-event-plugin](fullstop-plugins/fullstop-hello-event-plugin)
* [fullstop-ami-plugin](fullstop-plugins/fullstop-ami-plugin)
* [fullstop-instance-plugin](fullstop-plugins/fullstop-instance-plugin)
* [fullstop-keypair-plugin](fullstop-plugins/fullstop-keypair-plugin)
* [fullstop-region-plugin](fullstop-plugins/fullstop-region-plugin)
* [fullstop-registry-plugin](fullstop-plugins/fullstop-registry-plugin)
* [fullstop-subnet-plugin](fullstop-plugins/fullstop-subnet-plugin)

##Configuration

This enviroment variable should be set:

    FULLSTOP_LOGS
    FULLSTOP_SQS_URL
    FULLSTOP_SQS_REGION
    FULLSTOP_S3_REGION
    FULLSTOP_WHITELISTED_AMI_ACCOUNT
    FULLSTOP_AMI_NAME_START_WITH
    FULLSTOP_S3_BUCKET
    FULLSTOP_KIO_URL
    FULLSTOP_PIERONE_URL

Example:

    $ export FULLSTOP_LOGS=/fullstop_logs_dir
    $ export FULLSTOP_SQS_URL=https://sqs.eu-central-1.amazonaws.com/ACCOUNT_ID/fullstop
    $ export FULLSTOP_SQS_REGION=eu-central-1
    $ export FULLSTOP_S3_REGION=eu-west-1
    $ export FULLSTOP_WHITELISTED_AMI_ACCOUNT=999999999999
    $ export FULLSTOP_AMI_NAME_START_WITH=Taupage
    $ export FULLSTOP_S3_BUCKET=fullstop-bucket
    $ export FULLSTOP_KIO_URL: https://application.registry.address
    $ export FULLSTOP_PIERONE_URL: https://docker.repository.address

##How to build

    $ mvn clean install

##How to run

    $ cd fullstop

    $ mvn spring-boot:run
    
##How to build a docker image

Build fullstop:

    $ mvn clean install -U
    
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
