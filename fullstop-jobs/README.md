## Fullstop Jobs

Contains modules with background-jobs, not triggered by CloudTrail-Events.

All jobs can be triggered via rest controller, using bean name, here an example:

curl -X POST http://localhost:8080/api/jobs/noPasswordsJob/run

### FetchAmiJob

Reports instances running with an oudated Taupage image.

#### Scheduling

The job will start without delay and run every 4 hours

### FetchEC2Job

Reports unsecured / open endpoints on public EC2 instances.

See ["What is an open endpoint?"](#what-is-an-open-endpoint) for details.
#### Scheduling

The job will start 4 minutes after startup and will then run every 5 minutes.



### FetchElasticLoadBalancersJob

Reports unsecured / open endpoints on public / internet-facing ELBs. If the ```FetchEC2Job``` has already written a
violation for those instances, this job will skip them.

See ["What is an open endpoint?"](#what-is-an-open-endpoint) for details.

#### Scheduling

The job will start 2 minutes after startup and will then run every 5 minutes.

### KeyRotationJob

Reports access keys, that have not been renewed within the last x days.

#### Configuration

Environment variable `FULLSTOP_ACCESS_KEYS_EXPIRE_AFTER_DAYS` can be used to control
after how many days an access key is considered "expired". Defaults to 30.

#### Scheduling

The job will run every day at 10 pm.

### NoPasswordsJob

Reports IAM users, that have password authentication enabled. Temporary access keys
are preferred over long-living passwords.

#### Scheduling

The job will run every day at 11 pm.

### CrossAccountPolicyForIAMJob

This job will check, whether you have granted another account access to your account.

#### Scheduling

This job will start after 15 minutes and run every 2.5 hours.

### FetchRdsJob

Reports RDS instances that are publicly accessible, which is potentially a security risk.
Data sources like RDS instances should always run in a private subnet of the VPC.
Access to the data from outside should only be possible using a well-defined (REST-)API
with security mechanisms, e.g. OAuth2 in place.

#### Scheduling

This job will start immediately and run every 5 minutes.


### ScmCommitsJob

This job will check if every commit of your application has a valid reference to a ticket / issue. It will look at a 24 hour timeframe,
starting on "yesterday" midnight to "today" midnight. There will be only one violation per repository per day.

#### Scheduling

The job starts with a delay of 10 minuts and runs every 2.5 hours.

## FAQ

### What is an open endpoint

Only HTTPS (port 443) is allowed for incoming traffic. Exception: HTTP (port 80) might be
used to implement redirects to HTTPS. This is more convenient for users when browsing to
a website. All other ports are considered "insecure" and will cause a violation.

Web services should only grant access to authenticated / authorized users. Fullstop tries
to call the root url of every public ELB / EC2 instance wihtout any authentication.
If the result does not return a 401 (Unauthorized), 403 (Forbidden) or some 5xx (Server error)
status, the endpoint is also considered "insecure".
