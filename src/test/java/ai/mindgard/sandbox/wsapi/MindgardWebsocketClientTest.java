package ai.mindgard.sandbox.wsapi;

import ai.mindgard.JSON;
import ai.mindgard.sandbox.Mindgard;
import ai.mindgard.sandbox.Probe;
import ai.mindgard.sandbox.wsapi.messages.*;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;

import static ai.mindgard.JSON.json;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MindgardWebsocketClientTest {

    @Test
    void joinsOrchestratorGroupOnOpen() {
        var mg = mock(Mindgard.class);
        var ws = mock(WebSocket.class);
        CliInitResponse cliInit = new CliInitResponse("groupId", "url");
        var mgws = new MindgardWebsocketClient(cliInit, mg, l -> {});

        when(ws.sendText(any(), anyBoolean()))
            .thenReturn(CompletableFuture.completedFuture(ws));

        mgws.onOpen(ws);

        var ordered = inOrder(ws);
        ordered.verify(ws).sendText(json(new JoinGroup(cliInit.groupId(),"joinGroup")), true);
        ordered.verify(ws).sendText(mgws.startTestMessage(), true);
    }

    @Test
    void pushesResultsToMindgardClientOnTextCombiningFrames() {
        var mg = mock(Mindgard.class);
        var ws = mock(WebSocket.class);
        CliInitResponse cliInit = new CliInitResponse("groupId", "url");
        var mgws = new MindgardWebsocketClient(cliInit, mg, l -> {});

        var messages = json(new Message("Request","event","userId","connectionId","from","fromUserId","group","dataType",
                new MessageData("correlation-id-1","Request",new Payload("testId","Prompt is Hel|lo World","context_id","system_prompt"))
        )).split("\\|");

        var messages2 = json(new Message("Request","event","userId","connectionId","from","fromUserId","group","dataType",
                new MessageData("correlation-id-2","Request",new Payload("testId","Second Prompt is Gree|tings","context_id","system_prompt"))
        )).split("\\|");


        mgws.onText(ws,  messages[0], false);
        mgws.onText(ws,  messages[1], true);
        mgws.onText(ws,  messages2[0], false);
        mgws.onText(ws,  messages2[1], true);

        verify(mg).push(new Probe("correlation-id-1","Prompt is Hello World"));
        verify(mg).push(new Probe("correlation-id-2","Second Prompt is Greetings"));
    }

    void ignoresIrrelevantMessages() {
        var mg = mock(Mindgard.class);
        var ws = mock(WebSocket.class);
        CliInitResponse cliInit = new CliInitResponse("groupId", "url");
        var mgws = new MindgardWebsocketClient(cliInit, mg, l -> {});

        var messages = json(new Message("NotARequest","event","userId","connectionId","from","fromUserId","group","dataType",
                new MessageData("correlation-id-1","Request",new Payload("testId","Prompt is Hel|lo World","context_id","system_prompt"))
        )).split("\\|");

        var messages2 = json(new Message("Request","event","userId","connectionId","from","fromUserId","group","dataType",
                new MessageData(null,"Request",new Payload("testId","Second Prompt is Gree|tings","context_id","system_prompt"))
        )).split("\\|");


        mgws.onText(ws,  messages[0], false);
        mgws.onText(ws,  messages[1], true);
        mgws.onText(ws,  messages2[0], false);
        mgws.onText(ws,  messages2[1], true);

        verify(mg, never()).push(any());
    }
}
