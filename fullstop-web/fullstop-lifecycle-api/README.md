Application Lifecycle API
=========================

In order to understand, which applications are running, Fullstop. comes with a lifecycle api.
Everytime, a new instance of your application has started, stopped or rebooted, it is written into
the lifecycle DB. 

Besides application name and version, as well as eventType (started, stopped, ...), instanceId, 
imageId, imageName, instanceBootTime, region and creation date (the date, the event occured) are saved as well.

The API
-------
You can use the REST API to get information on a specific application.
```/api/lifecycle/applications/myApp/versions``` will return a list of all your instances that are or were running 
```myAPP```, including all versions of that application. The list is grouped by version and instanceId.
It is sorted by the date of creation (by the date it was written into the database), starting with the oldest entry.

If you want to filter your application to get a specific version, you can call
```/api/lifecycle/applications/myApp/versions/1.0-SNAPSHOT```. This will give you a list
of instances, that are oder were running ```myApp```with version ```1.0-SNAPSHOT```.