##Instance Plugin

###Aim of plugin
Find out if an instance has a public IP with an open port 443 
and respond with an “Unauthorized” on accessing “GET /”.
Find all ports besides 22 and 443 allowed as input.

###Reacts on

```
EVENT SOURCE = "ec2.amazonaws.com"
EVENT NAME = "RunInstances"
```

###Configuration

No configuration is needed for this plugin.