w4-bpmnplus-java-api-extra
==========================

This project is an extra layer that is built on top of W4 BPMN+ Engine Java API, and which brings some additionnal
features on client-side.

It's fully compatible with API interfaces so it require no extensive modification to the code of an existing
application if you want to make use (or discard) this layer instead of stock APIs.


Features
--------

### Auto Recovery

Tired of enclosing calls to W4 BPMN+ Engine in try/catch blocks to rebuild a new service if the connection
to the server was lost?

Using this feature, remote Java services will recover their connection to the engine once the network or
the engine return to an operational state.

Without having to take care of it while developing, the application will be compliant with restarts of Engine.


### Factory cache

This feature brings the ability to cache instances of all object produced by `ObjectFactory`. By default, creation
of new instances are always delegated to W4 BPMN+ Engine.

With this feature enabled, the creation of new instance will be remotely delegated only once saving huge number
of round-trips between the client and the server. All other instance creation will be served from a local cache.


License
-------

Copyright (c) 2015, W4 S.A. 

This project is licensed under the terms of the MIT License (see LICENSE file)

Ce projet est licencié sous les termes de la licence MIT (voir le fichier LICENSE)
