
package control;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import entities.Driver;
import entities.Trip;
//import com.project5779.javaproject2.model.backend.BackEndFactory;/
//import com.project5779.javaproject2.model.datasource.DataBaseFirebase;
//import com.project5779.javaproject2.model.entities.Drive;
//import com.project5779.javaproject2.model.entities.StateOfDrive;

import java.util.ArrayList;
import java.util.List;

import model.backend.Backend;
import model.backend.BackendFactory;
import model.datasource.FireBaseDataBase;



import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import java.util.List;

/***
 * The class represent the service that is activated when the data in the ride list changes and child is added
 * intent that runs in the background not deepened in activity
 * it sends a flag to the broadcastReceiver every time a new ride is added
 */

public class newTrip extends Service {

    private int lastCount = 0;
    Context context;
    FireBaseDataBase dbManager;

    /***
     * this class shows what happens every time something is requested from the service
     */
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {//when we turn up the service
        dbManager = (FireBaseDataBase) BackendFactory.getInstance();//set dbmanager
        context = getApplicationContext();//gets context
        dbManager.NotifyToTripList(new FireBaseDataBase.NotifyDataChange<List<Trip>>() {//open listing to drive list

            @Override
            public void OnDataChanged(List<Trip> obj) {

                try {
                    for (Trip d : obj ) {
                        if (d.getStatus().equals(Trip.TripStatus.AVAILABLE)) {
                            Intent intent = new Intent(context, BroadCastReceiverNotification.class);
                            sendBroadcast(intent);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception exception) {
            }
        });;
        return START_REDELIVER_INTENT;//says to system to continue the service even when the application closes.
    }

    /***
     * the system calls this method to retrieve the IBinder only when the first client connects
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}