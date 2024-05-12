package nz.govt.tewhatuora.Events;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.solace.messaging.receiver.InboundMessage;
import com.solace.messaging.receiver.InboundMessage.ReplicationGroupMessageId;

import nz.govt.tewhatuora.Service.EventLoader;

public class EventTest {

    @Test
    void testValidEvent() {

        String expected = "SUCCESS";

        try {
            InboundMessage message = mock(InboundMessage.class);
            ReplicationGroupMessageId messageId = mock(ReplicationGroupMessageId.class);
            when(messageId.toString()).thenReturn("rmid1:2fc6e-b5972b2d66f-00000000-0000000");
            when(message.getReplicationGroupMessageId()).thenReturn(messageId);

            when(message.getProperty("id")).thenReturn("86152393-fc5b-4b21-a315-5992f31d2aa1");
            when(message.getProperty("source")).thenReturn("https://hip.uat.digital.health.nz");
            when(message.getProperty("type")).thenReturn("demographics/patient/death/new/0.1.0");
            when(message.getProperty("subject")).thenReturn("ZAT2348");
            when(message.getProperty("datacontenttype")).thenReturn("application/json");
            when(message.getProperty("time"))
                    .thenReturn(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

            when(message.getDestinationName())
                    .thenReturn("demographics/patient/death/new/0.1.0/G00036-D/2203/FZZ988-H/ZAT2348"); // Set Topic
            when(message.getPayloadAsString()).thenReturn(
                    "{\"callbackUrl\": \"https://api.hip-uat.digital.health.nz/fhir/nhi/v1/Patient/ZAT2348\", \"deathDate\": \"2016-02-18\"}");

            EventLoader.processEvent(message);

            assertEquals(expected, "SUCCESS");

        } catch (Exception e) {

            assertEquals("FAIL", e.getMessage());

        }
    }

    @Test
    void testEventNotDefined() {

        String expected = "REJECT";

        InboundMessage message = mock(InboundMessage.class);
        ReplicationGroupMessageId messageId = mock(ReplicationGroupMessageId.class);
        when(messageId.toString()).thenReturn("rmid1:2fc6e-b5972b2d66f-00000000-0000000");
        when(message.getReplicationGroupMessageId()).thenReturn(messageId);

        when(message.getDestinationName())
                .thenReturn("demographics/patient"); // Set Topic
        when(message.getPayloadAsString()).thenReturn(
                "{\"callbackUrl\": \"https://api.hip-uat.digital.health.nz/fhir/nhi/v1/Patient/ZAT2348\", \"deathDate\": \"2016-02-18\"}");

        try {

            EventLoader.processEvent(message);
            assertEquals(expected, "SUCCESS");

        } catch (Exception e) {
            assertEquals(expected, "REJECT");
        }

    }

    @Test
    void testUnknownEvent() {

        String expected = "REJECT";

        InboundMessage message = mock(InboundMessage.class);
        ReplicationGroupMessageId messageId = mock(ReplicationGroupMessageId.class);
        when(messageId.toString()).thenReturn("rmid1:2fc6e-b5972b2d66f-00000000-0000000");
        when(message.getReplicationGroupMessageId()).thenReturn(messageId);

        when(message.getDestinationName())
                .thenReturn("demographics/patient/abcxyz"); // Set Topic
        when(message.getPayloadAsString()).thenReturn(
                "{\"callbackUrl\": \"https://api.hip-uat.digital.health.nz/fhir/nhi/v1/Patient/ZAT2348\", \"deathDate\": \"2016-02-18\"}");

        try {

            EventLoader.processEvent(message);
            assertEquals(expected, "SUCCESS");

        } catch (Exception e) {
            assertEquals(expected, "REJECT");
        }

    }

    @Test
    void testNullHeader() {

        String expected = "FAILED";

        try {
            InboundMessage message = mock(InboundMessage.class);
            ReplicationGroupMessageId messageId = mock(ReplicationGroupMessageId.class);
            when(messageId.toString()).thenReturn("rmid1:2fc6e-b5972b2d66f-00000000-0000000");
            when(message.getReplicationGroupMessageId()).thenReturn(messageId);

            when(message.getProperty("id")).thenReturn("86152393-fc5b-4b21-a315-5992f31d2aa1");
            when(message.getProperty("type")).thenReturn("demographics/patient/death/new/0.1.0");
            when(message.getProperty("subject")).thenReturn("ZAT2348");
            when(message.getProperty("datacontenttype")).thenReturn("application/json");
            when(message.getProperty("time"))
                    .thenReturn(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

            when(message.getDestinationName())
                    .thenReturn("demographics/patient/death/new/0.1.0/G00036-D/2203/FZZ988-H/ZAT2348"); // Set Topic
            when(message.getPayloadAsString()).thenReturn(
                    "{\"callbackUrl\": \"https://api.hip-uat.digital.health.nz/fhir/nhi/v1/Patient/ZAT2348\", \"deathDate\": \"2016-02-18\"}");

            EventLoader.processEvent(message);

            assertEquals(expected, "SUCCESS");

        } catch (Exception e) {

            assertEquals(expected, e.getMessage());

        }
    }

    @Test
    void testInvalidPayload() {

        String expected = "FAILED";

        try {
            InboundMessage message = mock(InboundMessage.class);
            ReplicationGroupMessageId messageId = mock(ReplicationGroupMessageId.class);
            when(messageId.toString()).thenReturn("rmid1:2fc6e-b5972b2d66f-00000000-0000000");
            when(message.getReplicationGroupMessageId()).thenReturn(messageId);

            when(message.getProperty("source")).thenReturn("https://hip.uat.digital.health.nz");
            when(message.getProperty("id")).thenReturn("86152393-fc5b-4b21-a315-5992f31d2aa1");
            when(message.getProperty("type")).thenReturn("demographics/patient/death/new/0.1.0");
            when(message.getProperty("subject")).thenReturn("ZAT2348");
            when(message.getProperty("datacontenttype")).thenReturn("application/json");
            when(message.getProperty("time"))
                    .thenReturn(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

            when(message.getDestinationName())
                    .thenReturn("demographics/patient/death/new/0.1.0/G00036-D/2203/FZZ988-H/ZAT2348"); // Set Topic
            when(message.getPayloadAsString()).thenReturn(
                    "INVALID PAYLOAD");

            EventLoader.processEvent(message);

            assertEquals(expected, "SUCCESS");

        } catch (Exception e) {

            assertEquals(expected, e.getMessage());

        }
    }
}
