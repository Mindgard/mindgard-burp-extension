package ai.mindgard.burp;

import ai.mindgard.MindgardSettings;
import ai.mindgard.MindgardSettingsManager;
import ai.mindgard.sandbox.Mindgard;
import ai.mindgard.sandbox.Probe;
import burp.api.montoya.core.Annotations;
import burp.api.montoya.http.handler.HttpRequestToBeSent;
import burp.api.montoya.http.handler.HttpResponseReceived;
import burp.api.montoya.http.handler.RequestToBeSentAction;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.internal.MontoyaObjectFactory;
import burp.api.montoya.internal.ObjectFactoryLocator;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MindgardHttpHandlerTest {

    @Test
    void handleHttpRequestToBeSentNoPendingProbes() {
        var burp = mock(MontoyaObjectFactory.class);
        var req = mock(RequestToBeSentAction.class);
        ObjectFactoryLocator.FACTORY = burp;
        var mindgard = mock(Mindgard.class);
        var mgsm = mock(MindgardSettingsManager.class);
        when(mindgard.pendingProbes()).thenReturn(Collections.emptyList());
        var originalRequest = mock(HttpRequestToBeSent.class);
        when(burp.requestResult(originalRequest)).thenReturn(req);

        var handler = new MindgardHttpHandler(mindgard, mgsm, l -> {});

        assertEquals(req, handler.handleHttpRequestToBeSent(originalRequest));
    }

    @Test
    void handleHttpRequestToBeSentNoMatchingPendingProbes() {
        var burp = mock(MontoyaObjectFactory.class);
        var req = mock(RequestToBeSentAction.class);
        ObjectFactoryLocator.FACTORY = burp;
        var mindgard = mock(Mindgard.class);
        var mgsm = mock(MindgardSettingsManager.class);
        when(mindgard.pendingProbes()).thenReturn(List.of(new Probe("does-not-match","a-prompt")));
        var originalRequest = mock(HttpRequestToBeSent.class);
        when(originalRequest.bodyToString()).thenReturn("request-body");
        when(burp.requestResult(originalRequest)).thenReturn(req);

        var handler = new MindgardHttpHandler(mindgard, mgsm, l -> {});

        assertEquals(req, handler.handleHttpRequestToBeSent(originalRequest));
    }

    @Test
    void handleHttpRequestToBeSentMatchingPendingProbes() {
        var burp = mock(MontoyaObjectFactory.class);
        var req = mock(RequestToBeSentAction.class);
        ObjectFactoryLocator.FACTORY = burp;
        var mindgard = mock(Mindgard.class);
        var mgsm = mock(MindgardSettingsManager.class);
        var replacedRequest= mock(HttpRequest.class);
        when(mindgard.pendingProbes()).thenReturn(List.of(new Probe("guid-to-replace","a-prompt")));
        var originalRequest = mock(HttpRequestToBeSent.class);
        when(originalRequest.bodyToString()).thenReturn("request-body guid-to-replace body-end");
        when(originalRequest.withBody("request-body a-prompt body-end")).thenReturn(replacedRequest);
        when(replacedRequest.withAddedHeader(any(), any())).thenReturn(replacedRequest);
        when(burp.requestResult(eq(replacedRequest), any())).thenReturn(req);

        var handler = new MindgardHttpHandler(mindgard, mgsm, l -> {});

        assertEquals(req, handler.handleHttpRequestToBeSent(originalRequest));
        verify(replacedRequest).withAddedHeader("X-Mindgard-ID", "guid-to-replace");
    }

    @Test
    void handleHttpRequestToBeSentMatchingPendingProbesEscapesChars() {
        var burp = mock(MontoyaObjectFactory.class);
        ObjectFactoryLocator.FACTORY = burp;
        var req = mock(RequestToBeSentAction.class);
        var mindgard = mock(Mindgard.class);
        var mgsm = mock(MindgardSettingsManager.class);
        var replacedRequest= mock(HttpRequest.class);
        when(mindgard.pendingProbes()).thenReturn(List.of(new Probe("guid-to-replace","a-pr\"ompt")));
        var originalRequest = mock(HttpRequestToBeSent.class);
        when(originalRequest.bodyToString()).thenReturn("request-body guid-to-replace body-end");
        when(originalRequest.withBody("request-body a-pr\\\\\\\"ompt body-end")).thenReturn(replacedRequest);
        when(replacedRequest.withAddedHeader(any(), any())).thenReturn(replacedRequest);
        when(burp.requestResult(eq(replacedRequest), any())).thenReturn(req);

        var handler = new MindgardHttpHandler(mindgard, mgsm, l -> {});

        assertEquals(req, handler.handleHttpRequestToBeSent(originalRequest));
        verify(replacedRequest).withAddedHeader("X-Mindgard-ID", "guid-to-replace");
    }

    @Test
    void sendsResponseToMindgard() {
        var burp = mock(MontoyaObjectFactory.class);
        ObjectFactoryLocator.FACTORY = burp;
        var mindgard = mock(Mindgard.class);
        var mgsm = mock(MindgardSettingsManager.class);
        var settings = mock(MindgardSettings.class);
        when(settings.selector()).thenReturn("$");
        when(mgsm.getSettings()).thenReturn(settings);

        var handler = new MindgardHttpHandler(mindgard, mgsm, l -> {});
        var originalResponse = mock(HttpResponseReceived.class);
        var originalRequest = mock(HttpRequest.class);
        var annotations = mock(Annotations.class);
        when(originalResponse.annotations()).thenReturn(annotations);

        when(originalResponse.initiatingRequest()).thenReturn(originalRequest);

        String correlationId = "1234";
        String responseBody = "{\"hello\":\"world\"}";

        when(originalRequest.headerValue("X-Mindgard-ID")).thenReturn(correlationId);
        when(originalResponse.bodyToString()).thenReturn(responseBody);

        handler.handleHttpResponseReceived(originalResponse);

        verify(mindgard).reply(correlationId, responseBody);
    }

    @Test
    void sendsResponseToMindgard_ExtractsJsonPath() {
        var burp = mock(MontoyaObjectFactory.class);
        ObjectFactoryLocator.FACTORY = burp;
        var mindgard = mock(Mindgard.class);
        var mgsm = mock(MindgardSettingsManager.class);
        var settings = mock(MindgardSettings.class);
        when(settings.selector()).thenReturn("$.hello");
        when(mgsm.getSettings()).thenReturn(settings);

        var handler = new MindgardHttpHandler(mindgard, mgsm, l -> {});
        var originalResponse = mock(HttpResponseReceived.class);
        var originalRequest = mock(HttpRequest.class);
        var annotations = mock(Annotations.class);
        when(originalResponse.annotations()).thenReturn(annotations);

        when(originalResponse.initiatingRequest()).thenReturn(originalRequest);

        String correlationId = "1234";
        String responseBody = "{\"hello\":\"world\"}";

        when(originalRequest.headerValue("X-Mindgard-ID")).thenReturn(correlationId);
        when(originalResponse.hasHeader("Content-Type")).thenReturn(true);
        when(originalResponse.header("Content-Type")).thenReturn(header("Content-Type","application/json"));
        when(originalResponse.bodyToString()).thenReturn(responseBody);

        handler.handleHttpResponseReceived(originalResponse);

        verify(mindgard).reply(correlationId, "world");
    }

    private static HttpHeader header(String name, String value) {
        return new HttpHeader() {
            public String name() {
                return name;
            }

            public String value() {
                return value;
            }
        };
    }
}