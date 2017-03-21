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
* `FULLSTOP_PLUGINS_SCM_HOSTS`: A map/dictionary of GitHub (Enterprise) hosts and a pattern of their allowed owners.
    * "owner" is the organization or user that hosts a repository. E.g. for https://github.com/zalando-stups/fullstop,
      the owner would be "zalando-stups"
    * for each GitHub host it is required to specify a regular expression that matches the allowed hosts
    * e.g. a possible scenario would allow deployments from all owners of your GitHub Enterprise installation:
        * `"github.my.company.com": "^.*$""`
    * at the same time you want to limit github.com deployments to your owners maintained by your company:
        * `"github.com": "^zalando-stups|zalando-incubator$`
    * deployments from repos that are not on one of the listed hosts, or that do not match one of the allowed owners
      will be marked with the **ILLEGAL_SCM_REPOSITORY** violation

#### How to set configuration values

You can always configure fullstop via environment variable:


    $ export FULLSTOP_KIO_URL: https://application.registry.address
    $ export FULLSTOP_PIERONE_URL: https://docker.repository.address
    
    # easiest way to specify a map property is using SPRING_APPLICATION_JSON variable
    $ export  SPRING_APPLICATION_JSON= "{\"fullstop\": {\"plugins\": {\"scm\": {\"hosts\": {\"github.my.company.com\": \"^.+$\", \"github.com\": \"^zalando-stups|zalando-incubator$\"}}}}}"


or modify [application.yml](../../fullstop/src/main/resources/config/application.yml)

```yml
fullstop:
    plugins:
        kio:
            url: ${FULLSTOP_KIO_URL}
        pierone:
            url: ${FULLSTOP_PIERONE_URL}
        scm:
            hosts:
                "github.com": "^zalando-stups|zalando-incubator$"
                "github.my.company.com": "^.+$"
```
