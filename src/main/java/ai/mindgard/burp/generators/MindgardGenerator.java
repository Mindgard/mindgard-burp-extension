package ai.mindgard.burp.generators;

import ai.mindgard.Log;
import ai.mindgard.sandbox.Mindgard;
import burp.api.montoya.intruder.GeneratedPayload;
import burp.api.montoya.intruder.IntruderInsertionPoint;
import burp.api.montoya.intruder.PayloadGenerator;

public class MindgardGenerator implements PayloadGenerator {
    private final Mindgard mindgard;
    private final Log logger;

    public MindgardGenerator(Mindgard mindgard, Log logger) {
        logger.log("Loading Generator");
        mindgard.reset();
        this.mindgard = mindgard;
        this.logger = logger;
    }

    @Override
    public GeneratedPayload generatePayloadFor(IntruderInsertionPoint intruderInsertionPoint) {
        if (!mindgard.isStarted()) {
            mindgard.startGeneratingProbes();
        }

        return mindgard.poll().map(taken -> {
            if (taken.correlationId() == null) {
                return end();
            }
            return GeneratedPayload.payload(taken.correlationId());
        }).orElseGet(this::end);
    }

    private GeneratedPayload end() {
        logger.log("END");
        mindgard.reset();
        return GeneratedPayload.end();
    }

}
