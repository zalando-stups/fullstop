##Save Security Groups Plugin

###Aim of plugin
Save the configuration of an instances security group in an S3 bucket.
###Reacts on

```
EVENT SOURCE = "ec2.amazonaws.com"
EVENT NAME = "RunInstances"
```

###Configuration

No configuration is needed for this plugin.