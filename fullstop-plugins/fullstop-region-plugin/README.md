##Region Plugin

###Aim of plugin
Find out if an instance was started in the wrong region.

###Reacts on

```
EVENT SOURCE = "ec2.amazonaws.com"
EVENT NAME = "RunInstances"
```

###Configuration

You can modify here the list of regions: [application.yml](../../fullstop/src/main/resources/config/application.yml)

```yml
fullstop:
    plugins:
        region:
            # The whitelist of regions
            whitelistedRegions:
              - 'eu-west-1'
              - 'eu-central-1'
```
If the list is empty, default will be used: "eu-central-1" and "eu-west-1".