## Fullstop Jobs

Contains modules with background-jobs, not triggered by CloudTrail-Events.

### FetchEC2Job

Reports unsecured / open endpoints on public EC2 instances.

See ["What is an open endpoint?"](#what-is-an-open-endpoint) for details.

### FetchElasticache

TODO Not yet implemented

### FetchElasticLoadBalancersJob

Reports unsecured / open endpoints on public / internet-facing ELBs.

See ["What is an open endpoint?"](#what-is-an-open-endpoint) for details.

### KeyRotationJob

Reports access keys, that have not been renewed within the last x days.

#### Configuration

Environment variable `FULLSTOP_ACCESS_KEYS_EXPIRE_AFTER_DAYS` can be used to control
after how many days an access key is considered "expired". Defaults to 30.

### NoPasswordsJob

Reports IAM users, that have password authentication enabled. Temporary access keys
are preferred over long-living passwords.

### FetchRdsJob

Reports RDS instances that are publicly accessible, which is potentially a security risk.
Data sources like RDS instances should always run in a private subnet of the VPC.
Access to the data from outside should only be possible using a well-defined (REST-)API
with security mechanisms, e.g. OAuth2 in place.

### FetchRedshiftJob

TODO Not yet implemented

## FAQ

### What is an open endpoint

Only HTTPS (port 443) is allowed for incoming traffic. Exception: HTTP (port 80) might be
used to implement redirects to HTTPS. This is more convenient for users when browsing to
a website. All other ports are considered "insecure" and will cause a violation.

Web services should only grant access to authenticated / authorized users. Fullstop tries
to call the root url of every public ELB / EC2 instance wihtout any authentication.
If the result does not return a 401 (Unauthorized), 403 (Forbidden) or some 5xx (Server error)
status, the endpoint is also considered "insecure".
