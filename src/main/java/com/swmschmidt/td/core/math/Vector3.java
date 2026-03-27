package com.swmschmidt.td.core.math;

public record Vector3(double x, double y, double z) {

    public static final Vector3 UP = new Vector3(0.0, 1.0, 0.0);

    public Vector3 add(Vector3 other) {
        return new Vector3(x + other.x, y + other.y, z + other.z);
    }

    public Vector3 subtract(Vector3 other) {
        return new Vector3(x - other.x, y - other.y, z - other.z);
    }

    public Vector3 multiply(double scalar) {
        return new Vector3(x * scalar, y * scalar, z * scalar);
    }

    public double dot(Vector3 other) {
        return x * other.x + y * other.y + z * other.z;
    }

    public Vector3 cross(Vector3 other) {
        return new Vector3(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        );
    }

    public double length() {
        return Math.sqrt(dot(this));
    }

    public Vector3 normalize() {
        double length = length();
        if (length == 0.0) {
            return this;
        }
        return multiply(1.0 / length);
    }
}
