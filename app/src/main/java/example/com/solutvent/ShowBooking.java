package example.com.solutvent;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

import example.com.solutvent.Common.Common;
import example.com.solutvent.Model.DataMessage;
import example.com.solutvent.Model.MyResponse;
import example.com.solutvent.Model.Request;
import example.com.solutvent.Model.Token;
import example.com.solutvent.Remote.APIService;
import example.com.solutvent.ViewHolder.ShowBookingViewHolder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ShowBooking extends AppCompatActivity{

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    SwipeRefreshLayout mSwipeRefreshLayout;

    FirebaseRecyclerAdapter<Request,ShowBookingViewHolder> adapter;

    MaterialSpinner spinner;

    APIService mService;

    String eventId="";
    String bookingid="";

    Request currentRequest;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(adapter != null){
            adapter.stopListening();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/KGSkinnyLatte.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_show_booking);

        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        recyclerView = (RecyclerView)findViewById(R.id.recyclerComment);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mService = Common.getFCMService();

        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //make sure intent data is not null thensave it to a string
                if(getIntent()!=null){
                    eventId = getIntent().getStringExtra(Common.INTENT_EVENT_ID);
                }
                //make sure current planner company details not null then only call load bookings
                if(Common.currentCompany !=null){
                    loadBookings();
                }
            }
        });

        //Thread to load comment on first launch
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);

                if(getIntent()!=null){
                    eventId = getIntent().getStringExtra(Common.INTENT_EVENT_ID);
                }
                if(Common.currentCompany !=null){
                    loadBookings();
                }
            }
        });

    }

    private void deleteBooking(String key) {
        requests.child(key).removeValue(); // delete item from firebase json Requests
        adapter.notifyDataSetChanged();
        loadBookings();
    }

    private void confirmDeal(){
        requests.child(bookingid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Request item = dataSnapshot.getValue(Request.class);
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("status", "2");
                requests.child(bookingid).updateChildren(hashMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                sendOrderStatusToUser(bookingid, currentRequest);
                                bookingid = "";
                                Toast.makeText(ShowBooking.this, "Planner confirmed the deal", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadBookings(){
        Query query = requests.orderByChild("plannerPhone").equalTo(Common.currentCompany.getPhone());

        FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(query,Request.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Request, ShowBookingViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ShowBookingViewHolder holder, final int position, @NonNull Request model) {
                holder.txtUserPhone.setText(model.getCustomerPhone());
                holder.txtUserEmail.setText(model.getCustomerEmail());
                holder.txtUserName.setText(model.getCustomerName());
                holder.txtStatusState.setText(convertCodeToStatus(model.getStatus()));
                if(holder.txtStatusState.getText().toString().equals("Awaiting Planner")) {
                    holder.btnUpdate.setVisibility(View.VISIBLE);
                    holder.btnUpdate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            confirmDeal();
                            bookingid = adapter.getRef(position).getKey();
                            currentRequest = adapter.getItem(position);
                        }
                    });
                } else if (holder.txtStatusState.getText().toString().equals("Awaiting Customer")){
                    holder.txtbookingstate.setText("Awaiting Customer");
                }else if (holder.txtStatusState.getText().toString().equals("Deal Confirmed")){
                    holder.txtbookingstate.setText("Deal Confirmed");
                }else if (holder.txtStatusState.getText().toString().equals("100% Complete")){
                    holder.txtbookingstate.setText("Pending Payment");
                } else if (holder.txtStatusState.getText().toString().equals("Completed")){
                    holder.txtbookingstate.setText("Booking Completed");
                } else {
                    holder.txtbookingstate.setText("Track Progress");
                }

                if(holder.txtStatusState.getText().toString().equals("Completed")){
                    holder.btnUpdate.setVisibility(View.GONE);
                } else if (holder.txtStatusState.getText().toString().equals("Awaiting Customer")){
                    holder.btnUpdate.setVisibility(View.GONE);
                } else {
                    holder.btnUpdate.setText("Update");
                    holder.btnUpdate.setVisibility(View.VISIBLE);
                    holder.btnUpdate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showUpdateBooking(adapter.getRef(position).getKey(),adapter.getItem(position));
                        }
                    });
                }

            }

            @Override
            public ShowBookingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.show_booking_layout,parent,false);
                return new ShowBookingViewHolder(view);
            }
        };

        loadComment(eventId);
    }

    private void showUpdateBooking(String key, final Request item){
        final String localKey=key;
        // create Dialog
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Update Status");
        alertDialog.setMessage("Please choose status");
        // get Inflater
        LayoutInflater inflater=this.getLayoutInflater();
        // create View
        final View view=inflater.inflate(R.layout.update_booking_layout,null);
        // find View in this layout
        final MaterialSpinner spinner=view.findViewById(R.id.status_spinner);
        final MaterialEditText payment = view.findViewById(R.id.edtPayment);
        payment.setVisibility(View.GONE);
        spinner.setItems("Update Progress","25% Complete", "50% Complete", "75% Complete", "100% Complete"); // set item of spinner

        spinner.setSelectedIndex(0);

        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                if (position == 4){
                    payment.setVisibility(View.VISIBLE);
                }
            }
        });

        // set View in dialog
        alertDialog.setView(view);
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // change status in item
                item.setStatus(String.valueOf(spinner.getSelectedIndex()));
                if(spinner.getSelectedIndex() == 4){
                    item.setPayment(payment.getText().toString());
                }
                requests.child(localKey).setValue(item); // update item in Requests Json
                adapter.notifyDataSetChanged();
                sendOrderStatusToUser(localKey,item);
                Toast.makeText(ShowBooking.this, "Status updated", Toast.LENGTH_SHORT).show();

            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        //show dialog
        alertDialog.show();
    }

    private void sendOrderStatusToUser(final String key, final Request item){
        DatabaseReference tokens = database.getReference("Tokens");
        tokens.orderByKey().equalTo(item.getCustomerPhone())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapShot:dataSnapshot.getChildren()){
                            Token token = postSnapShot.getValue(Token.class);

                            Map<String,String> dataSend = new HashMap<>();
                            dataSend.put("title","Booking Status");
                            dataSend.put("message","Your Booking" + key +" was updated by " + Common.currentUser.getFirstName());
                            DataMessage dataMessage = new DataMessage(token.getToken(),dataSend);

                            mService.sendNotification(dataMessage)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                            if(response.body().success == 1){
                                                Toast.makeText(ShowBooking.this, "Booking updated", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(ShowBooking.this, "Booking was updated but failed to send notification", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<MyResponse> call, Throwable t) {
                                            Log.e("ERROR",t.getMessage());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void loadComment(String eventId){
        adapter.startListening();

        recyclerView.setAdapter(adapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private String convertCodeToStatus(String status) {

        if(status.equals("0"))
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
        else if(status.equals("7"))
            return "Completed";
        else
            return "Pending";
    }
}