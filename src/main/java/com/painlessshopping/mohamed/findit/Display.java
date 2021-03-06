package com.painlessshopping.mohamed.findit;

import android.content.Intent;
import android.os.Bundle;
import androidx.core.view.GravityCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import com.google.android.material.navigation.NavigationView;
import com.painlessshopping.mohamed.findit.viewmodel.ThemeAdapter;
import com.painlessshopping.mohamed.findit.viewmodel.ThemeHandler;

import android.view.Window;

/**
 * Controls buttons and formatting for the Display Settings screen
 */

public class Display extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Window window = getWindow();
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeHandler.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        //Initializes toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Navigation bar
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Creates the list of themes
        ListView themes = (ListView) findViewById(R.id.themeList);
        themes.setAdapter(new ThemeAdapter(this, themes));

        //Sets toolbar title
        setTitle(getString(R.string.title_activity_display));


    }

    //Controls back button presses
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //No code to prevent people returning to Opening Screen that would not end
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //Controls toolbar buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Controls navigation bar button reactions
     *
     * @param item Selected item on the navigation bar
     */

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        android.app.FragmentManager fragmentManager = getFragmentManager();

        if (id == R.id.nav_search) {
            startActivity(new Intent(Display.this, HomeScreen.class));

        }  else if (id == R.id.nav_my_cart) {
            startActivity(new Intent(Display.this, MyCart.class));

        } else if (id == R.id.nav_featured_stores) {
            startActivity(new Intent(Display.this, FeaturedScreen.class));

        } else if (id == R.id.nav_language_settings) {
            startActivity(new Intent(Display.this, LangSettings.class));

        } else if (id == R.id.nav_display) {
            startActivity(new Intent(Display.this, Display.class));

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}//End of Class
