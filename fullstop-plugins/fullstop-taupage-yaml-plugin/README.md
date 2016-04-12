Taupage yaml plugin
===================

Aim of the plugin
-----------------
This plugin will check the userdata of an instance for certain data.
It will look for the application id, the application version and the source (docker file).

Reacts on
---------
```
EVENT SOURCE = "ec2.amazonaws.com"
EVENT NAME = "RunInstances"
```