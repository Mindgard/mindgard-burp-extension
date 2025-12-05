package ai.mindgard;

import ai.mindgard.DeviceCodeFlow.DeviceCodeData;
import ai.mindgard.DeviceCodeFlow.TokenData;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ai.mindgard.JSON.fromJson;
import static ai.mindgard.JSON.json;


public class MindgardAuthentication {

    private final HttpClient http;
    private Function<String, HttpRequest.BodyPublisher> publisher;
    private final Runnable sleep;
    private final DeviceCodeFlow deviceCodeFlow;
    private MindgardSettingsManager mgsm;

    /**
     * Constructor
     * @param settings The Mindgard Settings record
     */
    public MindgardAuthentication(MindgardSettingsManager mgsm) {
        this(
            mgsm,
            HttpClient.newHttpClient(), 
            HttpRequest.BodyPublishers::ofString, 
            DeviceCodeFlow::new, 
            MindgardAuthentication::sleep
        );
    }

    public MindgardAuthentication(MindgardSettingsManager mgsm, HttpClient http, Function<String,HttpRequest.BodyPublisher> bodyPublisherFactory, DeviceCodeFlow.Factory deviceCodeFlow, Runnable sleep) {
        this.mgsm = mgsm;
        this.http = http;
        this.publisher = bodyPublisherFactory;
        this.sleep = sleep;
        this.deviceCodeFlow = deviceCodeFlow.create(http, publisher, mgsm);
    }

    /**
     * Authenticates the user using a stored refresh token and retrieves a new access token.
     * <p>
     * This method reads the refresh token from the configured token file, constructs a request to the authentication
     * endpoint, and exchanges the refresh token for a new access token. If authentication fails, an
     * {@link AuthenticationFailedException} is thrown.
     *
     * @return the newly obtained access token as a {@code String}
     * @throws AuthenticationFailedException if authentication fails or an error occurs during the process
     */
    public String auth() {
        var settings = mgsm.getSettings();
        try {
            MindgardToken token = mgsm.getToken();
            record RefreshToken(String grant_type, String client_id, String audience, String refresh_token) {}
            record AccessToken(String access_token, String id_token, String scope, String expires_in, String token_type) {}

            var data = new RefreshToken("refresh_token", settings.clientID(), settings.audience(), token.token());

            // Send the authentication request to the URL contained in the settings.
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(settings.getLoginUrl() + "/oauth/token"))
                    .header("Content-Type", "application/json")
                    .POST(publisher.apply(json(data)))
                    .build();

            var response = http.send(request, HttpResponse.BodyHandlers.ofString());

            return fromJson(response.body(), AccessToken.class).access_token();
        } catch (Exception e) {
            throw new AuthenticationFailedException(e);
        }
    }

    /**
     * Validates the login using the provided device code data.
     * @param deviceCode the device code data obtained from the device code flow
     */
    public void validate_login(DeviceCodeData deviceCode) {
        Optional<TokenData> tokenData = Optional.empty();
        while ((tokenData = deviceCodeFlow.getToken(deviceCode)).isEmpty()) {
            sleep.run();
        }
        deviceCodeFlow.validateIdToken(tokenData.get().id_token());
        mgsm.setToken(tokenData.get().refresh_token());
    }

    private static void sleep() {
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Exception thrown when authentication fails.
     */
    public static class AuthenticationFailedException extends RuntimeException {
        public AuthenticationFailedException(Exception e) {
            super(e);
        }
    }

    /**
     * @return the device code data
     */
    public DeviceCodeData get_device_code() {return deviceCodeFlow.getDeviceCode(); }
}
