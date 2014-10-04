package org.interferometer.set.grid;

import org.interferometer.linear.Vector2;
import org.interferometer.set.linked.SetPathFinder;

public class GridPathFinder implements SetPathFinder<GridSet> {

    @Override
    public Vector2 getPoint(GridSet set) {
        // методом северо-западного угла:
        for(int i=0; i<set.getMaxXNumber(); ++i)
        for(int j=0; j<set.getMaxYNumber(); ++j)
            if(set.hasInt(i, j))
                return set.getArgument(i, j);
        return null;
    }

    @Override
    public void bypass(GridSet set, Vector2 begin) {
        // TODO Auto-generated method stub
        
    }
    
}