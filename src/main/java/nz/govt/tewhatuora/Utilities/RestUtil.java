package nz.govt.tewhatuora.Utilities;

import org.apache.http.client.HttpClient;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RestUtil {

    public static String RenewToken(String tokenServerUrl, String clientId, String clientSecret, String scope) {

        String token = "";
        // Create an HTTP client
        HttpClient httpClient = HttpClients.createDefault();

        // Create an HTTP POST request to the token server
        HttpPost httpPost = new HttpPost(tokenServerUrl);

        // Set the required headers
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

        // Create the request body
        String requestBody = "grant_type=client_credentials&" + "client_id=" + clientId + "&" + "client_secret="
                + clientSecret;

        // Include the scope if needed
        if (scope != null && !scope.isEmpty()) {
            requestBody += "&scope=" + scope;
        }

        // Set the request body
        httpPost.setEntity(new StringEntity(requestBody, "UTF-8"));

        try {
            // Execute the request
            HttpResponse response = httpClient.execute(httpPost);

            // Parse the response
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());

            if (statusCode == 200) {
                // Request successful, parse the access token
                token = ParseAccessToken(responseBody);
            } else {
                // Request failed, handle the error
                System.err.println("Token request failed. Status code: " + statusCode);
                System.err.println("Response: " + responseBody);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return token;
    }

    // Helper to derive a the server from a URL
    public static String GetServer(String searchString, String fullUrl) {

        int index = fullUrl.indexOf(searchString);
        String server = "";

        if (index != -1) {
            // Extract the substring before the keyword
            server = fullUrl.substring(0, index);
        } else {
            throw new RuntimeException("Invalid format, expecting \"" + searchString + "\" in the URL - " + fullUrl);
        }

        return server;
    }

    // Helper method to parse the access token from the response
    private static String ParseAccessToken(String responseBody) {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(responseBody);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not parse access token, ensure valid credentials are provided");
        }

        return jsonNode.get("access_token").asText();
    }

}
