package ai.mindgard;

import ai.mindgard.DeviceCodeFlow.DeviceCodeData;
import ai.mindgard.MindgardAuthentication.AuthenticationFailedException;
import ai.mindgard.DeviceCodeFlow.TokenData;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class MindgardAuthenticationTest {

    @Test
    void auth() throws IOException, InterruptedException {
        var http = mock(HttpClient.class);
        var response = mock(HttpResponse.class);

        BodyPublisher publisher = HttpRequest.BodyPublishers.ofString("example-token");
        Function<String, BodyPublisher> matchHttpBody = token -> token.contains("example-token") ? publisher : null;

        // Use mocks for settings and manager
        MindgardSettings settings = mock(MindgardSettings.class);
        when(settings.save(anyString())).thenReturn(true);
        when(settings.getLoginUrl()).thenReturn("https://login.sandbox.mindgard.ai");
        when(settings.clientID()).thenReturn("mindgard-client-id");
        when(settings.audience()).thenReturn("https://mindgard-audience.com");

        MindgardSettingsManager mgsm = mock(MindgardSettingsManager.class);
        when(mgsm.getSettings()).thenReturn(settings);
        doReturn(new MindgardToken("https://sandbox.mindgard.ai", "example-token")).when(mgsm).getToken();

        var auth = new MindgardAuthentication(mgsm, http, matchHttpBody, DeviceCodeFlow::new, () -> {});

        when(http.send(argThat(req -> Objects.equals(publisher,req.bodyPublisher().get())), any())).thenReturn(response);
        when(response.body()).thenReturn("{\"access_token\": \"example-access-token\", \"id_token\": \"x\", \"scope\": \"y\", \"expires_in\":\"z\", \"token_type\":\"u\"}");

        var accessToken = auth.auth();

        assertEquals("example-access-token", accessToken);
    }

    @Test
    void loginMissingTokenFile() throws IOException, InterruptedException {
        var http = mock(HttpClient.class);
        var response = mock(HttpResponse.class);

        BodyPublisher publisher = HttpRequest.BodyPublishers.ofString("example-token");
        Function<String, BodyPublisher> matchHttpBody = token -> token.contains("example-token") ? publisher : null;

        MindgardSettings settings = mock(MindgardSettings.class);
        when(settings.save(anyString())).thenReturn(true);

        MindgardSettingsManager mgsm = mock(MindgardSettingsManager.class);
        when(mgsm.getSettings()).thenReturn(settings);
        doReturn(new MindgardToken("https://sandbox.mindgard.ai", "")).when(mgsm).getToken();

        var auth = new MindgardAuthentication(mgsm, http, matchHttpBody, DeviceCodeFlow::new, () -> {});

        when(http.send(argThat(req -> Objects.equals(publisher,req.bodyPublisher().get())), any())).thenReturn(response);
        when(response.body()).thenReturn("{\"access_token\": \"example-access-token\", \"id_token\": \"x\", \"scope\": \"y\", \"expires_in\":\"z\", \"token_type\":\"u\"}");

        try {
            auth.auth();
            fail("Expected exception due to missing token file");
        } catch (AuthenticationFailedException e) { }
    }

    @Test
    public void login() throws IOException, InterruptedException {
        var http = mock(HttpClient.class);
        DeviceCodeData deviceCode = new DeviceCodeData("http://example.com/login", "http://example.com/login_complete", "user_code", "device_code", "", "");
        TokenData tokenData = new TokenData("refresh_token", "id_token", "", "", "", "", "", "");
        var deviceCodeFlow = mock(DeviceCodeFlow.class);

        when(deviceCodeFlow.getDeviceCode(any(Log.class))).thenReturn(deviceCode);
        when(deviceCodeFlow.getToken(deviceCode))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(tokenData));

        MindgardSettings settings = mock(MindgardSettings.class);
        when(settings.save(anyString())).thenReturn(true);

        MindgardSettingsManager mgsm = mock(MindgardSettingsManager.class);
        when(mgsm.getSettings()).thenReturn(settings);
        doNothing().when(mgsm).setToken(anyString());

        var auth = new MindgardAuthentication(mgsm, http, body -> null,(h,p,m) -> deviceCodeFlow, () -> {});
        auth.validate_login(deviceCode);
        verify(deviceCodeFlow).validateIdToken("id_token");
        verify(mgsm).setToken("refresh_token");
    }

}
