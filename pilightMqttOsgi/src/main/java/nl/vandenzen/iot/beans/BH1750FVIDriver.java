package nl.vandenzen.iot.beans;
//package io.github.s5uishida.iot.device.bh1750fvi.driver;

import uk.pigpioj.PigpioSocket;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Refer to https://www.mouser.com/datasheet/2/348/bh1750fvi-e-186247.pdf
 *
 * @author s5uishida
 *
 */
public class BH1750FVIDriver {
    private static final Logger LOG = Logger.getLogger(BH1750FVIDriver.class.getName());

    public static final byte I2C_ADDRESS_23 = 0x23;
    public static final byte I2C_ADDRESS_5C = 0x5c;

    private static final byte OPECODE_POWER_DOWN							= 0x00;
    private static final byte OPECODE_POWER_ON								= 0x01;
    private static final byte OPECODE_RESET									= 0x07;
    private static final byte OPECODE_CONTINUOUSLY_H_RESOLUTION_MODE		= 0x10;
    private static final byte OPECODE_CONTINUOUSLY_H_RESOLUTION_MODE2	= 0x11;
    private static final byte OPECODE_CONTINUOUSLY_L_RESOLUTION_MODE		= 0x13;
    private static final byte OPECODE_ONE_TIME_H_RESOLUTION_MODE			= 0x20;
    private static final byte OPECODE_ONE_TIME_H_RESOLUTION_MODE2		= 0x21;
    private static final byte OPECODE_ONE_TIME_L_RESOLUTION_MODE			= 0x23;

    private static final int H_RESOLUTION_MODE_MEASUREMENT_TIME_MILLIS	= 120;
    private static final int H_RESOLUTION_MODE2_MEASUREMENT_TIME_MILLIS	= 120;
    private static final int L_RESOLUTION_MODE_MEASUREMENT_TIME_MILLIS	= 16;

    private static final int SENSOR_DATA_LENGTH = 2;

    private final byte i2cAddress;
    private PigpioSocket i2cDevice;
    private final String i2cName;
    private final String logPrefix;

    private final AtomicInteger useCount = new AtomicInteger(0);

    private static final ConcurrentHashMap<String, BH1750FVIDriver> map = new ConcurrentHashMap<String, BH1750FVIDriver>();
    private final int i2cBusNumber;
    private int i2cHandle;

    synchronized public static BH1750FVIDriver getInstance(int i2cBusNumber, byte i2cAddress) {
        String key = i2cBusNumber + ":" + String.format("%x", i2cAddress);
        BH1750FVIDriver bh1750fvi = map.get(key);
        if (bh1750fvi == null) {
            bh1750fvi = new BH1750FVIDriver(i2cBusNumber, i2cAddress);
            map.put(key, bh1750fvi);
        }
        return bh1750fvi;
    }

    /**
     * Overload method because Blueprint attribute type for factory-method doesn't work, but maybe because this
     * constructor was not public
     * @param i2cBusNumber
     * @param i2cAddress
     * @return
     */
    synchronized public static BH1750FVIDriver getInstance(String i2cBusNumber, String i2cAddress) {
        return getInstance(Integer.parseInt(i2cBusNumber),Byte.decode(i2cAddress));
    }


    private BH1750FVIDriver(int i2cBusNumber, byte i2cAddress) {
        if (i2cBusNumber == 0 || i2cBusNumber == 1) {
            this.i2cBusNumber = i2cBusNumber;
        } else {
            throw new IllegalArgumentException("The set " + i2cBusNumber + " is not " +
                    0 + " or " + 1 + ".");
        }
        if (i2cAddress == I2C_ADDRESS_23 || i2cAddress == I2C_ADDRESS_5C) {
            this.i2cAddress = i2cAddress;
        } else {
            throw new IllegalArgumentException("The set " + String.format("%x", i2cAddress) + " is not " +
                    String.format("%x", I2C_ADDRESS_23) + " or " + String.format("%x", I2C_ADDRESS_5C) + ".");
        }

        i2cName = "I2C_" + i2cBusNumber + "_" + String.format("%x", i2cAddress);
        logPrefix = "[" + i2cName + "] ";

        try {
            i2cDevice = new PigpioSocket();
            ((PigpioSocket)i2cDevice).connect("ip6-localhost");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE,logPrefix + ex);
        }
    }

    synchronized public void open() throws IOException {
        try {
            LOG.log(Level.FINE,logPrefix + "before - useCount:{}", useCount.get());
            if (useCount.compareAndSet(0, 1)) {

                i2cHandle = i2cDevice.i2cOpen(i2cBusNumber, i2cAddress, 0);
                LOG.info(logPrefix + "opened");
            }
        } finally {
            LOG.log(Level.FINE,logPrefix + "after - useCount:{}", useCount.get());
        }
    }

    synchronized public void close() throws IOException {
        try {
            LOG.log(Level.FINE,logPrefix + "before - useCount:{}", useCount.get());
            if (useCount.compareAndSet(1, 0)) {
                i2cDevice.close();
                LOG.info(logPrefix + "closed");
            }
        } finally {
            LOG.log(Level.FINE,logPrefix + "after - useCount:{}", useCount.get());
        }
    }

    public int getI2cBusNumber() {
        return i2cBusNumber;
    }

    public byte getI2cAddress() {
        return i2cAddress;
    }

    public String getName() {
        return i2cName;
    }

    public String getLogPrefix() {
        return logPrefix;
    }

    private void dump(byte data, String tag) {
        if (LOG.isLoggable(Level.FINEST)) {
            StringBuffer sb = new StringBuffer();
            sb.append(String.format("%02x", data));
            LOG.log(Level.FINEST,logPrefix + "{}{}", new String[]{tag, sb.toString()});
        }
    }

    private void dump(byte[] data, String tag) {
        if (LOG.isLoggable(Level.FINEST)) {
            StringBuffer sb = new StringBuffer();
            for (byte data1 : data) {
                sb.append(String.format("%02x ", data1));
            }
            LOG.log(Level.FINEST,logPrefix + "{}{}", new String[]{tag, sb.toString().trim()});
        }
    }

    private void write(byte out) throws IOException {
        try {
            dump(out, "BH1750FVI sensor command: write: ");
            i2cDevice.i2cWriteByte(i2cHandle,out);
        } catch (Exception e) {
            String message = logPrefix + "failed to write.";
            LOG.log(Level.WARNING,message);
            throw new IOException(message, e);
        }
    }

    private byte[] read(int length) throws IOException {
        byte[] in = new byte[length];
        try {
            // original pi4j: i2cDevice.read(in, 0, length);
            //byte in = (byte) i2cDevice.i2cReadByteData(i2cHandle,(int)register&0xff);
            //int r1=i2cDevice.i2cReadI2CBlockData(i2cHandle, (int)register&0xff, in, length);
            i2cDevice.i2cReadDevice(i2cHandle,in, length);
            dump(in, "BH1750FVI sensor command: read:  ");
        } catch (Exception e) {
            String message = logPrefix + "failed to read.";
            LOG.log(Level.WARNING,message);
            throw new IOException(message, e);
        }
        return in;
    }

    public float getOptical() throws IOException {
        write(OPECODE_ONE_TIME_H_RESOLUTION_MODE);

        try {
            Thread.sleep(H_RESOLUTION_MODE_MEASUREMENT_TIME_MILLIS);
        } catch (InterruptedException e) {
        }

        byte[] data = read(SENSOR_DATA_LENGTH);

        return (float)((((int)(data[0] & 0xff) << 8) + (int)(data[1] & 0xff)) / 1.2);
    }

    /******************************************************************************************************************
     * Sample main
     ******************************************************************************************************************/
    public static void main(String[] args) throws IOException {
        BH1750FVIDriver bh1750fvi = null;
        try {
            bh1750fvi = BH1750FVIDriver.getInstance(1, BH1750FVIDriver.I2C_ADDRESS_23);
            bh1750fvi.open();

            while (true) {
                float value = bh1750fvi.getOptical();
                LOG.info("optical:" + value);

                Thread.sleep(10000);
            }
        } catch (InterruptedException e) {
            LOG.log(Level.WARNING,"caught - {}", e.toString());
        } catch (IOException e) {
            LOG.log(Level.WARNING,"caught - {}", e.toString());
        } finally {
            if (bh1750fvi != null) {
                bh1750fvi.close();
            }
        }
    }
}
