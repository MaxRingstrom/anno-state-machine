package com.jayway.annostatemachine.processor;

import com.jayway.annostatemachine.ConnectionRef;
import com.jayway.annostatemachine.SignalRef;
import com.jayway.annostatemachine.StateRef;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Messager;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

class Model {

        private ArrayList<SignalRef> mSignals = new ArrayList<>();
        private HashMap<String, ArrayList<ConnectionRef>> mLocalSignalTransitions = new HashMap<>();
        private HashMap<String, ArrayList<ConnectionRef>> mLocalAnySignalSpies = new HashMap<>();
        private HashMap<String, ArrayList<ConnectionRef>> mLocalSignalSpies = new HashMap<>();
        private HashMap<String, ArrayList<ConnectionRef>> mLocalAnySignalTransitions = new HashMap<>();
        private ArrayList<ConnectionRef> mGlobalSignalSpies = new ArrayList<>();
        private ArrayList<ConnectionRef> mGlobalAnySignalSpies = new ArrayList<>();
        private ArrayList<ConnectionRef> mGlobalSignalTransitions = new ArrayList<>();
        private ArrayList<ConnectionRef> mGlobalAnySignalTransitions = new ArrayList<>();

        private ArrayList<StateRef> mStates = new ArrayList<>();

        // Aggregated info
        private HashMap<String, HashMap<String, ArrayList<ConnectionRef>>> mLocalSignalTransitionsPerSignalPerState = new HashMap<>();
        private HashMap<String, HashMap<String, ArrayList<ConnectionRef>>> mLocalSignalSpiesPerSignalPerState = new HashMap<>();
        private HashMap<String, ArrayList<ConnectionRef>> mGlobalSignalSpiesPerSignal = new HashMap<>();
        private HashMap<String, ArrayList<ConnectionRef>> mGlobalSignalTransitionsPerSignal = new HashMap<>();

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
                        addGlobalAnySignalSpy(connection);
                    } else {
                        // Specific signal
                        addGlobalSignalSpy(connection);
                    }
                } else {
                    // Normal
                    if (hasWildcardSignal) {
                        // Any signal
                        addGlobalAnySignalTransition(connection);
                    } else {
                        // Specific signal
                        addGlobalSignalTransition(connection);
                    }
                }
            } else {
                // Local
                if (hasWildcardTo) {
                    // Eavesdrop
                    if (hasWildcardSignal) {
                        // Any signal
                        addLocalAnySignalSpy(connection);
                    } else {
                        // Specific signal
                        addLocalSignalSpy(connection);
                    }
                } else {
                    // Normal
                    if (hasWildcardSignal) {
                        // Any signal
                        addLocalAnySignalTransition(connection);
                    } else {
                        // Specific signal
                        addLocalSignalTransition(connection);
                    }
                }
            }
        }

        private void addLocalAnySignalTransition(ConnectionRef connection) {
            ArrayList<ConnectionRef> connections = mLocalAnySignalTransitions.get(connection.getFrom());
            if (connections == null) {
                connections = new ArrayList<>();
            }
            connections.add(connection);
            mLocalAnySignalTransitions.put(connection.getFrom(), connections);
        }

        private void addLocalSignalSpy(ConnectionRef connection) {
            ArrayList<ConnectionRef> connections = mLocalSignalSpies.get(connection.getFrom());
            if (connections == null) {
                connections = new ArrayList<>();
            }
            connections.add(connection);
            mLocalSignalSpies.put(connection.getFrom(), connections);
        }

        private void addLocalAnySignalSpy(ConnectionRef connection) {
            ArrayList<ConnectionRef> connections = mLocalAnySignalSpies.get(connection.getFrom());
            if (connections == null) {
                connections = new ArrayList<>();
            }
            connections.add(connection);
            mLocalAnySignalSpies.put(connection.getFrom(), connections);
        }

        private void addGlobalSignalTransition(ConnectionRef connection) {
            mGlobalSignalTransitions.add(connection);
        }

        private void addGlobalAnySignalTransition(ConnectionRef connection) {
            mGlobalAnySignalTransitions.add(connection);
        }

        private void addGlobalAnySignalSpy(ConnectionRef connection) {
            mGlobalAnySignalSpies.add(connection);
        }

        private void addGlobalSignalSpy(ConnectionRef connection) {
            mGlobalSignalSpies.add(connection);
        }

        private void addLocalSignalTransition(ConnectionRef connection) {
            ArrayList<ConnectionRef> connectionsForFromState = mLocalSignalTransitions.get(connection.getFrom());
            if (connectionsForFromState == null) {
                connectionsForFromState = new ArrayList<>();
            }
            connectionsForFromState.add(connection);
            mLocalSignalTransitions.put(connection.getFrom(), connectionsForFromState);
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
            for (Map.Entry<String, ArrayList<ConnectionRef>> connectionEntry : mLocalSignalTransitions.entrySet()) {
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
            aggregateLocalSignalTransitionsPerSignalPerState();
            aggregateGlobalSpiesPerSignal();
            aggregateLocalSpiesPerSignal();
            aggregateGlobalSignalTransitionsPerSignal();
        }

        private void aggregateGlobalSignalTransitionsPerSignal() {
            for (ConnectionRef globalSpecificConnection : mGlobalSignalTransitions) {
                ArrayList<ConnectionRef> connections =
                        mGlobalSignalTransitionsPerSignal.get(globalSpecificConnection.getSignal());
                if (connections == null) {
                    connections = new ArrayList<>();
                }
                connections.add(globalSpecificConnection);
                mGlobalSignalTransitionsPerSignal.put(globalSpecificConnection.getSignal(), connections);
            }
        }

        private void aggregateLocalSpiesPerSignal() {
            // Loop over all states and check their local spy connections
            for (Map.Entry<String, ArrayList<ConnectionRef>> spiesPerState : mLocalSignalSpies.entrySet()) {
                // Key is the state name
                // Value is the local signal spy for the state
                String stateName = spiesPerState.getKey();
                ArrayList<ConnectionRef> spiesForState = spiesPerState.getValue();
                HashMap<String, ArrayList<ConnectionRef>> spiesPerSignalInState = new HashMap<>();

                // Group the connections for the state by signal
                for (ConnectionRef connection: spiesForState) {
                    ArrayList<ConnectionRef> connections = spiesPerSignalInState.get(connection.getSignal());
                    if (connections == null) {
                        connections = new ArrayList<>();
                    }
                    connections.add(connection);
                    spiesPerSignalInState.put(connection.getSignal(), connections);
                }
                mLocalSignalSpiesPerSignalPerState.put(stateName, spiesPerSignalInState);
            }
        }

        private void aggregateGlobalSpiesPerSignal() {
            for (ConnectionRef globalSpecificSpy : mGlobalSignalSpies) {
                ArrayList<ConnectionRef> connections =
                        mGlobalSignalSpiesPerSignal.get(globalSpecificSpy.getSignal());
                if (connections == null) {
                    connections = new ArrayList<>();
                }
                connections.add(globalSpecificSpy);
                mGlobalSignalSpiesPerSignal.put(globalSpecificSpy.getSignal(), connections);
            }
        }

        private void aggregateLocalSignalTransitionsPerSignalPerState() {
            // Loop over all states and check their local signal connections
            for (Map.Entry<String, ArrayList<ConnectionRef>> connectionsPerState : mLocalSignalTransitions.entrySet()) {
                // Key is the state name
                // Value is the local signal connections for the state
                String stateName = connectionsPerState.getKey();
                ArrayList<ConnectionRef> connectionsForState = connectionsPerState.getValue();
                HashMap<String, ArrayList<ConnectionRef>> connectionsPerSignalInState = new HashMap<>();

                // Group the connections for the state by signal
                for (ConnectionRef connection: connectionsForState) {
                    ArrayList<ConnectionRef> connections = connectionsPerSignalInState.get(connection.getSignal());
                    if (connections == null) {
                        connections = new ArrayList<>();
                    }
                    connections.add(connection);
                    connectionsPerSignalInState.put(connection.getSignal(), connections);
                }
                mLocalSignalTransitionsPerSignalPerState.put(stateName, connectionsPerSignalInState);
            }
        }

    public List<ConnectionRef> getGlobalAnySignalTransitions() {
        return mGlobalAnySignalTransitions;
    }

    public HashMap<String, ArrayList<ConnectionRef>> getGlobalSignalTransitionsPerSignal() {
        return mGlobalSignalTransitionsPerSignal;
    }

    public ArrayList<ConnectionRef> getGlobalAnySignalSpies() {
        return mGlobalAnySignalSpies;
    }

    public HashMap<String, ArrayList<ConnectionRef>> getGlobalSignalSpiesPerSignal() {
        return mGlobalSignalSpiesPerSignal;
    }

    public HashMap<String, ArrayList<ConnectionRef>> getLocalSignalTransitions() {
        return mLocalSignalTransitions;
    }

    void validateModel(String errorTag, Messager messager) {
        for (Map.Entry<String, ArrayList<ConnectionRef>> entry : mLocalSignalTransitions.entrySet()) {
            if (entry.getValue() != null) {
                for (ConnectionRef connectionRef : entry.getValue()) {
                    if (!mStates.contains(new StateRef(connectionRef.getFrom()))) {
                        messager.printMessage(Diagnostic.Kind.ERROR, errorTag + " - Unknown FROM state "
                                + connectionRef.getFrom() + " used in connection " + connectionRef.getName() + ". Do you have a typo?");
                    }
                    if (!connectionRef.getTo().equals(ConnectionRef.WILDCARD) && !mStates.contains(new StateRef(connectionRef.getTo()))) {
                        messager.printMessage(Diagnostic.Kind.ERROR, errorTag + " - Unknown TO state "
                                + connectionRef.getTo() + " used in connection " + connectionRef.getName() + ". Do you have a typo?");
                    }
                    if (!mSignals.contains(new SignalRef(connectionRef.getSignal()))) {
                        messager.printMessage(Diagnostic.Kind.ERROR, errorTag + " - Unknown SIGNAL "
                                + connectionRef.getSignal() + " used in connection " + connectionRef.getName() + ". Do you have a typo?");
                    }
                }
            }
        }
    }

    public ArrayList<StateRef> getStates() {
        return mStates;
    }

    public ArrayList<ConnectionRef> getAnySignalTransitionsForState(StateRef stateRef) {
        return mLocalAnySignalTransitions.get(stateRef.getName());
    }

    public HashMap<String, ArrayList<ConnectionRef>> getLocalSignalTransitionsPerSignalForState(StateRef stateRef) {
        return mLocalSignalTransitionsPerSignalPerState.get(stateRef.getName());
    }

    public ArrayList<ConnectionRef> getLocalAnySignalSpiesForState(StateRef stateRef) {
        return mLocalAnySignalSpies.get(stateRef.getName());
    }

    public HashMap<String, ArrayList<ConnectionRef>> getLocalSignalSpiesPerSignalForState(StateRef stateRef) {
        return mLocalSignalSpiesPerSignalPerState.get(stateRef.getName());
    }
}
