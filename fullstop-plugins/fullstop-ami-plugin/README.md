##AMI Plugin

###Aim of plugin
Find out if an instance was started with a wrong image.

###Reacts on

```
EVENT SOURCE = "ec2.amazonaws.com"
EVENT NAME = "RunInstances"
```

###Configuration

You can always configure fullstop via environment variable:


    $ export FULLSTOP_TAUPAGE_OWNERS=999999999999
    $ export FULLSTOP_TAUPAGE_NAME_PREFIX=Taupage


or modify [application.yml](../../fullstop/src/main/resources/config/application.yml)

```yml
fullstop:
    plugins:
        ami:
            # Account containing whitelisted AMI
            taupageOwners: ${FULLSTOP_TAUPAGE_OWNERS}
            # will be checked if the image of the instance starts with this name
            taupageNamePrefix: ${FULLSTOP_TAUPAGE_NAME_PREFIX}
```
