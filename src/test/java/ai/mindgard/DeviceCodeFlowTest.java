package ai.mindgard;

import ai.mindgard.DeviceCodeFlow.DeviceCodePayload;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.function.Function;

import static ai.mindgard.JSON.json;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeviceCodeFlowTest {

    @Test
    void getDeviceCode() throws IOException, InterruptedException {
        HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.ofString("example-token");

        var http = mock(HttpClient.class);
        var response = mock(HttpResponse.class);

        var tokenData = new DeviceCodeFlow.DeviceCodeData("verification_uri", "verification_uri_complete","user_code","device_code","expires_in","interval");

        var matchHttpBody = mockHttp(json(DeviceCodePayload.INSTANCE),json(tokenData), publisher, http, response);


        var actual = new DeviceCodeFlow(http, matchHttpBody).getDeviceCode();

        assertEquals(tokenData, actual);
    }

    @Test
    void getToken() throws IOException, InterruptedException {
        HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.ofString("example-token");

        var deviceCodeData = new DeviceCodeFlow.DeviceCodeData("verification_uri", "verification_uri_complete","user_code","device_code","expires_in","interval");
        var tokenPayload = new DeviceCodeFlow.TokenPayload(
                "urn:ietf:params:oauth:grant-type:device_code",
                deviceCodeData.device_code(),
                Constants.CLIENT_ID,
                Constants.AUDIENCE
        );
        var tokenData = new DeviceCodeFlow.TokenData("refresh_token", "id_token",null,null,"access_token","scope","expires_in","token_type");
        var http = mock(HttpClient.class);
        var response = mock(HttpResponse.class);

        var matchHttpBody = mockHttp(json(tokenPayload), json(tokenData), publisher, http, response);

        var actual = new DeviceCodeFlow(http, matchHttpBody).getToken(deviceCodeData);

        assertEquals(tokenData, actual.get());
    }

    private static Function<String, HttpRequest.BodyPublisher> mockHttp(String expectedBody, String actualResponse, HttpRequest.BodyPublisher publisher, HttpClient http, HttpResponse response) throws IOException, InterruptedException {
        Function<String, HttpRequest.BodyPublisher> matchHttpBody = body -> {
            assertEquals(body, expectedBody);
            return publisher;
        };

        when(http.send(argThat(req -> Objects.equals(publisher, req.bodyPublisher().get())), any()))
                .thenReturn(response);
        when(response.body())
                .thenReturn(actualResponse);
        return matchHttpBody;
    }
}