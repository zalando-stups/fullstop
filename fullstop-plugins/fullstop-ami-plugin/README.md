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


    $ export FULLSTOP_WHITELISTED_AMI_ACCOUNT=999999999999
    $ export FULLSTOP_AMI_NAME_START_WITH=Taupage


or modify [application.yml](../../fullstop/src/main/resources/config/application.yml)

```yml
fullstop:
    plugins:
        ami:
            # Account containing whitelisted AMI
            whitelistedAmiAccount: ${FULLSTOP_WHITELISTED_AMI_ACCOUNT}
            # will be checked if the image of the instance starts with this name
            amiNameStartWith: ${FULLSTOP_AMI_NAME_START_WITH}
```
