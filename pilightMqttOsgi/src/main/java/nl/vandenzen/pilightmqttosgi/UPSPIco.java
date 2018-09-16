package nl.vandenzen.pilightmqttosgi;


import com.pi4j.io.gpio.*;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UPSPIco {

    public UPSPIco() throws Exception {
        logger.logp(Level.INFO, nl.vandenzen.pilightmqttosgi.UPSPIco.class.toString(),"constructor",
                "Start UPSPIco constructor, threadid="+Thread.currentThread());
        // create gpio controller
        gpio = GpioFactory.getInstance();
        //logger.log(Level.INFO,"Start i2c getDevices");
        //I2CBus bus= I2CFactory.getInstance(I2CBus.BUS_1);
        //statusDevice = bus.getDevice(0x69);
    }

    public String toggleGpio27() {

        // toggle the current state of gpio gpioPir #01  (should turn off)
        pin27.toggle();
        return "GPIO_27 toggled";
    }

    /**
     * Read 2 bytes
     * @param device
     * @param a
     * @return
     */
    private int readIntData(I2CDevice device,int offset) {
        byte[] p=new byte[2];
        int r;
        try {
            r = device.read(p, offset, 2);
        }
        catch (IOException ex) {
            logger.log(Level.SEVERE, "Exception reading ups device offset=" + offset,ex);
            return 0;
        }
        logger.log(Level.INFO,"read p[0]="+p[0]+" p[1]="+p[1]);
        if (r != 2) {
            logger.log(Level.SEVERE,"Read Error; r="+r);
            return 0;
        }

        // Divisor is default 1.2, if 0.5lx resolution, it is 2.4, at double measurement time+0.5lx res, then 4.8.
        //return new Float((int) ((p[0] & 0xFF) << 8) | (p[1] & 0xFF)) / 4.8f;
        return new Integer((int) ((p[0] & 0xFF) << 8) | (p[1] & 0xFF));
    }
    /**
     * Read 2 bytes
     * @param device
     * @param a
     * @return
     */
    private byte readByteData(I2CDevice device,int offset) {
        byte[] p=new byte[1];
        int r;
        try {
            r = device.read(p,offset,1);
        }
        catch (IOException ex) {
            logger.log(Level.SEVERE, "Exception reading ups device offset=" + offset,ex);
            return 0;
        }
        logger.log(Level.INFO,"read p[0]="+p[0]);
        if (r != 1) {
            logger.log(Level.SEVERE,"Read Error in read byte; r should be 1, r="+r);
            return 0;
        }

        // Divisor is default 1.2, if 0.5lx resolution, it is 2.4, at double measurement time+0.5lx res, then 4.8.
        //return new Float((int) ((p[0] & 0xFF) << 8) | (p[1] & 0xFF)) / 4.8f;
        return p[0];
    }

    public String upsGetPwrMode() {
        int data=readByteData(statusDevice,0);
        data=data & 0x7f;
        String value="";
        switch (data) {
            case 1:
                value="ONLINE";
                break;
            case 2:
                value="ONBATT";
                break;
            default:
                value="ERR";
                break;
        }
        return value;
    }



    public void init() throws Exception {
        logger.log(Level.INFO, "Start UPSPIco init");

        // provision gpio gpioPir #01 as an output gpioPir and turn on
        // bug? first export, then wait, then provision.
        // https://raspberrypi.stackexchange.com/questions/23162/gpio-value-file-appears-with-wrong-permissions-momentarily
        //gpio.export(PinMode.DIGITAL_OUTPUT,GpioPin);
        int maxTries = 5;
        Exception firstException = null;
        for (int i = 0; i < maxTries; i++) {
            try {
                logger.log(Level.INFO, "UPSPIco pin27 provision start");
                pin27 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_27, "UPSRpiHeartBeat", PinState.HIGH);
                logger.log(Level.INFO, "UPSPIco pin27 provisioned succesfully:" + pin27.toString());
                break;
            } catch (Exception ex) {
                if (i == 0) {
                    firstException = ex;
                }
                if (i == maxTries - 1) {
                    logger.log(Level.SEVERE, "Cannot provision output pin GPIO_27 after " + maxTries + " attempts, first exception was:", firstException);
                    logger.log(Level.SEVERE, "Cannot provision output pin GPIO_27 after " + maxTries + " attempts, last exception was:", ex);
                    throw (firstException);
                }
            }
            if (pin27 != null) {
                try {
                    logger.log(Level.INFO, "Unprovisioning UPSPIco pin27" + pin27.toString());
                    gpio.unprovisionPin(pin27);
                    logger.log(Level.INFO, "UPSPIco pin27 unprovisioned succesfully:");
                    pin27 = null;
                } catch (Exception ex1) {
                    logger.log(Level.INFO, "Ignoring exception trying to unprovisionPin(pin27)", ex1);
                }
            } else {
                logger.log(Level.INFO, "UPSPIco pin27==null, try number is "+i);
            }
            Thread.sleep(500);
        }



        // set shutdown state for this gpioPir
        pin27.setShutdownOptions(true, PinState.LOW);

    }
    public void destroy() {
        //pin27.removeAllListeners();
        logger.log(Level.INFO, "stop(): trying to unprovision pin27");
        gpio.unprovisionPin(pin27);
        logger.log(Level.INFO, "Unprovisioned pin27");
    }

    GpioPinDigitalOutput pin27 = null;
    final GpioController gpio;

    final static Logger logger = Logger.getLogger(UPSPIco.class.toString());

    private I2CDevice statusDevice;
    final static String TOPICPREFIX="upspico1/";
}
