package model.backend;


import android.app.Activity;
import android.content.Context;
import android.location.Location;

import java.sql.Time;
import java.util.List;

import entities.Driver;
import entities.Trip;
import model.datasource.Action;
import model.datasource.FireBaseDataBase;


public interface Backend {

 void addDriver(final Driver dr, final Action<String> action);

 List<Trip> getNotHandeledTrips();

 List<Trip> getSpecificDriverTrips(String _id);

 boolean deleteTrip(Trip t, Context c);

 void changeNow(Trip t, Driver d, final Action<Void> action);

 void changeFinish(Trip t, Driver d, final String fTime, final Action<Void> action);

 Driver loadDataOnCurrentDriver(Context c);

 int distanceCalc(Trip t, Context c);


}