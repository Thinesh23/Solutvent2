package example.com.solutvent;

import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import example.com.solutvent.Common.Common;
import example.com.solutvent.Helper.RecyclerItemTouchHelper;
import example.com.solutvent.Interface.RecyclerItemTouchHelperListener;
import example.com.solutvent.Model.Request;
import example.com.solutvent.ViewHolder.ShowManageBookingViewHolder;


public class ManageBooking extends AppCompatActivity {


    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference table_request;

    SwipeRefreshLayout mSwipeRefreshLayout;

    FirebaseRecyclerAdapter<Request,ShowManageBookingViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_booking);

        database = FirebaseDatabase.getInstance();
        table_request = database.getReference("Requests");

        recyclerView = (RecyclerView)findViewById(R.id.listBookings);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //make sure that the current user is staff then only able to delete
                if(Common.currentUser.getIsStaff().equals("true")){

                    FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>()
                            .setQuery(table_request,Request.class)
                            .build();

                    //displays the data from booking table to the layout text field
                    adapter = new FirebaseRecyclerAdapter<Request,ShowManageBookingViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull ShowManageBookingViewHolder viewHolder,final int position, @NonNull Request model) {
                            viewHolder.txt_booking_date.setText(model.getDate());
                            viewHolder.txt_booking_status.setText(convertCodeToStatus(model.getStatus()));
                            viewHolder.txt_booking_name.setText(model.getPlannerCompanyName());
                            viewHolder.txt_booking_time.setText(model.getTime());
                            if (viewHolder.txt_booking_status.getText().toString().equals("Completed")){
                                viewHolder.btnDelete.setVisibility(View.VISIBLE);
                                viewHolder.btnDelete.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        deleteBooking(adapter.getRef(position).getKey());
                                    }
                                });
                            } else if (viewHolder.txt_booking_status.getText().toString().equals("Booking Cancelled")){
                                viewHolder.btnDelete.setVisibility(View.VISIBLE);
                                viewHolder.btnDelete.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        deleteBooking(adapter.getRef(position).getKey());
                                    }
                                });
                            } else {
                                viewHolder.btnDelete.setVisibility(View.GONE);
                            }

                        }

                        @Override
                        public ShowManageBookingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.mbooking_item,parent,false);
                            return new ShowManageBookingViewHolder(view);
                        }
                    };

                    loadBookings();
                }
            }
        });

        //Thread to load booking on first launch
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);

                if(Common.currentUser.getIsStaff().equals("true")){

                    FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>()
                            .setQuery(table_request,Request.class)
                            .build();

                    //displays the data from booking table to the layout text field
                    adapter = new FirebaseRecyclerAdapter<Request,ShowManageBookingViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull ShowManageBookingViewHolder viewHolder, final int position, @NonNull Request model) {
                            viewHolder.txt_booking_date.setText(model.getDate());
                            viewHolder.txt_booking_status.setText(convertCodeToStatus(model.getStatus()));
                            viewHolder.txt_booking_name.setText(model.getPlannerCompanyName());
                            viewHolder.txt_booking_time.setText(model.getTime());
                            if (viewHolder.txt_booking_status.getText().toString().equals("Completed")){
                                viewHolder.btnDelete.setVisibility(View.VISIBLE);
                                viewHolder.btnDelete.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        deleteBooking(adapter.getRef(position).getKey());
                                    }
                                });
                            } else if (viewHolder.txt_booking_status.getText().toString().equals("Booking Cancelled")){
                                viewHolder.btnDelete.setVisibility(View.VISIBLE);
                                viewHolder.btnDelete.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        deleteBooking(adapter.getRef(position).getKey());
                                    }
                                });
                            } else {
                                viewHolder.btnDelete.setVisibility(View.GONE);
                            }

                        }

                        @Override
                        public ShowManageBookingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.mbooking_item,parent,false);
                            return new ShowManageBookingViewHolder(view);
                        }
                    };

                    loadBookings();
                }
            }
        });
    }

    //make sure adapter knows when a data is deleted and notify the adapter
    private void loadBookings(){
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }


    //delete booking
    private void deleteBooking(final String key) {
        table_request.child(key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ManageBooking.this, "Bookings Deleted !!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //get status of the booking and convert it to string
    private String convertCodeToStatus(String status) {
        if(status.equals("7"))
            return "Completed";
        else if(status.equals("0"))
            return "Awaiting Customer";
        else if(status.equals("1"))
            return "Awaiting Planner";
        else if(status.equals("2"))
            return "Deal Confirmed";
        else if(status.equals("3"))
            return "25% Complete";
        else if(status.equals("4"))
            return "50% Complete";
        else if(status.equals("5"))
            return "75% Complete";
        else if(status.equals("6"))
            return "100% Complete";
        else if(status.equals("8"))
            return "Booking Cancelled";
        else
            return "Awaiting Customer";
    }
}
