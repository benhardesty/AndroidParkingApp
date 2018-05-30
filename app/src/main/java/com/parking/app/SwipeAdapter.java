/*
 * Copyright © 2017 Benjamin Hardesty. All Rights Reserved.
*/

package com.parking.app;

import android.support.v4.view.PagerAdapter;
import android.view.View;

public class SwipeAdapter extends PagerAdapter {
    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return false;
    }
}
