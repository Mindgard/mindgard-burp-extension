package ai.mindgard;

import ai.mindgard.sandbox.Dataset;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class MindgardSettingsUI extends JPanel {
    private MindgardSettingsManager mgsm;
    Map<Component, Object> originalValues = new HashMap<>();
    List<JLabel> changedLabels = new ArrayList<>();
    private Log logger;

    public MindgardSettingsUI(MindgardSettingsManager mgsm, Log logger) {
        super();
        this.mgsm = mgsm;
        this.logger = logger;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTabbedPane tabs = new JTabbedPane();
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        LoginTab loginTab = new LoginTab(mgsm, this, logger); // passing 'this' avoids duplicating the setupUIChangeTracking
                                                      // method
        TestConfigTab tcTab = new TestConfigTab(mgsm, this);

        // Save Button
        JPanel saveButtonPanel = new JPanel();
        JButton saveButton = new JButton("Save All");
        saveButton.addActionListener((actionEvent) -> {
            saveUIContentsToSettings(
                    // Fetch contents of test config tab
                    tcTab.selectorField.getText(),
                    tcTab.projectIDField.getText(),
                    ((Dataset) tcTab.datasetField.getSelectedItem()).getDatasetName(),
                    tcTab.systemPromptField.getText(),
                    Optional.ofNullable(tcTab.customDataset).map(File::getAbsolutePath).orElse(null),
                    tcTab.excludeAttacksField.getText(),
                    tcTab.includeAttacksField.getText(),
                    Integer.parseInt(tcTab.promptRepeatsField.getText()),
                    Integer.parseInt(tcTab.parallelismField.getText()),
                    // Fetch contents of login tab
                    loginTab.urlField.getText(),
                    loginTab.audienceField.getText(),
                    loginTab.clientIDField.getText());
            loginTab.updateLoginStatus(this.mgsm);
        });

        saveButtonPanel.add(saveButton);

        // Position everything and build tabs.
        JPanel loginWrapper = new JPanel(new BorderLayout());
        loginWrapper.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        loginWrapper.add(loginTab.loginPanel, BorderLayout.NORTH);
        loginWrapper.add(loginTab.loginButtonPanel);
        JPanel testConfigWrapper = new JPanel(new BorderLayout());
        testConfigWrapper.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        testConfigWrapper.add(tcTab.testConfigPanel, BorderLayout.NORTH);
        tabs.addTab("Login", loginWrapper);
        tabs.addTab("Test Configuration", testConfigWrapper);
        add(tabs, BorderLayout.CENTER);
        add(saveButtonPanel, BorderLayout.SOUTH); // Ensure save button is at the bottom

        setPreferredSize(new Dimension(300, 120));
    }

    /**
     * Wraps and saves the contents of the UI components.
     * This method saves the current contents of each UI component in the extension
     * to the mindgard settings file.
     * 
     * @param selector          contents of the selector field
     * @param projectID         contents of the projectID field
     * @param dataset           chosen dataset domain
     * @param systemPrompt      contents of the system prompt field
     * @param customDatasetPath the path to the custom dataset on disk
     * @param exclude           the attacks to exclude from the test
     * @param incude            the attacks to include in the test
     * @param promptRepeats     the number of times prompts are repeated in each
     *                          attack
     * @param parallelism       the maximum amount of concurrent requests on the
     *                          target
     */
    private void saveUIContentsToSettings(
            String selector,
            String projectID,
            String dataset,
            String systemPrompt,
            String customDatasetPath,
            String exclude,
            String incude,
            Integer promptRepeats,
            Integer parallelism,
            String urlString,
            String audienceString,
            String clientIDString) {
        MindgardSettings newSettings = new MindgardSettings(
                urlString,
                audienceString,
                clientIDString,
                selector,
                projectID,
                dataset,
                systemPrompt,
                customDatasetPath,
                exclude,
                incude,
                promptRepeats,
                parallelism);
        mgsm.setSettings(newSettings);

        JOptionPane.showMessageDialog(
                this,
                "Mindgard settings updated successfully!" + "\n" +
                        "Tests run using this configuration can be found at " + urlString + "/results?project_id="
                        + projectID,
                "Mindgard Extension",
                JOptionPane.INFORMATION_MESSAGE);

        changedLabels.forEach(label -> {
            label.setForeground(Color.BLACK);
        });
        changedLabels.clear();
    }

    // Returns true if there are unsaved changes in the UI
    public boolean hasUnsavedChanges() {
        return !changedLabels.isEmpty();
    }

    /**
     * Sets up change tracking for a given UI field and its associated label.
     * 
     * This method attaches listeners to the provided field (which can be a
     * {@link JTextField},
     * {@link JComboBox}, {@link JTextArea}, or {@link JLabel}) to monitor for
     * changes in its value.
     * When a change is detected compared to the original value, the label's
     * foreground color is updated
     * to indicate that the field has been modified. If the field is reverted to its
     * original value,
     * the label color is reset.
     * 
     *
     * @param field the UI component to monitor for changes (must be a
     *              {@link JTextField}, {@link JComboBox},
     *              {@link JTextArea}, or {@link JLabel})
     * @param label the label associated with the field, whose color will be updated
     *              to reflect changes
     */
    public void setupUIChangeTracking(JComponent field, JLabel label) {
        if (field instanceof JTextField textField) {
            originalValues.put(textField, textField.getText());
            textField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    checkChanged();
                }

                public void removeUpdate(DocumentEvent e) {
                    checkChanged();
                }

                public void changedUpdate(DocumentEvent e) {
                    checkChanged();
                }

                private void checkChanged() {
                    String original = (String) originalValues.get(textField);
                    if (!textField.getText().equals(original)) {
                        label.setForeground(Color.decode("#CC5500"));
                        changedLabels.add(label);
                    } else {
                        label.setForeground(Color.BLACK);
                    }
                }
            });
        } else if (field instanceof JComboBox comboBox) {
            originalValues.put(comboBox, comboBox.getSelectedItem());
            comboBox.addActionListener(e -> {
                Object original = originalValues.get(comboBox);
                if (!Objects.equals(comboBox.getSelectedItem(), original)) {
                    label.setForeground(Color.decode("#CC5500"));
                    changedLabels.add(label);
                } else {
                    label.setForeground(Color.BLACK);
                }
            });
        } else if (field instanceof JTextArea textArea) {
            originalValues.put(textArea, textArea.getText());
            textArea.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    checkChanged();
                }

                public void removeUpdate(DocumentEvent e) {
                    checkChanged();
                }

                public void changedUpdate(DocumentEvent e) {
                    checkChanged();
                }

                private void checkChanged() {
                    String original = (String) originalValues.get(textArea);
                    if (!textArea.getText().equals(original)) {
                        label.setForeground(Color.decode("#CC5500"));
                        changedLabels.add(label);
                    } else {
                        label.setForeground(Color.BLACK);
                    }
                }
            });
        } else if (field instanceof JLabel labelField) {
            originalValues.put(labelField, labelField.getText());
            labelField.addPropertyChangeListener("text", e -> {
                String original = (String) originalValues.get(labelField);
                if (!labelField.getText().equals(original)) {
                    label.setForeground(Color.decode("#CC5500"));
                    changedLabels.add(label);
                } else {
                    label.setForeground(Color.BLACK);
                }
            });
        }
    }

}
