package nl.vandenzen.pilightmqttosgi;


import com.pi4j.io.gpio.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public class UPSPIco {

    public UPSPIco() throws Exception {
        logger.log(Level.INFO, "Start UPSPIco constructor");
        // create gpio controller
        gpio = GpioFactory.getInstance();
    }

    public String toggleGpio27() {

        // toggle the current state of gpio gpioPir #01  (should turn off)
        pin27.toggle();
        return "GPIO_27 toggled";

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
}
