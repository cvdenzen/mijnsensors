package nl.vandenzen.iot.beans;

import uk.pigpioj.PigpioSocket;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Lcddisplay {
    /*
#!/usr/bin/python

######################### Start of Adafruit library
#######################################################################################
        #######################################################################################
        #######################################################################################

        # Copyright (c) 2014 Adafruit Industries
# Author: Tony DiCola
#
        # Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
            #
            # The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
            #
            # THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
            # FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
            # OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.
            import time

import Adafruit_GPIO as GPIO
import Adafruit_GPIO.I2C as I2C
import Adafruit_GPIO.MCP230xx as MCP
import Adafruit_GPIO.PWM as PWM
*/

//Commands

    final static short LCD_CLEARDISPLAY = 0x01;
    final static short LCD_RETURNHOME = 0x02;
    final static short LCD_ENTRYMODESET = 0x04;
    final static short LCD_DISPLAYCONTROL = 0x08;
    final static short LCD_CURSORSHIFT = 0x10;
    final static short LCD_FUNCTIONSET = 0x20;
    final static short LCD_SETCGRAMADDR = 0x40;
    final static short LCD_SETDDRAMADDR = 0x80;
    private final short cols;
    private final short lines;
    private final short rs;
    private final short en;
    private final short d4;
    private final short d5;
    private final short d6;
    private final short d7;
    private final PigpioSocket gpio;
    private final short backlight;
    private final boolean blpol;
    private int displaycontrol;
    private final int displayfunction;
    private int displaymode;
    // Entry flags;
    final static short LCD_ENTRYRIGHT = 0x00;
    final static short LCD_ENTRYLEFT = 0x02;
    final static short LCD_ENTRYSHIFTINCREMENT = 0x01;
    final static short LCD_ENTRYSHIFTDECREMENT = 0x00;
    //Control flags;
    final static short LCD_DISPLAYON = 0x04;
    final static short LCD_DISPLAYOFF = 0x00;
    final static short LCD_CURSORON = 0x02;
    final static short LCD_CURSOROFF = 0x00;
    final static short LCD_BLINKON = 0x01;
    final static short LCD_BLINKOFF = 0x00;
    // Move flags;
    final static short LCD_DISPLAYMOVE = 0x08;
    final static short LCD_CURSORMOVE = 0x00;
    final static short LCD_MOVERIGHT = 0x04;
    final static short LCD_MOVELEFT = 0x00;
    //Function set flags;
    final static short LCD_8BITMODE = 0x10;
    final static short LCD_4BITMODE = 0x00;
    final static short LCD_2LINE = 0x08;
    final static short LCD_1LINE = 0x00;
    final static short LCD_5x10DOTS = 0x04;
    final static short LCD_5x8DOTS = 0x00;
    //Offset for up to 4 rows.;
    final static short[] LCD_ROW_OFFSETS = {0x00, 0x40, 0x14, 0x54};
    //Char LCD plate GPIO numbers.;
    final static short LCD_PLATE_RS = 15;
    final static short LCD_PLATE_RW = 14;
    final static short LCD_PLATE_EN = 13;
    final static short LCD_PLATE_D4 = 12;
    final static short LCD_PLATE_D5 = 11;
    final static short LCD_PLATE_D6 = 10;
    final static short LCD_PLATE_D7 = 9;
    final static short LCD_PLATE_RED = 6;
    final static short LCD_PLATE_GREEN = 7;
    final static short LCD_PLATE_BLUE = 8;
    //Char LCD plate button names.;
    final static short SELECT = 0;
    final static short RIGHT = 1;
    final static short DOWN = 2;
    final static short UP = 3;
    final static short LEFT = 4;
    //Char LCD backpack GPIO numbers.;
    final static short LCD_BACKPACK_RS = 1;
    final static short LCD_BACKPACK_EN = 2;
    final static short LCD_BACKPACK_D4 = 3;
    final static short LCD_BACKPACK_D5 = 4;
    final static short LCD_BACKPACK_D6 = 5;
    final static short LCD_BACKPACK_D7 = 6;
    final static short LCD_BACKPACK_LITE = 7;

    //  class Adafruit_CharLCD(object):
    //   """Class to represent and interact with an HD44780 character LCD display."""


    public Lcddisplay(String rs, String en, String d4, String d5, String d6, String d7, String cols, String lines, String backlight) {
        this(Short.decode(rs), Short.decode(en), Short.decode(d4), Short.decode(d5),
                Short.decode(d6), Short.decode(d7), Short.decode(cols), Short.decode(lines), Short.decode(backlight));
    }

    Lcddisplay(short rs, short en, short d4, short d5, short d6, short d7, short cols, short lines, short backlight) {

        /*
            """Initialize the LCD.  RS, EN, and D4...D7 parameters should be the pins
        connected to the LCD RS, clock enable, and data line 4 through 7 connections.
        The LCD will be used in its 4-bit mode so these 6 lines are the only ones
        required to use the LCD.  You must also pass in the number of columns and
        lines on the LCD.
        If you would like to control the backlight, pass in the pin connected to
        the backlight with the backlight parameter.  The invert_polarity boolean
        controls if the backlight is one with a LOW signal or HIGH signal.  The
        default invert_polarity value is True, i.e. the backlight is on with a
        LOW signal.
        You can enable PWM of the backlight pin to have finer control on the
        brightness.  To enable PWM make sure your hardware supports PWM on the
        provided backlight pin and set enable_pwm to True (the default is False).
        The appropriate PWM library will be used depending on the platform, but
        you can provide an explicit one with the pwm parameter.
        The initial state of the backlight is ON, but you can set it to an
        explicit initial state with the initial_backlight parameter (0 is off,
        1 is on/full bright).
        You can optionally pass in an explicit GPIO class,
        for example if you want to use an MCP230xx GPIO extender.  If you don't
        pass in an GPIO instance, the default GPIO for the running platform will
        be used.
        """
            # Save column and line state.

         */
        this.gpio = new PigpioSocket();
        try {
            gpio.connect("ip6-localhost");
        } catch (InterruptedException ignored) {
        }
        this.cols = cols;
        this.lines = lines;
        /*
    Save GPIO
    state and
    pin numbers.

         */
        this.rs = rs;
        this.en = en;
        this.d4 = d4;
        this.d5 = d5;
        this.d6 = d6;
        this.d7 = d7;
       /* #
    Save backlight
    state.*/
        this.backlight = backlight;
        this.blpol = false; // in my case, high=led on. Don't know how that translates to blpol
        /*
    Setup all pins as outputs.
    */
        for (short pin : new short[]{rs, en, d4, d5, d6, d7, backlight}) {
            gpio.setMode(pin, 1);
        }
        //Initialize the display.
        this.write8((short) 0x33, false);
        this.write8((short) 0x32, false);
//            #
//            Initialize display
//            control, function,
//                    and mode
//            registers.
        this.displaycontrol = LCD_DISPLAYON | LCD_CURSOROFF | LCD_BLINKOFF;
        this.displayfunction = LCD_4BITMODE | LCD_1LINE | LCD_2LINE | LCD_5x8DOTS;
        this.displaymode = LCD_ENTRYLEFT | LCD_ENTRYSHIFTDECREMENT;
//        #
//            Write registers.
        this.write8((short) (LCD_DISPLAYCONTROL | this.displaycontrol));
        this.write8((short) (LCD_FUNCTIONSET | this.displayfunction));
        this.write8((short) (LCD_ENTRYMODESET | this.displaymode));
//            set the
//            entry mode
        this.clear();
        setBacklight("128"); // half light
    }

    public void home() {
//        """Move the cursor back to its home (first line and first column)."""
        this.write8(LCD_RETURNHOME); //#set cursor position to zero
        this.delay_microseconds(3000);  //#this command takes a long time !
    }

    public void clear() {
//        """Clear the LCD."""
        this.write8(LCD_CLEARDISPLAY);  //#command to clear display
        this.delay_microseconds(3000); //#3000 microsecond sleep, clearing the display takes a long time
    }

    private void set_cursor(short col, short row) {
//        """Move the cursor to an explicit column and row position."""
//            #Clamp row to the last row of the display.
        if (row > this.lines) {
            row = (short) (this.lines - 1);
        }
//            #Set location.
        short b = (short) (LCD_SETDDRAMADDR | (col + LCD_ROW_OFFSETS[row]));
        this.write8(b);
    }

    /**
     * Set cursor position. Warning: not guarantee for sync with other
     * threads that may print messages on the screen and leave you
     * at another position on the screen than set by your call to set_cursor.
     *
     * @param pos col,line
     */
    private synchronized void set_cursor(String pos) {
        log.log(Level.INFO, "set_cursor, pos=" + pos);
        String[] s1 = pos.split(",", 2);
        set_cursor(Short.decode(s1[0]), Short.decode(s1[1]));
    }

    void enable_display(boolean enable) {
//        """Enable or disable the display.  Set enable to True to enable."""
        if (enable) {
            this.displaycontrol |= LCD_DISPLAYON;
        } else {
            this.displaycontrol &= ~LCD_DISPLAYON;
            this.write8((short) (LCD_DISPLAYCONTROL | this.displaycontrol));
        }
    }

    void show_cursor(boolean show) {
//        """Show or hide the cursor.  Cursor is shown if show is True."""
        if (show) {
            this.displaycontrol |= LCD_CURSORON;
        } else {
            this.displaycontrol &= ~LCD_CURSORON;
            this.write8((short) (LCD_DISPLAYCONTROL | this.displaycontrol));
        }
    }

    ;

    void blink(boolean blink) {
//        """Turn on or off cursor blinking.  Set blink to True to enable blinking."""
        if (blink) {
            this.displaycontrol |= LCD_BLINKON;
        } else {
            this.displaycontrol &= ~LCD_BLINKON;
            this.write8((short) (LCD_DISPLAYCONTROL | this.displaycontrol));
        }
    }

    ;

    void move_left() {
//        """Move display left one position."""
        this.write8((short) (LCD_CURSORSHIFT | LCD_DISPLAYMOVE | LCD_MOVELEFT));
    }

    void move_right() {
//                           """Move display right one position."""
        write8((short) (LCD_CURSORSHIFT | LCD_DISPLAYMOVE | LCD_MOVERIGHT));
    }

    void set_left_to_right() {
//                                  """Set text direction left to right."""
        this.displaymode |= LCD_ENTRYLEFT;
        this.write8((short) (LCD_ENTRYMODESET | this.displaymode));
    }

    void set_right_to_left() {
//                                  """Set text direction right to left."""
        this.displaymode &= ~LCD_ENTRYLEFT;
        this.write8((short) (LCD_ENTRYMODESET | this.displaymode));
    }

    void autoscroll(boolean autoscroll) {
//            """Autoscroll will 'right justify' text from the cursor if set True,
//        otherwise it will 'left justify' the text.
//        """
        if (autoscroll) {
            this.displaymode |= LCD_ENTRYSHIFTINCREMENT;
        } else {
            this.displaymode &= ~LCD_ENTRYSHIFTINCREMENT;
        }
        write8((short) (LCD_ENTRYMODESET | this.displaymode));
    }

    private synchronized void message(String text) {
//            """Write text to display.  Note that text can include newlines."""
        log.info("message, message=" + text);
        short line = 0;
//            #  Iterate through each character.
        int a = 5;
        for (byte b1 : text.getBytes()) {
//            # Advance to next line if character is a new line.
            if (b1 == '\n') {
                line += 1;
//            # Move to left or right side depending on text direction.
                short col = 0;
                if ((this.displaymode & LCD_ENTRYLEFT) > 0) col = 0;
                else col = (short) (this.cols - 1);
                set_cursor(col, line);
            }
//        # Write the character to the display.
            else {
                this.write8((short) (b1), true);
            }
        }
    }

    /**
     * @param csvMessage: col,line,message
     */
    public void messageAt(String csvMessage) {
        lock1.lock();
        try {
            String[] s1 = csvMessage.split(",", 3);
            short col = Short.decode(s1[0]);
            short line = Short.decode(s1[1]);
            String message = s1[2];
            set_cursor(col, line);
            message(message);
        }
        finally {
            lock1.unlock();
        }
    }

    /**
     * @param csvMessageChar A String like "8,1,223" will print a degree sign
     *                       at col 8 in line 1
     */
    public void messageCharAt(String csvMessageChar) {
        lock1.lock();
        String[] s1=csvMessageChar.split(",",3);
        try {
            set_cursor(Short.decode(s1[0]), Short.decode(s1[1]));
            write8(Short.decode(s1[2]), true);
        } finally {
            lock1.unlock();
        }
    }

    /**
     * @param dutyCycle 0..255 (default, can be changed by setRange)
     *                  Set backlight. dutyCycle might be from 0..255 ?
     */
    void setBacklight(String dutyCycle) {
//        """
        gpio.setPWMDutyCycle(backlight, Integer.parseInt(dutyCycle));
    }

    void write8(short value, boolean charMode) {
//        """Write 8-bit value in character or data mode.  Value should be an int
//        value from 0-255, and char_mode is True if character data or False if
//        non-character data (default).
//        """
//            #One millisecond delay to prevent writing too quickly.
        try {
            Thread.sleep(3);
        } catch (InterruptedException ex) {
        }
//        # Set character/data bit.
        gpio.write(this.rs, charMode);
        //            #
        //    Write upper 4bits .
        //                this.gpio.output_pins(
        //
        //    {
        //        this.d4:((value >> 4) & 1) > 0,
        //            this.d5:((value >> 5) & 1) > 0,
        //            this.d6:((value >> 6) & 1) > 0,
        //            this.d7:((value >> 7) & 1) > 0
        //    })
        short[] datapins = {d4, d5, d6, d7};

        for (short i = 0; i < datapins.length; i++) {
            gpio.write(datapins[i], ((value >> (4 + i)) & 1) > 0);
        }
        this.pulse_enable();
//        #
//        Write lower 4 bits.
//                this.gpio.output_pins(
//
//                {
//                        this.d4:(value & 1) > 0,
//                this.d5:((value >> 1) & 1) > 0,
//                this.d6:((value >> 2) & 1) > 0,
//                this.d7:((value >> 3) & 1) > 0
//    })

        for (short i = 0; i < datapins.length; i++) {
            gpio.write(datapins[i], ((value >> (i)) & 1) > 0);
        }
        this.pulse_enable();
    }

    void write8(short value) {
        write8(value, false);
    }

    void create_char(short location, short[] pattern) {
//        """Fill one of the first 8 CGRAM locations with custom characters.
//        The location parameter should be between 0 and 7 and pattern should
//        provide an array of 8 bytes containing the pattern. E.g. you can easyly
//        design your custom character at http://www.quinapalus.com/hd44780udg.html
//        To show your custom character use eg. lcd.message('\x01')
//        """
//            #
//        only position 0. .7
//        are allowed
        location &= 0x7;
        write8((short) (LCD_SETCGRAMADDR | (location << 3)), false);
//        for
//        i in
//
//        range(8):
        for (short i = 0; i < 8; i++) {
            this.write8(pattern[i], true);
        }
    }

    void delay_microseconds(int microseconds) {
//            #
//            Busy wait
//            in loop
//            because delays
//            are generally
//            very
//
//    short(few microseconds).
        long millis = microseconds / 1_000;
        microseconds = microseconds % 1_000; // nanos is max 999999
        try {
            Thread.sleep(0L, 1000 * microseconds);
        } catch (InterruptedException ex) {
        }
        ;
    }

    void pulse_enable() {
//                              #Pulse the clock enable line off, on, off to send command.
        this.gpio.write(this.en, false);
        this.delay_microseconds(1);
//                #1
//        microsecond pause -
//                enable pulse
//        must be>450 ns
        this.gpio.write(this.en, true);
        this.delay_microseconds(1);
//        #1
//        microsecond pause -
//                enable pulse
//        must be>450 ns
        this.gpio.write(this.en, false);
        this.delay_microseconds(1);
        //commands need>37 us to settle
    }

    private static Logger log = Logger.getLogger(Lcddisplay.class.getSimpleName());
    private Lock lock1 = new ReentrantLock();
}
