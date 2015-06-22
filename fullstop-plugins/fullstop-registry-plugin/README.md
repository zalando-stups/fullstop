## Registry Plugin

### Aim of plugin

* Find out if an application that is running on an instance
  is registered in [Kio](https://github.com/zalando-stups/kio)
* Find out if the application version for the application running on the instance is also 
  registered in Kio
* Find out if the version registered in Kio contains the same docker image used on the instance
* Find out if the docker image used by an instance is hosted in [Pier One](https://github.com/zalando-stups/pierone).
* Find out if the application version has all default approval types.
* Find out if code, test and deploy approvals were done by at least two people.

### Reacts on

```
EVENT SOURCE = "ec2.amazonaws.com"
EVENT NAME = "RunInstances"
```

### Configuration

#### Required

* `FULLSTOP_KIO_URL`: URL to [Kio](https://github.com/zalando-stups/kio) application registry.
* `FULLSTOP_PIERONE_URL`: URL to [Pier One](https://github.com/zalando-stups/pierone) docker registry.

#### Optional

* `FULLSTOP_DEFAULT_APPROVALS`: Comma-separated list of approval types that must be present on every application version. Defaults to `SPECIFICATION,CODE_CHANGE,TEST,DEPLOY`.
* `FULLSTOP_CODE_APPROVAL`: Approval type for code changes, defaults to `CODE_CHANGE`.
* `FULLSTOP_TEST_APPROVAL`: Approval type for tests, defaults to `TEST`.
* `FULLSTOP_DEPLOY_APPROVAL`: Approval type for deploys, defaults to `DEPLOY`.

### How to set configuration values

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
