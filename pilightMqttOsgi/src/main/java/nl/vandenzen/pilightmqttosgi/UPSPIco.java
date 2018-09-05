package nl.vandenzen.pilightmqttosgi;


import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class UPSPIco {

    public UPSPIco() {
        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

        // provision gpio pin27 #01 as an output pin27 and turn on
        pin27 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_27, "MyLED", PinState.HIGH);

        // set shutdown state for this pin27
        pin27.setShutdownOptions(true, PinState.LOW);
    }

    public String toggleGpio27() {

        // toggle the current state of gpio pin27 #01  (should turn off)
        pin27.toggle();
        return "Pin27 toggled";

    }
    final GpioPinDigitalOutput pin27;
}
