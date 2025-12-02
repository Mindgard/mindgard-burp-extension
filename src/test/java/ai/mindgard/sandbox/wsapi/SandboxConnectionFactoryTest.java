package ai.mindgard.sandbox.wsapi;

import ai.mindgard.JSON;
import ai.mindgard.MindgardAuthentication;
import ai.mindgard.MindgardSettings;
import ai.mindgard.MindgardSettings;
import ai.mindgard.sandbox.Mindgard;
import ai.mindgard.sandbox.wsapi.messages.CliInitResponse;
import ai.mindgard.sandbox.wsapi.messages.OrchestratorSetupRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opentest4j.AssertionFailedError;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SandboxConnectionFactoryTest {

    @Test
    void connect() throws IOException, InterruptedException {
        var http = mock(HttpClient.class, Mockito.RETURNS_DEEP_STUBS);
        var mindgard = mock(Mindgard.class);
        var websocket = mock(WebSocket.class);
        var auth = mock(MindgardAuthentication.class);
        var client = mock(MindgardWebsocketClient.class);
        HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.ofString("mock");
        var settings = new MindgardSettings("selector","projectID","dataset", "systemPrompt", null, null, null, null, 1);
        var expectedRequest = new OrchestratorSetupRequest(
            settings.projectID(),
            1,
            settings.systemPrompt(),
            settings.dataset(),
            null,
            "llm",
            "user",
            "sandbox",
            null,
            null,
            null,
                null
        );
        Function<String, HttpRequest.BodyPublisher> matchHttpBody = body -> {
            assertEquals(body, JSON.json(expectedRequest));
            return publisher;
        };
        var response = mock(HttpResponse.class);

        when(http.send(argThat(req -> Objects.equals(publisher,req.bodyPublisher().get())), any())).thenReturn(response);

        var cf = new SandboxConnectionFactory(http, matchHttpBody, (a,b,c) -> client);

        CliInitResponse cliInitResponse = new CliInitResponse("groupId", "http://example.com/ws");
        when(response.body()).thenReturn(JSON.json(cliInitResponse));
        when(
            http
                .newWebSocketBuilder()
                .subprotocols("json.webpubsub.azure.v1")
                .buildAsync(eq(URI.create("http://example.com/ws")), any(MindgardWebsocketClient.class))
        ).thenReturn(CompletableFuture.completedFuture(websocket));

        var connection = cf.connect(mindgard, auth, settings, l -> {});

        assertEquals(connection, new SandboxConnection(cliInitResponse,websocket,client));

    }

    @Test
    void connectAbortsOnError() throws IOException, InterruptedException {
        var http = mock(HttpClient.class, Mockito.RETURNS_DEEP_STUBS);
        var mindgard = mock(Mindgard.class);
        var websocket = mock(WebSocket.class);
        var auth = mock(MindgardAuthentication.class);
        var client = mock(MindgardWebsocketClient.class);
        HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.ofString("mock");
        var settings = new MindgardSettings("selector","projectID","dataset", "systemPrompt", null, null, null, null, 1);
        var expectedRequest = new OrchestratorSetupRequest(
                settings.projectID(),
                1,
                settings.systemPrompt(),
                settings.dataset(),
                null,
                "llm",
                "user",
                "sandbox",
                null,
                null,
                null,
                null
        );
        Function<String, HttpRequest.BodyPublisher> matchHttpBody = body -> {
            assertEquals(body, JSON.json(expectedRequest));
            return publisher;
        };
        var response = mock(HttpResponse.class);

        when(http.send(argThat(req -> Objects.equals(publisher,req.bodyPublisher().get())), any())).thenReturn(response);

        var cf = new SandboxConnectionFactory(http, matchHttpBody, (a,b,c) -> client);

        CliInitResponse cliInitResponse = new CliInitResponse(null, null, List.of(new CliInitResponse.Error("lol")));
        when(response.body()).thenReturn(JSON.json(cliInitResponse));
        when(
                http
                        .newWebSocketBuilder()
                        .subprotocols("json.webpubsub.azure.v1")
                        .buildAsync(eq(URI.create("http://example.com/ws")), any(MindgardWebsocketClient.class))
        ).thenReturn(CompletableFuture.completedFuture(websocket));

        try {
            var connection = cf.connect(mindgard, auth, settings, l -> {});
            fail("Expected exception due to CLIInit Errors");
        } catch (SandboxConnectionFactory.ConnectionFailedException e) {

        }
    }

    @Test
    void usesCustomDataset() throws IOException, InterruptedException {
        var customDataset = List.of(
            "Prompt1",
            "Prompt2"
        );

        Path customDatasetPath = Files.createTempFile("custom", "dataset");
        customDatasetPath.toFile().deleteOnExit();
        Files.write(customDatasetPath, customDataset);

        var http = mock(HttpClient.class, Mockito.RETURNS_DEEP_STUBS);
        var mindgard = mock(Mindgard.class);
        var websocket = mock(WebSocket.class);
        var auth = mock(MindgardAuthentication.class);
        var client = mock(MindgardWebsocketClient.class);
        HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.ofString("mock");
        var settings = new MindgardSettings("selector","projectID","dataset", "systemPrompt", customDatasetPath.toAbsolutePath().toString(), null, null, null, 1);
        var expectedRequest = new OrchestratorSetupRequest(
                settings.projectID(),
                1,
                settings.systemPrompt(),
                JSON.json(customDataset),
                JSON.json(customDataset),
                "llm",
                "user",
                "sandbox",
                null,
                null,
                null,
                null
        );
        Function<String, HttpRequest.BodyPublisher> matchHttpBody = body -> {
            assertEquals(body, JSON.json(expectedRequest));
            return publisher;
        };
        var response = mock(HttpResponse.class);

        when(http.send(argThat(req -> Objects.equals(publisher,req.bodyPublisher().get())), any())).thenReturn(response);

        var cf = new SandboxConnectionFactory(http, matchHttpBody, (a,b,c) -> client);

        CliInitResponse cliInitResponse = new CliInitResponse("groupId", "http://example.com/ws");
        when(response.body()).thenReturn(JSON.json(cliInitResponse));
        when(
                http
                        .newWebSocketBuilder()
                        .subprotocols("json.webpubsub.azure.v1")
                        .buildAsync(eq(URI.create("http://example.com/ws")), any(MindgardWebsocketClient.class))
        ).thenReturn(CompletableFuture.completedFuture(websocket));

        var connection = cf.connect(mindgard, auth, settings, l -> {});

        assertEquals(connection, new SandboxConnection(cliInitResponse,websocket,client));

    }

    @Test
    void usesExcludeSingleAttack() throws IOException, InterruptedException {
        
        var excludedAttack = "PersonGPT";
        var http = mock(HttpClient.class, Mockito.RETURNS_DEEP_STUBS);
        var mindgard = mock(Mindgard.class);
        var websocket = mock(WebSocket.class);
        var auth = mock(MindgardAuthentication.class);
        var client = mock(MindgardWebsocketClient.class);
        HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.ofString("mock");
        var settings = new MindgardSettings("selector","projectID","dataset", "systemPrompt", null, excludedAttack, null, null, 1);
        var expectedRequest = new OrchestratorSetupRequest(
                settings.projectID(),
                1,
                settings.systemPrompt(),
                settings.dataset(),
                null,
                "llm",
                "user",
                "sandbox",
                null,
                List.of(excludedAttack),
                null,
                null
        );

        Function<String, HttpRequest.BodyPublisher> matchHttpBody = body -> {
            assertEquals(body, JSON.json(expectedRequest));
            return publisher;
        };
        var response = mock(HttpResponse.class);

        when(http.send(argThat(req -> Objects.equals(publisher,req.bodyPublisher().get())), any())).thenReturn(response);

        var cf = new SandboxConnectionFactory(http, matchHttpBody, (a,b,c) -> client);

        CliInitResponse cliInitResponse = new CliInitResponse("groupId", "http://example.com/ws");
        when(response.body()).thenReturn(JSON.json(cliInitResponse));
        when(
                http
                        .newWebSocketBuilder()
                        .subprotocols("json.webpubsub.azure.v1")
                        .buildAsync(eq(URI.create("http://example.com/ws")), any(MindgardWebsocketClient.class))
        ).thenReturn(CompletableFuture.completedFuture(websocket));

        var connection = cf.connect(mindgard, auth, settings, l -> {});

        assertEquals(connection, new SandboxConnection(cliInitResponse,websocket,client));

    }

    @Test
    void usesExcludeMultipleAttacks() throws IOException, InterruptedException {
        
        var excludedAttack = "PersonGPT,AntiGPT,DynamicTest";
        var http = mock(HttpClient.class, Mockito.RETURNS_DEEP_STUBS);
        var mindgard = mock(Mindgard.class);
        var websocket = mock(WebSocket.class);
        var auth = mock(MindgardAuthentication.class);
        var client = mock(MindgardWebsocketClient.class);
        HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.ofString("mock");
        var settings = new MindgardSettings("selector","projectID","dataset", "systemPrompt", null, excludedAttack, null, null, 1);

        var expectedRequest = new OrchestratorSetupRequest(
                settings.projectID(),
                1,
                settings.systemPrompt(),
                settings.dataset(),
                null,
                "llm",
                "user",
                "sandbox",
                null,
                Arrays.asList("PersonGPT","AntiGPT","DynamicTest"),
                null,
                null
        );
        
        Function<String, HttpRequest.BodyPublisher> matchHttpBody = body -> {
            assertEquals(body, JSON.json(expectedRequest));
            return publisher;
        };
        var response = mock(HttpResponse.class);

        when(http.send(argThat(req -> Objects.equals(publisher,req.bodyPublisher().get())), any())).thenReturn(response);

        var cf = new SandboxConnectionFactory(http, matchHttpBody, (a,b,c) -> client);

        CliInitResponse cliInitResponse = new CliInitResponse("groupId", "http://example.com/ws");
        when(response.body()).thenReturn(JSON.json(cliInitResponse));
        when(
                http
                        .newWebSocketBuilder()
                        .subprotocols("json.webpubsub.azure.v1")
                        .buildAsync(eq(URI.create("http://example.com/ws")), any(MindgardWebsocketClient.class))
        ).thenReturn(CompletableFuture.completedFuture(websocket));

        var connection = cf.connect(mindgard, auth, settings, l -> {});

        assertEquals(connection, new SandboxConnection(cliInitResponse,websocket,client));

    }


    @Test
    void usesIncludeAttack() throws IOException, InterruptedException {
        
        var includedAttack = "PersonGPT";
        var http = mock(HttpClient.class, Mockito.RETURNS_DEEP_STUBS);
        var mindgard = mock(Mindgard.class);
        var websocket = mock(WebSocket.class);
        var auth = mock(MindgardAuthentication.class);
        var client = mock(MindgardWebsocketClient.class);
        HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.ofString("mock");
        var settings = new MindgardSettings("selector","projectID","dataset", "systemPrompt", null, null, includedAttack, null, 1);

        var expectedRequest = new OrchestratorSetupRequest(
                settings.projectID(),
                1,
                settings.systemPrompt(),
                settings.dataset(),
                null,
                "llm",
                "user",
                "sandbox",
                null,
                null,
                List.of("PersonGPT"),
                null
        );
        
        Function<String, HttpRequest.BodyPublisher> matchHttpBody = body -> {
            assertEquals(body, JSON.json(expectedRequest));
            return publisher;
        };
        var response = mock(HttpResponse.class);

        when(http.send(argThat(req -> Objects.equals(publisher,req.bodyPublisher().get())), any())).thenReturn(response);

        var cf = new SandboxConnectionFactory(http, matchHttpBody, (a,b,c) -> client);

        CliInitResponse cliInitResponse = new CliInitResponse("groupId", "http://example.com/ws");
        when(response.body()).thenReturn(JSON.json(cliInitResponse));
        when(
                http
                        .newWebSocketBuilder()
                        .subprotocols("json.webpubsub.azure.v1")
                        .buildAsync(eq(URI.create("http://example.com/ws")), any(MindgardWebsocketClient.class))
        ).thenReturn(CompletableFuture.completedFuture(websocket));

        var connection = cf.connect(mindgard, auth, settings, l -> {});

        assertEquals(connection, new SandboxConnection(cliInitResponse,websocket,client));

    }


    @Test
    void usesIncludeAndExcludeAttack() throws IOException, InterruptedException {
        var excludedAttack = "jail_breaking";
        var includedAttack = "PersonGPT";
        var http = mock(HttpClient.class, Mockito.RETURNS_DEEP_STUBS);
        var mindgard = mock(Mindgard.class);
        var websocket = mock(WebSocket.class);
        var auth = mock(MindgardAuthentication.class);
        var client = mock(MindgardWebsocketClient.class);
        HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.ofString("mock");
        var settings = new MindgardSettings("selector","projectID","dataset", "systemPrompt", null, excludedAttack, includedAttack, null, 1);

        var expectedRequest = new OrchestratorSetupRequest(
                settings.projectID(),
                1,
                settings.systemPrompt(),
                settings.dataset(),
                null,
                "llm",
                "user",
                "sandbox",
                null,
                List.of("jail_breaking"),
                List.of("PersonGPT"),
                null
                
        );
        
        Function<String, HttpRequest.BodyPublisher> matchHttpBody = body -> {
            assertEquals(body, JSON.json(expectedRequest));
            return publisher;
        };
        var response = mock(HttpResponse.class);

        when(http.send(argThat(req -> Objects.equals(publisher,req.bodyPublisher().get())), any())).thenReturn(response);

        var cf = new SandboxConnectionFactory(http, matchHttpBody, (a,b,c) -> client);

        CliInitResponse cliInitResponse = new CliInitResponse("groupId", "http://example.com/ws");
        when(response.body()).thenReturn(JSON.json(cliInitResponse));
        when(
                http
                        .newWebSocketBuilder()
                        .subprotocols("json.webpubsub.azure.v1")
                        .buildAsync(eq(URI.create("http://example.com/ws")), any(MindgardWebsocketClient.class))
        ).thenReturn(CompletableFuture.completedFuture(websocket));

        var connection = cf.connect(mindgard, auth, settings, l -> {});

        assertEquals(connection, new SandboxConnection(cliInitResponse,websocket,client));

    }

    @Test
    void usesExcludeMultipleAttacksCommaSeparatedWithWhitespaces() throws IOException, InterruptedException {

        var excludedAttack = "PersonGPT, AntiGPT,  DynamicTest";
        var http = mock(HttpClient.class, Mockito.RETURNS_DEEP_STUBS);
        var mindgard = mock(Mindgard.class);
        var websocket = mock(WebSocket.class);
        var auth = mock(MindgardAuthentication.class);
        var client = mock(MindgardWebsocketClient.class);
        HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.ofString("mock");
        var settings = new MindgardSettings("selector","projectID","dataset", "systemPrompt", null, excludedAttack, null, null, 1);

        var expectedRequest = new OrchestratorSetupRequest(
                settings.projectID(),
                1,
                settings.systemPrompt(),
                settings.dataset(),
                null,
                "llm",
                "user",
                "sandbox",
                null,
                Arrays.asList("PersonGPT","AntiGPT","DynamicTest"),
                null,
                null
        );

        Function<String, HttpRequest.BodyPublisher> matchHttpBody = body -> {
            assertEquals(body, JSON.json(expectedRequest));
            return publisher;
        };
        var response = mock(HttpResponse.class);

        when(http.send(argThat(req -> Objects.equals(publisher,req.bodyPublisher().get())), any())).thenReturn(response);

        var cf = new SandboxConnectionFactory(http, matchHttpBody, (a,b,c) -> client);

        CliInitResponse cliInitResponse = new CliInitResponse("groupId", "http://example.com/ws");
        when(response.body()).thenReturn(JSON.json(cliInitResponse));
        when(
                http
                        .newWebSocketBuilder()
                        .subprotocols("json.webpubsub.azure.v1")
                        .buildAsync(eq(URI.create("http://example.com/ws")), any(MindgardWebsocketClient.class))
        ).thenReturn(CompletableFuture.completedFuture(websocket));

        var connection = cf.connect(mindgard, auth, settings, l -> {});

        assertEquals(connection, new SandboxConnection(cliInitResponse,websocket,client));

    }

    @Test
    void usesPromptRepeatsNotSet() throws IOException, InterruptedException {

        var http = mock(HttpClient.class, Mockito.RETURNS_DEEP_STUBS);
        var mindgard = mock(Mindgard.class);
        var websocket = mock(WebSocket.class);
        var auth = mock(MindgardAuthentication.class);
        var client = mock(MindgardWebsocketClient.class);
        HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.ofString("mock");
        var settings = new MindgardSettings("selector","projectID","dataset", "systemPrompt", null, null, null, null, 1);

        var expectedRequest = new OrchestratorSetupRequest(
                settings.projectID(),
                1,
                settings.systemPrompt(),
                settings.dataset(),
                null,
                "llm",
                "user",
                "sandbox",
                null,
                null,
                null,
                null
        );

        Function<String, HttpRequest.BodyPublisher> matchHttpBody = body -> {
            assertEquals(body, JSON.json(expectedRequest));
            return publisher;
        };
        var response = mock(HttpResponse.class);

        when(http.send(argThat(req -> Objects.equals(publisher,req.bodyPublisher().get())), any())).thenReturn(response);

        var cf = new SandboxConnectionFactory(http, matchHttpBody, (a,b,c) -> client);

        CliInitResponse cliInitResponse = new CliInitResponse("groupId", "http://example.com/ws");
        when(response.body()).thenReturn(JSON.json(cliInitResponse));
        when(
                http
                        .newWebSocketBuilder()
                        .subprotocols("json.webpubsub.azure.v1")
                        .buildAsync(eq(URI.create("http://example.com/ws")), any(MindgardWebsocketClient.class))
        ).thenReturn(CompletableFuture.completedFuture(websocket));

        var connection = cf.connect(mindgard, auth, settings, l -> {});

        assertEquals(connection, new SandboxConnection(cliInitResponse,websocket,client));

    }

    @Test
    void usesPromptRepeatsSetToInteger() throws IOException, InterruptedException {

        var http = mock(HttpClient.class, Mockito.RETURNS_DEEP_STUBS);
        var mindgard = mock(Mindgard.class);
        var websocket = mock(WebSocket.class);
        var auth = mock(MindgardAuthentication.class);
        var client = mock(MindgardWebsocketClient.class);
        HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.ofString("mock");
        var settings = new MindgardSettings("selector","projectID","dataset", "systemPrompt", null, null, null, 3, 1);

        var expectedRequest = new OrchestratorSetupRequest(
                settings.projectID(),
                1,
                settings.systemPrompt(),
                settings.dataset(),
                null,
                "llm",
                "user",
                "sandbox",
                null,
                null,
                null,
                3
        );

        Function<String, HttpRequest.BodyPublisher> matchHttpBody = body -> {
            assertEquals(body, JSON.json(expectedRequest));
            return publisher;
        };
        var response = mock(HttpResponse.class);

        when(http.send(argThat(req -> Objects.equals(publisher,req.bodyPublisher().get())), any())).thenReturn(response);

        var cf = new SandboxConnectionFactory(http, matchHttpBody, (a,b,c) -> client);

        CliInitResponse cliInitResponse = new CliInitResponse("groupId", "http://example.com/ws");
        when(response.body()).thenReturn(JSON.json(cliInitResponse));
        when(
                http
                        .newWebSocketBuilder()
                        .subprotocols("json.webpubsub.azure.v1")
                        .buildAsync(eq(URI.create("http://example.com/ws")), any(MindgardWebsocketClient.class))
        ).thenReturn(CompletableFuture.completedFuture(websocket));

        var connection = cf.connect(mindgard, auth, settings, l -> {});

        assertEquals(connection, new SandboxConnection(cliInitResponse,websocket,client));

    }

    void usesParallelismSetToInteger() throws IOException, InterruptedException {

        var http = mock(HttpClient.class, Mockito.RETURNS_DEEP_STUBS);
        var mindgard = mock(Mindgard.class);
        var websocket = mock(WebSocket.class);
        var auth = mock(MindgardAuthentication.class);
        var client = mock(MindgardWebsocketClient.class);
        HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.ofString("mock");
        var settings = new MindgardSettings("selector","projectID","dataset", "systemPrompt", null, null, null, 3, 4);

        var expectedRequest = new OrchestratorSetupRequest(
                settings.projectID(),
                4,
                settings.systemPrompt(),
                settings.dataset(),
                null,
                "llm",
                "user",
                "sandbox",
                null,
                null,
                null,
                3
        );

        Function<String, HttpRequest.BodyPublisher> matchHttpBody = body -> {
            assertEquals(body, JSON.json(expectedRequest));
            return publisher;
        };
        var response = mock(HttpResponse.class);

        when(http.send(argThat(req -> Objects.equals(publisher,req.bodyPublisher().get())), any())).thenReturn(response);

        var cf = new SandboxConnectionFactory(http, matchHttpBody, (a,b,c) -> client);

        CliInitResponse cliInitResponse = new CliInitResponse("groupId", "http://example.com/ws");
        when(response.body()).thenReturn(JSON.json(cliInitResponse));
        when(
                http
                        .newWebSocketBuilder()
                        .subprotocols("json.webpubsub.azure.v1")
                        .buildAsync(eq(URI.create("http://example.com/ws")), any(MindgardWebsocketClient.class))
        ).thenReturn(CompletableFuture.completedFuture(websocket));

        var connection = cf.connect(mindgard, auth, settings, l -> {});

        assertEquals(connection, new SandboxConnection(cliInitResponse,websocket,client));

    }


}
