# investment-tax-relief-subscription

[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html) [![Build Status](https://travis-ci.org/hmrc/investment-tax-relief-subscription.svg?branch=master)](https://travis-ci.org/hmrc/investment-tax-relief-subscription) [ ![Download](https://api.bintray.com/packages/hmrc/releases/investment-tax-relief-subscription/images/download.svg) ](https://bintray.com/hmrc/releases/investment-tax-relief-subscription/_latestVersion)


API
----

| PATH | Supported Methods |
|------|-------------------|
|Subscribe to the TAVC investment tax relief service for the specified safeID:|
|```/:safeId/:postcode/subscribe ``` | POST |
|Get the subscription details for an existing subscription for the specified tavcReferenceNumber:|
|```/:tavcReferenceNumber/subscription``` | GET |


Requirements
------------

This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), so needs at least a [JRE] to run.


## Run the application


To update from Nexus and start all services from the RELEASE version instead of snapshot

```
sm --start TAVC_ALL -f
```

 
##To run the application locally execute the following:

Kill the service ```sm --stop  ITR_SUBSC``` and run:
```
sbt 'run 9638'
```

This service is part of the investment tax relief service and has dependent services.
For a full list of the dependent microservices that comprise this service please see the readme for our [Submission Frontend Service](https://github.com/hmrc/investment-tax-relief-submission-frontend/)


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
