
This files describes the actions to take for the separate karaf instance, NOT for the openhab instance.

# pilight, in a unix shell on iMac (jan 2020: deprecated, no more pilight, everything is Philips Hue)
scp /home/carl/IdeaProjects/mijnsensors_github/pilightMqttOsgi/lib/*.jar pi@rpi3:/usr/share/karaf/deploy
scp /home/carl/IdeaProjects/mijnsensors_github/pilightMqttOsgi/target/classes/nl/vandenzen.iot/pilightmqttosgi-features.xml  pi@rpi3:/usr/share/karaf/deploy
:q

# 20201113 deprecated, use standalone artemis server copy activemq.xml from lastpass to /usr/share/karaf/etc/activemq.xml
# rpi2: chmod g+w /usr/share/apache-karaf/deploy
# copy jar from imac to raspberry (rpi2=192.168.2.9 jan 2020)
mvn clean install && scp /home/carl/IdeaProjects/mijnsensors_github/pilightMqttOsgi/target/pilightMqttOsgi-1.0-SNAPSHOT.jar pi@raspberrypi:/usr/share/karaf/deploy
scp ~/IdeaProjects/mijnsensors_github/pilightMqttOsgi/src/main/resources/nl/vandenzen.iot/pilightmqttosgi.properties pi@rpi3:/usr/share/karaf/etc/
# deprecated, needed if deploy is not writable by user pi: cp ~/gitrepos/mijnsensors/pilightMqttOsgi/ pi@rpi2:/usr/share/apache-karaf/deploy
# On raspberry:
#sudo -s -E -u openhab
#cp /home/pi/tmp/pilightMqttOsgi-1.0-SNAPSHOT.jar /usr/share/apache-karaf/deploy

Camel custom setting file (for pilight server ip address, pilight port and other settings): karaf home etc/pilightmqttosgi.properties:
# this property file should exist in a camel property file path, e.g. karaf/etc

Connect to openhab system (raspberry):
Terminal, ssh pi@192.168.2.3 (ssh pi@rpi3) (of pi@168.2.18 27 jan 2020 ethernetkabel)

openhab@raspberrypi:/home/pi/gitrepos/etc_openhab2$ gpio readall



bin/client to connect to running karaf. User is karaf, password ?????

sudo systemctl enable /usr/share/karaf/bin/contrib/karaf.service
And then client, repo-add etc (see sowewhere else in this README.md).
====================================================================================================================
Install Raspberry PI OS (previously called Raspbian) (oct 2020)
laptop Debian:
gparted (of lsblk)
unmount the disk partitions
sudo dd bs=4M if=<your image file>.img of=/dev/<disk# from lsblk>
(duurt ca 3min voor 1.8Gb, the lite version)
remove/reinsert card (it will be mounted automatically)
cd to mount point parent
touch boot/ssh
sudo vi rootfs/etc/hostname, set e.g. to rpi2
sudo vi rootfs/etc/dphys-swapfile rasp OS swap size default is 100Mb, too small
2000 Mb maken
sudo umount the 2 partitions (boot and rootfs)

rpi:
sudo apt-get update
sudo apt-get upgrade
====================================================================================================================
Install Java. sudo apt install default-jdk (oct 2020: java 11).
====================================================================================================================
Install influxdb (oct 2020: v1.6.4-1)
sudo apt install influxdb
sudo apt install influxdb-client
influxd backup -portable -database openhab -host ip6-localhost:8088 /tmp/mysnapshot
rsync -avz pi@xxx:/tmp/ldfk tmp dit naar de target
backup/restore ?
Backup: set port 8088, see https://docs.influxdata.com/influxdb/v1.6/administration/backup_and_restore
sudo vi /etc/influxdb/influxdb.conf, set backup to ip6-localhost:8088
influxd restore -portable tmp/mysnapshot
influx
CREATE USER admin WITH PASSWORD '<password>' WITH ALL PRIVILEGES (password from Lastpass)
create database "openhab"
CREATE USER grafana WITH PASSWORD '<password>' (password from Lastpass)
CREATE USER openhab WITH PASSWORD '<password>' (password from Lastpass)
GRANT READ ON "openhab" TO "grafana"
GRANT ALL ON "openhab" TO "openhab"
user list (add users, finally set login auth-enabled to yes, see lastpass for accounts)
====================================================================================================================
install openhab:
sudo apt install openhab2, openhab2-addons, see web site openhab.
https://www.openhab.org/docs/installation/linux.html#package-repository-installation:
backup/restore settings
sudo systemctl daemon-reload
sudo systemctl enable openhab2
delay, influxd must be up to retrieve the latest values for e.g. lightOnIntensity (if persisted)
systemctl edit openhab2,
[Service]
  TimeoutSec=infinity
  ExecStartPre=sleep 120
============================================================================
Install Karaf (for pilightMqttOsgi):
Download karaf jar. Untar in /usr/share/karaf (or make a symlink, might be better)

sudo adduser karaf
sudo adduser pi openhab
sudo adduser pi karaf
sudo adduser openhab kmem
sudo adduser openhab i2c
sudo adduser openhab gpio
sudo adduser openhab spi
sudo adduser karaf openhab
sudo adduser karaf kmem
sudo adduser karaf i2c
sudo adduser karaf gpio
sudo adduser karaf spi

sudo adduser artemis
sudo adduser pi artemis


chmod g+w /usr/share/karaf/etc
chmod g+w /usr/share/karaf/deploy

sudo chown -R karaf.karaf /usr/share/karaf apache-karaf-4.3.0
Install karaf as service in systemd in Linux: see web site karaf:
karaf runtime, documentation, Service Script Templates (NOT WRAPPER!)
Run in subdir bin/contrib sudo ./karaf-service.sh -k /usr/share/karaf
sudo vi karaf.service, change user/group to karaf / karaf
sudo cp karaf.service /lib/systemd/system
sudo systemctl enable karaf.service
# if already in use by e.g. openhab, change ssh port in systemctl edit (see next lines) from 8101 in e.g. 8102.
# and etc/jetty.xml change secure.port to e.g. 8444.
sudo systemctl edit karaf, add next lines:
[Service]
  Environment="ORG_APACHE_KARAF_SSH_SSHPORT=8102"
  TimeoutSec=infinity
  ExecStartPre=sleep 10
#==== end edit
====================================================================================================================
Python scripts for sensors connected to rpi (pi4j doesn't work with java 9)
sudo apt-get install python3
# https://linuxconfig.org/how-to-change-default-python-version-on-debian-9-stretch-linux
sudo update-alternatives --install /usr/bin/python python /usr/bin/python2.7 1
sudo update-alternatives --install /usr/bin/python python /usr/bin/python3.7 2
user pi, /home/pi, mkdir tmp/pj, cd !$, jar -xf /usr/share/karaf/deploy/pilightMqttOsgi-1.0-SNAPSHOT.jar
cp -a pythonScripts ~
cd ~
find . -name "*.py" -exec chmod ugo+x {} \;
sudo apt install python3-venv python3-pip i2c-tools
# raspi-config enable i2c
sudo pip3 install smbus
sudo pip3 install paho-mqtt
crontab entries:
bh1750 (light), bme280 (humidity, temperature, pressure), lcd display 2x16)
# Light sensor to mqtt
* * * * * /home/pi/pythonScripts/bh1750/bh1750mqtt.py
# temp/press/humidity bme280 sensor
* * * * * /home/pi/pythonScripts/bme280/bme280mqtt.py
====================================================================================================================
MySensors:
gateway on rpi:
git clone https://github.com/mysensors/MySensors.git
cd MySensors
edit MyConfig.h, #define MY_RFM69_NETWORKID (100): change to 197 (or in commandline, see next line)

./configure --my-transport=rfm69 --my-rfm69-frequency=868 \
--my-rfm69-networkid=197 \
--my-gateway=mqtt --my-controller-ip-address=127.0.0.1 \
--my-mqtt-publish-topic-prefix=mysensors/all \
--my-mqtt-subscribe-topic-prefix=+/mysensors \
--my-mqtt-client-id=mygateway1 \
--my-mqtt-user=mysensors \
--my-mqtt-password='<PASS>'
# <PASS>: see lastpass, activemq.xml, user mysensors, passwd. Do not use strange characters like #^&$ (?),
# shell/make can be confused.
make
sudo make install
# Maybe do this: systemctl edit mysgw:
[Service]
  TimeoutSec=infinity
  ExecStartPre=sleep 240
# edit /etc/mysensors.conf, set logging to syslog
====================================================================================================================
Install grafana, https://grafana.com/tutorials/install-grafana-on-raspberry-pi/#1
backup/restore ? /var/lib/grafana/grafana.db
/etc/grafana/grafana.ini set auth.anonymous enabled=yes
systemctl edit grafana-server
[Service]
  TimeoutSec=infinity
  ExecStartPre=sleep 300
=====================================================================================================================
Install karaf
repo-add camel x.y.z (oct 2020 version camel 3.6.0)
feature:install camel
# cellar distributed karaf support, not useful (summer 2020)
repo-add cellar
feature:install cellar

- karaf, install camel and camel-blueprint
Install the features with pilightmqttosgi-features.xml (scp/rsync to /usr/share/karaf/deploy)
feature:install pilightmqttosgi
#
# end of feature install commands
#


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


lcd:
4 bit mode: lcd display use d4..d7
backlight led: 5V 23mA (isolated from rest)
RS=register select
RW=H=read, L=write. USE WRITE ONLY, otherwise 5V will be supplied to raspberry pins!
pwm, resistor, transistor.
2019-03-11 BME280 added (i2c address 0x76, python bme280.py displays values TEMP HUM PRESSURE).
See https://www.raspberrypi-spy.co.uk/2016/07/using-bme280-i2c-temperature-pressure-sensor-in-python/

Lightsensor i2c address 0x23 BH1750

