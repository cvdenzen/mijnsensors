package nl.vandenzen.pilightmqttosgi;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;

import org.apache.camel.*;
import org.apache.camel.impl.DefaultEndpoint;

public class PilightSubscriber implements Processor {
    public void process(Exchange exchange) {
        // open connection to pilight
        final String encoding="UTF-8";
        try {
            SocketChannel socket = SocketChannel.open(new InetSocketAddress("192.168.2.9", 5017));
            // Send the pilight identify string
            Charset charset=Charset.forName("ISO-8859-1");
            socket.write(charset.encode(MyRouteBuilder.pilightIdentify));
            // Create a channel/?? for the response
            // ??
            StringBuilder sb=new StringBuilder(5000);

            // Get a buffer for the response
            ByteBuffer buffer=ByteBuffer.allocateDirect(8192);
            // loop until cr, hope that read is not waiting too long
            while ((socket.read(buffer) != -1 ) || buffer.position() > 0) { // are we done?
                buffer.flip(); // prepare to read bytes from buffer
                byte[] buffer1 = new byte[9999];
                buffer.get(buffer1); // read the bytes
                sb.append(new String(buffer1,encoding));

                int endOfMessage=sb.indexOf("\r"); // pilight end of message is carriage return


                //
                //
                //
            }
        }
        catch (Exception ex) {
            System.err.println(ex);
        }
    }
}
