package nz.govt.tewhatuora.Service;

import com.google.gson.Gson;
import com.solace.messaging.receiver.InboundMessage;

import nz.govt.tewhatuora.Events.*;
import nz.govt.tewhatuora.Utilities.*;

import java.util.Objects;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EventLoader {

    private static final Logger logger = LogManager.getLogger();

    // Where users will implement their own code that handles what is done with the
    // Events.
    // below is an example of code that just prints the Event paylod to the comand
    // line
    public static void processEvent(InboundMessage message) {

        String event = "";
        // Get Event from Topic Taxonomy
        try {
            event = EventUtil.GetEvent(message.getDestinationName());
        } catch (Exception e) {

            logger.info("Dropped message '{}', event not found in topic '{}'\n",
                    message.getReplicationGroupMessageId().toString(),
                    message.getDestinationName());
            throw new RuntimeException("REJECT");

        }

        if (validEvent(message)) {

            switch (event) {
                case "death":
                    Death.processDeath(message);
                    break;

                default:
                    System.out.println(message.getPayloadAsString());
                    System.out.println("Unknown Event: " + event);
                    logger.info("Dropped message '{}', event unknown '{}'\n",
                            message.getReplicationGroupMessageId().toString(), event);
                    throw new RuntimeException("REJECT");
            }

        } else {
            throw new RuntimeException("FAILED");
        }
    }

    private static boolean validEvent(InboundMessage message) {

        boolean result = true;

        try {

            if (areAnyNull(message.getProperty("id"),
                    message.getProperty("source"),
                    message.getProperty("type"),
                    message.getProperty("subject"),
                    message.getProperty("datacontenttype"),
                    message.getProperty("time"))) {

                result = false;

            }

            new Gson().fromJson(message.getPayloadAsString(), Object.class);

            return result;

        } catch (Exception e) {

            return false;
        }

    }

    private static boolean areAnyNull(Object... objects) {
        return Stream.of(objects)
                .anyMatch(Objects::isNull);
    }
}
