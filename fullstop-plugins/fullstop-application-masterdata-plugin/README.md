Application masterdata plugin
=============================

Aim of the plugin
-----------------

This plugin will check if an application on an instance has been registered in an application registry (e.g. [Kio] (https://stups.io/kio)).
It will also check, whether the application has a specification url and a specification type.

Reacts on
---------
```
EVENT SOURCE = "ec2.amazonaws.com"
EVENT NAME = "RunInstances"
EVENT NAME = "StartInstances"
```