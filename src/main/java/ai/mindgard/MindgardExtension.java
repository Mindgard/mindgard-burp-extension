package ai.mindgard;

import ai.mindgard.burp.generators.GeneratorFactory;
import ai.mindgard.sandbox.Mindgard;
import ai.mindgard.burp.generators.MindgardGenerator;
import ai.mindgard.burp.MindgardHttpHandler;
import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;

public class MindgardExtension implements BurpExtension {
    @Override
    public void initialize(MontoyaApi api) {
        api.extension().setName(getClass().getSimpleName());
        MindgardSettingsUI settings = new MindgardSettingsUI();
        Log logger = api.logging()::logToOutput;
        var auth = new MindgardAuthentication(logger);
        Mindgard mg = new Mindgard(logger, auth, settings);

        try {
            auth.auth();
        } catch (MindgardAuthentication.AuthenticationFailedException e) {
            auth.login();
            auth.auth();
        }

        api.intruder().registerPayloadGeneratorProvider(new GeneratorFactory<>(MindgardGenerator.class, () -> new MindgardGenerator(mg, logger)));
        api.http().registerHttpHandler(new MindgardHttpHandler(mg, settings, logger));

        api.userInterface().registerSuiteTab("Mindgard", settings);

        api.logging().logToOutput("Loaded Mindgard Generators");
    }

}
