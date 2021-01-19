#!/bin/bash
# Deprecated, use systemctl with different parameters for script name?
#
# Part of Carl van Denzen Home Automation
#
# Start python scripts
#
basedir=/home/pi/pythonScripts
$basedir/bh1750/bh1750mqtt.py &
$basedir/bme280/bme280mqtt.py &
$basedir/lcd/char_lcd_mqtt.py &
