package example.com.solutvent.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import example.com.solutvent.Common.Common;
import example.com.solutvent.Interface.IRecyclerItemSelectedListener;
import example.com.solutvent.Model.TimeSlot;
import example.com.solutvent.R;

public class MyTimeSlotAdapter extends RecyclerView.Adapter<MyTimeSlotAdapter.MyViewHolder>{

    Context context;
    List<TimeSlot> timeSlotList;
    List<CardView> cardViewList;
    LocalBroadcastManager localBroadcastManager;

    public MyTimeSlotAdapter(Context context) {
        this.context = context;
        this.timeSlotList = new ArrayList<>();
        this.localBroadcastManager = LocalBroadcastManager.getInstance(context);
        cardViewList = new ArrayList<>();
    }

    public MyTimeSlotAdapter(Context context, List<TimeSlot> timeSlotList) {
        this.context = context;
        this.timeSlotList = timeSlotList;
        this.localBroadcastManager = LocalBroadcastManager.getInstance(context);
        cardViewList = new ArrayList<>();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_time_slot, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
         holder.txt_time_slot.setText(new StringBuilder(Common.convertTimeSlotToString(position)));
         if(timeSlotList.size() == 0){//if all position is available, just show list

             holder.card_time_slot.setEnabled(true);

             holder.card_time_slot.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorwhite));

             holder.txt_time_slot_description.setText("Available");
             holder.txt_time_slot_description.setTextColor(ContextCompat.getColor(context, R.color.colorblack));
             holder.txt_time_slot.setTextColor(ContextCompat.getColor(context, R.color.colorblack));


         }else { //if position is full(booked)
             for(TimeSlot slotValue :timeSlotList){
                 //loop all time slot from server and set different color
                 int slot = Integer.parseInt(slotValue.getSlot().toString());
                 if(slot == position){

                     holder.card_time_slot.setEnabled(false);

                     holder.card_time_slot.setTag(Common.DISABLE_TAG);
                     holder.card_time_slot.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colordarker_gray));

                     holder.txt_time_slot_description.setText("Full");
                     holder.txt_time_slot_description.setTextColor(ContextCompat.getColor(context, R.color.colorwhite));
                     holder.txt_time_slot.setTextColor(ContextCompat.getColor(context, R.color.colorwhite));
                 }
             }
         }

         if(!cardViewList.contains(holder.card_time_slot))
             cardViewList.add(holder.card_time_slot);

             holder.setIRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                 @Override
                 public void onItemSelectedListener(View view, int pos) {
                     for(CardView cardView:cardViewList){
                         if (cardView.getTag() == null)
                             cardView.setCardBackgroundColor(ContextCompat.
                                     getColor(context, R.color.colorwhite));
                     }

                     holder.card_time_slot.setCardBackgroundColor(ContextCompat.
                             getColor(context, R.color.holo_orange_dark));

                     Intent intent = new Intent(Common.KEY_ENABLE_BUTTON_NEXT);
                     intent.putExtra(Common.KEY_TIME_SLOT, position);
                     intent.putExtra(Common.KEY_STEP, 0);
                     localBroadcastManager.sendBroadcast(intent);

                 }
             });
         }

    @Override
    public int getItemCount() {
        return Common.TIME_SLOT_TOTAL;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_time_slot, txt_time_slot_description;
        CardView card_time_slot;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public void setIRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(View itemView){
            super(itemView);
            card_time_slot = (CardView)itemView.findViewById(R.id.card_time_slot);
            txt_time_slot = (TextView)itemView.findViewById(R.id.txt_time_slot);
            txt_time_slot_description = (TextView)itemView.findViewById(R.id.txt_time_slot_description);

            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            iRecyclerItemSelectedListener.onItemSelectedListener(v, getAdapterPosition());

        }
    }
}
