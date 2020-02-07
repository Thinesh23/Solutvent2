package example.com.solutvent;

import android.provider.ContactsContract;
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
import example.com.solutvent.Model.User;
import example.com.solutvent.ViewHolder.ShowUserViewHolder;

public class ManageUser extends AppCompatActivity implements RecyclerItemTouchHelperListener {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference table_user,table_request;

    SwipeRefreshLayout mSwipeRefreshLayout;

    FirebaseRecyclerAdapter<User,ShowUserViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_user);

        database = FirebaseDatabase.getInstance();
        table_user = database.getReference("User");
        table_request = database.getReference("Requests");

        recyclerView = (RecyclerView)findViewById(R.id.listUsers);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //swipe to delete
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(Common.currentUser.getIsStaff().equals("true")){
                    Query query = table_user.orderByChild("isStaff").equalTo("false");

                    FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                            .setQuery(query,User.class)
                            .build();

                    adapter = new FirebaseRecyclerAdapter<User, ShowUserViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull ShowUserViewHolder holder, int position, @NonNull User model) {
                            holder.user_name.setText(model.getFirstName());
                            holder.user_phone.setText(model.getPhone());
                            holder.user_email.setText(model.getEmail());
                            holder.user_location.setText(model.getState());

                        }

                        @Override
                        public ShowUserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.user_item,parent,false);
                            return new ShowUserViewHolder(view);
                        }
                    };

                    loadUser();
                }
            }
        });

        //Thread to load user on first launch
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);

                if(Common.currentUser.getIsStaff().equals("true")){
                    Query query = table_user.orderByChild("isStaff").equalTo("false");

                    FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                            .setQuery(query,User.class)
                            .build();

                    adapter = new FirebaseRecyclerAdapter<User, ShowUserViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull ShowUserViewHolder holder, int position, @NonNull User model) {
                            holder.user_name.setText(model.getFirstName());
                            holder.user_phone.setText(model.getPhone());
                            holder.user_email.setText(model.getEmail());
                            holder.user_location.setText(model.getState());

                        }

                        @Override
                        public ShowUserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.user_item,parent,false);
                            return new ShowUserViewHolder(view);
                        }
                    };

                    loadUser();
                }
            }
        });
    }

    private void loadUser(){
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof ShowUserViewHolder){
            deleteUser(((ShowUserViewHolder) viewHolder).user_phone.getText().toString());
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(Common.currentUser.getIsStaff().equals("true")){

            if(item.getTitle().equals(Common.DELETE))
            {
                deleteUser(adapter.getRef(item.getOrder()).getKey());
            }
        } else {
            Toast.makeText(this, "You are not Authorized to do this action !!", Toast.LENGTH_SHORT).show();
        }

        return super.onContextItemSelected(item);
    }

    private void deleteUser(final String key) {
        table_user.child(key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ManageUser.this, "User Deleted !!", Toast.LENGTH_SHORT).show();
                table_request.orderByChild("customerPhone").equalTo(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot dataSnapshot1 :dataSnapshot.getChildren()){
                            dataSnapshot1.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                table_request.orderByChild("plannerPhone").equalTo(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot dataSnapshot1 :dataSnapshot.getChildren()){
                            dataSnapshot1.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

    }
}
