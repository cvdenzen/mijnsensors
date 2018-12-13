Status 20180215: UNDER DEVELOPMENT

The pilightMqttOsgi project:
- karaf, install camel and camel-blueprint
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

============================================================================
rpi board connection (as of dec 2018), should be usable on rpi2 and rpi3.
pi@rpi3:~ $ gpio readall
                +-----+-----+---------+------+---+---Pi 3---+---+------+---------+-----+-----+
                | BCM | wPi |   Name  | Mode | V | Physical | V | Mode | Name    | wPi | BCM |
                +-----+-----+---------+------+---+----++----+---+------+---------+-----+-----+
                |     |     |    3.3v |      |   |  1 || 2  |   |      | 5v      |     |     | R433,S433
                |   2 |   8 |   SDA.1 | ALT0 | 1 |  3 || 4  |   |      | 5v      |     |     |
                |   3 |   9 |   SCL.1 | ALT0 | 1 |  5 || 6  |   |      | 0v      |     |     | ATTiny85.p4 ?
                |   4 |   7 | GPIO. 7 |   IN | 1 |  7 || 8  | 0 | IN   | TxD     | 15  | 14  |
                |     |     |      0v |      |   |  9 || 10 | 1 | IN   | RxD     | 16  | 15  |
S43             |  17 |   0 | GPIO. 0 |  OUT | 0 | 11 || 12 | 1 | IN   | GPIO. 1 | 1   | 18  | TNYpin2
                |  27 |   2 | GPIO. 2 |   IN | 0 | 13 || 14 |   |      | 0v      |     |     |
                |  22 |   3 | GPIO. 3 |   IN | 0 | 15 || 16 | 0 | IN   | GPIO. 4 | 4   | 23  |
                |     |     |    3.3v |      |   | 17 || 18 | 0 | IN   | GPIO. 5 | 5   | 24  |
TNYpin5+RFMmosi |  10 |  12 |    MOSI |   IN | 0 | 19 || 20 |   |      | 0v      |     |     |
TNYpin6+RFMmiso |   9 |  13 |    MISO |   IN | 0 | 21 || 22 | 0 | IN   | GPIO. 6 | 6   | 25  | RFMdio0
TNYpin7+RFMsclk |  11 |  14 |    SCLK |   IN | 0 | 23 || 24 | 1 | IN   | CE0     | 10  | 8   | RFMnss
                |     |     |      0v |      |   | 25 || 26 | 1 | IN   | CE1     | 11  | 7   | TNYpin1
                |   0 |  30 |   SDA.0 |   IN | 1 | 27 || 28 | 1 | IN   | SCL.0   | 31  | 1   |
LCDdat4         |   5 |  21 | GPIO.21 |   IN | 1 | 29 || 30 |   |      | 0v      |     |     |
LCDdat5         |   6 |  22 | GPIO.22 |   IN | 1 | 31 || 32 | 0 | IN   | GPIO.26 | 26  | 12  | LCDpwm
LCDdat6         |  13 |  23 | GPIO.23 |   IN | 0 | 33 || 34 |   |      | 0v      |     |     |
LCDdat7         |  19 |  24 | GPIO.24 |   IN | 0 | 35 || 36 | 0 | IN   | GPIO.27 | 27  | 16  | LCDen
                |  26 |  25 | GPIO.25 |   IN | 0 | 37 || 38 | 0 | IN   | GPIO.28 | 28  | 20  | LCDrs
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

