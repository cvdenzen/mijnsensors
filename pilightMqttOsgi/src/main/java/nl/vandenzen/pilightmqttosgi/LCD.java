package nl.vandenzen.pilightmqttosgi;

import com.pi4j.component.lcd.LCDTextAlignment;
import com.pi4j.component.lcd.impl.GpioLcdDisplay;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.system.NetworkInfo;

import java.util.logging.Logger;

public class LCD extends GpioLcdDisplay {
    public final static int LCD_ROW_1 = 0;
    public final static int LCD_ROW_2 = 1;

    public LCD() {

        // initialize LCD
        // int rows, int columns, Pin rsPin, Pin strobePin, Pin... dataPins
        // final GpioLcdDisplay lcd =
                super(2,    // number of row supported by LCD
                16,       // number of columns supported by LCD
                RaspiPin.GPIO_28,  // LCD RS pin
                RaspiPin.GPIO_27,  // LCD strobe pin
                RaspiPin.GPIO_22,  // LCD data bit D4
                RaspiPin.GPIO_23,  // LCD data bit D5
                RaspiPin.GPIO_24,  // LCD data bit D6
                RaspiPin.GPIO_25); // LCD data bit D7

        logger.info("LCD constructor: finished super(...)");
    }

    public void backlight(short percentage) {


    }

    public static LCD getLCD() {
        return new LCD();
    }
    public static void main(String args[]) throws Exception {

        System.out.println("16X2 LCD Example with Raspberry Pi using Pi4J and JAVA");

        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();
        LCD lcd=new LCD();

        lcd.clear();
        Thread.sleep(1000);

        lcd.write(LCD_ROW_1, "WeArGenius");
        lcd.write(LCD_ROW_2, " ???");

        Thread.sleep(2000);
        for (String ipAddress : NetworkInfo.getIPAddresses()) {
            System.out.println("IP Addresses      :  " + ipAddress);
            lcd.writeln(LCD_ROW_2, ipAddress, LCDTextAlignment.ALIGN_CENTER);
        }

        gpio.shutdown();
    }
    final static Logger logger = Logger.getLogger(PirSensor.class.toString());
}