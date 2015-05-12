##Registry Plugin

###Aim of plugin
* Find out if an application that is running on an instance
  is registered in [kio](https://github.com/zalando-stups/kio)
* Find out if the application version for the application running on the instance is also 
  registered in kio
* Find out if the version registered in kio contains the same docker image used on the instance
* Find out if the docker image used by an instance is hosted in [Pier One](https://github.com/zalando-stups/pierone).


###Reacts on

```
EVENT SOURCE = "ec2.amazonaws.com"
EVENT NAME = "RunInstances"
```

###Configuration

You can always configure fullstop via environment variable:


    $ export FULLSTOP_KIO_URL: https://application.registry.address
    $ export FULLSTOP_PIERONE_URL: https://docker.repository.address


or modify [application.yml](../../fullstop/src/main/resources/config/application.yml)

```yml
fullstop:
    plugins:
        kio:
            url: ${FULLSTOP_KIO_URL}
        pierone:
            url: ${FULLSTOP_PIERONE_URL}
```
