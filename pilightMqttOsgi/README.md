Status 20180215: UNDER DEVELOPMENT

The pilightMqttOsgi project:
- karaf, install camel and camel-blueprint:
repo-add camel
feature:install camel (this installs latest version, 2.21.1 on 2018-05-28)
- In karaf:
repo-add activemq <version>
feature:install camel-jms
feature:install camel-paho
feature:install activemq
feature:install activemq-camel
feature:install activemq-cf (connection factory)
feature:install activemq-blueprint (no idea why)

feature:install camel-gson
feature:install camel-stream
feature:install camel-netty4

On Raspberry pi, run:
/usr/local/bin/pilight-receive --port=5017 --server=127.0.0.1 | nc -l 5018


Try to make pilight to mqtt gateway, something like pilight2mqtt. But there is a problem with pilight2mqtt: it cannot
handle multiple top level json objects and crashes. Because I prefer Java over Python, I decided to rebuild pilight2mqtt
in Java. I called it mqttPilight.
It will only do what I need:
- pass received codes from Pilight to mqtt
- pass set commands from mqtt to Pilight (don't know yet how to do this)

Camel custom setting file (for pilight server ip address, pilight port and other settings): karaf home etc/pilightmqttosgi.properties:
# this property file should exist in a camel property file path, e.g. karaf/etc
pilightserver=192.168.2.9
pilightport=5017
mqttserver=192.168.2.9
mqttport=1883


Additional feature: use ActiveMQ as MQTT broker. Add a connector to activemq.xml (that is: karaf home etc/activemq.xml)
 <transportConnectors>
   <transportConnector name="openwire" uri="tcp://0.0.0.0:61616"/>
   <transportConnector name="mqtt" uri="mqtt+nio://0.0.0.0:1883"/>
 </transportConnectors>

Status 20180209:
pimatic (no more openhab2 for now: 2.2 no sitemap, rules are difficult to edit, OH2 is going to use Microsofts Visual Studio).

pimatic heeft een config file. https://pimatic.org/guide/getting-started/configuration/



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
