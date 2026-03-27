package com.swmschmidt.td.core.gameplay.map;

import com.swmschmidt.td.core.math.Vector3;

import java.util.ArrayList;
import java.util.List;

public final class MapPath {
    private final List<Vector3> waypoints;
    private final List<Double> segmentLengths;
    private final double totalLength;

    public MapPath(List<Vector3> waypoints) {
        if (waypoints == null || waypoints.size() < 2) {
            throw new IllegalArgumentException("Path requires at least two waypoints");
        }

        this.waypoints = List.copyOf(waypoints);
        this.segmentLengths = new ArrayList<>(waypoints.size() - 1);

        double computedLength = 0.0;
        for (int i = 0; i < waypoints.size() - 1; i++) {
            double length = waypoints.get(i + 1).subtract(waypoints.get(i)).length();
            if (length <= 0.0) {
                throw new IllegalArgumentException("Path segments must have positive length");
            }
            segmentLengths.add(length);
            computedLength += length;
        }
        this.totalLength = computedLength;
    }

    public List<Vector3> waypoints() {
        return waypoints;
    }

    public double totalLength() {
        return totalLength;
    }

    public Vector3 start() {
        return waypoints.getFirst();
    }

    public Vector3 end() {
        return waypoints.getLast();
    }

    public Vector3 sample(double distanceAlongPath) {
        if (distanceAlongPath <= 0.0) {
            return start();
        }
        if (distanceAlongPath >= totalLength) {
            return end();
        }

        double remaining = distanceAlongPath;
        for (int i = 0; i < segmentLengths.size(); i++) {
            double segmentLength = segmentLengths.get(i);
            if (remaining <= segmentLength) {
                Vector3 from = waypoints.get(i);
                Vector3 to = waypoints.get(i + 1);
                double t = remaining / segmentLength;
                return from.add(to.subtract(from).multiply(t));
            }
            remaining -= segmentLength;
        }

        return end();
    }
}
