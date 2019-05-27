package com.example.user.android2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import model.datasource.Action;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import entities.Trip;
import model.backend.Backend;
import entities.Driver;
import model.backend.BackendFactory;
import model.datasource.FireBaseDataBase;


public class MyTrips extends Fragment {
    View view;
    private RecyclerView tripsRecycleView;
    private List<Trip> tripByDriver;
   // public Spinner filterFirstChoice;
    private Backend backend;
    private TripAdapter adapter;
    private Driver driver;
    //public TextView driversName;
    @SuppressLint("StaticFieldLeak")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        backend = BackendFactory.getInstance();
        view = inflater.inflate(R.layout.fragment_my_trips, container, false);

        //set the reccylerview adapter
        tripsRecycleView = (RecyclerView) view.findViewById(R.id.secondRecycleView);
        tripsRecycleView.setLayoutManager(new LinearLayoutManager(getActivity().getBaseContext()));
        if (tripsRecycleView.getAdapter() == null) {
            driver = backend.loadDataOnCurrentDriver(getActivity().getBaseContext());
            tripByDriver = backend.getSpecificDriverTrips(driver.getID());
            adapter = new TripAdapter(tripByDriver, driver, getActivity());
            tripsRecycleView.setAdapter(adapter);
        } else tripsRecycleView.getAdapter().notifyDataSetChanged();
        TextView driversName = (TextView) view.findViewById(R.id.nameText);
        driversName.setText(driver.getFirstName()+"   "+driver.getLastName());
        return view;
    }


    /**
     * the recyclerview adapter for the second fragment, works like expandable-recyclerview
     */

    private class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {
        private List<Trip> tripList;
        Activity activity;
        Driver theDriver;
        private ArrayList<Integer> counter;//for the collapse and expand

        public TripAdapter(List<Trip> trip, Driver driver, Activity _activity) {
            this.tripList = trip;
            this.activity = _activity;
            theDriver = driver;
            counter = new ArrayList<Integer>();
            for (int i = 0; i < tripList.size(); i++)
                counter.add(0);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.my_trip_list_item, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            //the title of each card
            holder.destination.setText(tripList.get(position).getDestination());
            holder.source.setText(tripList.get(position).getCurrentLocation());
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

        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView email;
            private TextView phone;
            private TextView name;
            private TextView from;
            private TextView to;
            private TextView start;
            private TextView finish;
            private TextView inStatus;
            Trip theTrip;
            private TextView destination;
            private TextView source;
            private TextView status;
            private AppCompatButton finishTrip;
            private ImageView deleteImage;
            private CardView cardView;
            View innerView;
            View title;


            public void getViews(View itemView) {
                name = (TextView) itemView.findViewById(R.id.passengerNameTextView);
                from = (TextView) itemView.findViewById(R.id.sourceExTextView);
                to = (TextView) itemView.findViewById(R.id.destinationExTextView);
                email = (TextView) itemView.findViewById(R.id.emailTextView);
                phone = (TextView) itemView.findViewById(R.id.phoneTextView);
                start = (TextView) itemView.findViewById(R.id.startTimeTextView);
                finish = (TextView) itemView.findViewById(R.id.endTimeTextView);
                inStatus = (TextView) itemView.findViewById(R.id.status2);
                finishTrip = (AppCompatButton) itemView.findViewById(R.id.doneButton);
                deleteImage=(ImageView)itemView.findViewById(R.id.deleteImage);
            }

            public ViewHolder(View itemView) {
                super(itemView);
                cardView = (CardView) itemView.findViewById(R.id.cardView);
                destination = (TextView) itemView.findViewById(R.id.destinationTextView);
                source = (TextView) itemView.findViewById(R.id.chosenFilterTextView);
                status = (TextView) itemView.findViewById(R.id.status);
                title = itemView.findViewById(R.id.titleLayout);
                innerView = itemView.findViewById(R.id.allDetails);
                getViews(itemView);


                deleteImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position=getAdapterPosition();
                        if(backend.deleteTrip(theTrip,activity)) {
                            tripList.remove(position);
                            notifyItemRemoved(position);
                            //FireBaseDataBase.NotifyToTripList();
                        }
                        else return;
                    }
                });

                //sets trip to be done
                finishTrip.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        Date d = new Date();
                        Time time = new Time(d.getTime());

                        backend.changeFinish(theTrip, theDriver, time.toString(), new Action<Void>() {
                            @Override
                            public void onSuccess(Void obj) {
                                Toast.makeText(activity.getBaseContext(), "The trip has been finished successfully!", Toast.LENGTH_LONG).show();

                            }
                            @Override
                            public void onFailure(Exception exception) {
                                Toast.makeText(activity.getBaseContext(), "Could not update the data, must be something wrong \n" + exception.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                        tripsRecycleView.getAdapter().notifyDataSetChanged();
                    }
                });

            }

            public void bind() {
                int position = getAdapterPosition();
                theTrip = tripList.get(position);
                status.setText(theTrip.getStatus().toString());
                //the inside of the cards
                name.setText(theTrip.getPassengerName());
                from.setText(theTrip.getCurrentLocation());
                to.setText(theTrip.getDestination());
                email.setText(theTrip.getPassengerEmail());
                phone.setText(theTrip.getPassengerNumber());
                start.setText(theTrip.getPickUpTime().toString());
                inStatus.setText(theTrip.getStatus().toString());
                if (theTrip.getStatus().equals(Trip.TripStatus.DONE))
                    finish.setText(theTrip.getDropOffTime().toString());
                else
                    finish.setText(R.string.finishTime);
            }
        }
    }
}

