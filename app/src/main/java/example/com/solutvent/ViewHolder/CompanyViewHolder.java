package example.com.solutvent.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import example.com.solutvent.Common.Common;
import example.com.solutvent.Interface.ItemClickListener;
import example.com.solutvent.R;

public class CompanyViewHolder extends RecyclerView.ViewHolder implements
        View.OnClickListener{

    public TextView event_name,event_booking,package_price, state_location;
    public ImageView event_image,fav_image,share_image,quick_cart;

    private ItemClickListener itemClickListener;

    public CompanyViewHolder(View itemView) {

        super (itemView);

        event_name = (TextView) itemView.findViewById(R.id.event_name);
        event_image = (ImageView) itemView.findViewById(R.id.event_image);
        package_price = (TextView) itemView.findViewById(R.id.package_price);
        state_location = (TextView) itemView.findViewById(R.id.state_location);
        //fav_image = (ImageView) itemView.findViewById(R.id.fav);
        //share_image = (ImageView) itemView.findViewById(R.id.btnShare);
        //quick_cart = (ImageView) itemView.findViewById(R.id.btn_quick_cart);

        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view){
        itemClickListener.onClick(view,getAdapterPosition(),false);
    }

}