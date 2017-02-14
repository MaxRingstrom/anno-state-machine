package com.jayway.annostatemachine.processor;


import com.jayway.annostatemachine.annotations.StateMachine;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes("com.jayway.annostatemachine.annotations.StateMachine")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class StateMachineProcessor extends AbstractProcessor {
    private static final String TAG = StateMachineProcessor.class.getSimpleName();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(StateMachine.class)) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Statemachine found: " + element.getSimpleName());
        }
        return true;
    }
}
