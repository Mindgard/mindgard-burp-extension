package ai.mindgard;

import ai.mindgard.burp.generators.GeneratorFactory;
import ai.mindgard.sandbox.Mindgard;
import ai.mindgard.sandbox.MindgardWebSocketPrompts;
import ai.mindgard.burp.generators.MindgardGenerator;
import ai.mindgard.burp.MindgardHttpHandler;
import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;

import java.time.Instant;

public class MindgardExtension implements BurpExtension {
    @Override
    public void initialize(MontoyaApi api) {
        api.extension().setName(getClass().getSimpleName());
        Log logger = api.logging()::logToOutput;
        MindgardSettingsManager mgsm = new MindgardSettingsManager();
        MindgardSettingsUI userInterface = new MindgardSettingsUI(mgsm, logger);
        var auth = new MindgardAuthentication(mgsm);
        Mindgard mg = new MindgardWebSocketPrompts(logger, auth, mgsm, 60L);

        api.intruder().registerPayloadGeneratorProvider(new GeneratorFactory<>(MindgardGenerator.class, () -> new MindgardGenerator(mg, logger)));
        api.http().registerHttpHandler(new MindgardHttpHandler(mg, mgsm, logger));
        api.userInterface().registerSuiteTab("Mindgard", userInterface);
        api.logging().logToOutput("["+ Instant.now() + "] Loaded Mindgard Generators");
    }

}
