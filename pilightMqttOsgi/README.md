Status 20180215: UNDER DEVELOPMENT
Beware: there is another (old) README.md file at mijnsensors/

This files describes the actions to take for the separate karaf instance, NOT for the openhab instance

repo-add activemq
feature:install activemq-broker
# camel 2.23.0
repo-add camel
feature:install camel
#feature:repo-add spring-legacy

#
# feature install can be done by pilightmqttosgi-features.xml.
# Copy this file (nl/vandenzen/pilightmqttosgi/pilightmqttosgi-features.xml) to /usr/share/openhab2/addons:
# iMac: scp /Users/carl/gitrepos/mijnsensors/pilightMqttOsgi/src/main/resources/nl/vandenzen/pilightmqttosgi/pilightmqttosgi-features.xml pi@rpi2:tmp
# rpi: user openhab, cp -a /home/pi/tmp/pilightmqttosgi-features.xml /usr/share/openhab2/addons
# feature:install pilightmqttosgi
#
# since karaf 4.2 (20180820) needs next features for activemq
#feature:install aries-blueprint
# and
#feature:install shell-compat

#feature:install activemq
#feature:install activemq-broker # (for mqtt?)

# karaf, install camel and camel-blueprint:
feature:install camel

feature:install camel-jms
feature:install camel-paho

#feature:install activemq-camel
#feature:install activemq-cf # (connection factory)
#feature:install activemq-blueprint # (no idea why)

feature:install camel-gson
feature:install camel-stream
feature:install camel-netty4
feature:install camel-mqtt
feature:install camel-quartz2

feature:install jms

feature:install service-wrapper # just to create karaf.service file, because the wrapper gives (tool readelf) machine Intel 80386 code

#
# end of feature install commands
#

wrapper:install

# vi bin/karaf.service, add User=openhab in [Service]
# See https://www.dexterindustries.com/howto/run-a-program-on-your-raspberry-pi-at-startup/
# cp bin/karaf.service /etc/systemd/system # or make a link


# if already in use by e.g. openhab, change ssh port in etc/apache.karaf.shell.cfg from 8101 in e.g. 8102.
# and etc/jetty.xml change secure.port to e.g. 8444.

# on raspberry, user root. (if chown -R openhab.openhab /usr/apache-karaf
adduser pi openhab
chmod g+w /usr/share/apache-karaf/etc
chmod g+w /usr/share/apache-karaf/deploy

# pilight, in a unix shell on iMac
scp ~/gitrepos/mijnsensors/pilightMqttOsgi/src/main/resources/nl/vandenzen/pilightmqttosgi/pilightmqttosgi.properties pi@192.168.2.9:/usr/share/apache-karaf/etc/
scp ~/gitrepos/mijnsensors/pilightMqttOsgi/src/main/resources/nl/vandenzen/pilightmqttosgi/activemq.xml pi@192.168.2.9:/usr/share/apache-karaf/etc/
# copy jar from imac to raspberry
scp ~/gitrepos/mijnsensors/pilightMqttOsgi/target/pilightMqttOsgi-1.0-SNAPSHOT.jar pi@192.168.2.9:tmp
# On raspberry:
sudo -s -E -u openhab
cp /home/pi/tmp/pilightMqttOsgi-1.0-SNAPSHOT.jar /usr/share/apache-karaf/deploy

# karaf service (systemd) on rpi is not possible through the service-wrapper (that only supports 80386 architecture).
# see https://karaf.apache.org/manual/latest/#_service_script_templates
cd bin/contrib
./karaf-service.sh  -k /usr/share/apache-karaf -u openhab
cp karaf*service /etc/systemd/system
systemctl enable karaf

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
pimatic? (no more openhab2 for now: 2.2 no sitemap, rules are difficult to edit, OH2 is going to use Microsofts Visual Studio).

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
(su is niet nodig voor deze app, draait onder user pi ?, beter om openhab met env te doen: sudo -E -s -u openhab)
cd ~/gitrepos/mijnsensors
git pull
(of git clone https://github.com/cvdenzen/mijnsensors.git)
cp -a pimatic-app/config.json ~/pimatic-app
service pimatic-app restart
plugin homeduino laden (eenmalig). Geen pilight (wordt niet meer ondersteund).

openhab@raspberrypi:/home/pi/gitrepos/etc_openhab2$ gpio readall

============================================================================
rpi board connection (as of dec 2018), should be usable on rpi2 and rpi3.
pi@rpi3:~ $ gpio readall
                +-----+-----+---------+------+---+---Pi 3---+---+------+---------+-----+-----+
                | BCM | wPi |   Name  | Mode | V | Physical | V | Mode | Name    | wPi | BCM |
                +-----+-----+---------+------+---+----++----+---+------+---------+-----+-----+
                |     |     |    3.3v |      |   |  1 || 2  |   |      | 5v      |     |     | R433,S433
SDA.1           |   2 |   8 |   SDA.1 | ALT0 | 1 |  3 || 4  |   |      | 5v      |     |     |
SCL.1           |   3 |   9 |   SCL.1 | ALT0 | 1 |  5 || 6  |   |      | 0v      |     |     | ATTiny85.p4 ?
                |   4 |   7 | GPIO. 7 |   IN | 1 |  7 || 8  | 0 | IN   | TxD     | 15  | 14  |
                |     |     |      0v |      |   |  9 || 10 | 1 | IN   | RxD     | 16  | 15  |
S43             |  17 |   0 | GPIO. 0 |  OUT | 0 | 11 || 12 | 1 | IN   | GPIO. 1 | 1   | 18  | TNYpin2
UPS             |  27 |   2 | GPIO. 2 |   IN | 0 | 13 || 14 |   |      | 0v      |     |     |
UPS             |  22 |   3 | GPIO. 3 |   IN | 0 | 15 || 16 | 0 | IN   | GPIO. 4 | 4   | 23  | PIR
                |     |     |    3.3v |      |   | 17 || 18 | 0 | IN   | GPIO. 5 | 5   | 24  | DHT11
TNYpin5+RFMmosi |  10 |  12 |    MOSI |   IN | 0 | 19 || 20 |   |      | 0v      |     |     |
TNYpin6+RFMmiso |   9 |  13 |    MISO |   IN | 0 | 21 || 22 | 0 | IN   | GPIO. 6 | 6   | 25  | RFMdio0
TNYpin7+RFMsclk |  11 |  14 |    SCLK |   IN | 0 | 23 || 24 | 1 | IN   | CE0     | 10  | 8   | RFMnss
                |     |     |      0v |      |   | 25 || 26 | 1 | IN   | CE1     | 11  | 7   | TNYpin1
ID_SD           |   0 |  30 |   SDA.0 |   IN | 1 | 27 || 28 | 1 | IN   | SCL.0   | 31  | 1   | ID_SC
                |   5 |  21 | GPIO.21 |   IN | 1 | 29 || 30 |   |      | 0v      |     |     |
LCDdat4         |   6 |  22 | GPIO.22 |   IN | 1 | 31 || 32 | 0 | IN   | GPIO.26 | 26  | 12  | LCDpwm
LCDdat5         |  13 |  23 | GPIO.23 |   IN | 0 | 33 || 34 |   |      | 0v      |     |     |
LCDdat6         |  19 |  24 | GPIO.24 |   IN | 0 | 35 || 36 | 0 | IN   | GPIO.27 | 27  | 16  | LCDen
LCDdat7         |  26 |  25 | GPIO.25 |   IN | 0 | 37 || 38 | 0 | IN   | GPIO.28 | 28  | 20  | LCDrs
                |     |     |      0v |      |   | 39 || 40 | 0 | IN   | GPIO.29 | 29  | 21  |
                +-----+-----+---------+------+---+----++----+---+------+---------+-----+-----+
                | BCM | wPi |   Name  | Mode | V | Physical | V | Mode | Name    | wPi | BCM |
                +-----+-----+---------+------+---+---Pi 3---+---+------+---------+-----+-----+

TNY=ATTiny85 for pilight 433MHz receiver (filter)
RFM=RFM69 for mysensors gateway
R43=Receiver 433 MHz
S43=Sender 433 MHz
LCD=LCD display
followed by 4 chars to denote pin
UPS=Uninterruptible Power Supply (reserved pin for UPS Pico)
GPIO.22 and GPIO.27 reserved for UPS
SDA.1, SCL.1 for I2C, e.g. light measurement BH1750
ID_SC, ID_SD EEPROM for HAT reserved pins

pilight https://manual.pilight.org/electronics/wiring.html
Except pin 8: changed to pin 7, also changed in /etc/avrdude.conf gpio reset=7.

mysensors rfm69: https://www.mysensors.org/build/raspberry
https://forum.mysensors.org/topic/9953/mqtt-gateway-floods-logfile-if-broker-is-not-reachable
An edit in core/MyGatewayTransportMQTTClient.cpp:
 (I added a "delay(3000)" after line 148 in reconnectMQTT(void))

lcd:
4 bit mode: lcd display use d4..d7
backlight led: 5V 23mA (isolated from rest)
RS=register select
RW=H=read, L=write. USE WRITE ONLY, otherwise 5V will be supplied to raspberry pins!
pwm, resistor, transistor.

