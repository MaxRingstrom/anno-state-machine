package se.transientink.annostatemachine.processor;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jayway.annostatemachine.ConnectionRef;
import com.jayway.annostatemachine.StateRef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class ModelJsonExporter {

    private static final String GUARD_COLOR = "#ff0000";
    private static final String NO_GUARD_COLOR = "#000000";

    public static String getVisualizerJson(Model model) {

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        Representation rep = new Representation();
        rep.nodes = new ArrayList<>();
        rep.edges = new ArrayList<>();

        Node node;
        Edge edge;

        final int globalStateId = 0;

        int stateId = 1; // 0 is reserved for the GLOBAL state
        HashMap<String, Integer> stateIdMap = new HashMap<>();

        node = new Node();
        node.id = globalStateId;
        node.label = "GLOBAL";
        node.color = "#aaffff";
        rep.nodes.add(node);

        for (StateRef ref : model.getStates()) {
            node = new Node();
            node.id = stateId;
            node.label = ref.getName();
            stateIdMap.put(ref.getName(), stateId);
            stateId++;
            rep.nodes.add(node);
        }

        ArrayList<String> exportedConnections = new ArrayList<>();
        for (ArrayList<ConnectionRef> transitionsForState : model.getLocalSignalTransitions().values()) {
            for (ConnectionRef connection : transitionsForState) {
                if (exportedConnections.contains(connection.getName())) {
                    continue;
                }
                edge = new Edge();
                edge.from = stateIdMap.get(connection.getFrom());
                edge.to = stateIdMap.get(connection.getTo());
                edge.label = connection.getSignalsAsString() + " (" + connection.getName() + ")";
                edge.arrows = "to";
                edge.color = connection.hasGuard() ? GUARD_COLOR : NO_GUARD_COLOR;
                rep.edges.add(edge);
                exportedConnections.add(connection.getName());
            }
        }

        exportedConnections.clear();
        for (ArrayList<ConnectionRef> autoTransitionsForState : model.getAutoConnections().values()) {
            for (ConnectionRef connection : autoTransitionsForState) {
                if (exportedConnections.contains(connection.getName())) {
                    continue;
                }
                edge = new Edge();
                edge.from = stateIdMap.get(connection.getFrom());
                edge.to = stateIdMap.get(connection.getTo());
                edge.label = connection.getSignalsAsString() + " (" + connection.getName() + ")";
                edge.arrows = "to";
                edge.color = connection.hasGuard() ? GUARD_COLOR : NO_GUARD_COLOR;
                rep.edges.add(edge);
                exportedConnections.add(connection.getName());
            }
        }

        // Local any signal transitions
        exportedConnections.clear();
        ArrayList<ConnectionRef> connections;
        for (StateRef state : model.getStates()) {
            connections = model.getAnySignalTransitionsForState(state);
            if (connections == null || connections.size() == 0) {
                continue;
            }
            for (ConnectionRef connection : connections) {
                if (exportedConnections.contains(connection.getName())) {
                    continue;
                }
                edge = new Edge();
                edge.from = stateIdMap.get(connection.getFrom());
                edge.to = stateIdMap.get(connection.getTo());
                edge.label = connection.getSignalsAsString() + " (" + connection.getName() + ")";
                edge.arrows = "to";
                edge.color = connection.hasGuard() ? GUARD_COLOR : NO_GUARD_COLOR;
                rep.edges.add(edge);
                exportedConnections.add(connection.getName());
            }
        }

        // Global signal transitions
        exportedConnections.clear();
        Collection<ArrayList<ConnectionRef>> globalSignalTransitions = model.getGlobalSignalTransitionsPerSignal().values();
        for (ArrayList<ConnectionRef> transitions : globalSignalTransitions) {
            for (ConnectionRef connection : transitions) {
                if (exportedConnections.contains(connection.getName())) {
                    continue;
                }
                edge = new Edge();
                edge.from = globalStateId;
                edge.to = stateIdMap.get(connection.getTo());
                edge.label = connection.getSignalsAsString() + " (" + connection.getName() + ")";
                edge.arrows = "to";
                edge.color = connection.hasGuard() ? GUARD_COLOR : NO_GUARD_COLOR;
                rep.edges.add(edge);
                exportedConnections.add(connection.getName());
            }
        }

        // Global any signal transitions
        exportedConnections.clear();
        ArrayList<ConnectionRef> globalAnySignalTransitions = model.getGlobalAnySignalTransitions();
        for (ConnectionRef connection : globalAnySignalTransitions) {
            if (exportedConnections.contains(connection.getName())) {
                continue;
            }
            edge = new Edge();
            edge.from = globalStateId;
            edge.to = stateIdMap.get(connection.getTo());
            edge.label = connection.getSignalsAsString() + " (" + connection.getName() + ")";
            edge.arrows = "to";
            edge.color = connection.hasGuard() ? GUARD_COLOR : NO_GUARD_COLOR;
            rep.edges.add(edge);
            exportedConnections.add(connection.getName());
        }

        return gson.toJson(rep);
    }

    public static class Representation {
        ArrayList<Node> nodes;
        ArrayList<Edge> edges;
    }

    public static class Node {
        int id;
        String label;
        String color; // #ff0000 = red
    }

    public static class Edge {
        int from;
        int to;
        String arrows; // "to", "from"
        String label;
        String color; // #ff0000 = red
    }
}
