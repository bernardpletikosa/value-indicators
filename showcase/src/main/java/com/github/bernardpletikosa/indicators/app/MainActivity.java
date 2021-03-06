package com.github.bernardpletikosa.indicators.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;

import com.github.bernardpletikosa.indicators.app.fragments.CircleFragment;
import com.github.bernardpletikosa.indicators.app.fragments.HalfPieFragment;
import com.github.bernardpletikosa.indicators.app.fragments.LineFragment;
import com.github.bernardpletikosa.indicators.app.fragments.PieFragment;
import com.github.bernardpletikosa.indicators.app.fragments.QuarterPieFragment;
import com.github.bernardpletikosa.indicators.app.fragments.TriangleFragment;

public class MainActivity extends ActionBarActivity
        implements DrawerFragment.NavigationDrawerCallbacks {

    private CharSequence mTitle;
    private DrawerFragment mNavigationDrawerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (DrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, getFragment(position)).commit();
    }

    private Fragment getFragment(int position) {
        switch (position) {
            case 0:
                return CircleFragment.newInstance();
            case 1:
                return LineFragment.newInstance();
            case 2:
                return PieFragment.newInstance();
            case 3:
                return HalfPieFragment.newInstance();
            case 4:
                return QuarterPieFragment.newInstance();
            case 5:
                return TriangleFragment.newInstance();
        }
        return null;
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 0:
                mTitle = getString(R.string.title_section1);
                break;
            case 1:
                mTitle = getString(R.string.title_section2);
                break;
            case 2:
                mTitle = getString(R.string.title_section3);
                break;
            case 3:
                mTitle = getString(R.string.title_section4);
                break;
            case 4:
                mTitle = getString(R.string.title_section5);
                break;
            case 5:
                mTitle = getString(R.string.title_section6);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            restoreActionBar();
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    private void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }
}
