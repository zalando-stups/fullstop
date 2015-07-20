##Application Lifecycle Plugin

###Aim of plugin
Writes the lifecycle of an application into a database. Right now, start time, end time, eventtype, istance id, region, 
application id and application version are tracked.

###Reacts on

```
EVENT SOURCE = "ec2.amazonaws.com"
EVENT NAME = "RunInstances"
EVENT NAME = "StartInstances"
EVENT NAME = "StopInstances"
EVENT NAME = "TerminateInstances"
```

###Configuration

No configuration is needed for this plugin.