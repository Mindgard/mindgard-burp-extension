package ai.mindgard;

import java.awt.*;
import java.io.File;
import java.text.NumberFormat;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import ai.mindgard.sandbox.Dataset;

public class TestConfigTab extends JPanel {
    public File customDataset;
    private final JFileChooser customDatasetField = new JFileChooser();
    
    public int testConfigRow = 0;
    public JPanel testConfigPanel;
    public JTextField selectorField;
    public JTextField projectIDField;
    public JComboBox<Dataset> datasetField;
    public JTextArea systemPromptField;
    public JTextField excludeAttacksField;
    public JTextField includeAttacksField;
    public JFormattedTextField promptRepeatsField;
    public JFormattedTextField parallelismField;
    
    public TestConfigTab(MindgardSettingsManager mgsm, MindgardSettingsUI ui) {
        testConfigPanel = new JPanel(new GridBagLayout());

        var settings = mgsm.getSettings();
    
        // Initialise GridBagConstraints before placing any components down.
        GridBagConstraints testconfigGBC = new GridBagConstraints();
        testconfigGBC.gridx    = 0;
        testconfigGBC.gridy    = 0;
        testconfigGBC.weightx  = 1.0;
        testconfigGBC.weighty  = 0.0;
        testconfigGBC.insets   = new Insets(0, 0, 10, 5);
        testconfigGBC.fill     = GridBagConstraints.HORIZONTAL;
        testconfigGBC.anchor   = GridBagConstraints.NORTHWEST;

        // Selector
        selectorField = new JTextField(settings.selector(), 20);
        JLabel selectorLabel = addTestConfigRow(testConfigPanel, testconfigGBC, "Selector: ", selectorField);
        ui.setupUIChangeTracking(selectorField, selectorLabel);

        // Project ID
        projectIDField = new JTextField(settings.projectID(), 20);
        JLabel projectIDLabel = addTestConfigRow(testConfigPanel, testconfigGBC, "Project ID: ", projectIDField);
        ui.setupUIChangeTracking(projectIDField, projectIDLabel);

        // Domain
        datasetField = new JComboBox<>(Dataset.values());
        datasetField.setSelectedIndex(Dataset.indexOfName(settings.dataset()));
        JLabel datasetLabel = addTestConfigRow(testConfigPanel, testconfigGBC, "Domain:", datasetField);
        ui.setupUIChangeTracking(datasetField, datasetLabel);

        // System Prompt
        systemPromptField = new JTextArea(settings.systemPrompt(), 5, 20);
        JScrollPane systemPromptScroll = new JScrollPane(systemPromptField);
        JLabel systemPromptLabel = addTestConfigRow(testConfigPanel, testconfigGBC, "System Prompt:", systemPromptScroll);
        ui.setupUIChangeTracking(systemPromptField, systemPromptLabel);

        //Custom Dataset
        var customDatasetPathLabel = new JLabel("No file selected");
        Optional.ofNullable(customDataset)
                .map(Object::toString)
                .ifPresent(customDatasetPathLabel::setText);
        customDatasetPathLabel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.GRAY),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5))
        );
        JLabel customDatasetLabel = addTestConfigRow(testConfigPanel, testconfigGBC,"Custom Dataset:", customDatasetPathLabel);
        ui.setupUIChangeTracking(customDatasetPathLabel, customDatasetLabel);

        // Buttons for custom dataset
        var datasetButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        var chooseButton = new JButton("Select File…");
        chooseButton.addActionListener(actionEvent -> {
            Optional.ofNullable(customDataset).ifPresent(customDatasetField::setSelectedFile);
            int result = customDatasetField.showOpenDialog(TestConfigTab.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = customDatasetField.getSelectedFile();
                customDatasetPathLabel.setText(selectedFile.getAbsolutePath());
                customDataset = selectedFile;
            }
        });
        var clearButton = new JButton("Remove Dataset");
        clearButton.addActionListener(actionEvent -> {
            customDatasetPathLabel.setText("No file selected");
            customDataset = null;
            customDatasetField.setSelectedFile(null);
        });
        datasetButtons.add(chooseButton);
        datasetButtons.add(clearButton);
        addTestConfigRow(testConfigPanel, testconfigGBC, "", datasetButtons);

        //Exclude Attacks
        excludeAttacksField = new JTextField(settings.exclude(), 20);
        excludeAttacksField.setToolTipText("e.g. AntiGPT,PersonGPT");
        JLabel excludeAttacksLabel = addTestConfigRow(testConfigPanel, testconfigGBC,"Exclude attack(s):", excludeAttacksField);
        ui.setupUIChangeTracking(excludeAttacksField, excludeAttacksLabel);

        // Include attacks
        includeAttacksField = new JTextField(settings.include(), 20);
        includeAttacksField.setToolTipText("e.g. AntiGPT,PersonGPT");
        JLabel includeAttacksLabel = addTestConfigRow(testConfigPanel, testconfigGBC,"Include attack(s):", includeAttacksField);
        ui.setupUIChangeTracking(includeAttacksField, includeAttacksLabel);

        // Prompt repeats
        promptRepeatsField = new JFormattedTextField(NumberFormat.getIntegerInstance());
        promptRepeatsField.setValue(settings.promptRepeats());
        promptRepeatsField.setToolTipText("e.g. 3");
        JLabel promptRepeatsLabel = addTestConfigRow(testConfigPanel, testconfigGBC,"Prompt Repeats:", promptRepeatsField);
        ui.setupUIChangeTracking(promptRepeatsField, promptRepeatsLabel);

        // Parallelism
        parallelismField = new JFormattedTextField(NumberFormat.getIntegerInstance());
        parallelismField.setValue(settings.parallelism());
        parallelismField.setToolTipText("e.g. 1");
        JLabel parallelismLabel = addTestConfigRow(testConfigPanel, testconfigGBC,"Parallelism:", parallelismField);
        ui.setupUIChangeTracking(parallelismField, parallelismLabel);

    }

     /**
     * Adds a row to the test configuration panel with a label and corresponding field.
     * @param panel The panel to which the row will be added
     * @param baseGbc The base GridBagConstraints to use for layout
     * @param labelText The text for the label
     * @param field The input field component
     * @return The JLabel that was added to the panel
     */
    private JLabel addTestConfigRow(JPanel panel, GridBagConstraints baseGbc,
                                    String labelText, JComponent field) {

        // Label, left column
        GridBagConstraints gbcLabel = (GridBagConstraints) baseGbc.clone();
        gbcLabel.gridx = 0;
        gbcLabel.gridy = testConfigRow;
        gbcLabel.weightx = 0.0;
        gbcLabel.gridwidth = 1;
        gbcLabel.fill = GridBagConstraints.NONE;
        gbcLabel.anchor = GridBagConstraints.WEST;

        JLabel label = new JLabel(labelText);
        panel.add(label, gbcLabel);

        // Field, right column
        GridBagConstraints gbcField = (GridBagConstraints) baseGbc.clone();
        gbcField.gridx = 1;
        gbcField.gridy = testConfigRow;
        gbcField.weightx = 1.0;
        gbcField.gridwidth = 1;
        gbcField.fill = GridBagConstraints.HORIZONTAL;
        gbcField.anchor = GridBagConstraints.WEST;

        panel.add(field, gbcField);

        testConfigRow++;
        return label;
    }

}
