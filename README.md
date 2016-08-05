# opennms-sendevent-webhook

[![Build Status](https://travis-ci.org/unicolet/opennms-sendevent-webhook.svg?branch=master)](https://travis-ci.org/unicolet/opennms-sendevent-webhook)

[send-event.pl](http://www.opennms.org/wiki/Send-event.pl) reinvented as a web hook.

    send-event.pl --interface 172.16.1.1 uei.opennms.org/internal/discovery/newSuspect

becomes:

    curl http://127.0.0.1:9090/uei.opennms.org/internal/discovery/newSuspect?ip=172.16.1.1

Adding the *gif=1* param will change the output to a 1x1 transparent gif instead of json. Great for
stealth tracking in web pages.

Reverse proxy friendly: if the ip param is not specified it will be extracted from (in order):

1. X-Real-IP header
2. X-Forwarded-For header
3. request.getRemoteAddr

# Installation

Make sure you have Java 7 or greater in your path. The software will not work with Java 6
due an upstream dependency having been compiled with Java 7.

Clone the repo, then run:

    ./gradlew clean shadowJar
    
on Windows run:

    gradlew.bat clean shadowJar

To run it as a standalone program on a console (all platforms):

    java -jar -DPORT=9090 -Dopennms.host=127.0.0.1 build/libs/opennms-sendevent-webhook-0.1-all.jar

The software bundles a SysV-style init script in init.d. To run it as a service type the following commands as root:

    mkdir -p /opt/opennms-sendevent-webhook
    cp build/libs/opennms-sendevent-webhook-0.1-all.jar /opt/opennms-sendevent-webhook/
    cp init.d/opennms-sendevent-webhook /etc/init.d && chmod 750 /etc/init.d/opennms-sendevent-webhook
    chkconfig add opennms-sendevent-webhook
    /etc/init.d/opennms-sendevent-webhook start

To run the software as a service under other inits (systemd, upstart) consider using a generator like [pleaserun](https://github.com/jordansissel/pleaserun).

Pull requests are welcome.

## Installation with RPM

A rpm version of this software is available at:

    https://packagecloud.io/unicoletti/opennms
    
**IMPORTANT**: the rpm has no explicit dependencies, but you must have java >= 7
in PATH to run the service.

# Configuration

Create a /etc/default/opennms-sendevent-webhook (Ubuntu,Debian) or /etc/sysconfig/opennms-sendevent-webhook (RH, Centos)
and change the following variables (their meaning should be self explanatory):

    PORT=9090
    OPENNMS_HOST=127.0.0.1
    user="root"
    group="root"
    chdir="/opt/opennms-sendevent-webhook"

# Authorization

The web hook implements a token-based authorization scheme to prevent unauthorized access.

**Access to the web hook is, by default, unrestricted.**

To enforce authentication create a *auth_tokens* file in the web hook process working
directory.

Each line represents a token that can be shared with the callers of the hook to authorize them.
The file is read with every request, so there is no need to restart or reload the web hook process.

Tokens must only contain mixed case letters and numbers.

Suppose *auth_tokens* contains (on a single line, among others or alon) the following token:

    fghsAYYE7h287
     
Then the previous invocation of the hook changes as follows:

    curl http://127.0.0.1:9090/fghsAYYE7h287/uei.opennms.org/internal/discovery/newSuspect?ip=172.16.1.1

Example of adding a token:

    echo fghsAYYE7h287 >> auth_tokens
    
Removing a token:

    sed -i -e '/^fghsAYYE7h287$/d' auth_tokens

# Credits

This softare uses a Java send-event class that implements the protocol, copied and slightly modified
from the OpenNMS wiki (http://www.opennms.org/wiki/Send_event_using_java).

# Development notes

How to bump version: edit ``build.gradle``, change version, then run ``./gradlew bumpVersion``. Commit && push

# License

Licensed under the GPL v3.

# TODO

- ~~API Versioning~~ [DONE]
- ~~Authorization~~ [DONE]
- ~~handle post body~~ [DONE]
