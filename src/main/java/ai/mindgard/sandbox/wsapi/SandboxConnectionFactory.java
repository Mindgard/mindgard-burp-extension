package ai.mindgard.sandbox.wsapi;

import ai.mindgard.Constants;
import ai.mindgard.JSON;
import ai.mindgard.Log;
import ai.mindgard.MindgardAuthentication;
import ai.mindgard.MindgardSettings;
import ai.mindgard.sandbox.Dataset;
import ai.mindgard.sandbox.Mindgard;
import ai.mindgard.sandbox.wsapi.messages.CliInitResponse;
import ai.mindgard.sandbox.wsapi.messages.OrchestratorSetupRequest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ai.mindgard.JSON.fromJson;
import static ai.mindgard.JSON.json;

public class SandboxConnectionFactory {

    private final HttpClient http;
    private final Function<String, HttpRequest.BodyPublisher> bodyPublisherFactory;
    private final MindgardWebsocketClient.Factory clientFactory;
    private MindgardSettings settings;

    public SandboxConnectionFactory(MindgardSettings settings) {
        this(settings, HttpClient.newHttpClient(), HttpRequest.BodyPublishers::ofString, MindgardWebsocketClient::new);
    }

    public SandboxConnectionFactory(MindgardSettings settings, HttpClient http, Function<String, HttpRequest.BodyPublisher> bodyPublisherFactory, MindgardWebsocketClient.Factory clientFactory) {
        this.http = http;
        this.bodyPublisherFactory = bodyPublisherFactory;
        this.clientFactory = clientFactory;
        this.settings = settings;
    }

    public SandboxConnectionFactory(MindgardSettings settings, HttpClient http) {
        this(settings, http, HttpRequest.BodyPublishers::ofString, MindgardWebsocketClient::new);
    }

    public SandboxConnection connect(Mindgard mindgard, MindgardAuthentication auth, MindgardSettings settings, Log logger) {
        CliInitResponse cliInitResponse = cliInit(settings.projectID(), auth.auth(), settings, logger);
        if (cliInitResponse.errors() != null) {
            throw new ConnectionFailedException(cliInitResponse.errors());
        }
        var client = clientFactory.create(cliInitResponse, mindgard, logger);
        var ws = http.newWebSocketBuilder()
                .subprotocols("json.webpubsub.azure.v1")
                .buildAsync(URI.create(cliInitResponse.url()), client)
                .join();

        return new SandboxConnection(cliInitResponse, ws, client);
    }


    public static class ConnectionFailedException extends RuntimeException {
        public ConnectionFailedException(Exception e) {
            super(e);
        }

        public ConnectionFailedException(List<CliInitResponse.Error> errors) {
            super(errors.stream().map(CliInitResponse.Error::message).collect(Collectors.joining()));
        }
    }

    private List<String> commaSeperatedToList(String includedOrExcluded) {
        if (includedOrExcluded == null || includedOrExcluded.isEmpty()) {
            return null;
        }
        return Arrays.stream(includedOrExcluded.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    private CliInitResponse cliInit(String projectID, String accessToken, MindgardSettings settings, Log logger) {
        try {
            var params = new OrchestratorSetupRequest(
                    projectID,
                    settings.parallelism(),
                    settings.systemPrompt(),
                    Dataset.fromFile(settings.customDatasetFilename()).map(JSON::json).orElse(settings.dataset()),
                    Dataset.fromFile(settings.customDatasetFilename()).map(JSON::json).orElse(null),
                    "llm",
                    "user",
                    "sandbox",
                    null,
                    commaSeperatedToList(settings.exclude()),
                    commaSeperatedToList(settings.include()),
                    settings.promptRepeats()
            );

            var cliInit = HttpRequest.newBuilder()
                    .uri(URI.create(settings.serverConfiguration().getMindgardApiUrl() + "/api/v1/tests/cli_init"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("User-Agent", "mindgard-burp/1.1.0") // TODO: update version dynamically
                    .header("X-User-Agent", "mindgard-burp/1.1.0") // TODO: update version dynamically
                    .POST(bodyPublisherFactory.apply(json(params)))
                    .build();

            var cliInitResponse = http.send(cliInit, HttpResponse.BodyHandlers.ofString());
            return fromJson(cliInitResponse.body(), CliInitResponse.class);
        } catch (Exception e) {
            throw new ConnectionFailedException(e);
        }
    }

    /**
     * Validates whether a given project exists for the current user
     * Returns true if valid, false otherwise.
     */
    public boolean validateProject(MindgardSettings settings, String projectID) {
        try {
            var auth = new MindgardAuthentication(settings);

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(settings.serverConfiguration().getMindgardApiUrl() + "/api/v1/projects/validate/" + projectID))
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + auth.auth())
                    .GET()
                    .build();

            var response = http.send(request, HttpResponse.BodyHandlers.ofString());
            var json = JSON.fromJson(response.body(), java.util.Map.class);
            Object validObj = json.get("valid");
            if (validObj instanceof Boolean) {
                return (Boolean) validObj;
            } else if (validObj instanceof String) {
                return Boolean.parseBoolean((String) validObj);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
