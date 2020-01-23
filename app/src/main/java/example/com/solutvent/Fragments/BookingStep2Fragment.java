package example.com.solutvent.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import example.com.solutvent.Common.Common;
import example.com.solutvent.Model.Request;
import example.com.solutvent.R;

import static example.com.solutvent.Common.Common.currentCompany;
import static example.com.solutvent.Common.Common.currentUser;

public class BookingStep2Fragment  extends Fragment{

    SimpleDateFormat simpleDateFormat;
    LocalBroadcastManager localBroadcastManager;

    FirebaseDatabase database;
    DatabaseReference booking, requests;

    @BindView(R.id.txt_person_name)
    TextView organizerName;
    @BindView(R.id.txt_booking_date)
    TextView bookingDate;
    @BindView(R.id.txt_booking_time)
    TextView bookingTime;
    @BindView(R.id.txt_user_no)
    TextView organizerPhone;
    @BindView(R.id.txt_user_mail)
    TextView organizerMail;
    @BindView(R.id.txt_company_name)
    TextView companyName;
    @BindView(R.id.txt_contact_no)
    TextView companyPhone;
    @BindView(R.id.txt_contact_mail)
    TextView companyEmail;
    @OnClick(R.id.btn_confirm)
    void confirmBooking(){
        final Request request = new Request();

        request.setId(String.valueOf(System.currentTimeMillis()));
        request.setOrganizerEmail(Common.currentUser.getEmail());
        request.setOrganizerName(Common.currentUser.getFirstName());
        request.setOrganizerPhone(Common.currentUser.getPhone());
        request.setPlannerCompanyName(Common.currentCompany.getCompanyName());
        request.setPlannerEmail(Common.currentCompany.getEmail());
        request.setPlannerPhone(Common.currentCompany.getPhone());
        request.setTime(new StringBuilder(Common.convertTimeSlotToString(Common.currentTimeSlot)).toString());
        request.setDate(simpleDateFormat.format(Common.currentDate.getTime()));
        request.setSlot(Long.valueOf(Common.currentTimeSlot));
        request.setStatus("Pending");
        request.setPayment("0");

        database = FirebaseDatabase.getInstance();
        booking = database.getReference("Bookings").child(Common.currentCompany.getPhone()).child(Common.simpleDateFormat.format(Common.currentDate.getTime()));
        requests = database.getReference("Requests");

        booking.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                booking.child(Long.valueOf(Common.currentTimeSlot).toString())
                        .setValue(request);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        requests.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                requests.child(request.getId())
                        .setValue(request);

                    Toast.makeText(getContext(), "Thank you, booking confirmed", Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                    resetStaticData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void resetStaticData() {
        Common.step = 0;
        Common.currentTimeSlot = -1;
        Common.currentDate.add(Calendar.DATE, 0);
    }

    Unbinder unbinder;

    static BookingStep2Fragment instance;

    public static BookingStep2Fragment getInstance(){
        if(instance == null)
            instance = new BookingStep2Fragment();
        return instance;
    }

    private BroadcastReceiver confirmBookingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setData();

        }
    };

    private void setData(){
        organizerName.setText(currentUser.getFirstName());
        organizerMail.setText(currentUser.getEmail());
        organizerPhone.setText(currentUser.getPhone());
        companyName.setText(currentCompany.getCompanyName());
        companyEmail.setText(currentCompany.getEmail());
        companyPhone.setText(currentCompany.getPhone());
        bookingTime.setText(new StringBuilder(Common.convertTimeSlotToString(Common.currentTimeSlot)));
        bookingDate.setText(simpleDateFormat.format(Common.currentDate.getTime()));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Appy format for date display on confirm
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        localBroadcastManager.registerReceiver(confirmBookingReceiver, new IntentFilter(Common.KEY_CONFIRM_BOOKING));

    }

    @Override
    public  void onDestroy(){
        localBroadcastManager.unregisterReceiver(confirmBookingReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View itemView = inflater.inflate(R.layout.fragment_booking_step_two, container, false);
        unbinder = ButterKnife.bind(this, itemView);

        return itemView;
    }
}
