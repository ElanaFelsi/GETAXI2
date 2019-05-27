package com.example.user.android2;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import control.DriverMenu;
import entities.Driver;
import entities.Trip;
import model.backend.Backend;
import model.backend.BackendFactory;
import model.datasource.Action;
import model.datasource.FireBaseDataBase;

public class AvailableTripsFragment extends Fragment {

    View view;
    //definition
    private RecyclerView tripsRecycleView;
    public Backend backend;
    private TripAdapter adapter;
    private AppCompatButton changeFilter;
    private Driver registeredDriver;
    private EditText filterText;

    private SearchView searchView;

    public static List<Trip> trips;


    public static AvailableTripsFragment newInstance(String param1, String param2) {
        AvailableTripsFragment fragment = new AvailableTripsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //  @SuppressLint("StaticFieldLeak")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        backend = BackendFactory.getInstance();
        view = inflater.inflate(R.layout.fragment_available_trips, container, false);
        tripsRecycleView = (RecyclerView) view.findViewById(R.id.firstRecycleView);
        tripsRecycleView.setLayoutManager(new LinearLayoutManager(getActivity().getBaseContext()));
        searchView = (SearchView) view.findViewById(R.id.tripsSearchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String text) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                adapter.getFilter().filter(text);
                adapter.notifyDataSetChanged();
                return false;
            }
        });

        //set the reccylerview adapter
        if (tripsRecycleView.getAdapter() == null) {
            trips = backend.getNotHandeledTrips();
            while (registeredDriver == null)
                registeredDriver = backend.loadDataOnCurrentDriver(getActivity().getBaseContext());
            adapter = new TripAdapter(tripsRecycleView, trips, registeredDriver, getActivity());
            tripsRecycleView.setAdapter(adapter);
        } else tripsRecycleView.getAdapter().notifyDataSetChanged();

        return view;
    }


    class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> implements Filterable {

        private Backend backend;
        private RecyclerView recyclerView;
        private List<Trip> tripList;
        private List<Trip> originalTripList;
        private Filter tripFilter;
        private String strFilterText;
        Activity activity;
        private AppCompatButton smsConfirm;
        private AppCompatButton emailConfirm;
        private AppCompatButton phoneConfirm;
        Driver theDriver;
        private ArrayList<Integer> counter;//for the collapse and expand

        public TripAdapter(RecyclerView recyclerView, List<Trip> trip, Driver driver, Activity _activity) {
            this.recyclerView = recyclerView;
            this.tripList = trip;
            this.originalTripList = new ArrayList<Trip>(tripList);
            backend = BackendFactory.getInstance();
            this.activity = _activity;
            theDriver = driver;
            counter = new ArrayList<Integer>();
            for (int i = 0; i < tripList.size(); i++)
                counter.add(0);

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.trip_list_item, parent, false);

            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.destination.setText(tripList.get(position).getDestination());
            holder.theFilter.setText(tripList.get(position).getCurrentLocation());
            //collapse and expand
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (counter.get(position) % 2 == 0) {
                        holder.innerView.setVisibility(View.VISIBLE);
                        holder.title.setVisibility(View.INVISIBLE);
                    } else {
                        holder.innerView.setVisibility(View.GONE);
                        holder.title.setVisibility(View.VISIBLE);
                    }
                    counter.set(position, counter.get(position) + 1);
                }
            });

            holder.bind();
        }

        @Override
        public int getItemCount() {
            return tripList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private TextView Email;
            private TextView PhoneNumber;
            private TextView Name;
            private TextView PickUP;
            private TextView des;
            private TextView PTime;
            private TextView DTime;
            private AppCompatButton driveNow;
            //private AppCompatButton finishTrip;
            Trip trip;
            private TextView destination;
            private TextView theFilter;
            private CardView cardView;
            View innerView;
            View title;

            public ViewHolder(View itemView) {
                super(itemView);
                cardView = (CardView) itemView.findViewById(R.id.cardView);
                destination = (TextView) itemView.findViewById(R.id.destinationTextView);
                theFilter = (TextView) itemView.findViewById(R.id.chosenFilterTextView);
                title = itemView.findViewById(R.id.titleLayout);
                innerView = itemView.findViewById(R.id.allDetails);
                getViews(itemView);
                driveNow.setOnClickListener(this);
            }

            public void getViews(View itemView) {
                Name = (TextView) itemView.findViewById(R.id.passengerNameTextView);
                PickUP = (TextView) itemView.findViewById(R.id.sourceExTextView);
                des = (TextView) itemView.findViewById(R.id.destinationExTextView);
                Email = (TextView) itemView.findViewById(R.id.emailTextView);
                PhoneNumber = (TextView) itemView.findViewById(R.id.phoneTextView);
                PTime = (TextView) itemView.findViewById(R.id.startTimeTextView);
                DTime = (TextView) itemView.findViewById(R.id.endTimeTextView);
                driveNow = (AppCompatButton) itemView.findViewById(R.id.confirmButton);
                // finishTrip = (AppCompatButton) itemView.findViewById(R.id.doneButton);
            }

            public void bind() {
                int position = getAdapterPosition();
                trip = tripList.get(position);
                Name.setText(trip.getPassengerName());
                PickUP.setText(trip.getCurrentLocation());
                des.setText(trip.getDestination());
                Email.setText(trip.getPassengerEmail());
                PhoneNumber.setText(trip.getPassengerNumber());
                PTime.setText(trip.getPickUpTime().toString());
                if (trip.getStatus().equals(Trip.TripStatus.DONE))
                    DTime.setText(trip.getDropOffTime().toString());
                else
                    DTime.setText(R.string.finishTime);
            }

            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.confirmButton:
                        confirmDialog();
                        break;

                    default:
                        break;
                }
            }

            private void confirmDialog() {
                AlertDialog.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case AlertDialog.BUTTON_NEGATIVE:
                                dialog.cancel();
                                break;
                        }
                    }
                };
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity, R.style.MyDialogTheme);
                alertDialogBuilder.setTitle("Confirm by:");
                // Get the layout inflater
                LayoutInflater inflater = LayoutInflater.from(activity.getBaseContext());
                final View dialogView = inflater.inflate(R.layout.confirmation_dialog_layout, null);
                alertDialogBuilder.setView(dialogView.findViewById(R.id.buttonLayout));
                smsConfirm = dialogView.findViewById(R.id.bySMS);
                alertDialogBuilder.setNegativeButton("Cancel ", onClickListener);
                final AlertDialog dialog = alertDialogBuilder.create();
                dialog.show();

                //*
                // sends sms comformation to user with price of trip*/
                smsConfirm.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(View view) {
                        Uri uri = Uri.parse(getString(R.string.smsto) + trip.getPassengerNumber());
                        Intent intentSMS = new Intent(Intent.ACTION_SENDTO, uri);
                        intentSMS.putExtra(getString(R.string.sms_body), "hello " + trip.getPassengerName()
                                + "\n" + getString(R.string.sms_body_come) + " " + trip.getCurrentLocation() + "\n to " + trip.getDestination()+"\n"+" the final price is: "+( backend.distanceCalc(trip, getActivity().getBaseContext())+25)+"Shekel.");
                        startActivity(intentSMS);
                        changeNow();
                        dialog.cancel();
                    }

                });
            }

            private void changeNow() {
                backend.changeNow(trip, theDriver, new Action<Void>() {
                    @Override
                    public void onSuccess(Void d) {
                        Toast.makeText(activity.getBaseContext(), "The trip is now in process!", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        Toast.makeText(activity.getBaseContext(), "Could not update the data, must be something wrong \n" + exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
                tripList.remove(getAdapterPosition());
                recyclerView.getAdapter().notifyDataSetChanged();
            }

        }

        /**
         * get filer for trips according to address*/

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {

                    FilterResults results = new FilterResults();
                    List<Trip> filteredList;
                    filteredList = new ArrayList<Trip>(originalTripList);
                    // We implement here the filter logic
                    if (constraint == null || constraint.length() == 0) {
                        // No filter implemented we return all the list
                        results.count = originalTripList.size();
                        results.values = originalTripList;
                    } else {
                        // We perform filtering operation
                        List<Trip> tempList = new ArrayList<Trip>();
                        for (Trip trip : filteredList) {
                            if ((trip.getCurrentLocation() + "").toUpperCase().contains(constraint.toString().toUpperCase()))
                                tempList.add(trip);
                        }
                        results.values = tempList;
                        results.count = tempList.size();

                    }
                    return results;
                    // return null;
                }

                protected void publishResults(CharSequence constraint,
                                              FilterResults results) {

                    tripList.removeAll(tripList);
                    tripList.addAll((List<Trip>) results.values);
                    recyclerView.getAdapter().notifyDataSetChanged();
                }
            };
        }
    }
}


