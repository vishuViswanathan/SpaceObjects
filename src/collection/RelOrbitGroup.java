package collection;

import javax.media.j3d.BranchGroup;

/**
 * Created by mviswanathan on 19-09-2019.
 */
public class RelOrbitGroup extends BranchGroup {
    boolean initiated = false;
    public boolean isInitiated() {
        return initiated;
    }

    public void setInitiated() {
        initiated = true;
    }
}
