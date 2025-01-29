# SPPropertiesService

This is a SailPoint plugin based on the Compass example
https://community.sailpoint.com/t5/IdentityIQ-Wiki/Plugin-custom-REST-API-Filter-data-visibility-based-on-requester/ta-p/184970

This code has 2 services.

1) returns the application connection info.
2) returns the values in a custom object

Change accordingly as needed.

it is built with netbeans 22 jdk11

1) Import SPRight.xml
2) import Capability.cml
3) create a new scope for the service
4) Create a new user to act as the proxy
5) assign the user the SPPropertiesService Capability and Scope
5) Create the OAuth client and associate the user in the step4 as the proxy
6) see test code on how to get a token to run the services
