package example.com.solutvent.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import example.com.solutvent.R;

public class ShowBookingViewHolder extends RecyclerView.ViewHolder{
    public TextView txtUserPhone,txtUserEmail, txtUserName, txtStatusState, txtbookingstate;

    public Button btnUpdate;

    public ShowBookingViewHolder(View itemView) {
        super(itemView);
        txtUserEmail = (TextView)itemView.findViewById(R.id.txtUserEmail);
        txtUserPhone = (TextView)itemView.findViewById(R.id.txtUserPhone);
        txtUserName = (TextView)itemView.findViewById(R.id.txtUserName);
        txtStatusState = (TextView)itemView.findViewById(R.id.txtStatus);
        txtbookingstate = (TextView) itemView.findViewById(R.id.txtView);

        btnUpdate = itemView.findViewById(R.id.btnUpdate);
    }
}