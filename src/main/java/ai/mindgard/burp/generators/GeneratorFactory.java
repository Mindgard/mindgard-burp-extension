package ai.mindgard.burp.generators;

import burp.api.montoya.intruder.AttackConfiguration;
import burp.api.montoya.intruder.PayloadGenerator;
import burp.api.montoya.intruder.PayloadGeneratorProvider;

import java.util.function.Supplier;

public class GeneratorFactory<T extends PayloadGenerator> implements PayloadGeneratorProvider {
    private Class<T> generatorCls;
    private Supplier<T> constructor;

    public GeneratorFactory(Class<T> generatorCls, Supplier<T> constructor) {
        this.generatorCls = generatorCls;
        this.constructor = constructor;
    }

    @Override
    public String displayName() {
        return generatorCls.getSimpleName();
    }

    @Override
    public PayloadGenerator providePayloadGenerator(AttackConfiguration attackConfiguration) {
        return constructor.get();
    }
}
