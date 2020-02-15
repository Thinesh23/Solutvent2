package example.com.solutvent.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import example.com.solutvent.R;

public class ShowManageBookingViewHolder  extends RecyclerView.ViewHolder{

    public TextView txt_booking_name, txt_booking_time, txt_booking_date,txt_booking_status;

    public Button btnDelete;

    public ShowManageBookingViewHolder(View itemView) {
        super(itemView);
        txt_booking_status = (TextView) itemView.findViewById(R.id.txt_booking_status);
        txt_booking_name = (TextView) itemView.findViewById(R.id.txt_com_name);
        txt_booking_time = (TextView) itemView.findViewById(R.id.txt_booking_time);
        txt_booking_date = (TextView) itemView.findViewById(R.id.txt_booking_date);
        btnDelete = (Button) itemView.findViewById(R.id.btnDelete);
    }

}
