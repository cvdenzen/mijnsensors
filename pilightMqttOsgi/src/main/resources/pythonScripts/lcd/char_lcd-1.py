#!/usr/bin/python
# Example using a character LCD connected to a Raspberry Pi or BeagleBone Black.
import time

import Adafruit_CharLCD as LCD


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
# lcd_backlight invert polarity is false in my case nov 2020 rpi3
lcd = LCD.Adafruit_CharLCD(lcd_rs, lcd_en, lcd_d4, lcd_d5, lcd_d6, lcd_d7,
                           lcd_columns, lcd_rows, lcd_backlight, False)
#lcd.clear()
# Print a two line message
lcd.message('Hello\nworld!')

# Wait 5 seconds
time.sleep(10.0)

# Demo showing the cursor.
#lcd.clear()
#lcd.show_cursor(True)
#lcd.message('Show cursor')

#time.sleep(5.0)

# Demo showing the blinking cursor.
#lcd.clear()
#lcd.blink(True)
#lcd.message('Blink cursor')

#time.sleep(5.0)

# Stop blinking and showing cursor.
#lcd.show_cursor(False)
#lcd.blink(False)

# Demo turning backlight off and on.
lcd.clear()
lcd.message('Flash backlight\nin 5 seconds...')
time.sleep(5.0)
# Turn backlight off.
lcd.set_backlight(0)
time.sleep(2.0)
# Change message.
#lcd.clear()
lcd.message('Goodbye!')
# Turn backlight on.
lcd.set_backlight(1)
time.sleep(5.0)
lcd.set_backlight(0)
