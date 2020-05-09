package uaic.fii.model;

import java.util.Objects;

public class Node {
    public int id;

    Node() {}

    Node(int id) {
        this.id = id;
    }

    public int getId() { return id; }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || ! getClass().isAssignableFrom(o.getClass())) return false;
        Node node = (Node) o;
        return id == node.id;
    }



    public Node deepClone() {
        return new Node(id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static class Location {
        double x, y;

        protected Location(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() { return x; }
        public double getY() { return y; }
        public void setX(double x) { this.x = x; }
        public void setY(double y) { this.y = y; }
    }

    public static class TimeWindow {
        double start, end;

        protected TimeWindow(double start, double end) {
            this.start = start;
            this.end = end;
        }

        public double getStart() {
            return start;
        }
        public double getEnd() {
            return end;
        }
    }
}