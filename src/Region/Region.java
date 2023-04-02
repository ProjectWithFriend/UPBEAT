package Region;

import Player.*;

public interface Region {
    boolean getIsCityCenter();
    Player getOwner();

    void removeCityCenter();

    Point getLocation();
    long getDeposit();

    Player updateDeposit(long amount);

    /**
     * assign new owner to the region
     * @param owner new owner of region
     * @return old owner of that region
     */
    Player updateOwner(Player owner);
    void setCityCenter(Player owner);

}
