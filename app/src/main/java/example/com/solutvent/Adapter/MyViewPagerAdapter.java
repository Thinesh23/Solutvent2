package example.com.solutvent.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import example.com.solutvent.Fragments.BookingStep1Fragment;
import example.com.solutvent.Fragments.BookingStep2Fragment;

public class MyViewPagerAdapter extends FragmentPagerAdapter{

    public MyViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return BookingStep1Fragment.getInstance();
            case 1:
                return BookingStep2Fragment.getInstance();
        }

        return null;

    }


    @Override
    public int getCount() {
        return 2;
    }
}
