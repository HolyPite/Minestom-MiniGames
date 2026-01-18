package me.holypite.games.elytra;

import net.minestom.server.coordinate.Pos;

public record Checkpoint(int id, double x, double y, double z, double radius, String axis) {
    
    public Pos position() {
        return new Pos(x, y, z);
    }

    /**
     * Precise detection: checks if the player passed through the ring's plane 
     * within the radius.
     */
    public boolean isPassed(Pos oldPos, Pos newPos) {
        double ringCoord = getCoord(position(), axis);
        double oldCoord = getCoord(oldPos, axis);
        double newCoord = getCoord(newPos, axis);

        // Check if player crossed the plane
        if ((oldCoord < ringCoord && newCoord >= ringCoord) || (oldCoord > ringCoord && newCoord <= ringCoord)) {
            // Calculate intersection point on the plane
            double t = (ringCoord - oldCoord) / (newCoord - oldCoord);
            double intersectA = getOtherCoordA(oldPos, axis) + t * (getOtherCoordA(newPos, axis) - getOtherCoordA(oldPos, axis));
            double intersectB = getOtherCoordB(oldPos, axis) + t * (getOtherCoordB(newPos, axis) - getOtherCoordB(oldPos, axis));

            // Check if intersection point is within radius from ring center
            double distSq = Math.pow(intersectA - getOtherCoordA(position(), axis), 2) + 
                           Math.pow(intersectB - getOtherCoordB(position(), axis), 2);
            
            return distSq <= radius * radius;
        }
        
        return false;
    }

    private double getCoord(Pos pos, String axis) {
        return switch (axis.toUpperCase()) {
            case "X" -> pos.x();
            case "Y" -> pos.y();
            default -> pos.z();
        };
    }

    private double getOtherCoordA(Pos pos, String axis) {
        return switch (axis.toUpperCase()) {
            case "X" -> pos.y();
            case "Y" -> pos.x();
            default -> pos.x();
        };
    }

    private double getOtherCoordB(Pos pos, String axis) {
        return switch (axis.toUpperCase()) {
            case "X" -> pos.z();
            case "Y" -> pos.z();
            default -> pos.y();
        };
    }
}
