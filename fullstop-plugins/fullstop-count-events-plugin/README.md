##Count Events Plugin

###Aim of plugin

This plugin counts all events for an account. It provides these data in an component 'CountEventsMetric'.

###Reacts on

```
ALL EVENTS
```

###Configuration

No configuration is needed for this plugin.

###Reporting

not implemented, provide a restendpoint in your app

with console-reporter it would look like this:

```

-- Meters ----------------------------------------------------------------------
meter.events.test
             count = 100
         mean rate = 16.79 events/second
     1-minute rate = 19.00 events/second
     5-minute rate = 19.00 events/second
    15-minute rate = 19.00 events/second
meter.events.test2
             count = 50
         mean rate = 8.39 events/second
     1-minute rate = 9.60 events/second
     5-minute rate = 9.60 events/second
    15-minute rate = 9.60 events/second

```
