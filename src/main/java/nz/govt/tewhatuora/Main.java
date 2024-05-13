package nz.govt.tewhatuora;

import com.solace.messaging.MessagingService;
import com.solace.messaging.config.MessageAcknowledgementConfiguration.Outcome;
import com.solace.messaging.receiver.PersistentMessageReceiver;
import com.solace.messaging.resources.Queue;

import java.io.IOException;
import nz.govt.tewhatuora.Service.EventLoader;
import nz.govt.tewhatuora.Service.GlobalProperties;
import nz.govt.tewhatuora.Utilities.EventUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private static final String API = "Java";

    private static volatile int msgRecvCounter = 0; // num messages received
    private static volatile boolean hasDetectedRedelivery = false; // detected any messages being redelivered?
    private static volatile boolean isShutdown = false; // are we done?

    final static String QUEUE_NAME = GlobalProperties.getProperty("nems.broker.queue");

    private static final Logger logger = LogManager.getLogger();

    /**
     * This is the main app. Use this type of app for receiving Guaranteed messages
     * (e.g. via a queue endpoint).
     */
    public static void main(String[] args) throws InterruptedException, IOException {

        final MessagingService messagingService = EventUtil.ConnectOAuth();

        final PersistentMessageReceiver receiver = messagingService
                .createPersistentMessageReceiverBuilder()
                .withRequiredMessageClientOutcomeOperationSupport(Outcome.FAILED, Outcome.REJECTED)
                .build(Queue.durableExclusiveQueue(QUEUE_NAME));

        try {

            receiver.start();
            logger.info("Successfully connected to queue '%s'\n", QUEUE_NAME);
            // System.out.printf("Successfully connected to queue '%s'\n", QUEUE_NAME);

        } catch (RuntimeException e) {

            System.err.printf("%n*** Could not establish a connection to queue '%s': %s%n", QUEUE_NAME, e.getMessage());
            System.err.println("  or see the SEMP CURL scripts inside the 'semp-rest-api' directory.");
            System.err.println(
                    "NOTE: see HowToEnableAutoCreationOfMissingResourcesOnBroker.java sample for how to construct queue with consumer app.");
            System.err.println("Exiting.");
            return;

        }

        // asynchronous anonymous receiver message callback
        receiver.receiveAsync(message -> {
            msgRecvCounter++;
            if (message.isRedelivered()) { // useful check
                // this is the broker telling the consumer that this message has been sent and
                // not ACKed before. This can happen if an exception is thrown, or the broker
                // restarts, or the network disconnects perhaps an error in processing? Should
                // do extra checks to avoid duplicate processing
                hasDetectedRedelivery = true;
            }

            try {
                // Where customer code can be implemeted to handle events before they are ACKed
                EventLoader.processEvent(message);

                receiver.settle(message, Outcome.ACCEPTED);

            } catch (Exception e) {

                if (e.getMessage().equals("REJECT")) {
                    receiver.settle(message, Outcome.REJECTED);
                } else {
                    receiver.settle(message, Outcome.FAILED);
                }

            }

        });

        // async queue receive working now, so time to wait until done...
        System.out.println("connected, and running. Press [ENTER] to quit.");
        while (System.in.available() == 0 && !isShutdown) {
            Thread.sleep(4000); // wait 4 second
            System.out.printf(
                    "%s Received msgs/s: %,d%n",
                    API, msgRecvCounter); // simple way of calculating message rates
            msgRecvCounter = 0;
            if (hasDetectedRedelivery) {
                System.out.println("*** Redelivery detected ***");
                hasDetectedRedelivery = false; // only show the error once per second
            }
        }
        isShutdown = true;
        receiver.terminate(1500L);
        Thread.sleep(1000);
        messagingService.disconnect();
        System.out.println("Main thread quitting.");

    }

}
