package ai.mindgard;

import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;

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

    public interface Factory {
        DeviceCodeFlow create(HttpClient http, Function<String,HttpRequest.BodyPublisher> publisher);
    }
    public DeviceCodeFlow(HttpClient http, Function<String,HttpRequest.BodyPublisher> publisher) {
        this.http = http;
        this.publisher = publisher;
    }

    public void validateIdToken(String idToken) {
        try {
            String issuer = Constants.LOGIN_DOMAIN + "/";

            JwkProvider jwkProvider = new JwkProviderBuilder(issuer).build();

            String kid = JWT.decode(idToken).getKeyId();

            RSAPublicKey publicKey = (RSAPublicKey) jwkProvider.get(kid).getPublicKey();

            Algorithm algorithm = Algorithm.RSA256(publicKey, null);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(issuer)
                    .withAudience(Constants.CLIENT_ID)
                    .build();

            verifier.verify(idToken);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public record DeviceCodePayload(String client_id, String scope, String audience){
        public static DeviceCodePayload INSTANCE = new DeviceCodePayload(Constants.CLIENT_ID, "openid profile email offline_access", Constants.AUDIENCE);
    }
    public record DeviceCodeData(String verification_uri, String verification_uri_complete, String user_code, String device_code, String expires_in, String interval) {}

    public DeviceCodeData getDeviceCode() {;
        var request = HttpRequest.newBuilder()
                .uri(URI.create(Constants.LOGIN_DOMAIN + "/oauth/device/code"))
                .header("Content-Type", "application/json")
                .POST(publisher.apply(json(DeviceCodePayload.INSTANCE)))
                .build();
        try {
            var response = http.send(request, HttpResponse.BodyHandlers.ofString());
            return fromJson(response.body(), DeviceCodeData.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    public record TokenPayload(String grant_type, String device_code, String client_id, String audience) {}
    public record TokenData(String refresh_token, String id_token, String error_description, String error, String access_token, String scope, String expires_in, String token_type) {}
    public Optional<TokenData> getToken(DeviceCodeData code) {
        var tokenPayload = new TokenPayload(
                "urn:ietf:params:oauth:grant-type:device_code",
                code.device_code(),
                Constants.CLIENT_ID,
                Constants.AUDIENCE
        );

        var request = HttpRequest.newBuilder()
                .uri(URI.create(Constants.LOGIN_DOMAIN + "/oauth/token"))
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
