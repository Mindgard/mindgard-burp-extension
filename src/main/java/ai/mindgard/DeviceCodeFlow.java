package ai.mindgard;

import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.interfaces.RSAPublicKey;
import java.util.Optional;
import java.util.function.Function;

import static ai.mindgard.JSON.fromJson;
import static ai.mindgard.JSON.json;

public class DeviceCodeFlow {
    private final HttpClient http;
    private final Function<String, HttpRequest.BodyPublisher> publisher;
    private MindgardSettingsManager mgsm;

    public interface Factory {
        DeviceCodeFlow create(HttpClient http, Function<String,HttpRequest.BodyPublisher> publisher, MindgardSettingsManager mgsm);
    }
    public DeviceCodeFlow(HttpClient http, Function<String,HttpRequest.BodyPublisher> publisher, MindgardSettingsManager mgsm) {
        this.http = http;
        this.publisher = publisher;
        this.mgsm = mgsm;
    }

    public void validateIdToken(String idToken) {
        var settings = mgsm.getSettings();
        try {
            String issuer = settings.getLoginUrl() + "/";

            JwkProvider jwkProvider = new JwkProviderBuilder(issuer).build();

            String kid = JWT.decode(idToken).getKeyId();

            RSAPublicKey publicKey = (RSAPublicKey) jwkProvider.get(kid).getPublicKey();

            Algorithm algorithm = Algorithm.RSA256(publicKey, null);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(issuer)
                    .withAudience(settings.clientID())
                    .build();

            verifier.verify(idToken);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public record DeviceCodePayload(String client_id, String scope, String audience){}
    public record DeviceCodeData(String verification_uri, String verification_uri_complete, String user_code, String device_code, String expires_in, String interval) {}

    public DeviceCodeData getDeviceCode(Log logger) {;
        var settings = mgsm.getSettings();
        var request = HttpRequest.newBuilder()
                .uri(URI.create(settings.getLoginUrl() + "/oauth/device/code"))
                .header("Content-Type", "application/json")
                .POST(publisher.apply(json(new DeviceCodePayload(
                    settings.clientID(),
                    "openid profile email offline_access",
                    settings.audience()))))
                .build();
        logger.log("Settings login URL: " + settings.getLoginUrl());
        logger.log("Client DID: " + settings.clientID());
        try {
            var response = http.send(request, HttpResponse.BodyHandlers.ofString());
            logger.log("Received response for device code request: " + response.body());
            logger.log("Response status code: " + response.statusCode());
            return fromJson(response.body(), DeviceCodeData.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    public record TokenPayload(String grant_type, String device_code, String client_id, String audience) {}
    public record TokenData(String refresh_token, String id_token, String error_description, String error, String access_token, String scope, String expires_in, String token_type) {}
    public Optional<TokenData> getToken(DeviceCodeData code) {
        var settings = mgsm.getSettings();
        var tokenPayload = new TokenPayload(
                "urn:ietf:params:oauth:grant-type:device_code",
                code.device_code(),
                settings.clientID(),
                settings.audience()
        );

        var request = HttpRequest.newBuilder()
                .uri(URI.create(settings.getLoginUrl() + "/oauth/token"))
                .header("Content-Type", "application/json")
                .POST(publisher.apply(json(tokenPayload)))
                .build();
        try {
            var response = http.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() >= 400
                    ? Optional.empty()
                    : Optional.of(fromJson(response.body(), TokenData.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
