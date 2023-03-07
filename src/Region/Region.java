package Region;

import Player.*;

public interface Region {
    boolean getIsCityCenter();
    Player getOwner();

    Point getLocation();
    long getDeposit();

    void updateDeposit(long amount);

    void updateOwner(Player owner);
    void setCityCenter(Player owner);

}
