package example.com.solutvent.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import dmax.dialog.SpotsDialog;
import example.com.solutvent.Adapter.MyTimeSlotAdapter;
import example.com.solutvent.Common.Common;
import example.com.solutvent.Common.SpacesItemDecoration;
import example.com.solutvent.Interface.ITimeSlotListener;
import example.com.solutvent.Model.TimeSlot;
import example.com.solutvent.R;

public class BookingStep1Fragment extends Fragment implements ITimeSlotListener {

    ITimeSlotListener iTimeSlotListener;
    AlertDialog dialog;

    Unbinder unbinder;
    LocalBroadcastManager localBroadcastManager;

    FirebaseDatabase database;
    DatabaseReference booking;

    @BindView(R.id.recycler_time_slot)
    RecyclerView recycler_time_slot;

    @BindView(R.id.calendarView)
    HorizontalCalendarView calendarView;

    SimpleDateFormat simpleDateFormat;

/*    BroadcastReceiver displayTimeSlot = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Calendar date = Calendar.getInstance();
            date.add(Calendar.DATE, 0);
            loadAvailableTimeSlotOfCompany(Common.currentCompany.getPhone(),
                    simpleDateFormat.format(date.getTime()));

        }
    };*/

    private void loadAvailableTimeSlotOfCompany(final String companyId, final String date){
        dialog.show();

        database = FirebaseDatabase.getInstance();
        booking = database.getReference("Bookings");

        booking.child(companyId).child(date).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    List<TimeSlot> timeSlots = new ArrayList<>();
                    for(DataSnapshot postSnapShot:dataSnapshot.getChildren()){
                        timeSlots.add(postSnapShot.getValue(TimeSlot.class));
                    }
                    iTimeSlotListener.onTimeSlotLoadSuccess(timeSlots);
                }else {
                    iTimeSlotListener.onTimeSlotLoadEmpty();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static BookingStep1Fragment instance;

    public static BookingStep1Fragment getInstance(){
        if(instance == null)
            instance = new BookingStep1Fragment();
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        iTimeSlotListener = this;

        simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy"); //28_03_2019 (this is the key)

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();

        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, 0);
        loadAvailableTimeSlotOfCompany(Common.currentCompany.getPhone(),
                simpleDateFormat.format(date.getTime()));
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull  LayoutInflater inflater, @Nullable ViewGroup container,@Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View itemView = inflater.inflate(R.layout.fragment_booking_step_one,container,false);
        unbinder = ButterKnife.bind(this,itemView);
        
        init(itemView);

        return itemView;

    }

    private void init(View itemView) {
        recycler_time_slot.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        recycler_time_slot.setLayoutManager(gridLayoutManager);
        recycler_time_slot.addItemDecoration(new SpacesItemDecoration(8));

        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DATE,0);
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DATE,2);
        HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(itemView, R.id.calendarView)
                .range(startDate,endDate)
                .datesNumberOnScreen(1)
                .mode(HorizontalCalendar.Mode.DAYS)
                .defaultSelectedDate(startDate)
                .build();
        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                if(Common.currentDate.getTimeInMillis() != date.getTimeInMillis()){
                    Common.currentDate = date;//this code will not load again if you select new day same with day selected
                    loadAvailableTimeSlotOfCompany(Common.currentCompany.getPhone(),simpleDateFormat.format(date.getTime()));
                }
            }
        });

    }

    @Override
    public void onTimeSlotLoadSuccess(List<TimeSlot> timeSlotList){
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(getContext(), timeSlotList);
        recycler_time_slot.setAdapter(adapter);

        dialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadFailed(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadEmpty() {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(getContext());
        recycler_time_slot.setAdapter(adapter);

        dialog.dismiss();
    }
}
