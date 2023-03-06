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
    public void updateDeposit(long amount) {
        deposit = Math.max(0, amount + deposit);
        deposit = Math.min(maxDeposit, deposit);
    }

    @Override
    public void updateOwner(Player owner) {
        this.owner = owner;
    }

    @Override
    public void setCityCenter(Player owner) {
        isCityCenter = true;
        updateOwner(owner);
    }

    @Override
    public Point getLocation() {
        return this.location;
    }
}
