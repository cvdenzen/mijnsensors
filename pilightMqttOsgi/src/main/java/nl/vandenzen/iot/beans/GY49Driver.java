package nl.vandenzen.iot.beans;

/*
Carl van Denzen, march 2025, copied from BME280Driver
Some adjustions made for:
- pigpio
- Apache Camel
*/
// Thanks to:
//package io.github.s5uishida.iot.device.bme280.driver;

//import org.slf4j.Logger;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import uk.pigpioj.PigpioSocket;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Math.pow;
import static java.lang.Thread.sleep;

/*
 * Refer to http://static.cactus.io/docs/sensors/barometric/bme280/BST-BME280_DS001-10.pdf
 *
 * @author s5uishida
 *
 */
public class GY49Driver {
    private static final Logger LOG = Logger.getLogger(GY49Driver.class.getName());

    private final byte i2cAddress;
    private final String i2cName;
    private final String logPrefix;

    private final AtomicInteger useCount = new AtomicInteger(0);

    private static final ConcurrentHashMap<String, GY49Driver> map = new ConcurrentHashMap<String, GY49Driver>();
    private final int i2cBusNumber;
    private PigpioSocket i2cDevice;
    private int i2cHandle;

    synchronized public static GY49Driver getInstance(int i2cBusNumber, byte i2cAddress) {
        String key = i2cBusNumber + ":" + String.format("%x", i2cAddress);
        GY49Driver gy49 = map.get(key);
        if (gy49 == null) {
            gy49 = new GY49Driver(i2cBusNumber, i2cAddress);
            map.put(key, gy49);
        }
        return gy49;
    }

    /**
     * Overload method because Blueprint attribute type for factory-method doesn't work, but maybe because this
     * constructor was not public
     * @param i2cBusNumber
     * @param i2cAddress
     * @return
     */
    synchronized public static GY49Driver getInstance(String i2cBusNumber, String i2cAddress) {
        return getInstance(Integer.parseInt(i2cBusNumber),Byte.decode(i2cAddress));
    }

    private GY49Driver(int i2cBusNumber, byte i2cAddress) {
        if (i2cBusNumber == 0 || i2cBusNumber == 1) {
            this.i2cBusNumber = i2cBusNumber;
        } else {
            throw new IllegalArgumentException("The set " + i2cBusNumber + " is not " +
                    0 + " or " + 1 + ".");
        }
        if (i2cAddress == 0x4a) {
            this.i2cAddress = i2cAddress;
        } else {
            throw new IllegalArgumentException("The set " + String.format("%x", i2cAddress) + " is not " +
                    String.format("%x", 0x4a) + ".");
        }
        i2cName = "I2C_" + i2cBusNumber + "_" + String.format("%x", i2cAddress);
        logPrefix = "[" + i2cName + "] ";
        try {
            i2cDevice = new PigpioSocket();
            i2cDevice.connect("ip6-localhost");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE,logPrefix + ex);
        }
        LOG.setLevel(Level.FINEST);
        LOG.log(Level.SEVERE,logPrefix + "LOG level set to "+LOG.getLevel().toString());

        LOG.log(Level.FINE,logPrefix + "Exiting constructor");

    }

    synchronized public void open() throws IOException {
        try {
            LOG.log(Level.FINE,logPrefix + "before - useCount:{}", useCount.get());
            if (useCount.compareAndSet(0, 1)) {
                i2cHandle = i2cDevice.i2cOpen(i2cBusNumber, i2cAddress, 0);
                // readChipId();
                // readSensorCoefficients();
                printParameters();
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
                LOG.log(Level.FINE,logPrefix + "closed");
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

    private void dump(byte register, byte data, String tag) {
        if (LOG.isLoggable(Level.FINEST)) {
            StringBuffer sb = new StringBuffer();
            sb.append(String.format("%02x ", register));
            sb.append(String.format("%02x", data));
            LOG.log(Level.FINEST,
                    logPrefix + "{}{}",
                    new String[]{tag, sb.toString()});
        }
    }

    private void dump(byte register, byte[] data, String tag) {
        if (LOG.isLoggable(Level.FINEST)) {
            StringBuffer sb = new StringBuffer();
            sb.append(String.format("%02x ", register));
            for (byte data1 : data) {
                sb.append(String.format("%02x ", data1));
            }
            LOG.log(Level.FINEST,
                    logPrefix + "{}{}",
                    new String[]{tag, sb.toString().trim()});
        }
    }

    private void write(byte register, byte out) throws IOException {
        try {
            dump(register, out, "GY49 sensor command: write: ");
            //i2cDevice.write(register, out);
            i2cDevice.i2cWriteByteData(i2cHandle,(int)register & 0xff, out);
        } catch (Exception e) {
            String message = logPrefix + "failed to write.";
            LOG.log(Level.WARNING,message);
            throw new IOException(message, e);
        }
    }

    private byte read(byte register) throws IOException {
        try {
//            byte in = (byte) i2cDevice.read(register);
            byte in = (byte) i2cDevice.i2cReadByteData(i2cHandle,(int)register&0xff);
            dump(register, in, "GY49 sensor command: read:  ");
            return in;
        } catch (Exception e) {
            String message = logPrefix + "failed to read.";
            LOG.log(Level.WARNING,message);
            throw new IOException(message, e);
        }
    }

    private byte[] read(byte register, int length) throws IOException {
        try {
            byte[] in = new byte[length];
            //i2cDevice.read(register, in, 0, length);
            //i2cDevice.i2cReadBlockData(i2cHandle,(int)register&0xff, in);
            int r1=i2cDevice.i2cReadI2CBlockData(i2cHandle, (int)register&0xff, in, length);
            dump(register, in, "GY49 sensor command: read:  ");
            return in;
        } catch (Exception e) {
            String message = logPrefix + "failed to read.";
            LOG.log(Level.WARNING,message);
            throw new IOException(message, e);
        }
    }


    private void setSensor() throws IOException, InterruptedException {

        // Select configuration register
        write((byte)0x02,(byte)0x40); // Continuous mode, Integration time = 800 ms
        sleep(300);
    }

    private void printParameters() {
    }

    public float getSensorValue() throws IOException {
        LOG.log(Level.FINE,logPrefix + "getSensorValues, p002");
        // Read 2 bytes of data
        // luminance msb, luminance lsb
        byte[] data;
        {
            data = read((byte)0x03,2);
        }

        // Convert the data to lux
        int exponent = (data[0] & 0xF0) >> 4;
        int mantissa = ((data[0] & 0x0F) << 4) | (data[1] & 0x0F);
        float luminance = (float)(pow(2, exponent) * mantissa * 0.045f);

        return luminance;
    }

    /******************************************************************************************************************
     * Sample main
     ******************************************************************************************************************/
    public static void main(String[] args) throws IOException {
        GY49Driver gy49 = null;
        try {
            gy49 = GY49Driver.getInstance(/*bus*/1, /*Driver.I2C_ADDRESS_*/(byte)0x4a);
            gy49.open();

            while (true) {
                float value = gy49.getSensorValue();
                LOG.info("lux:" + value);

                sleep(10000);
            }
        } catch (InterruptedException e) {
            LOG.log(Level.WARNING,"caught - {}", e.toString());
        } catch (IOException e) {
            LOG.log(Level.WARNING,"caught - {}", e.toString());
        } finally {
            if (gy49 != null) {
                gy49.close();
            }
        }
    }
}