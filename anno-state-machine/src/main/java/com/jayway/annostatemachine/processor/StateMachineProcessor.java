package com.jayway.annostatemachine.processor;


import com.jayway.annostatemachine.ConnectionRef;
import com.jayway.annostatemachine.SignalRef;
import com.jayway.annostatemachine.StateRef;
import com.jayway.annostatemachine.annotations.Connection;
import com.jayway.annostatemachine.annotations.Signals;
import com.jayway.annostatemachine.annotations.StateMachine;
import com.jayway.annostatemachine.annotations.States;

import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * Reads state machine declaration classes with the annotation {@link StateMachine} and generates
 * the corresponding state machine implementation classes. The implementation classes are placed
 * in a sub package of the declaration class' package. The sub package is named "generated".
 */
@SupportedAnnotationTypes("com.jayway.annostatemachine.annotations.StateMachine")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
final public class StateMachineProcessor extends AbstractProcessor {

    private static final String TAG = StateMachineProcessor.class.getSimpleName();
    private static final String GENERATED_FILE_SUFFIX = "Impl";
    private final StateMachineCreator mStateMachineCreator;

    private Model mModel = new Model();

    public StateMachineProcessor() {
        super();
        mStateMachineCreator = new StateMachineCreator();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(StateMachine.class)) {
            // Clean model for each state machine source file
            mModel = new Model();
            if (element.getKind().isClass()) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                        "Statemachine found: " + ((TypeElement) element).getQualifiedName().toString());
                generateModel(element);
                if (mModel.validateModel(element.getSimpleName().toString(), processingEnv.getMessager())) {
                    mStateMachineCreator.writeStateMachine(element, mModel, processingEnv);
                } else {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, mModel.getSourceClassName() + " - Invalid state machine - Not generating implementation.");
                }
            } else {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Non class using " + StateMachine.class.getSimpleName() + " annotation");
            }
        }
        return true;
    }

    private void generateModel(Element element) {
        // Find the top element in order to get the package name that the source class resides in
        // even if it is an inner class.
        Element topElement = element;
        while (((TypeElement) topElement).getNestingKind().isNested()) {
            topElement = element.getEnclosingElement();
        }

        StateMachine stateMachineAnnotation = element.getAnnotation(StateMachine.class);
        mModel.setDispatchMode(stateMachineAnnotation.dispatchMode(), stateMachineAnnotation.queueId());

        String topElementQualifiedName = ((TypeElement) topElement).getQualifiedName().toString();
        String sourceClassPackage = topElementQualifiedName.substring(0, topElementQualifiedName.lastIndexOf("."));
        String sourceClassQualifiedName = ((TypeElement) element).getQualifiedName().toString();
        String sourceClassName = element.getSimpleName().toString();

        String generatedPackage = sourceClassPackage + ".generated";
        String generatedClassName = element.getSimpleName() + GENERATED_FILE_SUFFIX;
        String generatedClassFullPath = generatedPackage + "." + generatedClassName;

        mModel.setSource(sourceClassQualifiedName, sourceClassName);
        mModel.setTarget(generatedPackage, generatedClassName, generatedClassFullPath);

        for (Element enclosedElement : element.getEnclosedElements()) {
            if (enclosedElement.getAnnotation(States.class) != null) {
                collectStates(enclosedElement);
            } else if (enclosedElement.getAnnotation(Signals.class) != null) {
                collectSignals(enclosedElement);
            } else if (enclosedElement.getAnnotation(Connection.class) != null) {
                collectConnection(enclosedElement);
            }
        }

        mModel.aggregateConnectionsPerSignal();
    }

    private void collectStates(Element element) {
        if (!(element.getKind().equals(ElementKind.ENUM))) {
            // States annotation on something other than an enum
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Non enum " + element.getSimpleName() + " of type " + element.getKind() + " using annotation " + States.class.getSimpleName());
            return;
        }

        mModel.setStatesEnum((TypeElement) element);
        List<? extends Element> values = element.getEnclosedElements();
        for (Element valueElement : values) {
            if (valueElement.getKind().equals(ElementKind.ENUM_CONSTANT)) {
                StateRef stateRef = new StateRef(valueElement.getSimpleName().toString());
                mModel.add(stateRef);
            }
        }
    }

    private void collectSignals(Element element) {
        if (!(element.getKind().equals(ElementKind.ENUM))) {
            // Signal annotation on something other than an enum
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Non enum " + element.getSimpleName() + " of type " + element.getKind() + " using annotation " + Signals.class.getSimpleName());
            return;
        }
        mModel.setSignalsEnum((TypeElement) element);
        List<? extends Element> values = element.getEnclosedElements();
        for (Element valueElement : values) {
            if (valueElement.getKind() == ElementKind.ENUM_CONSTANT) {
                SignalRef signalRef = new SignalRef(valueElement.getSimpleName().toString());
                mModel.add(signalRef);
            }
        }
    }

    private void collectConnection(Element element) {
        if (!(element.getKind() == ElementKind.METHOD)) {
            // Connection annotation on something other than a method
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Non method " + element.getSimpleName() + " using annotation " + Connection.class.getSimpleName());
            return;
        }

        String connectionName = element.getSimpleName().toString();
        Connection annotation = element.getAnnotation(Connection.class);

        ConnectionRef connectionRef = new ConnectionRef(connectionName, annotation.from(),
                annotation.to(), annotation.on(), annotation.runOnUiThread());
        mModel.add(connectionRef);
    }

}