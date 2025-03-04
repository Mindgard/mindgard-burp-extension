package ai.mindgard.burp.generators;

import ai.mindgard.sandbox.Mindgard;
import ai.mindgard.sandbox.Probe;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.internal.MontoyaObjectFactory;
import burp.api.montoya.internal.ObjectFactoryLocator;
import burp.api.montoya.intruder.GeneratedPayload;
import burp.api.montoya.intruder.IntruderInsertionPoint;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MindgardGeneratorTest {

    @Test
    void generatePayloadWhenFinished() {
        var mindgard = mock(Mindgard.class);
        var burp = mock(MontoyaObjectFactory.class);
        var end = mock(GeneratedPayload.class);
        ObjectFactoryLocator.FACTORY = burp;
        when(burp.payloadEnd()).thenReturn(end);

        when(mindgard.isStarted()).thenReturn(true);
        when(mindgard.poll()).thenReturn(Optional.empty());

        var generator = new MindgardGenerator(mindgard, l -> {});
        GeneratedPayload result = generator.generatePayloadFor(() -> ByteArray.byteArrayOfLength(0));

        assertEquals(end, result);
        verify(mindgard, never()).startGeneratingProbes();
    }

    @Test
    void generatePayloadStartsTest() {
        var mindgard = mock(Mindgard.class);
        var burp = mock(MontoyaObjectFactory.class);
        var end = mock(GeneratedPayload.class);
        ObjectFactoryLocator.FACTORY = burp;
        when(burp.payloadEnd()).thenReturn(end);

        when(mindgard.isStarted()).thenReturn(false);
        when(mindgard.poll()).thenReturn(Optional.empty());

        var generator = new MindgardGenerator(mindgard, l -> {});
        GeneratedPayload result = generator.generatePayloadFor(() -> ByteArray.byteArrayOfLength(0));

        assertEquals(end, result);
        verify(mindgard).startGeneratingProbes();
    }

    @Test
    void generatePayloadEndsTestWhenMissingCorrelationId() {
        var mindgard = mock(Mindgard.class);
        var burp = mock(MontoyaObjectFactory.class);
        var end = mock(GeneratedPayload.class);
        ObjectFactoryLocator.FACTORY = burp;
        when(burp.payloadEnd()).thenReturn(end);

        when(mindgard.isStarted()).thenReturn(true);
        when(mindgard.poll()).thenReturn(Optional.of(new Probe(null, "some prompt")));

        var generator = new MindgardGenerator(mindgard, l -> {});
        GeneratedPayload result = generator.generatePayloadFor(() -> ByteArray.byteArrayOfLength(0));

        assertEquals(end, result);

    }

    @Test
    void generatePayloadReturnsCorrelationFromSandbox() {
        var mindgard = mock(Mindgard.class);
        var burp = mock(MontoyaObjectFactory.class);
        var payload = mock(GeneratedPayload.class);
        ObjectFactoryLocator.FACTORY = burp;
        when(burp.payload("correlation-id")).thenReturn(payload);

        when(mindgard.isStarted()).thenReturn(true);
        when(mindgard.poll()).thenReturn(Optional.of(new Probe("correlation-id", "some prompt")));

        var generator = new MindgardGenerator(mindgard, l -> {});
        GeneratedPayload result = generator.generatePayloadFor(() -> ByteArray.byteArrayOfLength(0));

        assertEquals(payload, result);

    }

}