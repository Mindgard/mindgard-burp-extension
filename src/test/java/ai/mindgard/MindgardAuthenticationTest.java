package ai.mindgard;

import ai.mindgard.DeviceCodeFlow.DeviceCodeData;
import ai.mindgard.MindgardAuthentication.AuthenticationFailedException;
import ai.mindgard.DeviceCodeFlow.TokenData;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static ai.mindgard.MindgardAuthentication.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class MindgardAuthenticationTest {


    @Test
    void auth() throws IOException, InterruptedException {
        File tmpFile = File.createTempFile("mindgard","token");
        Files.write(tmpFile.toPath(), List.of("example-token"));
        var http = mock(HttpClient.class);
        var response = mock(HttpResponse.class);

        BodyPublisher publisher = HttpRequest.BodyPublishers.ofString("example-token");
        Function<String, BodyPublisher> matchHttpBody = token -> token.contains("example-token") ? publisher : null;

        var auth = new MindgardAuthentication(l -> {}, tmpFile, http, matchHttpBody, DeviceCodeFlow::new, () -> {});

        when(http.send(argThat(req -> Objects.equals(publisher,req.bodyPublisher().get())), any())).thenReturn(response);
        when(response.body()).thenReturn("{\"access_token\": \"example-access-token\", \"id_token\": \"x\", \"scope\": \"y\", \"expires_in\":\"z\", \"token_type\":\"u\"}");

        var accessToken = auth.auth();

        assertEquals("example-access-token", accessToken);
    }

    @Test
    void loginMissingTokenFile() throws IOException, InterruptedException {
        File tmpFile = new File("/does/not/exist");
        var http = mock(HttpClient.class);
        var response = mock(HttpResponse.class);

        BodyPublisher publisher = HttpRequest.BodyPublishers.ofString("example-token");
        Function<String, BodyPublisher> matchHttpBody = token -> token.contains("example-token") ? publisher : null;

        var auth = new MindgardAuthentication(l -> {}, tmpFile, http, matchHttpBody, DeviceCodeFlow::new, () -> {});

        when(http.send(argThat(req -> Objects.equals(publisher,req.bodyPublisher().get())), any())).thenReturn(response);
        when(response.body()).thenReturn("{\"access_token\": \"example-access-token\", \"id_token\": \"x\", \"scope\": \"y\", \"expires_in\":\"z\", \"token_type\":\"u\"}");

        try {
            var accessToken = auth.auth();
            fail("Expected exception due to missing token file");
        } catch (AuthenticationFailedException e) { }
    }

    @Test
    public void login() throws IOException, InterruptedException {
        File tmpFile = File.createTempFile("mindgard","test");
        tmpFile.deleteOnExit();
        var http = mock(HttpClient.class);
        var log = mock(Log.class);
        DeviceCodeData deviceCode = new DeviceCodeData("http://example.com/login", "http://example.com/login_complete", "user_code", "device_code", "", "");
        TokenData tokenData = new TokenData("refresh_token", "id_token", "", "", "", "", "", "");
        var deviceCodeFlow = mock(DeviceCodeFlow.class);


        when(deviceCodeFlow.getDeviceCode()).thenReturn(deviceCode);
        when(deviceCodeFlow.getToken(deviceCode))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(tokenData));

        var auth = new MindgardAuthentication(log, tmpFile, http, body -> null,(h,p) -> deviceCodeFlow, () -> {});
        auth.login();

        verify(log).log("Click http://example.com/login_complete");
        verify(log).log("Confirm you see user_code");
        verify(deviceCodeFlow).validateIdToken("id_token");

        var storedRefreshToken = Files.readAllLines(tmpFile.toPath()).get(0);
        assertEquals("refresh_token", storedRefreshToken);

    }

}