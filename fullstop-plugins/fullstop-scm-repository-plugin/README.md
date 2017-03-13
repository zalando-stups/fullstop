##SCM Repository Plugin

###Aim of plugin

Find mismatches of the scm repository in our Kio (application registry) compared with the information found in
scm-source.json from Pierone (Docker registry).

###Reacts on

```
EVENT SOURCE = "ec2.amazonaws.com"
EVENT NAME = "RunInstances"
```

###Configuration

#### Required

* `FULLSTOP_KIO_URL`: URL to [Kio](https://github.com/zalando-stups/kio) application registry.
* `FULLSTOP_PIERONE_URL`: URL to [Pier One](https://github.com/zalando-stups/pierone) docker registry.

#### How to set configuration values

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
