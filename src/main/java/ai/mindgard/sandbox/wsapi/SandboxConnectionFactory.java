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
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ai.mindgard.JSON.fromJson;
import static ai.mindgard.JSON.json;

/**
 * Factory class responsible for creating and managing connections to the Mindgard Sandbox WebSocket API.
 * <p>
 * This class provides methods to initialize and validate connections, as well as to handle authentication and
 * configuration for the Mindgard sandbox environment. It supports both default and parameterized construction,
 * allowing for flexible dependency injection of HTTP clients, body publishers, and WebSocket client factories.
 * </p>
 *
 * <p>
 * Usage example:
 * <pre>
 *     SandboxConnectionFactory factory = new SandboxConnectionFactory();
 *     SandboxConnection connection = factory.connect(mindgard, auth, settings, logger);
 * </pre>
 * </p>
 *
 */
public class SandboxConnectionFactory {

    private final HttpClient http;
    private final Function<String, HttpRequest.BodyPublisher> bodyPublisherFactory;
    private final MindgardWebsocketClient.Factory clientFactory;

    /**
     * Constructors
     */
    public SandboxConnectionFactory() {
        this(HttpClient.newHttpClient(), HttpRequest.BodyPublishers::ofString, MindgardWebsocketClient::new);
    }

    /**
     * Parameterized Constructor
     * @param http HTTP Client
     * @param bodyPublisherFactory Function to create Body Publishers
     * @param clientFactory Factory for Mindgard WebSocket Clients
     */
    public SandboxConnectionFactory(HttpClient http, Function<String, HttpRequest.BodyPublisher> bodyPublisherFactory, MindgardWebsocketClient.Factory clientFactory) {
        this.http = http;
        this.bodyPublisherFactory = bodyPublisherFactory;
        this.clientFactory = clientFactory;
    }

    /**
     * Constructor with only HttpClient
     * @param http HTTP Client
     */
    public SandboxConnectionFactory(HttpClient http) {
        this(http, HttpRequest.BodyPublishers::ofString, MindgardWebsocketClient::new);
    }


    /**
     * Establishes a connection to the Mindgard Sandbox WebSocket API.
     * @param mindgard The Mindgard Extension instance
     * @param auth Authentication handler
     * @param settings Mindgard settings
     * @param logger Logger instance
     * @return Established SandboxConnection
     */
    public SandboxConnection connect(Mindgard mindgard, MindgardAuthentication auth, MindgardSettings settings, Log logger) {
        CliInitResponse cliInitResponse = cliInit(settings.projectID(), auth.auth(), settings, logger);
        if (cliInitResponse.errors() != null) {
            throw new ConnectionFailedException(cliInitResponse.errors());
        }
        var client = clientFactory.create(cliInitResponse, mindgard, logger);
        var ws = http.newWebSocketBuilder()
                .subprotocols("json.webpubsub.azure.v1") // Magic string
                .buildAsync(URI.create(cliInitResponse.url()), client)
                .join();

        return new SandboxConnection(cliInitResponse, ws, client);
    }

    /**
     * Initializes the CLI connection with the Mindgard Orchestratration Service.
     * @param projectID the project ID
     * @param accessToken the access token
     * @param settings Mindgard settings
     * @param logger Logger instance
     * @return CLI Initialization Response
     */
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
                    .uri(URI.create(settings.addSubdomainToURI(settings.url(), "api") + "/api/v1/tests/cli_init"))
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
     * Validates the provided project ID against the Mindgard API.
     * @param projectID The project ID to validate
     * @param apiURL The base API URL
     * @return true if the project ID is valid, false otherwise
     */
    public boolean validateProject(String projectID, String apiURL) {
        try {
            Log dummyLog = l -> {}; // Using dummy log for simplicity since MindgardAuthentication.auth() doesn't use logging
            var auth = new MindgardAuthentication(dummyLog);

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(apiURL + "/api/v1/projects/validate/" + projectID))
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

    /**
     * Converts a comma-separated string into a list of trimmed strings.
     * @param includedOrExcluded The comma-separated string
     * @return List of trimmed strings, or null if input is null or empty
     */
    private List<String> commaSeperatedToList(String includedOrExcluded) {
        if (includedOrExcluded == null || includedOrExcluded.isEmpty()) {
            return null;
        }
        return Arrays.stream(includedOrExcluded.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }
        /**
     * Exceptions thrown when connection to the Mindgard Sandbox WebSocket API fails.
     */
    public static class ConnectionFailedException extends RuntimeException {
        public ConnectionFailedException(Exception e) {
            super(e);
        }

        public ConnectionFailedException(List<CliInitResponse.Error> errors) {
            super(errors.stream().map(CliInitResponse.Error::message).collect(Collectors.joining()));
        }
    }
}
