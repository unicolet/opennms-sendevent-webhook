# opennms-sendevent-webhook

send-event.pl reinvented as a web hook.

    send-event.pl --interface 172.16.1.1 uei.opennms.org/internal/discovery/newSuspect

becomes:

    curl http://127.0.0.1:9090/uei.opennms.org/internal/discovery/newSuspect?ip=172.16.1.1


Installation
------------

Clone the repo, then run:

    ./gradlew clean shadowJar
    
on Windows run:

    gradlew.bat clean shadowJar

To run it as a standalone program on a console (all platforms):

    java -jar -DPORT=9090 -Dopennms.host=127.0.0.1 build/libs/opennms-sendevent-webhook-0.1-all.jar

To run it as a service (*nix only) as root type:

    mkdir -p /opt/opennms-sendevent-webhook
    cp build/libs/opennms-sendevent-webhook-0.1-all.jar /opt/opennms-sendevent-webhook/
    cp init.d/opennms-sendevent-webhook /etc/init.d && chmod 750 /etc/init.d/opennms-sendevent-webhook
    chkconfig add opennms-sendevent-webhook
    /etc/init.d/opennms-sendevent-webhook start

Configuration
-------------

Create a /etc/default/opennms-sendevent-webhook (Ubuntu,Debian) or /etc/sysconfig/opennms-sendevent-webhook (RH, Centos)
and change the following variables (their meaning should be self explanatory):

    PORT=9090
    OPENNMS_HOST=127.0.0.1
    user="root"
    group="root"
    chdir="/opt/opennms-sendevent-webhook"

Authorization
-------------

The web hook implements a token-based authorization scheme to prevent unauthorized access.

**Access to the web hook is, by default, unrestricted.**

To enforce authentication create a *auth_tokens* file in the web hook process working
directory.

Each line represents a token that can be shared with the callers of the hook to authorize them.
The file is read with every request, so there is no need to restart or reload the web hook process.

Tokens must only contain mixed case letters and numbers.

Suppose *auth_tokens* contains (on a single line, among others) the following token:

     fghsAYYE7h287
     
Then the previous invocation of the hook changes as follows:

    curl http://127.0.0.1:9090/fghsAYYE7h287/uei.opennms.org/internal/discovery/newSuspect?ip=172.16.1.1

