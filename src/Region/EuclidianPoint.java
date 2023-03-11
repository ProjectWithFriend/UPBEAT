package Region;

public class EuclidianPoint implements Point {
    private final long x, y;

    public EuclidianPoint(long x, long y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public long getX() {
        return x;
    }

    @Override
    public long getY() {
        return y;
    }

    @Override
    public String toString() {
        return String.format("x: %d, y: %d", x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Point point))
            return false;
        return point.getX() == x && point.getY() == y;
    }

    @Override
    public int hashCode() {
        long tmp = (y + ((x + 1) / 2));
        return (int) (x + (tmp * tmp));
    }
}
