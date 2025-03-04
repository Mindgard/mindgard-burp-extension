package ai.mindgard.sandbox.wsapi;

import ai.mindgard.Log;
import ai.mindgard.sandbox.Mindgard;
import ai.mindgard.sandbox.Probe;
import ai.mindgard.sandbox.wsapi.messages.*;

import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

import static ai.mindgard.JSON.fromJson;
import static ai.mindgard.JSON.json;

public class MindgardWebsocketClient implements WebSocket.Listener {

    private CliInitResponse cliInitVals;
    private final Mindgard mindgard;
    private final Log logger;

    public interface Factory{
        MindgardWebsocketClient create(CliInitResponse cliInitVals, Mindgard mindgard, Log logger);
    }

    public MindgardWebsocketClient(CliInitResponse cliInitVals, Mindgard mindgard, Log logger) {
        this.cliInitVals = cliInitVals;
        this.mindgard = mindgard;
        this.logger = logger;
    }

    @Override
    public void onOpen(WebSocket ws) {
        logger.log("onOpen using subprotocol " + ws.getSubprotocol());

        String joinGroup = json(new JoinGroup(cliInitVals.groupId(), "joinGroup"));
        logger.log(joinGroup);

        ws.sendText(joinGroup, true)
                .thenRun(() -> logger.log("Sent joinGroup request"));
        String sendtogroup = startTestMessage();
        logger.log(sendtogroup);
        ws.sendText(sendtogroup, true);
        WebSocket.Listener.super.onOpen(ws);
    }

    String startTestMessage() {
        var payload = new WSPayload("","StartTest",new GroupId(cliInitVals.groupId()));

        return json(new SendToGroup("sendToGroup", "orchestrator", payload, "json"));
    }

    private final StringBuilder partial = new StringBuilder();
    @Override
    public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {

        logger.log("onText received " + last + " " + data);
        partial.append(data);
        if (last) {
            logger.log("COMPLETE");

            logger.log(partial.toString());

            var req = fromJson(partial.toString(), Message.class);

            logger.log(req.toString());

            if (Objects.nonNull(req.data()) && Objects.equals("Request", req.data().messageType())) {
                logger.log(req.data().payload().prompt());
                logger.log(req.toString());
                if (req.data().correlationId() != null) {
                    mindgard.push(new Probe(req.data().correlationId(), req.data().payload().prompt()));
                } else {
                    logger.log("MISSING CORRELATION ID FOR " + req.data());
                }
            }


            partial.setLength(0);
        }
        return WebSocket.Listener.super.onText(ws, data, last);
    }

    public void reply(WebSocket ws, String correlationId, String response) {
        var reply = new ReplyData(correlationId, "Response", "ok",
                new ResponsePayload(response, null));
        logger.log(json(reply));


        ws.sendText(json(new SendResponseToGroup("sendToGroup", "orchestrator", reply, "json")), true);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        logger.log("onError " + webSocket.toString());
        error.printStackTrace();
        WebSocket.Listener.super.onError(webSocket, error);
    }

    @Override
    public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
        logger.log("ping");
        return WebSocket.Listener.super.onPing(webSocket, message);
    }

    @Override
    public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
        logger.log("pong");
        return WebSocket.Listener.super.onPong(webSocket, message);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        logger.log("onCLose " + statusCode + " " + reason);
        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }

}
