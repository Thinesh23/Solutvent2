package example.com.solutvent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
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
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import example.com.solutvent.Common.Common;
import example.com.solutvent.Common.Config;
import example.com.solutvent.Model.DataMessage;
import example.com.solutvent.Model.MyResponse;
import example.com.solutvent.Model.Request;
import example.com.solutvent.Model.Token;
import example.com.solutvent.Remote.APIService;
import example.com.solutvent.ViewHolder.BookingHistoryViewHolder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class BookingHistory extends AppCompatActivity {

    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Request,BookingHistoryViewHolder> adapter;

    FirebaseDatabase database;
    DatabaseReference requests;

    SwipeRefreshLayout mSwipeRefreshLayout;

    APIService mService;

    private static final int PAYPAL_REQUEST_CODE = 9999;

    String bookingid="";
    String paid="";

    Request currentRequest;

    //Paypal payment
    static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX) //use SandBox for testing
            .clientId(Config.PAYPAL_CLIENT_ID);

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/KGSkinnyLatte.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_booking_history);

        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        mService = Common.getFCMService();

        recyclerView = (RecyclerView)findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if(Common.currentUser !=null){
                    loadHistory();
                }
            }
        });

        //Thread to load comment on first launch
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);

                if(Common.currentUser !=null){
                    loadHistory();
                }
            }
        });

    }

   private void loadHistory(){
       Query getOrderByUser = requests.orderByChild("customerPhone").equalTo(Common.currentUser.getPhone());
       FirebaseRecyclerOptions<Request> orderOptions = new FirebaseRecyclerOptions.Builder<Request>()
               .setQuery(getOrderByUser,Request.class)
               .build();

       adapter = new FirebaseRecyclerAdapter<Request, BookingHistoryViewHolder>(orderOptions) {
           @Override
           protected void onBindViewHolder(@NonNull BookingHistoryViewHolder viewHolder, final int position, @NonNull final Request model) {
               viewHolder.txt_booking_date.setText(model.getDate());
               viewHolder.txt_booking_status.setText(convertCodeToStatus(model.getStatus()));
               viewHolder.txt_booking_name.setText(model.getPlannerCompanyName());
               viewHolder.txt_booking_time.setText(model.getTime());

               if (viewHolder.txt_booking_status.getText().equals("100% Complete")){
                   viewHolder.txt_booking_payment.setVisibility(View.VISIBLE);
                   viewHolder.txt_booking_payment.setText("Total: RM " + model.getPayment());
                   viewHolder.btnConfirm.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                   viewHolder.btnConfirm.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           confirmPayment(model.getPayment());
                           bookingid = adapter.getRef(position).getKey();
                           currentRequest = adapter.getItem(position);
                       }
                   });
               } else if (viewHolder.txt_booking_status.getText().equals("Completed")) {
                   viewHolder.btnConfirm.setVisibility(View.GONE);
                   viewHolder.txt_booking_payment.setText("Payment Completed");
               } else {
                   viewHolder.txt_booking_payment.setVisibility(View.GONE);
                   viewHolder.btnConfirm.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colordarker_gray));
               }

           }

           @NonNull
           @Override
           public BookingHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
               View itemView = LayoutInflater.from(parent.getContext())
                       .inflate(R.layout.show_booking_history,parent,false);
               return new BookingHistoryViewHolder(itemView);
           }
       };

       loadComment();
    }

    private void confirmPayment(String payment){
        String formatAmount = payment
                .replace("RM","")
                .replace(",","")
                .replace(" ","");
        PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(formatAmount),
                "MYR",
                "Event App Order",
                PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent(getApplicationContext(), PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT,payPalPayment);
        startActivityForResult(intent,PAYPAL_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PAYPAL_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                final PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirmation != null) {
                    try {
                        String paymentDetail = confirmation.toJSONObject().toString(4);
                        final JSONObject jsonObject = new JSONObject(paymentDetail);
                        final String response = jsonObject.getJSONObject("response").getString("state");

                        requests.child(bookingid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Request item = dataSnapshot.getValue(Request.class);
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("status", "6");
                                requests.child(bookingid).updateChildren(hashMap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                sendNotification();
                                                bookingid = "";
                                                Toast.makeText(BookingHistory.this, "Booking status completed", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                        });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });



                    }catch (JSONException e) {
                        e.printStackTrace();

                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(BookingHistory.this, "Payment Cancelled", Toast.LENGTH_SHORT).show();
                } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                    Toast.makeText(BookingHistory.this, "Invalid Payment", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void sendNotification(){
        DatabaseReference tokens = database.getReference("Tokens");
        tokens.orderByKey().equalTo(currentRequest.getPlannerPhone())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapShot:dataSnapshot.getChildren()){
                            Token token = postSnapShot.getValue(Token.class);

                            Map<String,String> dataSend = new HashMap<>();
                            dataSend.put("title","Booking Status");
                            dataSend.put("message","Payment has been made by " + Common.currentUser.getFirstName());
                            DataMessage dataMessage = new DataMessage(token.getToken(),dataSend);

                            mService.sendNotification(dataMessage)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                            if(response.body().success == 1){
                                                Toast.makeText(BookingHistory.this, "Booking updated", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(BookingHistory.this, "Booking was updated but failed to send notification", Toast.LENGTH_SHORT).show();
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

    private void loadComment(){
        adapter.startListening();
        recyclerView.setAdapter(adapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private String convertCodeToStatus(String status) {
        if(status.equals("6"))
            return "Completed";
        else if(status.equals("1"))
            return "Deal Confirmed";
        else if(status.equals("2"))
            return "25% Complete";
        else if(status.equals("3"))
            return "50% Complete";
        else if(status.equals("4"))
            return "75% Complete";
        else if(status.equals("5"))
            return "100% Complete";
        else
            return "Pending";
    }
}
