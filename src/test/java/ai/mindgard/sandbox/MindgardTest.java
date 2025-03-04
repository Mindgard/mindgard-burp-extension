package ai.mindgard.sandbox;

import ai.mindgard.Log;
import ai.mindgard.MindgardAuthentication;
import ai.mindgard.MindgardSettings;
import ai.mindgard.sandbox.wsapi.SandboxConnection;
import ai.mindgard.sandbox.wsapi.SandboxConnectionFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MindgardTest {

    @Test
    void generatedProbesFromSandboxAreEnqueuedUntilSentToTargetAppAndThenRemainPendingUntilReplySentToSandbox() {
        var auth = mock(MindgardAuthentication.class);
        var settings = mock(MindgardSettings.class);
        var connectionFactory = mock(SandboxConnectionFactory.class);
        var connection = mock(SandboxConnection.class);
        Log log = l -> {
        };
        var mg = new Mindgard(log, auth, settings, () -> connectionFactory);
        when(connectionFactory.connect(mg, auth, settings, log)).thenReturn(connection);

        // Ask sandbox to generate probes
        assertFalse(mg.isStarted());
        assertTrue(mg.hasMoreProbes());

        mg.startGeneratingProbes();

        assertTrue(mg.isStarted());
        assertTrue(mg.hasMoreProbes());
        assertTrue(mg.pendingProbes().isEmpty());

        // Simulate a new probe coming in from Sandbox
        Probe expected = new Probe("1234", "Prompt1");

        mg.push(expected);

        assertTrue(mg.pendingProbes().isEmpty());
        assertTrue(mg.isStarted());
        assertTrue(mg.hasMoreProbes());

        // Probe becomes available to send to the target application

        Probe actual = mg.poll(1L).get();

        assertEquals(expected, actual);
        assertEquals(List.of(actual), mg.pendingProbes());
        assertTrue(mg.isStarted());
        assertFalse(mg.hasMoreProbes());

        // When reply comes in from the target application the probe is no longer retained

        verifyNoInteractions(connection);
        mg.reply(expected.correlationId(), "Reply1");
        verify(connection).reply(expected.correlationId(), "Reply1");
        assertTrue(mg.pendingProbes().isEmpty());
    }

    @Test
    void resetRestartsTest() {
        var auth = mock(MindgardAuthentication.class);
        var settings = mock(MindgardSettings.class);
        var connectionFactory = mock(SandboxConnectionFactory.class);
        var connection = mock(SandboxConnection.class);
        Log log = l -> {
        };
        var mg = new Mindgard(log, auth, settings, () -> connectionFactory);
        when(connectionFactory.connect(mg, auth, settings, log)).thenReturn(connection);

        assertFalse(mg.isStarted());
        assertTrue(mg.hasMoreProbes());

        mg.push(new Probe("123", "Prompt"));
        mg.startGeneratingProbes();

        assertTrue(mg.isStarted());
        assertTrue(mg.hasMoreProbes());
        assertTrue(mg.pendingProbes().isEmpty());

        mg.reset();

        assertFalse(mg.isStarted());
        assertTrue(mg.pendingProbes().isEmpty());
    }

}