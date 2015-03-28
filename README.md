# opennms-sendevent-webhook

send-event.pl reinvented as a web hook.

    send-event.pl \
                --interface 172.16.1.1 \
                uei.opennms.org/internal/discovery/newSuspect

becomes:

    curl http://127.0.0.1:9090/uei.opennms.org/internal/discovery/newSuspect?ip=172.16.1.1


Installation
------------

Clone the repo, then run:

    ./gradlew clean shadowJar
    
on Windows run:

    gradlew.bat clean shadowJar

To run as a service (*nix only) as root type:

    mkdir -p /opt/opennms-sendevent-webhook
    cp build/libs/opennms-sendevent-webhook-0.1-all.jar /opt/opennms-sendevent-webhook/
    cp init.d/opennms-sendevent-webhook /etc/init.d && chmod 750 /etc/init.d/opennms-sendevent-webhook
    chkconfig add opennms-sendevent-webhook
    /etc/init.d/opennms-sendevent-webhook start


