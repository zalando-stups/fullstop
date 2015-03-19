###Fullstop - Audit reporting

###How

Aim of the project is to enrich CloudTrail log events.

In our scenario we have multiple AWS accounts that need to be handled.

Each of this account has CloudTrail activated and is configured to write
in a bucket that reside in the account where also fullstop is running.
(Right now in AWS is not possible to read CloudTrail logs from different account)

Fullstop will then process events collected from CloudTrail.

For enrich CloudTrail log events with information that comes
from other system than AWS, we should only configure fullstop to do so.

Could be complicated if we need information from the AWS api,
because events are coming from different accounts.
To solve that problem we are using cross account role in order
to call the AWS api of this accounts.
The account that is running fullstop should therfore be trusted
by all other accounts in order to perform this operations.

###Configuration

You will need to provive AWS credentials.
We use for that the nice [aws-minion](https://github.com/zalando/aws-minion) tool.

This enviroment variable should be set:

    FULLSTOP_SQS_URL
    FULLSTOP_SQS_REGION
    FULLSTOP_S3_REGION

Example:

    $ export FULLSTOP_SQS_URL=https://sqs.eu-central-1.amazonaws.com/ACCOUNT_ID/fullstop
    $ export FULLSTOP_SQS_REGION=eu-central-1
    $ export FULLSTOP_S3_REGION=eu-west-1

###How to build/run

    mvn clean install

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
