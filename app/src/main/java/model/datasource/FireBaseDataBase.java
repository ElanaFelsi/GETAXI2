package model.datasource;


import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import entities.Driver;
import entities.Trip;
import model.backend.Backend;

public class FireBaseDataBase implements Backend {
    static DatabaseReference DriversRef;
    static DatabaseReference TripRef;

    public static ChildEventListener driverRefChildEventListener;
    public static ChildEventListener tripRefChildEventListener;
    private static ChildEventListener serviceListener;

    public static List<Driver> driverList;
    public static List<Trip> tripList;

    private FirebaseAuth userAuth;
    private FirebaseUser currentUser;


    /**
     * static declarations
     */
    static {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DriversRef = database.getReference("drivers");
        driverList = new ArrayList<Driver>();
        TripRef = database.getReference("trips");
        tripList = new ArrayList<Trip>();
    }

    public interface NotifyDataChange<T> {
        void OnDataChanged(T obj);

        void onFailure(Exception exception);
    }

    /**
     * @param dr     the driver to add
     * @param action details on success or fail of adding the driver to the firebase
     */
    @Override
    public void addDriver(final Driver dr, final Action<String> action) {
        String key = dr.getID();//setting key
        DriversRef.child(key).setValue(dr).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                action.onSuccess(dr.getID());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                action.onFailure(e);
            }
        });
    }

    /**
     * @return the all available trips
     */
    @Override
    public List<Trip> getNotHandeledTrips() {
        List<Trip> trips = new ArrayList<>();
        for (Trip i : tripList) {
            if (i.getStatus() == Trip.TripStatus.AVAILABLE)
                trips.add(i);
        }
        return trips;
    }

    /**
     * @param _id specific trips according to driver id
     * @return list of trips
     */
    @Override
    public List<Trip> getSpecificDriverTrips(String _id) {
        List<Trip> trips = new ArrayList<>();
        for (Trip i : tripList) {
            if (i.getDriverID() != null)
                if (i.getDriverID().equals(_id))
                    trips.add(i);
        }
        return trips;
    }

    /**
     * @param c   for geocoder
     * @param str the location in a string
     * @return Location made from this string
     */
    private Location fromStringToLocation(Context c, String str) {
        Geocoder gc = new Geocoder(c);
        try {
            if (gc.isPresent()) {
                str = str.substring(str.indexOf(",") + 1, str.length());
                List<Address> list = gc.getFromLocationName(str, 1);
                if (list == null) {
                    Toast.makeText(c, "your location does not exist", Toast.LENGTH_LONG).show();
                    return null;
                } else {
                    Address address = list.get(0);
                    double lat = address.getLatitude();
                    double lng = address.getLongitude();
                    Location location = new Location(str);
                    location.setLatitude(lat);
                    location.setLongitude(lng);

                    return location;
                }
            }
            return null;
        } catch (Exception exception) {
            Toast.makeText(c, "must be something wrong with the location\n" + exception.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    /**
     * @param t the trip to calculate the distance
     * @param c for calculating the location
     * @return the distance in km
     */
    public int distanceCalc(Trip t, Context c) {
        int temp = Math.round(fromStringToLocation(c, t.getCurrentLocation()).distanceTo(fromStringToLocation(c, t.getDestination())) / 1000);
        return temp;
    }

    /**
     * @param t the trip to calculate the price
     * @return the price for the trip
     */
    private double priceCalc(Trip t) {
        SimpleDateFormat start = new SimpleDateFormat("hh:mm:ss");
        SimpleDateFormat finish = new SimpleDateFormat("hh:mm:ss");
        Date s,f;
        long sum;
        try {
             s = start.parse(t.getPickUpTime());
             f = finish.parse(t.getDropOffTime());
            sum= f.getTime()-s.getTime();
            return sum*20;
        } catch (ParseException ex) {
            Log.v("Exception", ex.getLocalizedMessage());
        }
        return 0;
    }

    /**
     * every  time a driver is changed or updated
     * @param notifyDataChange
     */
    public static void NotifyToDriversList(final NotifyDataChange<List<Driver>> notifyDataChange) {
        if (notifyDataChange != null) {
            if (driverRefChildEventListener != null) {
                notifyDataChange.onFailure(new Exception("first unNotify trip list"));
                return;
            }
            driverList.clear();
            driverRefChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Driver d = dataSnapshot.getValue(Driver.class);
                    String id= dataSnapshot.getKey();
                    d.setID(id);
                    driverList.add(d);
                    notifyDataChange.OnDataChanged(driverList);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Driver d = dataSnapshot.getValue(Driver.class);
                    String id = dataSnapshot.getKey();
                    for (int i = 0; i < driverList.size(); i++) {
                        if (driverList.get(i).getID().equals(id)) {
                            driverList.set(i, d);
                            break;
                        }
                    }
                    notifyDataChange.OnDataChanged(driverList);
                }

                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                public void onCancelled(DatabaseError databaseError) {
                    notifyDataChange.onFailure(databaseError.toException());
                }
            };
            DriversRef.addChildEventListener(driverRefChildEventListener);
        }
    }
    public static void stopNotifyToDriversList() {
        if (driverRefChildEventListener != null) {
            DriversRef.removeEventListener(driverRefChildEventListener);
            driverRefChildEventListener = null;
        }
    }

    /**
     * notify trip list every time a trip is changed
     * @param notifyDataChange
     */
    public static void NotifyToTripList(final NotifyDataChange<List<Trip>> notifyDataChange) {
        if (notifyDataChange != null) {

            if (tripRefChildEventListener != null) {
                if (serviceListener != null) {

                    notifyDataChange.onFailure(new Exception("first unNotify trips list"));
                    return;
                } else {
                    serviceListener = new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            notifyDataChange.OnDataChanged(tripList);
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {
                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    };
                    TripRef.addChildEventListener(serviceListener);
                    return;
                }
            }
            tripList.clear();
            tripRefChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Trip trip = dataSnapshot.getValue(Trip.class);
                    String id = dataSnapshot.getKey();
                    trip.setTripID(id);
                    tripList.add(trip);
                    notifyDataChange.OnDataChanged(tripList);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Trip trip = dataSnapshot.getValue(Trip.class);
                    String id = dataSnapshot.getKey();
                    trip.setTripID(id);


                    for (int i = 0; i < tripList.size(); i++) {
                        if (( tripList.get(i).getTripID()).equals(id)) {
                            tripList.set(i, trip);
                            break;
                        }
                    }
                    notifyDataChange.OnDataChanged(tripList);
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Trip trip = dataSnapshot.getValue(Trip.class);
                    String id =dataSnapshot.getKey();
                    trip.setTripID(id);

                    for (int i = 0; i < tripList.size(); i++) {
                        if (tripList.get(i).getTripID() == id) {
                            tripList.remove(i);
                            break;
                        }
                    }
                    notifyDataChange.OnDataChanged(tripList);
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    notifyDataChange.onFailure(databaseError.toException());
                }
            };
            TripRef.addChildEventListener(tripRefChildEventListener);
        }
    }
    public static void stopNotifyToTripList() {
        if (tripRefChildEventListener != null) {
            TripRef.removeEventListener(tripRefChildEventListener);
            tripRefChildEventListener = null;
        }
    }

    public boolean deleteTrip(Trip t, Context c) {
        if (t.getStatus().equals("INPROCESS")) {
            Toast.makeText(c, "your trip is not done, can not be deleted.", Toast.LENGTH_LONG).show();
            return false;
        } else {
            Toast.makeText(c, "your trip has been deleted successfully.", Toast.LENGTH_LONG).show();
            return true;
        }
    }

    /**
     * @param t      the trip to change
     * @param d      the driver that took this trip
     * @param action returns data weather the trip has changed to now or failed
     */
    public void changeNow(Trip t, Driver d, final Action<Void> action) {
        t.setDriverID(d.getID());
        t.setStatus(Trip.TripStatus.INPROCESS);
        //status
        TripRef.child(t.getTripID()).child("status").setValue(t.getStatus()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                action.onSuccess(aVoid);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                action.onFailure(e);
            }
        });
        //driversID
        TripRef.child(t.getTripID()).child("driverID").setValue(t.getDriverID().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                action.onSuccess(aVoid);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                action.onFailure(e);
            }
        });


    }

    /**
     * @param t      the trip to change to finish
     * @param fTime  the finish time
     * @param action returns data weather the trip has changed to finished or failed
     */
    public void changeFinish(Trip t, Driver d, final String fTime, final Action<Void> action) {
        t.setStatus(Trip.TripStatus.DONE);
        t.setDropOffTime(fTime);
        //status
        TripRef.child(t.getTripID()).child("status").setValue(t.getStatus()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                action.onSuccess(aVoid);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                action.onFailure(e);
            }
        });
        //dropofftime
        TripRef.child(t.getTripID()).child("dropOffTime").setValue(t.getDropOffTime().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                action.onSuccess(aVoid);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                action.onFailure(e);
            }
        });
    }

    /**
     * @param c for the toast
     * @return the full data on current user
     */
    public Driver loadDataOnCurrentDriver(Context c) {
        userAuth = FirebaseAuth.getInstance();
        currentUser = userAuth.getCurrentUser();
        String email = currentUser.getEmail();
        Driver d = null;
        try {

            while (d == null) {//if driverList has not been fully loaded yet
                for (Driver i : driverList)
                    if (email.equals(i.getEmail()))
                        d = i;
            }
            return d;
        } catch (Exception ex) {
            Toast.makeText(c, ex.toString(), Toast.LENGTH_LONG).show();
            return null;
        }

    }

}