package ai.mindgard;

public class MindgardSettingsManager {
    private static MindgardSettings currentSettings = MindgardSettings.loadOrCreate(Constants.SETTINGS_FILE_NAME);

    public static MindgardSettings getSettings() {
        return currentSettings;
    }

    public static void updateSettings(MindgardSettings newSettings) {
        currentSettings = newSettings;
        newSettings.save(Constants.SETTINGS_FILE_NAME);
    }

    public static void reloadSettings() {
        currentSettings = MindgardSettings.loadOrCreate(Constants.SETTINGS_FILE_NAME);
    }
}
