package ai.mindgard.burp;

import ai.mindgard.JSON;
import ai.mindgard.Log;
import ai.mindgard.MindgardSettings;
import ai.mindgard.sandbox.Mindgard;
import ai.mindgard.sandbox.Probe;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.requests.HttpRequest;
import com.jayway.jsonpath.JsonPath;

import java.util.Optional;
import java.util.stream.Collectors;

import static burp.api.montoya.http.handler.RequestToBeSentAction.continueWith;
import static burp.api.montoya.http.handler.ResponseReceivedAction.continueWith;

public class MindgardHttpHandler implements HttpHandler {
    private final Mindgard mindgard;
    private final Log logger;
    private final MindgardSettings settings;

    public MindgardHttpHandler(Mindgard mindgard, MindgardSettings settings, Log logger) {
        this.mindgard = mindgard;
        this.logger = logger;
        this.settings = settings;
    }

    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {
        String newBody = requestToBeSent.bodyToString();
        for (Probe probe : mindgard.pendingProbes()) {
            if (newBody.contains(probe.correlationId())) {
                return continueWith(
                    requestToBeSent.withBody(
                            newBody.replace(probe.correlationId(), JSON.escape(probe.prompt()))
                    ).withAddedHeader("X-Mindgard-ID", probe.correlationId()), requestToBeSent.annotations()
                );
            }
        }
        return continueWith(requestToBeSent);
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
        HttpRequest request = responseReceived.initiatingRequest();

        Optional<String> correlationId = Optional.ofNullable(request.headerValue("X-Mindgard-ID"));

        Optional<Probe> probe = correlationId.map(id -> mindgard.reply(id, extractReply(responseReceived)));

        return continueWith(
            responseReceived,
            responseReceived.annotations()
                .withNotes(probe.map(Probe::prompt).orElse(""))
        );
    }

    private String extractReply(HttpResponseReceived response) {
        if (response.hasHeader("Content-Type")) {
            if (response.header("Content-Type").value().startsWith("text/event-stream")) {
                return extractReplyFromTextEventStream(response.bodyToString());
            }
            if (response.header("Content-Type").value().equals("application/json")) {
                try {
                    return extractReplyFromJSON(response.bodyToString());
                } catch (Exception e) {
                    logger.log(e.getMessage());
                }
            }
        }


        return response.bodyToString();
    }

    private String extractReplyFromJSON(String body) {
        return JsonPath.read(body, settings.selector()).toString();
    }

    private String extractReplyFromTextEventStream(String body) {
        return body.lines()
                .filter(line -> line.contains("token"))
                .map(line -> extractReplyFromJSON(
                    line
                        .replaceAll("\\\\u0000","")
                )).collect(Collectors.joining(""));
    }
}
