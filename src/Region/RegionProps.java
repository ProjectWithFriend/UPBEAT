package Region;

import Player.*;

public class RegionProps implements Region {
    private final long maxDeposit;
    private boolean isCityCenter;
    private final Point location;
    private long deposit;
    private Player owner;

    public RegionProps(Point location, long maxDeposit) {
        this.maxDeposit = maxDeposit;
        this.isCityCenter = false;
        this.location = location;
        this.deposit = 0;
        this.owner = null;
    }

    @Override
    public boolean getIsCityCenter() {
        return isCityCenter;
    }

    @Override
    public Player getOwner() {
        return this.owner;
    }

    @Override
    public long getDeposit() {
        return deposit;
    }

    @Override
    public Player updateDeposit(long amount) {
        deposit = Math.max(0, amount + deposit);
        deposit = Math.min(maxDeposit, deposit);
        return deposit > 0 && owner != null ? null : updateOwner(null);
    }

    @Override
    public Player updateOwner(Player owner) {
        Player oldOwner = this.owner;
        this.owner = owner;
        return oldOwner;
    }

    @Override
    public void setCityCenter(Player owner) {
        this.isCityCenter = true;
        this.owner = owner;
    }

    @Override
    public void removeCityCenter() {
        isCityCenter = false;
    }

    @Override
    public Point getLocation() {
        return this.location;
    }

    @Override
    public String toString() {
        return String.format("owner: %s, location: %s", owner, location);
    }
}
