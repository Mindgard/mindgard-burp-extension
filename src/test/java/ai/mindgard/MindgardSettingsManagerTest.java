package ai.mindgard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MindgardSettingsManagerTest {
    MindgardSettings settings = mock(MindgardSettings.class);
    MindgardToken token = mock(MindgardToken.class);

    String sandboxUrl = "https://sandbox.mindgard.ai";
    String otherUrl = "https://example.us.mindgard.ai";

    @BeforeEach
    void setup() {
        when(settings.save(anyString())).thenReturn(true);
        when(settings.url()).thenReturn(sandboxUrl);
        when(token.token()).thenReturn("my-cool-token");
    }

    @DisplayName("validLogin returns true when the login is for the currently set URL and a token is present")
    @Test
    void validLogin_returnsTrue_whenValid() throws IOException, InterruptedException {
        when(token.url()).thenReturn(sandboxUrl);

        try (
            MockedStatic<MindgardSettings> settingsStatic = mockStatic(MindgardSettings.class);
            MockedStatic<MindgardToken> tokenStatic = mockStatic(MindgardToken.class);
        ) {
            settingsStatic.when(MindgardSettings::loadOrCreate).thenReturn(settings);
            tokenStatic.when(MindgardToken::loadOrCreate).thenReturn(token);

            MindgardSettingsManager manager = new MindgardSettingsManager();

            assertTrue(manager.validLogin());
        }
    }

    @DisplayName("validLogin returns false when the login is not for the currently set URL")
    @Test
    void validLogin_returnsFalse_whenUrlsDoNotMatch() throws IOException, InterruptedException {
        when(token.url()).thenReturn(otherUrl);

        try (
            MockedStatic<MindgardSettings> settingsStatic = mockStatic(MindgardSettings.class);
            MockedStatic<MindgardToken> tokenStatic = mockStatic(MindgardToken.class);
        ) {
            settingsStatic.when(MindgardSettings::loadOrCreate).thenReturn(settings);
            tokenStatic.when(MindgardToken::loadOrCreate).thenReturn(token);

            MindgardSettingsManager manager = new MindgardSettingsManager();

            assertFalse(manager.validLogin());
        }
    }

    @DisplayName("validLogin returns false when there is no token")
    @Test
    void validLogin_returnsFalse_whenThereIsNoToken() throws IOException, InterruptedException {
        when(token.token()).thenReturn("");

        try (
            MockedStatic<MindgardSettings> settingsStatic = mockStatic(MindgardSettings.class);
            MockedStatic<MindgardToken> tokenStatic = mockStatic(MindgardToken.class);
        ) {
            settingsStatic.when(MindgardSettings::loadOrCreate).thenReturn(settings);
            tokenStatic.when(MindgardToken::loadOrCreate).thenReturn(token);

            MindgardSettingsManager manager = new MindgardSettingsManager();

            assertFalse(manager.validLogin());
        }
    }
}
