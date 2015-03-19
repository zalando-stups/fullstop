###Fullstop - Audit reporting

... processes logfiles from cloudtrail to handle specific events on the platform.

###Configuration

This enviroment variable should be set:


    FULLSTOP_SQS_URL (example: https://sqs.eu-central-1.amazonaws.com/ACCOUNT_ID/fullstop)
    FULLSTOP_SQS_REGION (example: eu-west-1)

    FULLSTOP_S3_REGION (example: eu-west-1)


###How to build

    mvn clean install

###How to run

    mvn spring-boot:run


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
