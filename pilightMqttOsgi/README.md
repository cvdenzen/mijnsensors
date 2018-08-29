Status 20180215: UNDER DEVELOPMENT

The pilightMqttOsgi project: 20180820 activemq version 5.15.4, 5.15.5
repo-add activemq
# since karaf 4.2 (20180820) needs next features for activemq
feature:install aries-blueprint
#and add the spring-legacy repo: (or not?)
feature:repo-add spring-legacy
# and
feature:install shell-compat

feature:install activemq
feature:install activemq-broker # (for mqtt?)

# karaf, install camel and camel-blueprint: (20180820: camel version 2.22.0)
repo-add camel
feature:install camel

feature:install camel-jms
feature:install camel-paho

feature:install activemq-camel
feature:install activemq-cf # (connection factory)
feature:install activemq-blueprint # (no idea why)

feature:install camel-gson
feature:install camel-stream
feature:install camel-netty4
feature:install camel-mqtt
feature:install camel-quartz2

feature:install jms

# then, if already in use by e.g. openhab, change ssh port in etc/apache.karaf.shell.cfg from 8101 in e.g. 8102.
# otherwise, karaf client will connect to whatever karaf instance started first.

# pilight, in a unix shell:
cp -avv ~/gitrepos/mijnsensors/pilightMqttOsgi/src/main/resources/nl/vandenzen/pilightmqttosgi/pilightmqttosgi.properties ~/Downloads/apache-karaf-4.2.0/etc/
cp -avv ~/gitrepos/mijnsensors/pilightMqttOsgi/src/main/resources/nl/vandenzen/pilightmqttosgi/activemq.xml ~/Downloads/apache-karaf-4.2.0/etc/
cp -avvv ~/gitrepos/mijnsensors/pilightMqttOsgi/target/pilightMqttOsgi-1.0-SNAPSHOT.jar ~/Downloads/apache-karaf-4.2.0/deploy/;ls -l ~/Downloads/apache-karaf-4.2.0/deploy/


Try to make pilight to mqtt gateway, something like pilight2mqtt. But there is a problem with pilight2mqtt: it cannot
handle multiple top level json objects and crashes. Because I prefer Java over Python, I decided to rebuild pilight2mqtt
in Java. I called it mqttPilight.
It will only do what I need:
- pass received codes from Pilight to mqtt
- pass set commands from mqtt to Pilight (don't know yet how to do this)

Camel custom setting file (for pilight server ip address, pilight port and other settings): karaf home etc/pilightmqttosgi.properties:
# this property file should exist in a camel property file path, e.g. karaf/etc


Additional feature: use ActiveMQ as MQTT broker. Add a connector to activemq.xml (that is: karaf home etc/activemq.xml)
 <transportConnectors>
   <transportConnector name="openwire" uri="tcp://0.0.0.0:61616"/>
   <transportConnector name="mqtt" uri="mqtt+nio://0.0.0.0:1883"/>
 </transportConnectors>

Status 20180209:
pimatic (no more openhab2 for now: 2.2 no sitemap, rules are difficult to edit, OH2 is going to use Microsofts Visual Studio).

pimatic heeft een config file. https://pimatic.org/guide/getting-started/configuration/


On Raspberry pi, run:
/usr/local/bin/pilight-receive --port=5017 --server=127.0.0.1 | nc -l 5018


On iMac:
edit with IntelliJ in ~/gitrepos/mijnsensors/pimatic-app/config.json
git add *
git commit
git push

Connect to openhab system (raspberry):
Terminal, ssh pi@192.168.2.9
(su is niet nodig voor deze app, draait onder user pi ?)
cd ~/gitrepos/mijnsensors
git pull
(of git clone https://github.com/cvdenzen/mijnsensors.git)
cp -a pimatic-app/config.json ~/pimatic-app
service pimatic-app restart
plugin homeduino laden (eenmalig). Geen pilight (wordt niet meer ondersteund).
