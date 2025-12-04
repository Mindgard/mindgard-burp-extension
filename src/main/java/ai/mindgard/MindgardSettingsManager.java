package ai.mindgard;

import ai.mindgard.sandbox.wsapi.SandboxConnectionFactory;

public class MindgardSettingsManager {
    private MindgardSettings mindgardSettings;

    /**
     * When created, loads the current settings file.
     */
    public MindgardSettingsManager() {
        this.mindgardSettings = MindgardSettings.loadOrCreate(Constants.SETTINGS_FILE_NAME);
    }

    /**
     * Saves new settings contents to the settings file.
     * Writes first, as validation is part of writing before local state is updated
     * @param newSettings the new settings
    */
    public void setSettings(MindgardSettings newSettings) {
        validateSettings(newSettings);
        newSettings.save(Constants.SETTINGS_FILE_NAME);
        this.mindgardSettings = newSettings;
    }
   
   /**
    * Gets the settings from disk
    * @return the populated MindgardSettings record
    */
    public MindgardSettings getSettings() {return this.mindgardSettings;}


    /**
     * Validates settings before saving
     * Throws error message if project ID doesn't exist in the user's tenancy.
     * @param newSettings the new settings to be validated
     */
    private void validateSettings (MindgardSettings newSettings) {
        // Validate project ID before saving
        Exception validationException = null;
        try {
            SandboxConnectionFactory validator = new SandboxConnectionFactory();
            validator.validateProject(newSettings.projectID(), newSettings.getAPIUrl(), this);
        } catch (Exception e) {
            String message = (validationException == null)
                ? "Project ID is invalid. Please go to " + newSettings.url() + "/results to create a new project or find the ID of an existing project."
                : "There was a problem validating the project ID: " + e.getMessage();
            javax.swing.SwingUtilities.invokeLater(() -> {
                javax.swing.JOptionPane.showMessageDialog(null,
                    message,
                    "Mindgard Settings Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE
                );}
        );}
    }
}