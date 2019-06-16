/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.eventsourcingexperiments.events;

public interface GraphEvent {

    public static NodeAdded nodeAdded(int nodeId) {
        return new NodeAdded(nodeId);
    }

    public static EdgeAdded edgeAdded(int from, int to) {
        return new EdgeAdded(from, to);
    }

    interface Visitor {
        void visit(NodeAdded event);
        void visit(EdgeAdded event);
    }

    void accept(Visitor visitor);

    final class NodeAdded implements GraphEvent {
        private final int nodeId;

        private NodeAdded(int nodeId) {
            this.nodeId = nodeId;
        }

        public int getNodeId() {
            return nodeId;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }
    }

    final class EdgeAdded implements GraphEvent {
        private final int from;
        private final int to;

        private EdgeAdded(int from, int to) {
            this.from = from;
            this.to = to;
        }

        public int getFrom() {
            return from;
        }

        public int getTo() {
            return to;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }
    }
}
