package nl.vandenzen.pilightmqttosgi;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.platform.Platform;
import com.pi4j.platform.PlatformAlreadyAssignedException;
import com.pi4j.platform.PlatformManager;

import java.io.IOException;
import java.math.BigDecimal;

public class LightSensorReaderBH1750 {
    /*
     * #%L
     * **********************************************************************
     * ORGANIZATION  :  Pi4J
     * PROJECT       :  Pi4J :: Java Examples
     * FILENAME      :  I2CExample.java
     *
     * This file is part of the Pi4J project. More information about
     * this project can be found here:  http://www.pi4j.com/
     * **********************************************************************
     * %%
     * Copyright (C) 2012 - 2018 Pi4J
     * %%
     * This program is free software: you can redistribute it and/or modify
     * it under the terms of the GNU Lesser General Public License as
     * published by the Free Software Foundation, either version 3 of the
     * License, or (at your option) any later version.
     *
     * This program is distributed in the hope that it will be useful,
     * but WITHOUT ANY WARRANTY; without even the implied warranty of
     * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     * GNU General Lesser Public License for more details.
     *
     * You should have received a copy of the GNU General Lesser Public
     * License along with this program.  If not, see
     * <http://www.gnu.org/licenses/lgpl-3.0.html>.
     * #L%
     */

    /**
     * This example code demonstrates how to perform simple I2C
     * communication on the BananaPro.  For this example we will
     * connect to a 'TSL2561' LUX sensor.
     * <p>
     * Data Sheet:
     * https://www.adafruit.com/datasheets/TSL256x.pdf
     * <p>
     * You should get something similar printed in the console
     * when executing this program:
     * <p>
     * > <--Pi4J--> I2C Example ... started.
     * > ... reading ID register from TSL2561
     * > TSL2561 ID = 0x50 (should be 0x50)
     * > ... powering up TSL2561
     * > ... reading DATA registers from TSL2561
     * > TSL2561 DATA 0 = 0x1e
     * > TSL2561 DATA 1 = 0x04
     * > ... powering down TSL2561
     * > Exiting I2CExample
     *
     * @author Robert Savage
     */
    //public class I2CExample {
/*
pi@raspberrypi:~ $ sudo i2cdetect -y 1
     0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f
00:          -- -- -- -- -- -- -- -- -- -- -- -- --
10: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
20: -- -- -- 23 -- -- -- -- -- -- -- -- -- -- -- --
30: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
40: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
50: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
60: -- -- -- -- -- -- -- -- 68 69 6a 6b -- -- -- --
70: -- -- -- -- -- -- -- --

23 is light meter BH1750FVI
 */
    // TSL2561 I2C address
    public static final int BH1750FVI_ADDR = 0x23;

    // TSL2561 registers
    public static final byte TSL2561_REG_ID = (byte) 0x8A;
    public static final byte TSL2561_REG_DATA_0 = (byte) 0x8C;
    public static final byte TSL2561_REG_DATA_1 = (byte) 0x8E;
    public static final byte TSL2561_REG_CONTROL = (byte) 0x80;

    // TSL2561 power control values
    public static final byte TSL2561_POWER_UP = (byte) 0x03;
    public static final byte TSL2561_POWER_DOWN = (byte) 0x00;

    /**
     * Program Main Entry Point
     *
     * @param args
     * @throws InterruptedException
     * @throws PlatformAlreadyAssignedException
     * @throws IOException
     * @throws UnsupportedBusNumberException
     */


    /*
    from: https://bitbucket.org/MattHawkinsUK/rpispy-misc/raw/master/python/bh1750.py
    # Define some constants from the datasheet

DEVICE     = 0x23 # Default device I2C address

POWER_DOWN = 0x00 # No active state
POWER_ON   = 0x01 # Power on
RESET      = 0x07 # Reset data register value

# Start measurement at 4lx resolution. Time typically 16ms.
CONTINUOUS_LOW_RES_MODE = 0x13
# Start measurement at 1lx resolution. Time typically 120ms
CONTINUOUS_HIGH_RES_MODE_1 = 0x10
# Start measurement at 0.5lx resolution. Time typically 120ms
CONTINUOUS_HIGH_RES_MODE_2 = 0x11
# Start measurement at 1lx resolution. Time typically 120ms
# Device is automatically set to Power Down after measurement.
ONE_TIME_HIGH_RES_MODE_1 = 0x20
# Start measurement at 0.5lx resolution. Time typically 120ms
# Device is automatically set to Power Down after measurement.
ONE_TIME_HIGH_RES_MODE_2 = 0x21
# Start measurement at 1lx resolution. Time typically 120ms
# Device is automatically set to Power Down after measurement.
ONE_TIME_LOW_RES_MODE = 0x23

#bus = smbus.SMBus(0) # Rev 1 Pi uses 0
bus = smbus.SMBus(1)  # Rev 2 Pi uses 1

def convertToNumber(data):
  # Simple function to convert 2 bytes of data
  # into a decimal number. Optional parameter 'decimals'
  # will round to specified number of decimal places.
  result=(data[1] + (256 * data[0])) / 1.2
  return (result)

def readLight(addr=DEVICE):
  # Read data from I2C interface
  data = bus.read_i2c_block_data(addr,ONE_TIME_HIGH_RES_MODE_1)
  return convertToNumber(data)

def main():

  while True:
    lightLevel=readLight()
    print("Light Level : " + format(lightLevel,'.2f') + " lx")
    time.sleep(0.5)
     */
    private I2CDevice device;

    LightSensorReaderBH1750(I2CBus bus) throws IOException {
        device = bus.getDevice(0x23);
    }

    void init() {
        try {
            device.write((byte) 0x10);  //11x resolution 120ms

        } catch (IOException ignore) {
            ignore.printStackTrace();
        }
    }

    public BigDecimal read() {
        byte[] p = new byte[2];

        int r;
        try {
            r = device.read(p, 0, 2);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        if (r != 2) {
            throw new IllegalStateException("Read Error; r=" + r);
        }
        return new BigDecimal((p[0] << 8) | p[1]);
    }
}