package com.jayway.annostatemachine.processor;


import com.jayway.annostatemachine.ConnectionRef;
import com.jayway.annostatemachine.NullEventListener;
import com.jayway.annostatemachine.PayloadModifier;
import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.StateMachineEventListener;
import com.jayway.annostatemachine.StateRef;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.SimpleElementVisitor7;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Creates a java file with the state machine implementation.
 */
final class StateMachineCreator {

    /**
     * Emits a block of code that calls the provided transition connection methods in order until
     * one returns true. The next state or null is returned from the generated code.
     */
    private void emitGlobalAnySignalTransitionsBlock(Model model, JavaWriter javaWriter) throws IOException {

        List<ConnectionRef> transitions = model.getGlobalAnySignalTransitions();

        if (transitions.size() == 0) {
            return;
        }
        javaWriter.emitEmptyLine();

        for (ConnectionRef connection : transitions) {
            javaWriter.emitStatement("if (%s(payload)) return %s",
                    connection.getName(), model.getStatesEnumName() + "." + connection.getTo());
        }
    }

    /**
     * Emits the field declarations that are needed for the rest of the state machine code.
     */
    void emitFieldDeclarations(Model model, JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.emitField(model.getStatesEnumName(), "mCurrentState", EnumSet.of(Modifier.PRIVATE));
        javaWriter.emitField("boolean", "mWaitingForInit", EnumSet.of(Modifier.PRIVATE), "true");
        javaWriter.emitField(StateMachineEventListener.class.getSimpleName(), "mEventListener", EnumSet.of(Modifier.PRIVATE));
    }

    void emitPassThroughConstructors(Element element, final Model model,
                                     final Messager messager,
                                     final JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        List<? extends Element> elements = element.getEnclosedElements();
        for (final Element childElement : elements) {
            if (childElement.getKind() == ElementKind.CONSTRUCTOR) {
                childElement.accept(new SimpleElementVisitor7<Void, Element>() {
                    @Override
                    public Void visitExecutable(ExecutableElement e, Element element) {
                        List<String> params = new ArrayList<>();
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
                            javaWriter.beginMethod(null, model.getTargetClassName(), EnumSet.of(Modifier.PUBLIC), params, null);
                            javaWriter.emitStatement("super(%s)", paramListString);
                            javaWriter.endMethod();
                        } catch (IOException e1) {
                            messager.printMessage(Diagnostic.Kind.ERROR, "Error when creating pass through constructor");
                            e1.printStackTrace();
                        }
                        return null;
                    }
                }, element);
            }
        }
    }

    /**
     * Emit the method that dispatches the signal to the  It also emits the code that handles global signal connections.
     *
     * @throws IOException
     */
    void emitSignalDispatcher(Model model, JavaWriter javaWriter) throws IOException {

        javaWriter.emitEmptyLine();
        javaWriter.beginMethod(model.getStatesEnumName(), "dispatchSignal", EnumSet.of(Modifier.PRIVATE),
                model.getSignalsEnumName(), "signal", "SignalPayload", "payload");

        javaWriter.emitStatement(model.getStatesEnumName() + " nextState = null");
        javaWriter.emitStatement("mEventListener.onDispatchingSignal(mCurrentState, signal)");

        javaWriter.emitEmptyLine();

        javaWriter.beginControlFlow("switch (mCurrentState)");

        for (StateRef state : model.getStates()) {
            javaWriter.emitStatement("case %s: nextState = handleSignalIn%s(signal, payload); break",
                    state.getName(), camelCase(state.getName()));
        }

        javaWriter.endControlFlow();

        emitGlobalSpecificSignalSpyHandler(model, javaWriter);
        emitGlobalAnySignalSpyHandler(model, javaWriter);

        javaWriter.emitEmptyLine();

        if (model.getGlobalSignalTransitionsPerSignal().size() > 0
                || model.getGlobalAnySignalTransitions().size() > 0) {
            javaWriter.beginControlFlow("if (nextState == null)");

            emitGlobalSpecificSignalConnectionHandler(model, javaWriter);
            emitGlobalAnySignalTransitionsBlock(model, javaWriter);

            javaWriter.endControlFlow();
            javaWriter.emitEmptyLine();
        }

        javaWriter.emitStatement("return nextState");

        javaWriter.endMethod();
    }

    private static String camelCase(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }

    public void writeStateMachine(Element stateMachineDeclarationElement, Model model, ProcessingEnvironment processingEnv) {
        JavaFileObject source;
        try {
            source = processingEnv.getFiler().createSourceFile(model.getTargetClassQualifiedName());
            try (Writer writer = source.openWriter();
                 JavaWriter javaWriter = new JavaWriter(writer)) {

                javaWriter.emitPackage(model.getTargetPackage());
                javaWriter.emitImports(model.getSourceQualifiedName(),
                        SignalPayload.class.getCanonicalName(),
                        NullEventListener.class.getCanonicalName(),
                        StateMachineEventListener.class.getCanonicalName(),
                        PayloadModifier.class.getCanonicalName());
                javaWriter.emitStaticImports(model.getSourceQualifiedName() + ".*");
                javaWriter.emitEmptyLine();

                emitClassJavaDoc(model, javaWriter);
                javaWriter.beginType(model.getTargetClassName(), "class", EnumSet.of(Modifier.PUBLIC), model.getSourceClassName());

                model.describeContents(javaWriter);

                emitFieldDeclarations(model, javaWriter);

                emitPassThroughConstructors(stateMachineDeclarationElement, model, processingEnv.getMessager(), javaWriter);

                generateInitMethod(model, javaWriter);

                emitSignalDispatcher(model, javaWriter);

                generateSignalHandlersForStates(model, javaWriter);

                generateSendMethods(model, javaWriter);
                generateSwitchStateMethod(model, javaWriter);

                // End class
                javaWriter.emitEmptyLine();
                javaWriter.endType();

                writer.close();
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Couldn't create generated state machine class: " + model.getTargetClassName());
            e.printStackTrace();
        }

    }

    private void emitClassJavaDoc(Model model, JavaWriter javaWriter) throws IOException {
        javaWriter.emitJavadoc("A state machine implementation of the declaration found in {@link %s %s}.\nNote that this class has been generated. Any changes will be overwritten.\n\nEdit {@link %s, %s} to change the state machine declaration.",
                model.getSourceClassName(), model.getSourceClassName(), model.getSourceClassName(), model.getSourceClassName());
    }


    private void emitGlobalSpecificSignalConnectionHandler(Model model, JavaWriter javaWriter) throws IOException {
        HashMap<String, ArrayList<ConnectionRef>>
                globalSpecificSignalTransitionsPerSignal = model.getGlobalSignalTransitionsPerSignal();
        if (globalSpecificSignalTransitionsPerSignal.size() == 0) {
            return;
        }

        for (Map.Entry<String, ArrayList<ConnectionRef>> connectionsForSignal : globalSpecificSignalTransitionsPerSignal.entrySet()) {
            javaWriter.beginControlFlow("if (signal.equals(" + model.getSignalsEnumName() + "." + connectionsForSignal.getKey() + "))");
            for (ConnectionRef connection : connectionsForSignal.getValue()) {
                javaWriter.emitStatement("if (%s(payload)) return %s", connection.getName(), model.getStatesEnumName() + "." + connection.getTo());
            }
            javaWriter.endControlFlow();
        }
    }

    private void emitGlobalAnySignalSpyHandler(Model model, JavaWriter javaWriter) throws IOException {
        ArrayList<ConnectionRef> globalAnySignalSpies = model.getGlobalAnySignalSpies();
        if (globalAnySignalSpies.size() == 0) {
            return;
        }
        javaWriter.emitEmptyLine();

        for (ConnectionRef connection : globalAnySignalSpies) {
            javaWriter.emitStatement("%s(payload)", connection.getName());
        }
    }

    private void emitGlobalSpecificSignalSpyHandler(Model model, JavaWriter javaWriter) throws IOException {
        HashMap<String, ArrayList<ConnectionRef>> globalSignalSpiesPerSignal = model.getGlobalSignalSpiesPerSignal();
        if (globalSignalSpiesPerSignal.size() == 0) {
            return;
        }
        javaWriter.emitEmptyLine();

        for (Map.Entry<String, ArrayList<ConnectionRef>> connectionsForSignal : globalSignalSpiesPerSignal.entrySet()) {
            javaWriter.beginControlFlow("if (signal.equals(" + model.getSignalsEnumName() + "." + connectionsForSignal.getKey() + "))");
            for (ConnectionRef connection : connectionsForSignal.getValue()) {
                javaWriter.emitStatement("%s(payload)", connection.getName());
            }
            javaWriter.endControlFlow();
        }
    }

    private void generateSendMethods(Model model, JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.beginMethod("void", "send", EnumSet.of(Modifier.PUBLIC), model.getSignalsEnumName(), "signal", "SignalPayload", "payload");

        javaWriter.beginControlFlow("if (mWaitingForInit)");
        javaWriter.emitStatement("throw new IllegalStateException(\"Missing call to init\")");
        javaWriter.endControlFlow();

        javaWriter.emitEmptyLine();
        javaWriter.beginControlFlow("if (payload == null)");
        javaWriter.emitStatement("payload = new SignalPayload()");
        javaWriter.endControlFlow();
        javaWriter.emitStatement("PayloadModifier.setSignalOnPayload(signal, payload)");

        javaWriter.emitEmptyLine();
        javaWriter.emitStatement(model.getStatesEnumName() + " nextState = dispatchSignal(signal, payload)");
        javaWriter.beginControlFlow("if (nextState != null)");
        javaWriter.emitStatement("switchState(nextState)");
        javaWriter.endControlFlow();
        javaWriter.endMethod();

        javaWriter.emitEmptyLine();
        javaWriter.beginMethod("void", "send", EnumSet.of(Modifier.PUBLIC), model.getSignalsEnumName(), "signal");
        javaWriter.emitStatement("send(signal, null)");
        javaWriter.endMethod();
    }

    private void generateSwitchStateMethod(Model model, JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.beginMethod("void", "switchState", EnumSet.of(Modifier.PRIVATE), model.getStatesEnumName(), "nextState");
        javaWriter.emitStatement("mEventListener.onChangingState(mCurrentState, nextState)");
        javaWriter.emitStatement("mCurrentState = nextState");
        javaWriter.endMethod();
    }

    private void generateSignalHandlersForStates(Model model, JavaWriter javaWriter) throws IOException {
        for (StateRef stateRef : model.getStates()) {
            generateSignalHandler(stateRef, model, javaWriter);
        }
    }

    private void generateSignalHandler(StateRef stateRef, Model model, JavaWriter javaWriter) throws IOException {
        ArrayList<ConnectionRef> anySignalConnectionsForState = model.getAnySignalTransitionsForState(stateRef);
        javaWriter.emitEmptyLine();
        javaWriter.beginMethod(model.getStatesEnumName(), "handleSignalIn" + camelCase(stateRef.getName()), EnumSet.of(Modifier.PRIVATE), model.getSignalsEnumName(), "signal", "SignalPayload", "payload");

        emitLocalSpecificSignalSpyHandler(stateRef, model, javaWriter);
        emitLocalAnySignalSpyHandler(stateRef, model, javaWriter);

        HashMap<String, ArrayList<ConnectionRef>> connectionsPerSignal = model.getLocalSignalTransitionsPerSignalForState(stateRef);
        if (connectionsPerSignal != null) {
            for (Map.Entry<String, ArrayList<ConnectionRef>> connectionsForSignalEntry : connectionsPerSignal.entrySet()) {
                javaWriter.beginControlFlow("if (signal.equals(" + model.getSignalsEnumName() + "." + connectionsForSignalEntry.getKey() + "))");
                for (ConnectionRef connectionForSignal : connectionsForSignalEntry.getValue()) {
                    javaWriter.emitStatement("if (%s(payload)) return %s", connectionForSignal.getName(), model.getStatesEnumName() + "." + connectionForSignal.getTo());
                }
                javaWriter.endControlFlow();
            }
            javaWriter.emitEmptyLine();
        }

        if (anySignalConnectionsForState != null) {
            javaWriter.emitEmptyLine();
            for (ConnectionRef connection : anySignalConnectionsForState) {
                javaWriter.emitStatement("if (%s(payload)) return %s", connection.getName(), model.getStatesEnumName() + "." + connection.getTo());
            }
        }

        javaWriter.emitStatement("return null");
        javaWriter.endMethod();
    }

    private void emitLocalAnySignalSpyHandler(StateRef stateRef, Model model, JavaWriter javaWriter) throws IOException {
        ArrayList<ConnectionRef> localAnySignalSpies = model.getLocalAnySignalSpiesForState(stateRef);
        if (localAnySignalSpies != null) {
            for (ConnectionRef connection : localAnySignalSpies) {
                javaWriter.emitStatement("%s(payload)", connection.getName());
            }
            javaWriter.emitEmptyLine();
        }
    }

    private void emitLocalSpecificSignalSpyHandler(StateRef stateRef, Model model, JavaWriter javaWriter) throws IOException {
        HashMap<String, ArrayList<ConnectionRef>> connectionsPerSignal = model.getLocalSignalSpiesPerSignalForState(stateRef);
        if (connectionsPerSignal != null) {
            for (Map.Entry<String, ArrayList<ConnectionRef>> connectionsForSignalEntry : connectionsPerSignal.entrySet()) {
                javaWriter.beginControlFlow("if (signal.equals(" + model.getSignalsEnumName() + "." + connectionsForSignalEntry.getKey() + "))");
                for (ConnectionRef connectionForSignal : connectionsForSignalEntry.getValue()) {
                    javaWriter.emitStatement("%s(payload)", connectionForSignal.getName());
                }
                javaWriter.endControlFlow();
            }
            javaWriter.emitEmptyLine();
        }
    }

    private void generateInitMethod(Model model, JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.beginMethod("void", "init", EnumSet.of(Modifier.PUBLIC), model.getStatesEnumName(), "startingState", StateMachineEventListener.class.getSimpleName(), "eventListener");
        javaWriter.emitStatement("mCurrentState = startingState");
        javaWriter.emitStatement("mEventListener = eventListener != null ? eventListener : new NullEventListener()");
        javaWriter.emitStatement("mWaitingForInit = false");
        javaWriter.endMethod();
    }

}
