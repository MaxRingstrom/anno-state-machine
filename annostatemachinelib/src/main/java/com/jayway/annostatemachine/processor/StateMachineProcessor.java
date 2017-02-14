package com.jayway.annostatemachine.processor;


import com.jayway.annostatemachine.Constants;
import com.jayway.annostatemachine.annotations.Connection;
import com.jayway.annostatemachine.annotations.Signal;
import com.jayway.annostatemachine.annotations.State;
import com.jayway.annostatemachine.annotations.StateMachine;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("com.jayway.annostatemachine.annotations.StateMachine")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class StateMachineProcessor extends AbstractProcessor {

    private static final String TAG = StateMachineProcessor.class.getSimpleName();
    private static final String NEWLINE = "\n\n";
    private static final String GENERATED_FILE_SUFFIX = "Impl";

    private Model mModel = new Model();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(StateMachine.class)) {
            if (element.getKind().isClass()) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                        "Statemachine found: " + element.getSimpleName());
                generateStateMachine(element, roundEnv);
            } else {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Non class using " + StateMachine.class.getSimpleName() + " annotation");
            }
        }
        return true;
    }

    private void generateStateMachine(Element element, RoundEnvironment roundEnv) {
        String packageOfStateMachineSource = ((TypeElement)element).getQualifiedName().toString();

        String generatedPackage = Constants.libPackageName + ".generated";
        String generatedClassName = element.getSimpleName() + GENERATED_FILE_SUFFIX;
        String generatedClassFullPath = generatedPackage + "." + generatedClassName;

        JavaFileObject source = null;
        try {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                    "Generating state machine implementation for " + element.getSimpleName());
            source = processingEnv.getFiler().createSourceFile(generatedClassFullPath);
            try (Writer writer = source.openWriter(); JavaWriter javaWriter = new JavaWriter(writer)) {

                StringBuilder sb = new StringBuilder();

                javaWriter.emitPackage(generatedPackage);
                javaWriter.emitImports(packageOfStateMachineSource);
                javaWriter.emitEmptyLine();
                javaWriter.beginType(generatedClassName, "class", EnumSet.of(Modifier.PUBLIC), element.getSimpleName().toString());

                generateMetadata(element, writer, javaWriter);

                // End class
                javaWriter.emitEmptyLine();
                javaWriter.endType();

                writer.close();
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Couldn't create generated state machine class: " + generatedClassName);
            e.printStackTrace();
        }
    }

    private void generateMetadata(Element element, Writer writer, JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        for (Element enclosedElement : element.getEnclosedElements()) {
            if (enclosedElement.getAnnotation(State.class) != null) {
                collectState(enclosedElement);
            } else if (enclosedElement.getAnnotation(Signal.class) != null) {
                collectSignal(enclosedElement);
            } else if (enclosedElement.getAnnotation(Connection.class) != null) {
                collectConnection(enclosedElement);
            }
        }

        mModel.describeContents(javaWriter);
    }

    private void collectState(Element element) {
        if (!(element.getKind() == ElementKind.FIELD)) {
            // State annotation on something other than a field
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Non field " + element.getSimpleName() + " using annotation " + State.class.getSimpleName());
            return;
        }

        StateRef stateRef = new StateRef(element.getSimpleName().toString());
        mModel.add(stateRef);
    }

    private void collectSignal(Element element) {
        if (!(element.getKind() == ElementKind.FIELD)) {
            // Signal annotation on something other than a field
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Non field " + element.getSimpleName() + " using annotation " + Signal.class.getSimpleName());
            return;
        }
        SignalRef signalRef = new SignalRef(element.getSimpleName().toString());
        mModel.add(signalRef);
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

        ConnectionRef connectionRef = new ConnectionRef(connectionName, annotation.from(), annotation.to(), annotation.signal());
        mModel.add(connectionRef);
    }

    private static class Model {

        private ArrayList<SignalRef> mSignals = new ArrayList<>();
        private ArrayList<ConnectionRef> mConnections = new ArrayList<>();
        private ArrayList<StateRef> mStates = new ArrayList<>();

        public void add(SignalRef signal) {
            mSignals.add(signal);
        }

        public void add(ConnectionRef connection) {
            mConnections.add(connection);
        }

        public void add(StateRef state) {
            mStates.add(state);
        }

        public void describeContents(JavaWriter javaWriter) throws IOException {
            javaWriter.emitSingleLineComment("--- States ---");
            for (StateRef stateRef : mStates) {
                javaWriter.emitSingleLineComment(" " + stateRef);
            }

            javaWriter.emitEmptyLine();
            javaWriter.emitSingleLineComment("--- Signals ---");
            for (SignalRef signalRef : mSignals) {
                javaWriter.emitSingleLineComment(" " + signalRef);
            }

            javaWriter.emitEmptyLine();
            javaWriter.emitSingleLineComment("--- Connections ---");
            for (ConnectionRef connectionRef : mConnections) {
                javaWriter.emitSingleLineComment(" " + connectionRef);
            }
        }
    }

    private static class SignalRef {
        String name;

        SignalRef(String name) {
            this.name = name.toUpperCase();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class ConnectionRef {

        private final String name;
        private final String from;
        private final String to;
        private final String signal;

        ConnectionRef(String name, String from, String to, String signal) {
            this.name = name.toUpperCase();
            this.from = from.toUpperCase();
            this.to = to.toUpperCase();
            this.signal = signal.toUpperCase();
        }

        @Override
        public String toString() {
            return name + ": " + from + " --" + signal + "--> " + to;
        }
    }

    private static class StateRef {
        private final String name;

        StateRef(String name) {
            this.name = name.toUpperCase();
        }

        @Override
        public String toString() {
            return name;
        }
    }
}