package nl.vandenzen.iot.beans;

/*
Carl van Denzen, november 2020.
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

/*
 * Refer to http://static.cactus.io/docs/sensors/barometric/bme280/BST-BME280_DS001-10.pdf
 *
 * @author s5uishida
 *
 */
public class BME280Driver {
    private static final Logger LOG = Logger.getLogger(BME280Driver.class.getName());

        private static final byte DIG_T1_REG = (byte) 0x88;
    private static final byte DIG_T2_REG = (byte) 0x8a;
    private static final byte DIG_T3_REG = (byte) 0x8c;
    private static final byte DIG_P1_REG = (byte) 0x8e;
    private static final byte DIG_P2_REG = (byte) 0x90;
    private static final byte DIG_P3_REG = (byte) 0x92;
    private static final byte DIG_P4_REG = (byte) 0x94;
    private static final byte DIG_P5_REG = (byte) 0x96;
    private static final byte DIG_P6_REG = (byte) 0x98;
    private static final byte DIG_P7_REG = (byte) 0x9a;
    private static final byte DIG_P8_REG = (byte) 0x9c;
    private static final byte DIG_P9_REG = (byte) 0x9e;
    private static final byte DIG_H1_REG = (byte) 0xa1;
    private static final byte DIG_H2_REG = (byte) 0xe1;
    private static final byte DIG_H3_REG = (byte) 0xe3;
    private static final byte DIG_H4_REG = (byte) 0xe4;
    private static final byte DIG_H5_REG = (byte) 0xe5;
    private static final byte DIG_H6_REG = (byte) 0xe7;

    private static final byte CHIP_ID_REG = (byte) 0xd0;
    private static final byte RESET_REG = (byte) 0xe0;

    private static final byte CONTROL_HUMIDITY_REG = (byte) 0xf2;
    private static final byte CONTROL_HUMIDITY_OSRS_H_0 = (byte) 0x00;
    private static final byte CONTROL_HUMIDITY_OSRS_H_1 = (byte) 0x01;
    private static final byte CONTROL_HUMIDITY_OSRS_H_2 = (byte) 0x02;
    private static final byte CONTROL_HUMIDITY_OSRS_H_4 = (byte) 0x03;
    private static final byte CONTROL_HUMIDITY_OSRS_H_8 = (byte) 0x04;
    private static final byte CONTROL_HUMIDITY_OSRS_H_16 = (byte) 0x05;

    private static final byte STATUS_REG = (byte) 0xf3;
    private static final byte STATUS_MEASURING_BIT = (byte) 0x08;
    private static final byte STATUS_IM_UPDATE_BIT = (byte) 0x01;

    private static final byte CONTROL_MEASUREMENT_REG = (byte) 0xf4;
    private static final byte CONTROL_MEASUREMENT_OSRS_T_0 = (byte) 0x00;
    private static final byte CONTROL_MEASUREMENT_OSRS_T_1 = (byte) 0x20;
    private static final byte CONTROL_MEASUREMENT_OSRS_T_2 = (byte) 0x40;
    private static final byte CONTROL_MEASUREMENT_OSRS_T_4 = (byte) 0x60;
    private static final byte CONTROL_MEASUREMENT_OSRS_T_8 = (byte) 0x80;
    private static final byte CONTROL_MEASUREMENT_OSRS_T_16 = (byte) 0xa0;
    private static final byte CONTROL_MEASUREMENT_OSRS_P_0 = (byte) 0x00;
    private static final byte CONTROL_MEASUREMENT_OSRS_P_1 = (byte) 0x04;
    private static final byte CONTROL_MEASUREMENT_OSRS_P_2 = (byte) 0x08;
    private static final byte CONTROL_MEASUREMENT_OSRS_P_4 = (byte) 0x0c;
    private static final byte CONTROL_MEASUREMENT_OSRS_P_8 = (byte) 0x10;
    private static final byte CONTROL_MEASUREMENT_OSRS_P_16 = (byte) 0x14;
    private static final byte CONTROL_MEASUREMENT_SLEEP_MODE = (byte) 0x00;
    private static final byte CONTROL_MEASUREMENT_FORCED_MODE = (byte) 0x01;
    private static final byte CONTROL_MEASUREMENT_NORMAL_MODE = (byte) 0x03;

    private static final byte CONFIG_REG = (byte) 0xf5;
    private static final byte CONFIG_T_SB_0_5 = (byte) 0x00;
    private static final byte CONFIG_T_SB_62_5 = (byte) 0x20;
    private static final byte CONFIG_T_SB_125 = (byte) 0x40;
    private static final byte CONFIG_T_SB_250 = (byte) 0x60;
    private static final byte CONFIG_T_SB_500 = (byte) 0x80;
    private static final byte CONFIG_T_SB_1000 = (byte) 0xa0;
    private static final byte CONFIG_T_SB_10 = (byte) 0xb0;
    private static final byte CONFIG_T_SB_20 = (byte) 0xe0;
    private static final byte CONFIG_FILTER_OFF = (byte) 0x00;
    private static final byte CONFIG_FILTER_2 = (byte) 0x04;
    private static final byte CONFIG_FILTER_4 = (byte) 0x08;
    private static final byte CONFIG_FILTER_8 = (byte) 0x0c;
    private static final byte CONFIG_FILTER_16 = (byte) 0x10;
    private static final byte CONFIG_SPI3W = (byte) 0x01;

    private static final byte PRESSURE_DATA_REG = (byte) 0xf7;
    private static final byte TEMPERATURE_DATA_REG = (byte) 0xfa;
    private static final byte HUMIDITY_DATA_REG = (byte) 0xfd;

    private static final int CALIBRATION_DATA_LENGTH_1 = 24;
    private static final int CALIBRATION_DATA_LENGTH_2 = 7;

    private static final int SENSOR_DATA_LENGTH = 8;

    private static final int MEASUREMENT_TIME_MILLIS = 10;

    private final byte i2cAddress;
    private final String i2cName;
    private final String logPrefix;

    private final AtomicInteger useCount = new AtomicInteger(0);

    private static final ConcurrentHashMap<String, BME280Driver> map = new ConcurrentHashMap<String, BME280Driver>();
    private final int i2cBusNumber;
    private PigpioSocket i2cDevice;
    private int i2cHandle;

    synchronized public static BME280Driver getInstance(int i2cBusNumber, byte i2cAddress) {
        String key = i2cBusNumber + ":" + String.format("%x", i2cAddress);
        BME280Driver bme280 = map.get(key);
        if (bme280 == null) {
            bme280 = new BME280Driver(i2cBusNumber, i2cAddress);
            map.put(key, bme280);
        }
        return bme280;
    }

    /**
     * Overload method because Blueprint attribute type for factory-method doesn't work, but maybe because this
     * constructor was not public
     * @param i2cBusNumber
     * @param i2cAddress
     * @return
     */
    synchronized public static BME280Driver getInstance(String i2cBusNumber, String i2cAddress) {
        return getInstance(Integer.parseInt(i2cBusNumber),Byte.decode(i2cAddress));
    }

    private BME280Driver(int i2cBusNumber, byte i2cAddress) {
        if (i2cBusNumber == 0 || i2cBusNumber == 1) {
            this.i2cBusNumber = i2cBusNumber;
        } else {
            throw new IllegalArgumentException("The set " + i2cBusNumber + " is not " +
                    0 + " or " + 1 + ".");
        }
        if (i2cAddress == 0x76 || i2cAddress == 0x77) {
            this.i2cAddress = i2cAddress;
        } else {
            throw new IllegalArgumentException("The set " + String.format("%x", i2cAddress) + " is not " +
                    String.format("%x", 0x76) + " or " + String.format("%x", 0x77) + ".");
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
                readChipId();
                readSensorCoefficients();
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
            dump(register, out, "BME280 sensor command: write: ");
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
            dump(register, in, "BME280 sensor command: read:  ");
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
            dump(register, in, "BME280 sensor command: read:  ");
            return in;
        } catch (Exception e) {
            String message = logPrefix + "failed to read.";
            LOG.log(Level.WARNING,message);
            throw new IOException(message, e);
        }
    }

    private void readChipId() throws IOException {
        byte chipId = read(CHIP_ID_REG);
        if (chipId != (byte) 0x60) {
            String message = logPrefix + "Chip ID[" + String.format("%x", chipId) + "] is not 0x60.";
            LOG.log(Level.WARNING,message);
            throw new IllegalStateException(message);
        }
    }

    private int dig_T1;
    private int dig_T2;
    private int dig_T3;
    private int dig_P1;
    private int dig_P2;
    private int dig_P3;
    private int dig_P4;
    private int dig_P5;
    private int dig_P6;
    private int dig_P7;
    private int dig_P8;
    private int dig_P9;
    private int dig_H1;
    private int dig_H2;
    private int dig_H3;
    private int dig_H4;
    private int dig_H5;
    private int dig_H6;

    private int signed16Bits(byte[] data, int offset) {
        int byte0 = data[offset] & 0xff;
        int byte1 = (int) data[offset + 1];

        return (byte1 << 8) + byte0;
    }

    private int unsigned16Bits(byte[] data, int offset) {
        int byte0 = data[offset] & 0xff;
        int byte1 = data[offset + 1] & 0xff;

        return (byte1 << 8) + byte0;
    }

    private void readSensorCoefficients() throws IOException {
        byte[] data = read(DIG_T1_REG, CALIBRATION_DATA_LENGTH_1);

        dig_T1 = unsigned16Bits(data, 0);
        dig_T2 = signed16Bits(data, 2);
        dig_T3 = signed16Bits(data, 4);

        dig_P1 = unsigned16Bits(data, 6);
        dig_P2 = signed16Bits(data, 8);
        dig_P3 = signed16Bits(data, 10);
        dig_P4 = signed16Bits(data, 12);
        dig_P5 = signed16Bits(data, 14);
        dig_P6 = signed16Bits(data, 16);
        dig_P7 = signed16Bits(data, 18);
        dig_P8 = signed16Bits(data, 20);
        dig_P9 = signed16Bits(data, 22);

        dig_H1 = read(DIG_H1_REG) & 0xff;

        data = read(DIG_H2_REG, CALIBRATION_DATA_LENGTH_2);

        dig_H2 = signed16Bits(data, 0);
        dig_H3 = data[2] & 0xff;
        dig_H4 = ((data[3] & 0xff) << 4) + (data[4] & 0x0f);
        dig_H5 = ((data[5] & 0xff) << 4) + ((data[4] & 0xff) >> 4);
        dig_H6 = data[6];
    }

    private void printParameters() {
        LOG.log(Level.FINE,logPrefix + "dig_T1:{} u16", dig_T1);
        LOG.log(Level.FINE,logPrefix + "dig_T2:{} s16", dig_T2);
        LOG.log(Level.FINE,logPrefix + "dig_T3:{} s16", dig_T3);

        LOG.log(Level.FINE,logPrefix + "dig_P1:{} u16", dig_P1);
        LOG.log(Level.FINE,logPrefix + "dig_P2:{} s16", dig_P2);
        LOG.log(Level.FINE,logPrefix + "dig_P3:{} s16", dig_P3);
        LOG.log(Level.FINE,logPrefix + "dig_P4:{} s16", dig_P4);
        LOG.log(Level.FINE,logPrefix + "dig_P5:{} s16", dig_P5);
        LOG.log(Level.FINE,logPrefix + "dig_P6:{} s16", dig_P6);
        LOG.log(Level.FINE,logPrefix + "dig_P7:{} s16", dig_P7);
        LOG.log(Level.FINE,logPrefix + "dig_P8:{} s16", dig_P8);
        LOG.log(Level.FINE,logPrefix + "dig_P9:{} s16", dig_P9);

        LOG.log(Level.FINE,logPrefix + "dig_H1:{} u8", dig_H1);
        LOG.log(Level.FINE,logPrefix + "dig_H2:{} s16", dig_H2);
        LOG.log(Level.FINE,logPrefix + "dig_H3:{} u8", dig_H3);
        LOG.log(Level.FINE,logPrefix + "dig_H4:{} s16", dig_H4);
        LOG.log(Level.FINE,logPrefix + "dig_H5:{} s16", dig_H5);
        LOG.log(Level.FINE,logPrefix + "dig_H6:{} s8", dig_H6);
    }

    public float[] getSensorValues() throws IOException {
        LOG.log(Level.FINE,logPrefix + "getSensorValues, p002");
        write(CONTROL_HUMIDITY_REG, CONTROL_HUMIDITY_OSRS_H_1);
        LOG.log(Level.FINE,logPrefix + "getSensorValues, p004");
        write(CONTROL_MEASUREMENT_REG,
                (byte) (CONTROL_MEASUREMENT_OSRS_T_1 | CONTROL_MEASUREMENT_OSRS_P_1 | CONTROL_MEASUREMENT_FORCED_MODE));
        LOG.log(Level.FINE,logPrefix + "getSensorValues, p006");
        write(CONFIG_REG, CONFIG_T_SB_0_5);

        LOG.log(Level.FINE,logPrefix + "getSensorValues, p008");
        try {
            Thread.sleep(MEASUREMENT_TIME_MILLIS);
        } catch (InterruptedException e) {
        }

        byte[] data = read(PRESSURE_DATA_REG, SENSOR_DATA_LENGTH);
        LOG.log(Level.FINE,logPrefix + "getSensorValues, p010");
        for (int i=0;i<data.length;i++) {
            LOG.log(Level.FINE,logPrefix + "getSensorValues, data["+i+"]="+data[i]);
        }
        LOG.log(Level.FINE,logPrefix + "getSensorValues, p010");

        int adc_P = (((int) (data[0] & 0xff) << 16) + ((int) (data[1] & 0xff) << 8) + ((int) (data[2] & 0xff))) >> 4;
        int adc_T = (((int) (data[3] & 0xff) << 16) + ((int) (data[4] & 0xff) << 8) + ((int) (data[5] & 0xff))) >> 4;
        int adc_H = ((int) (data[6] & 0xff) << 8) + ((int) (data[7] & 0xff));

        // Temperature
        int varT1 = ((((adc_T >> 3) - (dig_T1 << 1))) * (dig_T2)) >> 11;
        int varT2 = (((((adc_T >> 4) - dig_T1) * ((adc_T >> 4) - dig_T1)) >> 12) * dig_T3) >> 14;
        int t_fine = varT1 + varT2;
        float temperature = ((t_fine * 5 + 128) >> 8) / 100F;

        // Pressure
        long varP1 = (long) t_fine - 128000;
        long varP2 = varP1 * varP1 * (long) dig_P6;
        varP2 += ((varP1 * (long) dig_P5) << 17);
        varP2 += (((long) dig_P4) << 35);
        varP1 = ((varP1 * varP1 * (long) dig_P3) >> 8) + ((varP1 * (long) dig_P2) << 12);
        varP1 = (((((long) 1) << 47) + varP1)) * ((long) dig_P1) >> 33;
        LOG.log(Level.FINE,logPrefix + "getSensorValues, p020");

        float pressure;
        if (varP1 == 0) {
            pressure = 0F;
        } else {
            long p = 1048576 - adc_P;
            p = (((p << 31) - varP2) * 3125) / varP1;
            varP1 = (((long) dig_P9) * (p >> 13) * (p >> 13)) >> 25;
            varP2 = (((long) dig_P8) * p) >> 19;
            pressure = (((p + varP1 + varP2) >> 8) + (((long) dig_P7) << 4)) / 256F / 100F;
        }
        LOG.log(Level.FINE,logPrefix + "getSensorValues, p030");

        // Humidity
        int v_x1_u32r = t_fine - 76800;
        v_x1_u32r = (((((adc_H << 14) - (dig_H4 << 20) - (dig_H5 * v_x1_u32r)) +
                16384) >> 15) * (((((((v_x1_u32r * dig_H6) >> 10) * (((v_x1_u32r * dig_H3) >> 11) + 32768)) >> 10) +
                2097152) * dig_H2 + 8192) >> 14));
        v_x1_u32r -= (((((v_x1_u32r >> 15) * (v_x1_u32r >> 15)) >> 7) * dig_H1) >> 4);
        v_x1_u32r = (v_x1_u32r < 0) ? 0 : v_x1_u32r;
        v_x1_u32r = (v_x1_u32r > 419430400) ? 419430400 : v_x1_u32r;
        float humidity = (v_x1_u32r >> 12) / 1024F;

        float[] ret = new float[3];
        ret[0] = temperature;
        ret[1] = humidity;
        ret[2] = pressure;

        return ret;
    }

    /******************************************************************************************************************
     * Sample main
     ******************************************************************************************************************/
    public static void main(String[] args) throws IOException {
        BME280Driver bme280 = null;
        try {
            bme280 = BME280Driver.getInstance(/*bus*/1, /*BME280Driver.I2C_ADDRESS_*/(byte)0x76);
            bme280.open();

            while (true) {
                float[] values = bme280.getSensorValues();
                LOG.info("temperature:" + values[0]);
                LOG.info("humidity:" + values[1]);
                LOG.info("pressure:" + values[2]);

                Thread.sleep(10000);
            }
        } catch (InterruptedException e) {
            LOG.log(Level.WARNING,"caught - {}", e.toString());
        } catch (IOException e) {
            LOG.log(Level.WARNING,"caught - {}", e.toString());
        } finally {
            if (bme280 != null) {
                bme280.close();
            }
        }
    }
}