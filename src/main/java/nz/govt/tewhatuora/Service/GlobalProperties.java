package nz.govt.tewhatuora.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.solace.messaging.config.SolaceProperties.TransportLayerProperties;
import com.solace.messaging.config.SolaceProperties.ServiceProperties;
import com.solace.messaging.config.SolaceProperties.AuthenticationProperties;

public class GlobalProperties {

    private static Properties properties = new Properties();
    public static String propertyFile = "application.properties";

    static {
        try {
            // Load the properties file
            properties.load(GlobalProperties.class.getClassLoader().getResourceAsStream(propertyFile));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);

        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static Properties loadProperties() throws IOException {
        Properties configuration = new Properties();
        InputStream inputStream = GlobalProperties.class
                .getClassLoader()
                .getResourceAsStream(propertyFile);
        configuration.load(inputStream);
        inputStream.close();
        return configuration;
    }

    public static Properties setNEMSProperties() {

        return setNEMSProperties("OAUTH");
    }

    public static Properties setNEMSProperties(String authType) {

        switch (authType.toUpperCase()) {
            case "BASIC":
                return setNEMSBasicProperties();
            default:
                return setNEMSOAuthProperties();
        }

    }

    private static Properties setNEMSOAuthProperties() {

        properties.setProperty(TransportLayerProperties.HOST, getProperty("nems.broker.host")); // host:port
        properties.setProperty(ServiceProperties.VPN_NAME, getProperty("nems.broker.vpn")); // message-vpn
        properties.setProperty(TransportLayerProperties.CONNECTION_RETRIES_PER_HOST, "5");
        properties.setProperty(TransportLayerProperties.RECONNECTION_ATTEMPTS, getProperty("nems.broker.retries")); // recommended
        properties.setProperty(TransportLayerProperties.RECONNECTION_ATTEMPTS_WAIT_INTERVAL,
                getProperty("nems.broker.retryinterval"));

        return properties;
    }

    private static Properties setNEMSBasicProperties() {

        properties.setProperty(TransportLayerProperties.HOST, getProperty("nems.broker.host")); // host:port
        properties.setProperty(ServiceProperties.VPN_NAME, getProperty("nems.broker.vpn")); // message-vpn
        properties.setProperty(AuthenticationProperties.SCHEME_BASIC_USER_NAME, getProperty("nems.broker.username")); // client-username
        properties.setProperty(AuthenticationProperties.SCHEME_BASIC_PASSWORD, getProperty("nems.broker.password"));
        properties.setProperty(TransportLayerProperties.CONNECTION_RETRIES_PER_HOST, "5");
        properties.setProperty(TransportLayerProperties.RECONNECTION_ATTEMPTS, getProperty("nems.broker.retries")); // recommended
        properties.setProperty(TransportLayerProperties.RECONNECTION_ATTEMPTS_WAIT_INTERVAL,
                getProperty("nems.broker.retryinterval"));

        return properties;
    }
}