package Region;

import Game.*;

public interface Point {
    long getX();

    long getY();

    default Point direction(Direction direction) {
        boolean isEven = getX() % 2 == 0;
        long x = getX();
        long y = getY();
        return switch (direction) {
            case Up -> Point.of(x, y - 1);
            case UpRight -> isEven ? Point.of(x + 1, y) : Point.of(x + 1, y - 1);
            case DownRight -> isEven ? Point.of(x + 1, y + 1) : Point.of(x + 1, y);
            case Down -> Point.of(x, y + 1);
            case DownLeft -> isEven ? Point.of(x - 1, y + 1) : Point.of(x - 1, y);
            case UpLeft -> isEven ? Point.of(x - 1, y) : Point.of(x - 1, y - 1);
        };
    }

    default boolean isValidPoint(long rows, long cols) {
        return getX() >= 0 && getY() >= 0 && getY() < rows && getX() < cols;
    }

    static Point of(long x, long y) {
        return new EuclidianPoint(x, y);
    }
}
