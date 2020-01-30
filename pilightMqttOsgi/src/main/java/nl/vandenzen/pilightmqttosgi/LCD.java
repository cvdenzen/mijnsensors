import com.pi4j.component.lcd.LCDTextAlignment;
import com.pi4j.component.lcd.impl.GpioLcdDisplay;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.system.NetworkInfo;

public class LCD extends GpioLcdDisplay x {
    public final static int LCD_ROW_1 = 0;
    public final static int LCD_ROW_2 = 1;

    public static void main(String args[]) throws Exception {

        System.out.println("16X2 LCD Example with Raspberry Pi using Pi4J and JAVA");

        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

        // initialize LCD
        final GpioLcdDisplay lcd = new GpioLcdDisplay(2,    // number of row supported by LCD
                16,       // number of columns supported by LCD
                RaspiPin.GPIO_28,  // LCD RS pin
                RaspiPin.GPIO_27,  // LCD strobe pin
                RaspiPin.GPIO_22,  // LCD data bit D4
                RaspiPin.GPIO_23,  // LCD data bit D5
                RaspiPin.GPIO_24,  // LCD data bit D6
                RaspiPin.GPIO_25); // LCD data bit D7

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

    public static LCD getLCD() throws Exception {
        main(new String[][]);
        return new GpioLcdDisplay(2,    // number of row supported by LCD
                16,       // number of columns supported by LCD
                RaspiPin.GPIO_28,  // LCD RS pin
                RaspiPin.GPIO_27,  // LCD strobe pin
                RaspiPin.GPIO_22,  // LCD data bit D4
                RaspiPin.GPIO_23,  // LCD data bit D5
                RaspiPin.GPIO_24,  // LCD data bit D6
                RaspiPin.GPIO_25); // LCD data bit D7;
    }

    ;
}