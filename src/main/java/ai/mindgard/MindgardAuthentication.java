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
    private final File tokenFile;
    private final MindgardSettings settings;

    public MindgardAuthentication(MindgardSettings settings) {
        this(settings, MindgardSettings.file("token.txt"), HttpClient.newHttpClient(), HttpRequest.BodyPublishers::ofString, DeviceCodeFlow::new, MindgardAuthentication::sleep);
    }

    public MindgardAuthentication(MindgardSettings settings, File tokenFile, HttpClient http, Function<String,HttpRequest.BodyPublisher> bodyPublisherFactory, DeviceCodeFlow.Factory deviceCodeFlow, Runnable sleep) {
        this.tokenFile = tokenFile;
        this.http = http;
        this.publisher = bodyPublisherFactory;
        this.sleep = sleep;
        this.deviceCodeFlow = deviceCodeFlow.create(settings.serverConfiguration(), http, publisher);
        this.settings = settings;
    }

    public static class AuthenticationFailedException extends RuntimeException {
        public AuthenticationFailedException(Exception e) {
            super(e);
        }
    }

    public String auth() {
        try (Stream<String> lines = Files.lines(tokenFile.toPath())) {
            String refreshToken = lines.collect(Collectors.joining(""));
            record RefreshToken(String grant_type, String client_id, String audience, String refresh_token) {
            }
            record AccessToken(String access_token, String id_token, String scope, String expires_in,
                               String token_type) {
            }
            var serverConfig = settings.serverConfiguration();

            var data = new RefreshToken("refresh_token",
                    serverConfig.getMindgardClientID(),
                    serverConfig.getMindgardAudience(),
                    refreshToken);

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(serverConfig.getMindgardLoginUrl() + "/oauth/token"))
                    .header("Content-Type", "application/json")
                    .POST(publisher.apply(json(data)))
                    .build();

            var response = http.send(request, HttpResponse.BodyHandlers.ofString());

            return fromJson(response.body(), AccessToken.class).access_token();
        } catch (Exception e) {
            throw new AuthenticationFailedException(e);
        }
    }

    public DeviceCodeData get_device_code() {
        return deviceCodeFlow.getDeviceCode();
    }

    public void validate_login(DeviceCodeData deviceCode) {
        Optional<TokenData> tokenData = Optional.empty();
        while ((tokenData = deviceCodeFlow.getToken(deviceCode)).isEmpty()) {
            sleep.run();
        }
        deviceCodeFlow.validateIdToken(tokenData.get().id_token());
        store(tokenData.get().refresh_token());
    }

    private void store(String refreshToken) {
        try {
            Files.writeString(tokenFile.toPath(), refreshToken);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void sleep() {
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
