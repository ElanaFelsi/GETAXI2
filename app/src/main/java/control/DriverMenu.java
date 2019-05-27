package control;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.user.android2.AvailableTripsFragment;
import com.example.user.android2.MyTrips;
import com.example.user.android2.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import entities.Trip;
import entities.Driver;
import model.backend.Backend;
import model.backend.BackendFactory;
import model.datasource.FireBaseDataBase;

public class DriverMenu extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    String user;
    private FirebaseAuth userAuth;
    public static Location thisLoca;
    private FusedLocationProviderClient mFusedLocationClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        userAuth=FirebaseAuth.getInstance();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        FireBaseDataBase.NotifyToDriversList(new FireBaseDataBase.NotifyDataChange<List<Driver>>() {
            @Override
            public void OnDataChanged(List<Driver> obj) {
                FireBaseDataBase.driverList = obj;
            }

            @Override
            public void onFailure(Exception exception) {
                Toast.makeText(getBaseContext(), "error to get Drivers list\n" + exception.toString(), Toast.LENGTH_LONG).show();
            }
        });

        FireBaseDataBase.NotifyToTripList(new FireBaseDataBase.NotifyDataChange<List<Trip>>() {
            @Override
            public void OnDataChanged(List<Trip> obj) {
                FireBaseDataBase.tripList = obj;
            }

            @Override
            public void onFailure(Exception exception) {
                Toast.makeText(getBaseContext(), "error to get trips list\n" + exception.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.driver_menu, menu);
        return true;
    }

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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.availableTrips) {
            availabletripsfragment();

        } else if (id == R.id.myTrips) {
            mytripsfragment();

        } else if (id == R.id.signOut) {
            signOut();

        } else if (id == R.id.settings) {

        }
        else if (id == R.id.nav_rate) {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void mytripsfragment() {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction fragmentTransaction=manager.beginTransaction();
        MyTrips fragment = new MyTrips();
        fragmentTransaction.replace(R.id.firstFragment,fragment);
        fragmentTransaction.commit();
    }

    private void signOut() {
        userAuth.signOut();
        finish();
        System.exit(0);
    }

    private void availabletripsfragment() {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction fragmentTransaction=manager.beginTransaction();
        AvailableTripsFragment fragment = new AvailableTripsFragment();
        fragmentTransaction.replace(R.id.firstFragment,fragment);
        fragmentTransaction.commit();
    }

    protected void onDestroy() {
        FireBaseDataBase.stopNotifyToTripList();
        FireBaseDataBase.stopNotifyToDriversList();
        stopService(new Intent(getBaseContext(), newTrip.class));
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getLocation(Activity a) {
        try {
            //     Check the SDK version and whether the permission is already granted or not.
            if ( a.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED&& a.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                a.requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            if(thisLoca==null) {   //get Provider location from the user location services
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getBaseContext());
                //run the function on the background and add onSuccess listener
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(a, new OnSuccessListener<Location>() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onSuccess(Location _location) {
                                // Got last known location. In some rare situations this can be null.
                                if (_location != null) {
                                    //save the location
                                    thisLoca = _location;
                                } else {
                                    Toast.makeText(getBaseContext(), "can't find your location", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        } catch (Exception ex) {
            Toast.makeText(a, "must be something wrong with getting your location", Toast.LENGTH_LONG).show();
        }

    }
}
