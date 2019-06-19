/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package uschi2000.eventsourcingexperiments.events;

public interface GraphEvent {

    static NodeAdded nodeAdded(int nodeId, int seqId) {
        return new NodeAdded(nodeId, seqId);
    }

    static EdgeAdded edgeAdded(int from, int to, int seqId) {
        return new EdgeAdded(from, to, seqId);
    }

    interface Visitor {
        void visit(NodeAdded event);
        void visit(EdgeAdded event);
    }

    void accept(Visitor visitor);
    int seqId();

    final class NodeAdded implements GraphEvent {
        private final int nodeId;
        private final int seqId;

        private NodeAdded(int nodeId, int seqId) {
            this.nodeId = nodeId;
            this.seqId = seqId;
        }

        public int getNodeId() {
            return nodeId;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public int seqId() {
            return seqId;
        }
    }

    final class EdgeAdded implements GraphEvent {
        private final int from;
        private final int to;
        private final int seqId;

        private EdgeAdded(int from, int to, int seqId) {
            this.from = from;
            this.to = to;
            this.seqId = seqId;
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

        @Override
        public int seqId() {
            return seqId;
        }
    }
}
