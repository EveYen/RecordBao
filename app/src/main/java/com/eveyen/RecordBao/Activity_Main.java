package com.eveyen.RecordBao;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

public class Activity_Main extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FrameLayout rootLayout;
    private Fragment fragment = null;
    private FragmentManager fragmentManager;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootLayout = (FrameLayout) findViewById(R.id.content_fragment);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initDrawer();
        initNavigationView();

        fragment = new Fragment_Main();
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_fragment, fragment).commit();
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /**
     * initDrawer
     * 初始化整個含側邊選單的layout
     */
    private void initDrawer() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (drawer != null) {
            drawer.setDrawerListener(toggle);
        }
        toggle.syncState();
    }

    /**
     * initNavigationView
     * 指定側邊選單內的listener
     */
    private void initNavigationView() {
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);//指定listener
        }
    }

    /**
     * onBackPressed
     * 設定返回鍵的動作
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }
    }

    /**
     * 載入menu
     * @param menu
     * @return boolean
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * 設置menu內選項的動作
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_flow) {
            startService(new Intent(Activity_Main.this,FloatWindows.class));
            //onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * onNavigationItemSelected
     * 設定側邊選單動作，開啟fragment
     * @param item
     * @return
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_note:
                fragment = new Fragment_Main();
                toolbar.setTitle("Note");
                break;
            case R.id.nav_record:
                fragment = new Fragment_Record();
                toolbar.setTitle("Record");
                break;
            case R.id.nav_ckip:
                fragment = new Fragment_CKIP();
                toolbar.setTitle("CKIP Test");
                break;
            default:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_fragment, fragment).commit();
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW,
                "Activity_Main Page",
                Uri.parse("http://host/path"),
                Uri.parse("android-app://com.eveyen.RecordBao/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW,
                "Activity_Main Page",
                Uri.parse("http://host/path"),
                Uri.parse("android-app://com.eveyen.RecordBao/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
