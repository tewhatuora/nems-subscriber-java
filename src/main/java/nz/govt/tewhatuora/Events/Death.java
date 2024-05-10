package nz.govt.tewhatuora.Events;

import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.solace.messaging.receiver.InboundMessage;

import nz.govt.tewhatuora.Models.DeathModel;
import nz.govt.tewhatuora.Service.GlobalProperties;
import nz.govt.tewhatuora.Utilities.EventUtil;
import nz.govt.tewhatuora.Utilities.NhiUtil;
import nz.govt.tewhatuora.Database.DeathDatabase;
import nz.govt.tewhatuora.Utilities.RestUtil;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class Death {

    final static String TOKEN_SERVER = GlobalProperties.getProperty("nhi.auth.tokenserver");
    final static String CLIENT_ID = GlobalProperties.getProperty("nhi.auth.clientid");
    final static String CLIENT_SECRET = GlobalProperties.getProperty("nhi.auth.clientsecret");
    final static String SCOPE = GlobalProperties.getProperty("nhi.auth.scope");
    final static String API_KEY = GlobalProperties.getProperty("nhi.auth.apikey");
    final static String USER_ID = GlobalProperties.getProperty("nhi.auth.userid");

    public static void processDeath(InboundMessage message) {

        String token = RestUtil.RenewToken(TOKEN_SERVER, CLIENT_ID, CLIENT_SECRET, SCOPE);
        String jsonString = message.getPayloadAsString();
        String status = EventUtil.GetFieldFromString(message.getDestinationName(), "/", 3);
        String deathDate = "";
        String nhiServer = "";
        String nhiId = "";
        String source = "";
        boolean valid = true;

        Gson gson = new GsonBuilder().create();

        try {
            DeathModel deathNotice = gson.fromJson(jsonString, DeathModel.class);
            nhiId = EventUtil.GetFieldFromString(deathNotice.getCallbackUrl(), "/", -1);
            deathDate = deathNotice.getDeathDate();
            nhiServer = RestUtil.GetServer("Patient", deathNotice.getCallbackUrl());
        } catch (Exception e) {
            System.out.println(jsonString);
            System.out.println("Payload Could Not Be Parsed - Dropping Message");
            System.out.println(e);
            valid = false;
        }

        if (!NhiUtil.ValidateDate(deathDate)) {
            valid = false;
            System.out.println(jsonString);
            System.out.println(
                    "Death Date Invalid - Dropping Message : " + message.getReplicationGroupMessageId().toString());
        }

        if (valid) {
            IGenericClient client = NhiUtil.GetClient(nhiServer, token, API_KEY,
                    USER_ID);

            try {

                HashMap<String, String> deathData = NhiUtil.GetDeathData(client, nhiId);
                source = deathData.get("display");
                String content = "|| Date: " + deathDate
                        + " || Status: " + deathData.get("code") + " : " + deathData.get("display")
                        + " || Topic: " + message.getDestinationName()
                        + " || Message ID: " + message.getReplicationGroupMessageId().toString()
                        + " ||";
                String border = "=".repeat(content.length());

                System.out.printf("%s\n%s\n%s\n", border, content, border);
            } catch (Exception e) {

                System.out.println("Message Dropped : " + message.getReplicationGroupMessageId().toString());
                System.out.println(e.getMessage());
            }

            System.out.println("Calling Procedure");
            Boolean dbStatus = DeathDatabase.CallDeathDtProc("database", nhiId, deathDate, status, source);

            if (dbStatus) {
                System.out.println("Procedure Completed");
            } else {
                throw new RuntimeException("Error occured writing to database");
            }
        }

    }

}
