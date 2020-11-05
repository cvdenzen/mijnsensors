#!/usr/bin/python
# Example using a character LCD connected to a Raspberry Pi or BeagleBone Black.
# mqtt listener:
# topic:
# +/f1_nw/display/message
# +/f1_nw/display/backlight (0 or 1)
# +/f1_nw/display/command
#  clear (dummy payload)
#  cursor (True, False)
#  blink (True, False)
#  move_left (dummy payload, shift one position left)
#  move_right (dummy payload, shift one position right)
#
# https://www.eclipse.org/paho/index.php?page=clients/python/docs/index.php
# http://www.steves-internet-guide.com/loop-python-mqtt-client/
import time

import Adafruit_CharLCD as LCD
import paho.mqtt.client as mqtt #import the client1


# Raspberry Pi pin configuration:
lcd_rs        = 20  # Note this might need to be changed to 21 for older revision Pi's.
lcd_en        = 16
lcd_d4        = 6
lcd_d5        = 13
lcd_d6        = 19
lcd_d7        = 26
lcd_backlight = 12

# Define LCD column and row size for 16x2 LCD.
lcd_columns = 16
lcd_rows    = 2

# Initialize the LCD using the pins above.
lcd = LCD.Adafruit_CharLCD(lcd_rs, lcd_en, lcd_d4, lcd_d5, lcd_d6, lcd_d7,
                           lcd_columns, lcd_rows, lcd_backlight,False)


# Print a two line message
lcd.message('Hello\nworld!')
# Wait 5 seconds

def on_disconnect(client, userdata,rc=0):
    #logging.debug("DisConnected result code "+str(rc))
    client.loop_stop()

def on_message(client, userdata, message):
    print("message received " ,str(message.payload.decode("utf-8")))
    print("message topic=",message.topic)
    print("message qos=",message.qos)
    print("message retain flag=",message.retain)

def on_message_command_clear(client, userdata, message):
    lcd.clear()

def on_message_command_cursor(client, userdata, message):
    lcd.cursor(str(message.payload.decode("utf-8")))

def on_message_command_blink(client, userdata, message):
    lcd.blink(str(message.payload.decode("utf-8")))

def on_message_command_move_left(client, userdata, message):
    lcd.move_left()

def on_message_command_move_right(client, userdata, message):
    lcd.move_right()

def on_message_display_message(client, userdata, message):
    lcd.message(str(message.payload.decode("iso-8859-1")))

def on_message_display_backlight(client, userdata, message):
    lcd.set_backlight(int(message.payload.decode("utf-8")))
    print("backlight message received " ,str(message.payload.decode("utf-8")))
    print("backlight message topic=",message.topic)
    print("backlight message qos=",message.qos)
    print("backlight message retain flag=",message.retain)

def main():
    broker_address="127.0.0.1"
    #broker_address="iot.eclipse.org" #use external broker
    client = mqtt.Client("f1_nw_rpi3_display_carl") #create new instance
    client.connect(broker_address) #connect to broker
    client.subscribe([("+/f1_nw/display/message",0), ("+/f1_nw/display/backlight",0),("+/f1_nw/display/command/#",0)])
    client.message_callback_add("+/f1_nw/display/message",on_message_display_message)
    client.message_callback_add("+/f1_nw/display/backlight",on_message_display_backlight)
    client.message_callback_add("+/f1_nw/display/command/clear",on_message_command_clear)
    client.message_callback_add("+/f1_nw/display/command/cursor",on_message_command_cursor)
    client.message_callback_add("+/f1_nw/display/command/blink",on_message_command_blink)
    client.message_callback_add("+/f1_nw/display/command/move_left",on_message_command_move_left)
    client.message_callback_add("+/f1_nw/display/command/move_right",on_message_command_move_right)

    client.loop_start()


    while True:
        time.sleep(10.0)

if __name__=="__main__":
    main()
