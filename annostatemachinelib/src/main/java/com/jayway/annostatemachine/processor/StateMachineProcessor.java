package com.jayway.annostatemachine.processor;


import com.jayway.annostatemachine.ConnectionRef;
import com.jayway.annostatemachine.NullEventListener;
import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.SignalRef;
import com.jayway.annostatemachine.StateMachineEventListener;
import com.jayway.annostatemachine.StateRef;
import com.jayway.annostatemachine.annotations.Connection;
import com.jayway.annostatemachine.annotations.Signals;
import com.jayway.annostatemachine.annotations.StateMachine;
import com.jayway.annostatemachine.annotations.States;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

// Notes
// Add general signal handler that can act on multiple from states
// Add a general signal handler that acts on all signals in one, several or all states with or without to state.

@SupportedAnnotationTypes("com.jayway.annostatemachine.annotations.StateMachine")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class StateMachineProcessor extends AbstractProcessor {

    private static final String TAG = StateMachineProcessor.class.getSimpleName();
    private static final String NEWLINE = "\n\n";
    private static final String GENERATED_FILE_SUFFIX = "Impl";

    private Model mModel = new Model();
    private String mStateMachineSourceQualifiedName;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(StateMachine.class)) {
            // Clean model for each statemachine source file
            mModel = new Model();
            if (element.getKind().isClass()) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                        "Statemachine found: " + ((TypeElement) element).getQualifiedName().toString());
                generateStateMachine(element, roundEnv);
            } else {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Non class using " + StateMachine.class.getSimpleName() + " annotation");
            }
        }
        return true;
    }

    private void generateStateMachine(Element element, RoundEnvironment roundEnv) {
        mStateMachineSourceQualifiedName = ((TypeElement) element).getQualifiedName().toString();

        Element topElement = element;
        while (((TypeElement) topElement).getNestingKind().isNested()) {
            topElement = element.getEnclosingElement();
        }

        String topElementQualifiedName = ((TypeElement) topElement).getQualifiedName().toString();
        String sourceClassPackage = topElementQualifiedName.substring(0, topElementQualifiedName.lastIndexOf("."));
        String generatedPackage = sourceClassPackage + ".generated";

        String generatedClassName = element.getSimpleName() + GENERATED_FILE_SUFFIX;
        String generatedClassFullPath = generatedPackage + "." + generatedClassName;

        JavaFileObject source;
        try {
            source = processingEnv.getFiler().createSourceFile(generatedClassFullPath);
            try (Writer writer = source.openWriter(); JavaWriter javaWriter = new JavaWriter(writer)) {
                generateMetadata(element, writer, javaWriter);

                javaWriter.emitPackage(generatedPackage);
                javaWriter.emitImports(mStateMachineSourceQualifiedName,
                        SignalPayload.class.getCanonicalName(),
                        NullEventListener.class.getCanonicalName(),
                        StateMachineEventListener.class.getCanonicalName());
                javaWriter.emitStaticImports(mStateMachineSourceQualifiedName + ".*");
                javaWriter.emitEmptyLine();
                javaWriter.beginType(generatedClassName, "class", EnumSet.of(Modifier.PUBLIC), element.getSimpleName().toString());

                mModel.describeContents(javaWriter);

                validateModel();

                generateFields(javaWriter);

                generatePassThroughConstructors(element, generatedClassName, javaWriter);

                generateInitMethod(javaWriter);

                generateSignalDispatcher(javaWriter);

                generateSignalHandlersForStates(javaWriter);

                generateSendMethods(javaWriter);
                generateSwitchStateMethod(javaWriter);

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

    private void emitGlobalAnySignalConnectionHandler(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();

        for (ConnectionRef connection : mModel.mGlobalAnySignalConnections) {
            javaWriter.emitStatement("if (%s(payload)) return %s", connection.getName(), mModel.getStatesEnumName() + "." + connection.getTo());
        }
    }

    private void emitGlobalSpecificSignalConnectionHandler(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();

        javaWriter.emitEmptyLine();

        for (Map.Entry<String, ArrayList<ConnectionRef>> connectionsForSignal : mModel.mGlobalSpecificSignalConnectionsPerSignal.entrySet()) {
            javaWriter.beginControlFlow("if (signal.equals(" + mModel.getSignalsEnumName() + "." + connectionsForSignal.getKey() + "))");
            for (ConnectionRef connection : connectionsForSignal.getValue()) {
                javaWriter.emitStatement("if (%s(payload)) return %s", connection.getName(), mModel.getStatesEnumName() + "." + connection.getTo());
            }
            javaWriter.endControlFlow();
        }
    }

    private void emitGlobalAnySignalEavesdropperHandler(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();

        for (ConnectionRef connection : mModel.mGlobalAnySignalEavesDroppers) {
            javaWriter.emitStatement("%s(payload)", connection.getName());
        }
    }

    private void emitGlobalSpecificSignalEavesdropperHandler(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();

        for (Map.Entry<String, ArrayList<ConnectionRef>> connectionsForSignal : mModel.mGlobalSpecificSignalEavesdroppersPerSignal.entrySet()) {
            javaWriter.beginControlFlow("if (signal.equals(" + mModel.getSignalsEnumName() + "." + connectionsForSignal.getKey() + "))");
            for (ConnectionRef connection : connectionsForSignal.getValue()) {
                javaWriter.emitStatement("%s(payload)", connection.getName());
            }
            javaWriter.endControlFlow();
        }
    }

    private void validateModel() {
        for (Map.Entry<String, ArrayList<ConnectionRef>> entry : mModel.mLocalSpecificSignalConnections.entrySet()) {
            if (entry.getValue() != null) {
                for (ConnectionRef connectionRef : entry.getValue()) {
                    if (!mModel.mStates.contains(new StateRef(connectionRef.getFrom()))) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, mStateMachineSourceQualifiedName + " - Unknown FROM state "
                                + connectionRef.getFrom() + " used in connection " + connectionRef.getName() + ". Do you have a typo?");
                    }
                    if (!connectionRef.getTo().equals(ConnectionRef.WILDCARD) && !mModel.mStates.contains(new StateRef(connectionRef.getTo()))) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, mStateMachineSourceQualifiedName + " - Unknown TO state "
                                + connectionRef.getTo() + " used in connection " + connectionRef.getName() + ". Do you have a typo?");
                    }
                    if (!mModel.mSignals.contains(new SignalRef(connectionRef.getSignal()))) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, mStateMachineSourceQualifiedName + " - Unknown SIGNAL "
                                + connectionRef.getSignal() + " used in connection " + connectionRef.getName() + ". Do you have a typo?");
                    }
                }
            }
        }
    }

    private void generatePassThroughConstructors(Element element, final String generatedClassName, final JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        List<? extends Element> elements = element.getEnclosedElements();
        for (final Element childElement : elements) {
            if (childElement.getKind() == ElementKind.CONSTRUCTOR) {
                childElement.accept(new ElementVisitor<Object, Object>() {
                    @Override
                    public Object visit(Element e, Object o) {
                        return null;
                    }

                    @Override
                    public Object visit(Element e) {
                        return null;
                    }

                    @Override
                    public Object visitPackage(PackageElement e, Object o) {
                        return null;
                    }

                    @Override
                    public Object visitType(TypeElement e, Object o) {
                        return null;
                    }

                    @Override
                    public Object visitVariable(VariableElement e, Object o) {
                        return null;
                    }

                    @Override
                    public Object visitExecutable(ExecutableElement e, Object o) {
                        List<String> params = new ArrayList<String>();
                        String paramListString = "";
                        String paramType;
                        String paramName;
                        for (VariableElement el : e.getParameters()) {
                            paramType = el.asType().toString();
                            paramName = el.getSimpleName().toString();
                            params.add(paramType);
                            params.add(paramName);
                            paramListString += (paramName + ",");
                        }
                        if (!paramListString.isEmpty()) {
                            // Remove trailing ,
                            paramListString = paramListString.substring(0, paramListString.length() - 1);
                        }
                        try {
                            javaWriter.beginMethod(null, generatedClassName, EnumSet.of(Modifier.PUBLIC), params, null);
                            javaWriter.emitStatement("super(%s)", paramListString);
                            javaWriter.endMethod();
                        } catch (IOException e1) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Error when creating pass through constructor");
                            e1.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    public Object visitTypeParameter(TypeParameterElement e, Object o) {
                        return null;
                    }

                    @Override
                    public Object visitUnknown(Element e, Object o) {
                        return null;
                    }
                }, element);
            }
        }
    }

    private void generateSendMethods(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.beginMethod("void", "send", EnumSet.of(Modifier.PUBLIC), mModel.getSignalsEnumName(), "signal", "SignalPayload", "payload");

        javaWriter.beginControlFlow("if (mWaitingForInit)");
        javaWriter.emitStatement("throw new IllegalStateException(\"Missing call to init\")");
        javaWriter.endControlFlow();

        javaWriter.emitStatement(mModel.getStatesEnumName() + " nextState = dispatchSignal(signal, payload)");
        javaWriter.beginControlFlow("if (nextState != null)");
        javaWriter.emitStatement("switchState(nextState)");
        javaWriter.endControlFlow();
        javaWriter.endMethod();

        javaWriter.emitEmptyLine();
        javaWriter.beginMethod("void", "send", EnumSet.of(Modifier.PUBLIC), mModel.getSignalsEnumName(), "signal");
        javaWriter.emitStatement("send(signal, null)");
        javaWriter.endMethod();
    }

    private void generateSwitchStateMethod(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.beginMethod("void", "switchState", EnumSet.of(Modifier.PRIVATE), mModel.getStatesEnumName(), "nextState");
        javaWriter.emitStatement("mEventListener.onChangingState(mCurrentState, nextState)");
        javaWriter.emitStatement("mCurrentState = nextState");
        javaWriter.endMethod();
    }

    private void generateSignalHandlersForStates(JavaWriter javaWriter) throws IOException {
        for (StateRef stateRef : mModel.mStates) {
            generateSignalHandler(stateRef, javaWriter);
        }
    }

    private void generateSignalHandler(StateRef stateRef, JavaWriter javaWriter) throws IOException {
        ArrayList<ConnectionRef> anySignalConnectionsForState = mModel.mLocalAnySignalConnections.get(stateRef.getName());
        javaWriter.emitEmptyLine();
        javaWriter.beginMethod(mModel.getStatesEnumName(), "handleSignalIn" + camelCase(stateRef.getName()), EnumSet.of(Modifier.PRIVATE), mModel.getSignalsEnumName(), "signal", "SignalPayload", "payload");

        emitLocalSpecificSignalEavesdropperHandler(stateRef, javaWriter);
        emitLocalAnySignalEavesdropperHandler(stateRef, javaWriter);

        javaWriter.emitEmptyLine();
        HashMap<String, ArrayList<ConnectionRef>> connectionsPerSignal = mModel.mLocalSpecificSignalConnectionsPerStateGroupedBySignal.get(stateRef.getName());
        if (connectionsPerSignal != null) {
            for (Map.Entry<String, ArrayList<ConnectionRef>> connectionsForSignalEntry : connectionsPerSignal.entrySet()) {
                javaWriter.beginControlFlow("if (signal.equals(" + mModel.getSignalsEnumName() + "." + connectionsForSignalEntry.getKey() + "))");
                for (ConnectionRef connectionForSignal : connectionsForSignalEntry.getValue()) {
                    javaWriter.emitStatement("if (%s(payload)) return %s", connectionForSignal.getName(), mModel.getStatesEnumName() + "." + connectionForSignal.getTo());
                }
                javaWriter.endControlFlow();
            }
        }

        javaWriter.emitEmptyLine();
        if (anySignalConnectionsForState != null) {
            for (ConnectionRef connection : anySignalConnectionsForState) {
                javaWriter.emitStatement("if (%s(payload)) return %s", connection.getName(), mModel.getStatesEnumName() + "." + connection.getTo());
            }
        }

        javaWriter.emitStatement("return null");
        javaWriter.endMethod();
    }

    private void emitLocalAnySignalEavesdropperHandler(StateRef stateRef, JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        ArrayList<ConnectionRef> localAnySignalEavesdroppers = mModel.mLocalAnySignalEavesdroppers.get(stateRef.getName());
        if (localAnySignalEavesdroppers != null) {
            for (ConnectionRef connection : localAnySignalEavesdroppers) {
                javaWriter.emitStatement("%s(payload)", connection.getName());
            }
        }
    }

    private void emitLocalSpecificSignalEavesdropperHandler(StateRef stateRef, JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();

        HashMap<String, ArrayList<ConnectionRef>> connectionsPerSignal = mModel.mLocalSpecificSignalEavesdroppersPerStateGroupedBySignal.get(stateRef.getName());
        if (connectionsPerSignal != null) {
            for (Map.Entry<String, ArrayList<ConnectionRef>> connectionsForSignalEntry : connectionsPerSignal.entrySet()) {
                javaWriter.beginControlFlow("if (signal.equals(" + mModel.getSignalsEnumName() + "." + connectionsForSignalEntry.getKey() + "))");
                for (ConnectionRef connectionForSignal : connectionsForSignalEntry.getValue()) {
                    javaWriter.emitStatement("%s(payload)", connectionForSignal.getName());
                }
                javaWriter.endControlFlow();
            }
        }
    }

    private void generateFields(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.emitField(mModel.getStatesEnumName(), "mCurrentState", EnumSet.of(Modifier.PRIVATE));
        javaWriter.emitField("boolean", "mWaitingForInit", EnumSet.of(Modifier.PRIVATE), "true");
        javaWriter.emitField(StateMachineEventListener.class.getSimpleName(), "mEventListener", EnumSet.of(Modifier.PRIVATE));
    }

    private void generateInitMethod(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.beginMethod("void", "init", EnumSet.of(Modifier.PUBLIC), mModel.getStatesEnumName(), "startingState", StateMachineEventListener.class.getSimpleName(), "eventListener");
        javaWriter.emitStatement("mCurrentState = startingState");
        javaWriter.emitStatement("mEventListener = eventListener != null ? eventListener : new NullEventListener()");
        javaWriter.emitStatement("mWaitingForInit = false");
        javaWriter.endMethod();
    }

    private void generateSignalDispatcher(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.beginMethod(mModel.getStatesEnumName(), "dispatchSignal", EnumSet.of(Modifier.PRIVATE), mModel.getSignalsEnumName(), "signal", "SignalPayload", "payload");

        javaWriter.emitStatement("mEventListener.onDispatchingSignal(mCurrentState, signal)");

        emitGlobalSpecificSignalEavesdropperHandler(javaWriter);
        emitGlobalAnySignalEavesdropperHandler(javaWriter);

        javaWriter.emitEmptyLine();
        javaWriter.beginControlFlow("switch (mCurrentState)");

        for (StateRef state : mModel.mStates) {
            javaWriter.emitStatement("case %s: return handleSignalIn%s(signal, payload)",
                    state.getName(), camelCase(state.getName()));
        }

        javaWriter.endControlFlow();

        emitGlobalSpecificSignalConnectionHandler(javaWriter);
        emitGlobalAnySignalConnectionHandler(javaWriter);

        javaWriter.emitStatement("return null");

        javaWriter.endMethod();
    }

    private void generateMetadata(Element element, Writer writer, JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
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
            // Signal annotation on something other than a enum
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

        ConnectionRef connectionRef = new ConnectionRef(connectionName, annotation.from(), annotation.to(), annotation.signal());
        mModel.add(connectionRef);
    }

    private static class Model {

        private ArrayList<SignalRef> mSignals = new ArrayList<>();
        private HashMap<String, ArrayList<ConnectionRef>> mLocalSpecificSignalConnections = new HashMap<>();
        private HashMap<String, ArrayList<ConnectionRef>> mLocalAnySignalEavesdroppers = new HashMap<>();
        private HashMap<String, ArrayList<ConnectionRef>> mLocalSpecificSignalEavesdroppers = new HashMap<>();
        private HashMap<String, ArrayList<ConnectionRef>> mLocalAnySignalConnections = new HashMap<>();
        private ArrayList<ConnectionRef> mGlobalSpecificSignalEavesdroppers = new ArrayList<>();
        private ArrayList<ConnectionRef> mGlobalAnySignalEavesDroppers = new ArrayList<>();
        private ArrayList<ConnectionRef> mGlobalSpecificSignalConnections = new ArrayList<>();
        private ArrayList<ConnectionRef> mGlobalAnySignalConnections = new ArrayList<>();

        private ArrayList<StateRef> mStates = new ArrayList<>();

        // Aggregated info
        private HashMap<String, HashMap<String, ArrayList<ConnectionRef>>> mLocalSpecificSignalConnectionsPerStateGroupedBySignal = new HashMap<>();
        private HashMap<String, HashMap<String, ArrayList<ConnectionRef>>> mLocalSpecificSignalEavesdroppersPerStateGroupedBySignal = new HashMap<>();
        private HashMap<String, ArrayList<ConnectionRef>> mGlobalSpecificSignalEavesdroppersPerSignal = new HashMap<>();
        private HashMap<String, ArrayList<ConnectionRef>> mGlobalSpecificSignalConnectionsPerSignal = new HashMap<>();

        private String mSignalsEnumClassQualifiedName;
        private String mStatesEnumClassQualifiedName;
        private String mSignalsEnumName;

        public String getStatesEnumName() {
            return mStatesEnumName;
        }

        public String getSignalsEnumQualifiedName() {
            return mSignalsEnumClassQualifiedName;
        }

        public String getStatesEnumQualifiedName() {
            return mStatesEnumClassQualifiedName;
        }

        public String getSignalsEnumName() {
            return mSignalsEnumName;
        }

        private String mStatesEnumName;

        public void add(SignalRef signal) {
            mSignals.add(signal);
        }

        public void add(ConnectionRef connection) {
            boolean hasWildcardFrom = ConnectionRef.WILDCARD.equals(connection.getFrom());
            boolean hasWildcardTo = ConnectionRef.WILDCARD.equals(connection.getTo());
            boolean hasWildcardSignal = ConnectionRef.WILDCARD.equals(connection.getSignal());

            if (hasWildcardFrom) {
                // Global
                if (hasWildcardTo) {
                    // Eavesdrop
                    if (hasWildcardSignal) {
                        // Any signal
                        addGlobalAnySignalEavesDropper(connection);
                    } else {
                        // Specific signal
                        addGlobalSpecificSignalEavesDropper(connection);
                    }
                } else {
                    // Normal
                    if (hasWildcardSignal) {
                        // Any signal
                        addGlobalAnySignalConnection(connection);
                    } else {
                        // Specific signal
                        addGlobalSpecificSignalConnection(connection);
                    }
                }
            } else {
                // Local
                if (hasWildcardTo) {
                    // Eavesdrop
                    if (hasWildcardSignal) {
                        // Any signal
                        addLocalAnySignalEavesdropper(connection);
                    } else {
                        // Specific signal
                        addLocalSpecificSignalEavesdropper(connection);
                    }
                } else {
                    // Normal
                    if (hasWildcardSignal) {
                        // Any signal
                        addLocalAnySignalConnection(connection);
                    } else {
                        // Specific signal
                        addLocalSpecificSignalConnection(connection);
                    }
                }
            }
        }

        private void addLocalAnySignalConnection(ConnectionRef connection) {
            ArrayList<ConnectionRef> connections = mLocalAnySignalConnections.get(connection.getFrom());
            if (connections == null) {
                connections = new ArrayList<>();
            }
            connections.add(connection);
            mLocalAnySignalConnections.put(connection.getFrom(), connections);
        }

        private void addLocalSpecificSignalEavesdropper(ConnectionRef connection) {
            ArrayList<ConnectionRef> connections = mLocalSpecificSignalEavesdroppers.get(connection.getFrom());
            if (connections == null) {
                connections = new ArrayList<>();
            }
            connections.add(connection);
            mLocalSpecificSignalEavesdroppers.put(connection.getFrom(), connections);
        }

        private void addLocalAnySignalEavesdropper(ConnectionRef connection) {
            ArrayList<ConnectionRef> connections = mLocalAnySignalEavesdroppers.get(connection.getFrom());
            if (connections == null) {
                connections = new ArrayList<>();
            }
            connections.add(connection);
            mLocalAnySignalEavesdroppers.put(connection.getFrom(), connections);
        }

        private void addGlobalSpecificSignalConnection(ConnectionRef connection) {
            mGlobalSpecificSignalConnections.add(connection);
        }

        private void addGlobalAnySignalConnection(ConnectionRef connection) {
            mGlobalAnySignalConnections.add(connection);
        }

        private void addGlobalAnySignalEavesDropper(ConnectionRef connection) {
            mGlobalAnySignalEavesDroppers.add(connection);
        }

        private void addGlobalSpecificSignalEavesDropper(ConnectionRef connection) {
            mGlobalSpecificSignalEavesdroppers.add(connection);
        }

        private void addLocalSpecificSignalConnection(ConnectionRef connection) {
            ArrayList<ConnectionRef> connectionsForFromState = mLocalSpecificSignalConnections.get(connection.getFrom());
            if (connectionsForFromState == null) {
                connectionsForFromState = new ArrayList<>();
            }
            connectionsForFromState.add(connection);
            mLocalSpecificSignalConnections.put(connection.getFrom(), connectionsForFromState);
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
            for (Map.Entry<String, ArrayList<ConnectionRef>> connectionEntry : mLocalSpecificSignalConnections.entrySet()) {
                javaWriter.emitSingleLineComment("");
                javaWriter.emitSingleLineComment(" State: " + connectionEntry.getKey());
                for (ConnectionRef connection : connectionEntry.getValue()) {
                    javaWriter.emitSingleLineComment("   " + connection);
                }
            }
        }

        public void setSignalsEnum(TypeElement element) {
            mSignalsEnumClassQualifiedName = element.getQualifiedName().toString();
            mSignalsEnumName = element.getSimpleName().toString();
        }

        public void setStatesEnum(TypeElement element) {
            mStatesEnumClassQualifiedName = element.getQualifiedName().toString();
            mStatesEnumName = element.getSimpleName().toString();
        }

        public void aggregateConnectionsPerSignal() {
            aggregateLocalSpecificSignalConnectionsPerSignalPerState();
            aggregateGlobalSpecificEavesdroppersPerSignal();
            aggregateLocalSpecificEavesdroppersPerSignal();
            aggregateGlobalSpecificSignalConnectionsPerSignal();
        }

        private void aggregateGlobalSpecificSignalConnectionsPerSignal() {
            for (ConnectionRef globalSpecificConnection : mGlobalSpecificSignalConnections) {
                ArrayList<ConnectionRef> connections =
                        mGlobalSpecificSignalConnectionsPerSignal.get(globalSpecificConnection.getSignal());
                if (connections == null) {
                    connections = new ArrayList<>();
                }
                connections.add(globalSpecificConnection);
                mGlobalSpecificSignalConnectionsPerSignal.put(globalSpecificConnection.getSignal(), connections);
            }
        }

        private void aggregateLocalSpecificEavesdroppersPerSignal() {
            // Loop over all states and check their local specific eavesdropper connections
            for (Map.Entry<String, ArrayList<ConnectionRef>> eavesdroppersPerState : mLocalSpecificSignalEavesdroppers.entrySet()) {
                // Key is the state name
                // Value is the local specific signal eavesdropper for the state
                String stateName = eavesdroppersPerState.getKey();
                ArrayList<ConnectionRef> eavesdroppersForState = eavesdroppersPerState.getValue();
                HashMap<String, ArrayList<ConnectionRef>> eavesdroppersPerSignalInState = new HashMap<>();

                // We group the connections for the state by signal instead
                for (ConnectionRef connection: eavesdroppersForState) {
                    ArrayList<ConnectionRef> connections = eavesdroppersPerSignalInState.get(connection.getSignal());
                    if (connections == null) {
                        connections = new ArrayList<>();
                    }
                    connections.add(connection);
                    eavesdroppersPerSignalInState.put(connection.getSignal(), connections);
                }
                mLocalSpecificSignalEavesdroppersPerStateGroupedBySignal.put(stateName, eavesdroppersPerSignalInState);
            }
        }

        private void aggregateGlobalSpecificEavesdroppersPerSignal() {
            for (ConnectionRef globalSpecificEavesdropper : mGlobalSpecificSignalEavesdroppers) {
                ArrayList<ConnectionRef> connections =
                        mGlobalSpecificSignalEavesdroppersPerSignal.get(globalSpecificEavesdropper.getSignal());
                if (connections == null) {
                    connections = new ArrayList<>();
                }
                connections.add(globalSpecificEavesdropper);
                mGlobalSpecificSignalEavesdroppersPerSignal.put(globalSpecificEavesdropper.getSignal(), connections);
            }
        }

        private void aggregateLocalSpecificSignalConnectionsPerSignalPerState() {
            // Loop over all states and check their local specific signal connections
            for (Map.Entry<String, ArrayList<ConnectionRef>> connectionsPerState : mLocalSpecificSignalConnections.entrySet()) {
                // Key is the state name
                // Value is the local specific signal connections for the state
                String stateName = connectionsPerState.getKey();
                ArrayList<ConnectionRef> connectionsForState = connectionsPerState.getValue();
                HashMap<String, ArrayList<ConnectionRef>> connectionsPerSignalInState = new HashMap<>();

                // We group the connections for the state by signal instead
                for (ConnectionRef connection: connectionsForState) {
                    ArrayList<ConnectionRef> connections = connectionsPerSignalInState.get(connection.getSignal());
                    if (connections == null) {
                        connections = new ArrayList<>();
                    }
                    connections.add(connection);
                    connectionsPerSignalInState.put(connection.getSignal(), connections);
                }
                mLocalSpecificSignalConnectionsPerStateGroupedBySignal.put(stateName, connectionsPerSignalInState);
            }
        }
    }

    private static String camelCase(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }
}