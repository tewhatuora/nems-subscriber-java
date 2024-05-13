package nz.govt.tewhatuora.Utilities;

import java.util.Properties;

import com.solace.messaging.MessagingService;
import com.solace.messaging.MessagingService.ReconnectionAttemptListener;
import com.solace.messaging.MessagingService.ServiceEvent;
import com.solace.messaging.PubSubPlusClientException;
import com.solace.messaging.config.AuthenticationStrategy.OAuth2;
import com.solace.messaging.config.SolaceProperties.AuthenticationProperties;
import com.solace.messaging.config.profile.ConfigurationProfile;

import nz.govt.tewhatuora.Service.GlobalProperties;

public class EventUtil {

    final static String QUEUE_NAME = GlobalProperties.getProperty("nems.broker.queue");
    final static String TOKEN_SERVER = GlobalProperties.getProperty("nems.auth.tokenserver");
    final static String CLIENT_ID = GlobalProperties.getProperty("nems.auth.clientid");
    final static String CLIENT_SECRET = GlobalProperties.getProperty("nems.auth.clientsecret");
    final static String SCOPE = GlobalProperties.getProperty("nems.auth.scope");
    final static String ISSUER = GlobalProperties.getProperty("nems.auth.issuer");

    // Method to get the event name from a topic string
    public static String GetEvent(String topic) {

        String[] event = topic.split("/");
        if (event.length < 3) {
            throw new RuntimeException("Topic (" + topic + ") does not have an event");
        }
        return event[2];

    }

    // Generic method to get any level of a topic taxonomy specified by postion
    public static String GetTopicLevel(String topic, int position) {

        String[] event = topic.split("/");
        return event[position];

    }

    // Generic method to get fields from a search string such as a URL. Negatives
    // count from the end.
    // If the position index is out of range an empty string is returned.
    public static String GetFieldFromString(String value, String search, int position) {

        String[] event = value.split(search);
        if (position < 0) {
            position = event.length + position;
        }

        if (position >= event.length || position < 0) {
            return "";
        }

        return event[position];

    }

    public static MessagingService ConnectOAuth() {

        final Properties NEMS_PROPERTIES = GlobalProperties.setNEMSProperties();
        String accessToken = RestUtil.RenewToken(TOKEN_SERVER, CLIENT_ID, CLIENT_SECRET, SCOPE);

        final MessagingService messagingService = MessagingService.builder(ConfigurationProfile.V1)
                .fromProperties(NEMS_PROPERTIES)
                .withAuthenticationStrategy(OAuth2.of(accessToken).withIssuerIdentifier(ISSUER))
                .build();

        MessagingService.ReconnectionAttemptListener listener = new ReconnectionAttemptListener() {

            @Override
            public void onReconnecting(ServiceEvent e) {
                System.out.print("Reconnecting, attempting to refresh access token");
                String reconnectToken = RestUtil.RenewToken(TOKEN_SERVER, CLIENT_ID, CLIENT_SECRET, SCOPE);
                messagingService.updateProperty(AuthenticationProperties.SCHEME_OAUTH2_ACCESS_TOKEN,
                        reconnectToken);
            }

        };

        messagingService.addReconnectionAttemptListener(listener);

        try {
            messagingService.connect(); // blocking connect
        } catch (PubSubPlusClientException e) {
            System.out.println(e.getMessage());
            String retryToken = RestUtil.RenewToken(TOKEN_SERVER, CLIENT_ID, CLIENT_SECRET, SCOPE);
            NEMS_PROPERTIES.setProperty(AuthenticationProperties.SCHEME_OAUTH2_ACCESS_TOKEN,
                    retryToken);
            messagingService.connect();
        }
        return messagingService;
    }

    public static MessagingService ConnectBasic() {

        final Properties NEMS_PROPERTIES = GlobalProperties.setNEMSProperties("BASIC");
        final MessagingService messagingService = MessagingService.builder(ConfigurationProfile.V1)
                .fromProperties(NEMS_PROPERTIES)
                .build();

        messagingService.connect(); // blocking connect

        return messagingService;
    }
}
