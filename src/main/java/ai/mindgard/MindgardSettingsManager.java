package ai.mindgard;
/**
 * Mindgard Settings Manager
 * Used to synchronise both the MindgardSettingsUI and MindgardSettings views of the state of the settings.
 */
public class MindgardSettingsManager {
    /**
     * Saves new settings contents to the settings file.
     * @param newSettings the new settings
    */
   public static void setSettings(MindgardSettings newSettings) {newSettings.save(Constants.SETTINGS_FILE_NAME);}
   
   /**
    * Gets the settings from disk
    * @return the populated MindgardSettings record
    */
   public static MindgardSettings getSettings() {return MindgardSettings.loadOrCreate(Constants.SETTINGS_FILE_NAME);}
}
