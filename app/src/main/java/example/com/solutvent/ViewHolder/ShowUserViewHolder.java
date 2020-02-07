package example.com.solutvent.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import example.com.solutvent.Common.Common;
import example.com.solutvent.Interface.ItemClickListener;
import example.com.solutvent.Model.User;
import example.com.solutvent.R;

public class ShowUserViewHolder extends RecyclerView.ViewHolder implements
        View.OnClickListener,
        View.OnCreateContextMenuListener{

    public TextView user_name, user_phone, user_email, user_location;

    private ItemClickListener itemClickListener;

    public RelativeLayout view_background;
    public LinearLayout view_foreground;

    public ShowUserViewHolder(View itemView) {

        super (itemView);

        user_name = (TextView) itemView.findViewById(R.id.user_name);
        user_phone = (TextView) itemView.findViewById(R.id.user_phone);
        user_email = (TextView) itemView.findViewById(R.id.user_email);
        user_location = (TextView) itemView.findViewById(R.id.user_location);
        view_background = (RelativeLayout) itemView.findViewById(R.id.view_background);
        view_foreground = (LinearLayout) itemView.findViewById(R.id.view_foreground);

        itemView.setOnCreateContextMenuListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view){
        itemClickListener.onClick(view,getAdapterPosition(),false);
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.setHeaderTitle("Select the action");
        contextMenu.add(0,0,getAdapterPosition(), Common.DELETE);
    }
}
