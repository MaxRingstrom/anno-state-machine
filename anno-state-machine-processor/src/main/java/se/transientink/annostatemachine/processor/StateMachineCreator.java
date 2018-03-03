/*
 * Copyright 2017 Jayway (http://www.jayway.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.transientink.annostatemachine.processor;


import com.jayway.annostatemachine.Config;
import com.jayway.annostatemachine.ConnectionRef;
import com.jayway.annostatemachine.DispatchCallback;
import com.jayway.annostatemachine.MainThreadPoster;
import com.jayway.annostatemachine.NoOpMainThreadPoster;
import com.jayway.annostatemachine.NullEventListener;
import com.jayway.annostatemachine.OnEnterRef;
import com.jayway.annostatemachine.OnExitRef;
import com.jayway.annostatemachine.PayloadModifier;
import com.jayway.annostatemachine.SignalDispatcher;
import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.StateMachineEventListener;
import com.jayway.annostatemachine.StateMachineFront;
import com.jayway.annostatemachine.StateRef;
import com.jayway.annostatemachine.annotations.StateMachine;
import com.jayway.annostatemachine.dispatchers.BackgroundQueueDispatcher;
import com.jayway.annostatemachine.dispatchers.CallingThreadDispatcher;
import com.jayway.annostatemachine.dispatchers.SharedBackgroundQueueDispatcher;
import com.jayway.annostatemachine.utils.StateMachineLogger;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

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
            emitTransitionCall(model, connection, javaWriter);
        }
    }

    /**
     * Emits the field declarations that are needed for the rest of the state machine code.
     */
    void generateFieldDeclarations(Model model, JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.emitField(model.getStatesEnumName(), "mCurrentState", EnumSet.of(Modifier.PRIVATE));
        javaWriter.emitField("boolean", "mWaitingForInit", EnumSet.of(Modifier.PRIVATE), "true");
        javaWriter.emitField(StateMachineEventListener.class.getSimpleName(), "mEventListener", EnumSet.of(Modifier.PRIVATE));
        javaWriter.emitField(SignalDispatcher.class.getSimpleName(), "mSignalDispatcher", EnumSet.of(Modifier.PRIVATE));
        javaWriter.emitField(DispatchCallback.class.getSimpleName(), "mDispatchCallback", EnumSet.of(Modifier.PRIVATE));
        javaWriter.emitField(StateMachineLogger.class.getSimpleName(), "mLogger", EnumSet.of(Modifier.PRIVATE), "Config.get().getLogger()");
        javaWriter.emitField(MainThreadPoster.class.getSimpleName(), "mMainThreadPoster", EnumSet.of(Modifier.PRIVATE), "new NoOpMainThreadPoster()");
        javaWriter.emitField(AtomicBoolean.class.getSimpleName(), "mIsShutdown", EnumSet.of(Modifier.PRIVATE), "new AtomicBoolean(false)");
        javaWriter.emitField("int", "mSharedId");
    }

    void generatePassThroughConstructors(Element element, final Model model,
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
    void generateSignalDispatcher(Model model, JavaWriter javaWriter) throws IOException {

        javaWriter.emitEmptyLine();
        javaWriter.beginMethod(model.getStatesEnumName(), "dispatchSignal", EnumSet.of(Modifier.PRIVATE),
                "Enum", "signal", "final SignalPayload", "payload");

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
                        PayloadModifier.class.getCanonicalName(),
                        SignalDispatcher.class.getCanonicalName(),
                        DispatchCallback.class.getCanonicalName(),
                        StateMachineLogger.class.getCanonicalName(),
                        Config.class.getCanonicalName(),
                        NoOpMainThreadPoster.class.getCanonicalName(),
                        MainThreadPoster.class.getCanonicalName(),
                        Callable.class.getCanonicalName(),
                        CountDownLatch.class.getCanonicalName(),
                        AtomicBoolean.class.getCanonicalName(),
                        WeakReference.class.getCanonicalName(),
                        StateMachineFront.class.getCanonicalName());

                switch (model.getDispatchMode()) {
                    case BACKGROUND_QUEUE:
                        javaWriter.emitImports(BackgroundQueueDispatcher.class.getCanonicalName());
                        break;
                    case SHARED_BACKGROUND_QUEUE:
                        javaWriter.emitImports(SharedBackgroundQueueDispatcher.class.getCanonicalName());
                        break;
                    case CALLING_THREAD:
                        // Intentional fall-through
                    default:
                        javaWriter.emitImports(CallingThreadDispatcher.class.getCanonicalName());
                }

                javaWriter.emitEmptyLine();

                generateClassJavaDoc(model, javaWriter);
                javaWriter.beginType(model.getTargetClassName(), "class", EnumSet.of(Modifier.PUBLIC),
                        model.getSourceClassName(), "StateMachineFront<" + model.getSourceClassName() + "." + model.getSignalsEnumName() + ">");

                model.describeContents(javaWriter);

                generateFieldDeclarations(model, javaWriter);

                generatePassThroughConstructors(stateMachineDeclarationElement, model, processingEnv.getMessager(), javaWriter);

                generateInitMethods(model, javaWriter);

                generateSignalDispatcher(model, javaWriter);
                generateBlockingDispatchCallback(model, javaWriter);

                generateSignalHandlersForStates(model, javaWriter);

                generateSendMethods(model, javaWriter);
                generateSwitchStateMethod(model, javaWriter);

                generateHandleOnEnter(model, javaWriter);
                generateHandleOnExit(model, javaWriter);

                generateShutdownMethod(javaWriter);

                generateCallAutoConnections(model, javaWriter);

                generateCallAutoConnectionsForStateMethods(model, javaWriter);

                if (model.hasMainThreadConnections()) {
                    generateRunOnMainThreadMethod(model, writer);
                }

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

    private void generateHandleOnEnter(Model model, JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.beginMethod("void", "handleOnEnter", EnumSet.of(Modifier.PRIVATE), model.getStatesEnumName(), "state");
        javaWriter.beginControlFlow("switch (state)");

        for (Map.Entry<String, OnEnterRef> callbackForState : model.getOnEnterCallbacks().entrySet()) {
            if (callbackForState.getValue().getRunOnMainThread()) {
                javaWriter.emitStatement("case %s: callConnectionOnMainThread(new Callable<Boolean>() { public Boolean call() throws Exception {%s();return true;}}); break",
                        callbackForState.getKey(), callbackForState.getValue().getConnectionName());
            } else {
                javaWriter.emitStatement("case %s: %s(); break",
                    callbackForState.getKey(), callbackForState.getValue().getConnectionName());
            }
        }

        javaWriter.endControlFlow();

        javaWriter.endMethod();
    }

    private void generateHandleOnExit(Model model, JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.beginMethod("void", "handleOnExit", EnumSet.of(Modifier.PRIVATE), model.getStatesEnumName(), "state");
        javaWriter.beginControlFlow("switch (state)");

        for (Map.Entry<String, OnExitRef> callbackForState : model.getOnExitCallbacks().entrySet()) {
            if (callbackForState.getValue().getRunOnMainThread()) {
                javaWriter.emitStatement("case %s: callConnectionOnMainThread(new Callable<Boolean>() { public Boolean call() throws Exception {%s();return true;}}); break",
                        callbackForState.getKey(), callbackForState.getValue().getConnectionName());
            } else {
                javaWriter.emitStatement("case %s: %s(); break",
                        callbackForState.getKey(), callbackForState.getValue().getConnectionName());
            }
        }

        javaWriter.endControlFlow();

        javaWriter.endMethod();
    }

    private void generateShutdownMethod(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.beginMethod("void", "shutDown", EnumSet.of(Modifier.PUBLIC));
        javaWriter.emitStatement("mIsShutdown.set(true)");
        javaWriter.emitStatement("mSignalDispatcher.shutDown()");
        javaWriter.endMethod();
    }

    private void generateClassJavaDoc(Model model, JavaWriter javaWriter) throws IOException {
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
                emitTransitionCall(model, connection, javaWriter);
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
            emitSpyCall(connection, javaWriter);
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
                emitSpyCall(connection, javaWriter);
            }
            javaWriter.endControlFlow();
        }
    }

    private void emitSpyCall(ConnectionRef connection, JavaWriter javaWriter) throws IOException {
        if (connection.getRunOnMainThread()) {
            javaWriter.emitStatement("callConnectionOnMainThread(new Callable<Boolean>() {" +
                    " public Boolean call() throws Exception { return %s(payload); }})", connection.getName());
        } else {
            javaWriter.emitStatement("%s(payload)", connection.getName());
        }
    }

    private void emitTransitionCall(Model model, ConnectionRef connection, JavaWriter javaWriter) throws IOException {
        if (connection.getRunOnMainThread()) {
            if (connection.hasGuard()) {
                javaWriter.beginControlFlow(
                        "if (callConnectionOnMainThread(new Callable<Boolean>() { public Boolean call() throws Exception {" +
                                " return " + connection.getName() + "(payload); }}))");
                javaWriter.emitStatement("return " + model.getStatesEnumName() + ".%s", connection.getTo());
                javaWriter.endControlFlow();
            } else {
                javaWriter.emitStatement(
                        "callConnectionOnMainThread(new Callable<Boolean>() { public Boolean call() throws Exception {" +
                                connection.getName() + "(payload); return true; }})");
                javaWriter.emitStatement("return " + model.getStatesEnumName() + ".%s", connection.getTo());
            }
        } else {
            if (connection.hasGuard()) {
                javaWriter.emitStatement("if(%s(payload)) return %s.%s", connection.getName(), model.getStatesEnumName(), connection.getTo());
            } else {
                javaWriter.emitStatement("%s(payload)", connection.getName());
                javaWriter.emitStatement("return %s.%s", model.getStatesEnumName(), connection.getTo());
            }
        }
    }

    private void generateSendMethods(Model model, JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.emitAnnotation(Override.class);
        javaWriter.beginMethod("void", "send", EnumSet.of(Modifier.PUBLIC, Modifier.SYNCHRONIZED), model.getSignalsEnumName(), "signal", "SignalPayload", "payload");

        javaWriter.beginControlFlow("if (mWaitingForInit)");
        javaWriter.emitStatement("throw new IllegalStateException(\"Missing call to init\")");
        javaWriter.endControlFlow();

        javaWriter.beginControlFlow("if (mIsShutdown.get())");
        javaWriter.emitStatement("mLogger.e(\"%s\", \"Send called after shut down\", new Exception(\"Ignoring signal \" + signal + \" - state machine has been shut down\"))", model.getTargetClassName());
        javaWriter.emitStatement("return");
        javaWriter.endControlFlow();

        javaWriter.emitEmptyLine();
        javaWriter.beginControlFlow("if (payload == null)");
        javaWriter.emitStatement("payload = new SignalPayload()");
        javaWriter.endControlFlow();
        javaWriter.emitStatement("PayloadModifier.setSignalOnPayload(signal, payload)");

        javaWriter.emitStatement("mSignalDispatcher.dispatch(signal, payload, mDispatchCallback, mLogger)");
        javaWriter.endMethod();

        javaWriter.emitEmptyLine();
        javaWriter.emitAnnotation(Override.class);
        javaWriter.beginMethod("void", "send", EnumSet.of(Modifier.PUBLIC), model.getSignalsEnumName(), "signal");
        javaWriter.emitStatement("send(signal, null)");
        javaWriter.endMethod();
    }

    void generateBlockingDispatchCallback(Model model, JavaWriter javaWriter) throws IOException {

        javaWriter.beginType("MachineCallback", "class", EnumSet.of(Modifier.PRIVATE, Modifier.STATIC),
                null,
                DispatchCallback.class.getSimpleName());

        javaWriter.emitEmptyLine();
        javaWriter.emitField(StateMachineLogger.class.getSimpleName(), "mLogger", EnumSet.of(Modifier.PRIVATE, Modifier.FINAL));
        javaWriter.emitField("WeakReference<" + model.getTargetClassName() + ">", "mStateMachineRef", EnumSet.of(Modifier.PRIVATE, Modifier.FINAL));

        javaWriter.emitEmptyLine();
        javaWriter.beginMethod(null, "MachineCallback", EnumSet.of(Modifier.PUBLIC), model.getTargetClassName(), "machine", StateMachineLogger.class.getSimpleName(), "logger");
        javaWriter.emitStatement("mStateMachineRef = new WeakReference<>(machine)");
        javaWriter.emitStatement("mLogger = logger");
        javaWriter.endMethod();

        javaWriter.emitEmptyLine();
        javaWriter.beginMethod("void", "dispatchBlocking", EnumSet.of(Modifier.PUBLIC),
                "Enum", "signal", "SignalPayload", "payload");
        javaWriter.beginControlFlow("try");

        javaWriter.emitStatement(model.getTargetClassName() + " machine = mStateMachineRef.get()");
        javaWriter.beginControlFlow("if (machine == null || machine.mIsShutdown.get())");
        javaWriter.emitStatement("mLogger.w(\"" + model.getTargetClassName() + "\", \"State machine is garbage collected or shut down - not calling dispatch on \" + signal);");
        javaWriter.emitStatement("return");
        javaWriter.endControlFlow();

        javaWriter.emitEmptyLine();
        javaWriter.emitStatement(model.getStatesEnumName() + " nextState = machine.dispatchSignal(signal, payload)");
        javaWriter.beginControlFlow("if (nextState != null)");
        javaWriter.emitStatement("machine.switchState(nextState)");
        javaWriter.endControlFlow();
        javaWriter.nextControlFlow("catch (Throwable t)");
        javaWriter.emitStatement("mLogger.e(\"%s\", \"Error when dispatching signal\", t)", model.getTargetClassName());
        javaWriter.endControlFlow();
        javaWriter.endMethod();

        javaWriter.endType();
    }

    private void generateSwitchStateMethod(Model model, JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.beginMethod("void", "switchState", EnumSet.of(Modifier.PRIVATE), model.getStatesEnumName(), "nextState");

        javaWriter.emitStatement("mEventListener.onChangingState(mCurrentState, nextState)");

        javaWriter.beginControlFlow("if (mCurrentState != null)");
        javaWriter.emitStatement("handleOnExit(mCurrentState)");
        javaWriter.endControlFlow();

        javaWriter.emitStatement("mCurrentState = nextState");

        javaWriter.emitStatement("handleOnEnter(nextState)");

        javaWriter.emitStatement(model.getStatesEnumName() + " nextStateFromAutoConnection = callAutoConnections(nextState)");
        javaWriter.beginControlFlow("if (nextStateFromAutoConnection != null)");
        javaWriter.emitStatement("switchState(nextStateFromAutoConnection)");
        javaWriter.endControlFlow();

        javaWriter.endMethod();
    }

    private void generateSignalHandlersForStates(Model model, JavaWriter javaWriter) throws IOException {
        for (StateRef stateRef : model.getStates()) {
            generateSignalHandler(stateRef, model, javaWriter);
        }
    }

    private void generateCallAutoConnections(Model model, JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.beginMethod(model.getStatesEnumName(), "callAutoConnections", EnumSet.of(Modifier.PRIVATE), model.getStatesEnumName(), "state");
        javaWriter.emitStatement("SignalPayload<" + model.getSignalsEnumName() + "> payload = new SignalPayload<>()");
        javaWriter.beginControlFlow("switch (state)");

        for (Map.Entry<String, ArrayList<ConnectionRef>> autoConnectionsForState : model.getAutoConnections().entrySet()) {
                javaWriter.emitStatement("case %s: return callAutoConnectionsFor%s(payload)",
                        autoConnectionsForState.getKey(), autoConnectionsForState.getKey());
        }

        javaWriter.endControlFlow();

        javaWriter.emitStatement("return null");

        javaWriter.endMethod();
    }

    private void generateCallAutoConnectionsForStateMethods(Model model, JavaWriter javaWriter) throws IOException {
        for (Map.Entry<String, ArrayList<ConnectionRef>> entry : model.getAutoConnections().entrySet()) {
            javaWriter.emitEmptyLine();
            javaWriter.beginMethod(model.getStatesEnumName(), "callAutoConnectionsFor" + entry.getKey(),
                    EnumSet.of(Modifier.PRIVATE), "SignalPayload<" + model.getSignalsEnumName() + ">", "payload");
            for (ConnectionRef connection : entry.getValue()) {
                emitTransitionCall(model, connection, javaWriter);
            }
            javaWriter.emitStatement("return null");
            javaWriter.endMethod();
        }
    }

    private void generateSignalHandler(StateRef stateRef, Model model, JavaWriter javaWriter) throws IOException {
        ArrayList<ConnectionRef> anySignalConnectionsForState = model.getAnySignalTransitionsForState(stateRef);
        javaWriter.emitEmptyLine();
        javaWriter.beginMethod(model.getStatesEnumName(), "handleSignalIn" + camelCase(stateRef.getName()), EnumSet.of(Modifier.PRIVATE), "Enum", "signal", "final SignalPayload", "payload");

        emitLocalSpecificSignalSpyHandler(stateRef, model, javaWriter);
        emitLocalAnySignalSpyHandler(stateRef, model, javaWriter);

        HashMap<String, ArrayList<ConnectionRef>> connectionsPerSignal = model.getLocalSignalTransitionsPerSignalForState(stateRef);
        if (connectionsPerSignal != null) {
            for (Map.Entry<String, ArrayList<ConnectionRef>> connectionsForSignalEntry : connectionsPerSignal.entrySet()) {
                javaWriter.beginControlFlow("if (signal.equals(" + model.getSignalsEnumName() + "." + connectionsForSignalEntry.getKey() + "))");
                for (ConnectionRef connectionForSignal : connectionsForSignalEntry.getValue()) {
                    emitTransitionCall(model, connectionForSignal, javaWriter);
                }
                javaWriter.endControlFlow();
            }
            javaWriter.emitEmptyLine();
        }

        if (anySignalConnectionsForState != null) {
            javaWriter.emitEmptyLine();
            for (ConnectionRef connection : anySignalConnectionsForState) {
                emitTransitionCall(model, connection, javaWriter);
            }
        }

        javaWriter.emitStatement("return null");
        javaWriter.endMethod();
    }

    private void emitLocalAnySignalSpyHandler(StateRef stateRef, Model model, JavaWriter javaWriter) throws IOException {
        ArrayList<ConnectionRef> localAnySignalSpies = model.getLocalAnySignalSpiesForState(stateRef);
        if (localAnySignalSpies != null) {
            for (ConnectionRef connection : localAnySignalSpies) {
                emitSpyCall(connection, javaWriter);
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
                    emitSpyCall(connectionForSignal, javaWriter);
                }
                javaWriter.endControlFlow();
            }
            javaWriter.emitEmptyLine();
        }
    }

    private void generateInitMethods(Model model, JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.beginMethod("void", "init", EnumSet.of(Modifier.PUBLIC), model.getStatesEnumName(), "startingState", StateMachineEventListener.class.getSimpleName(), "eventListener", MainThreadPoster.class.getSimpleName(), "mainThreadPoster");

        javaWriter.emitStatement("mMainThreadPoster = mainThreadPoster != null ? mainThreadPoster : new NoOpMainThreadPoster()");
        javaWriter.emitStatement("mDispatchCallback = new MachineCallback(this, mLogger)");
        javaWriter.emitStatement("mSharedId = " + model.getDispatchQueueId());

        String dispatchConstructorCall;
        switch (model.getDispatchMode()) {
            case BACKGROUND_QUEUE:
                dispatchConstructorCall = "BackgroundQueueDispatcher()";
                break;
            case SHARED_BACKGROUND_QUEUE:
                dispatchConstructorCall = "SharedBackgroundQueueDispatcher(mSharedId)";
                break;
            case CALLING_THREAD:
                // Intentional fall-through
            default:
                dispatchConstructorCall = "CallingThreadDispatcher()";

        }
        javaWriter.emitStatement("mSignalDispatcher = new " + dispatchConstructorCall);
        javaWriter.emitStatement("mEventListener = eventListener != null ? eventListener : new NullEventListener()");
        javaWriter.emitStatement("mWaitingForInit = false");
        if (model.hasMainThreadConnections() && (model.getDispatchMode() != StateMachine.DispatchMode.CALLING_THREAD)) {
            // If the state machine calls connection on the main thread there's a possibility that one
            // such connection, via auto transitions, will be called as a result of init being called. We therefore need to
            // run the switchState method on the dispatchers thread.
            javaWriter.emitStatement("mSignalDispatcher.runOnDispatchThread(new Runnable() { public void run() { switchState(startingState); }}, mLogger)");
        } else {
            javaWriter.emitStatement("switchState(startingState)");
        }
        javaWriter.endMethod();

        // If the state machine has at least one connection that wants the connection method to
        // be called on the ui thread, we force the user to specify a MainThreadPoster
        if (!model.hasMainThreadConnections()) {
            javaWriter.emitEmptyLine();
            javaWriter.beginMethod("void", "init", EnumSet.of(Modifier.PUBLIC), model.getStatesEnumName(), "startingState", StateMachineEventListener.class.getSimpleName(), "eventListener");
            javaWriter.emitStatement("init(startingState, eventListener, null)");
            javaWriter.endMethod();

            javaWriter.emitEmptyLine();
            javaWriter.beginMethod("void", "init", EnumSet.of(Modifier.PUBLIC), model.getStatesEnumName(), "startingState");
            javaWriter.emitStatement("init(startingState, null, null)");
            javaWriter.endMethod();
        }

        javaWriter.emitEmptyLine();
        javaWriter.beginMethod("void", "init", EnumSet.of(Modifier.PUBLIC), model.getStatesEnumName(), "startingState", MainThreadPoster.class.getSimpleName(), "mainThreadPoster");
        javaWriter.emitStatement("init(startingState, null, mainThreadPoster)");
        javaWriter.endMethod();
    }

    private void generateRunOnMainThreadMethod(Model model, Writer writer) throws IOException {
        writer.append("  private boolean callConnectionOnMainThread(final Callable<Boolean> callable) {\n" +
                        "    final CountDownLatch latch = new CountDownLatch(1);\n" +
                        "    final AtomicBoolean guardSatisfied = new AtomicBoolean();\n" +
                        "    mMainThreadPoster.runOnMainThread(new Runnable() { public void run() {;\n" +
                        "      try {\n" +
                        "        guardSatisfied.set(callable.call());\n" +
                        "      } catch (Throwable t) {\n" +
                        "        mLogger.e(\"" + model.getTargetClassName() + "\", \"Error when running connection method\", t);\n" +
                        "      }\n" +
                        "      latch.countDown();\n" +
                        "    }});\n" +
                        "    try {\n" +
                        "      latch.await();\n" +
                        "    } catch (InterruptedException e) {\n"+
                        "      mLogger.e(\"" + model.getTargetClassName() + "\", \"Thread interrupted when running connection method\", e);\n" +
                        "    }\n"+
                        "    return guardSatisfied.get();\n" +
                        "  }");
    }

}
