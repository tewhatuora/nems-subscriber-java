package nz.govt.tewhatuora.Utilities;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpRequestInterceptor;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Patient;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import nz.govt.tewhatuora.Service.GlobalProperties;

public class NhiUtil {

    final static boolean PROXY = Boolean.valueOf(GlobalProperties.getProperty("fhir.proxy.enabled"));
    final static String PROXY_SERVER = GlobalProperties.getProperty("fhir.proxy.server");
    final static Integer PROXY_PORT = Integer.valueOf(GlobalProperties.getProperty("fhir.proxy.port"));

    public static HashMap<String, String> GetDeathData(IGenericClient client, String patientIdentifier) {

        String statusDisplay = "";
        String statusCode = "";
        HashMap<String, String> deathData = new HashMap<String, String>();

        try {
            Patient patient = client
                    .read()
                    .resource(Patient.class)
                    .withId(patientIdentifier)
                    .execute();

            if (patient != null) {

                if (patient.getDeceasedDateTimeType() != null) {
                    List<Extension> extensions = patient.getDeceasedDateTimeType().getExtension();

                    if (extensions != null) {

                        for (Extension extension : extensions) {
                            if (extension.getUrl()
                                    .equals("http://hl7.org.nz/fhir/StructureDefinition/information-source")) {
                                // Assuming the extension is for a valueCodeableConcept
                                CodeableConcept valueCodeableConcept = (CodeableConcept) extension.getValue();

                                statusDisplay = valueCodeableConcept.getCodingFirstRep().getDisplay();
                                statusCode = valueCodeableConcept.getCodingFirstRep().getCode();

                            }
                        }

                        DateTimeType dateTime = patient.getDeceasedDateTimeType();
                        deathData.put("date", dateTime.toHumanDisplay());
                        deathData.put("code", statusCode);
                        deathData.put("display", statusDisplay);

                    }
                }
            }
        } catch (Exception e) {
            System.err.println("An unexpected exception occurred: " + e.getMessage());
            e.printStackTrace(); // Log the full stack trace
            return deathData;

        }
        return deathData;

    }

    public static IGenericClient GetClient(String server, String token, String apiKey, String userId) {

        FhirContext ctx = FhirContext.forR4();
        ctx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
        if (PROXY) {
            ctx.getRestfulClientFactory().setProxy(PROXY_SERVER, PROXY_PORT);
        }
        ctx.getRestfulClientFactory().setHttpClient(null);

        IGenericClient client = ctx.newRestfulGenericClient(server);
        client.registerInterceptor(new BearerTokenAuthInterceptor(token));

        HttpClientBuilder customClientBuilder = HttpClients.custom();
        customClientBuilder.addInterceptorFirst((HttpRequestInterceptor) (request, context) -> {
            request.addHeader("x-api-key", apiKey);
            request.addHeader("userid", userId);
        });

        ctx.getRestfulClientFactory().setHttpClient(customClientBuilder.build());

        return client;
    }

    // Validates the date to be YYYY or YYYY-MM or YYYY-MM-DD
    public static boolean ValidateDate(String date) {

        String regex = "([0-9]([0-9]([0-9][1-9]|[1-9]0)|[1-9]00)|[1-9]000)(-(0[1-9]|1[0-2])(-(0[1-9]|[1-2][0-9]|3[0-1]))?)?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(date);

        return matcher.matches();

    }
}
