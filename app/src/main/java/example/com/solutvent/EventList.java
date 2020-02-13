package example.com.solutvent;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.Query.*;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.yahoo.mobile.client.android.util.rangeseekbar.RangeSeekBar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import example.com.solutvent.Common.Common;
import example.com.solutvent.Interface.ItemClickListener;
import example.com.solutvent.Model.Event;
import example.com.solutvent.Model.User;
import example.com.solutvent.ViewHolder.CompanyViewHolder;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static example.com.solutvent.Common.Common.currentCompany;

public class EventList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference companyList;
    FirebaseStorage storage;
    StorageReference storageReference;

    DataSnapshot newSnap;

    String newEventId = "";

    MaterialEditText edtName, edtDescription, edtPrice, edtLocation;
    Button btnSelect, btnUpload, btnSetDate, btnSetTime;

    RadioGroup rdiBook;
    RadioButton rdiFree, rdiPaid;

    Calendar c;
    DatePickerDialog dpd;
    TimePickerDialog tpd;

    Event newEvent;
    Uri saveUri;

    String categoryId = "";
    String booking;
    FirebaseRecyclerAdapter<User,CompanyViewHolder> adapter;
    FirebaseRecyclerAdapter<User,CompanyViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    List<User> eventList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    ImageView filter_tag;

    //Facebook Share
    CallbackManager callbackManager;
    ShareDialog shareDialog;

    SwipeRefreshLayout swipeRefreshLayout;

    //Create Target from Picasso
    Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            //Create Photo from Bitmap
            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();
            if (ShareDialog.canShow(SharePhotoContent.class)){
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();
                shareDialog.show(content);
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Note: add this code before setContentView method
/*        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/KGSkinnyLatte.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());*/
        setContentView(R.layout.activity_event_list);

        //Init Facebook
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);

        //Firebase
        database = FirebaseDatabase.getInstance();
        companyList = database.getReference("User");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //when intent data from previous activity not null then save it to this string
                if(getIntent() != null){
                    categoryId = getIntent().getStringExtra("CategoryId");
                }
                //if selected category id not null then only load the planner list
                if (!categoryId.isEmpty() && categoryId != null){
                    if(Common.isConnectedToInternet(getBaseContext())){
                        loadListEvent(categoryId);
                    } else {
                        Toast.makeText(EventList.this, "Please check your connection !!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                }

            }
        });
        //Thread to load user on first launch
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if(getIntent() != null){
                    categoryId = getIntent().getStringExtra("CategoryId");
                }
                if (!categoryId.isEmpty() && categoryId != null){
                    if(Common.isConnectedToInternet(getBaseContext())){
                        loadListEvent(categoryId);
                    } else {
                        Toast.makeText(EventList.this, "Please check your connection !!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                }
                //Search bar
                materialSearchBar = (MaterialSearchBar)findViewById(R.id.searchBar);
                materialSearchBar.setHint("Enter planner name");
                loadSuggest();
                materialSearchBar.setCardViewElevation(10);
                materialSearchBar.addTextChangeListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    //search from suggestlist and then load the entered planner name
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        List<String> suggest = new ArrayList<String>();
                        for (String search:suggestList) {
                            if(search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                                suggest.add(search);
                        }
                        materialSearchBar.setLastSuggestions(suggest);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
                    @Override
                    public void onSearchStateChanged(boolean enabled) {
                        if (!enabled)
                            recyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void onSearchConfirmed(CharSequence text) {
                        startSearch(text);
                    }

                    @Override
                    public void onButtonClicked(int buttonCode) {

                    }
                });
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycler_event);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    //function to take data from firebase and load it when the user start search
    private void startSearch(CharSequence text) {
        //create query by name
        Query searchByName = companyList.orderByChild("firstName").equalTo(text.toString());
        //Create options with query
        FirebaseRecyclerOptions<User> companyOptions = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(searchByName,User.class)
                .build();

        searchAdapter = new FirebaseRecyclerAdapter<User, CompanyViewHolder>(companyOptions) {
            @Override
            protected void onBindViewHolder(@NonNull CompanyViewHolder viewHolder, final int position, @NonNull final User model) {
                viewHolder.event_name.setText(model.getFirstName());
                viewHolder.package_price.setText("Starting from RM "+model.getPrice());
                viewHolder.state_location.setText(model.getState());
                Picasso.with(getBaseContext()).load(model.getCompanyImage()).into(viewHolder.event_image);

                newEventId = searchAdapter.getRef(position).getKey();

                final User local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent eventDetail = new Intent(EventList.this, EventDetail.class);
                        eventDetail.putExtra("eventId", searchAdapter.getRef(position).getKey());
                        currentCompany = model;
                        startActivity(eventDetail);
                    }
                });
            }

            @NonNull
            @Override
            public CompanyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.event_item,parent,false);
                return new CompanyViewHolder(itemView);
            }
        };
        searchAdapter.startListening();
        recyclerView.setAdapter(searchAdapter);
    }

    //get all the planner with the selected category and add their names to the suggest list
    //this is to avoid other category names to be added to the list
    //later we will use suggest list for the search.
    private void loadSuggest(){
        companyList.orderByChild("menuId").equalTo(categoryId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                    User item = postSnapshot.getValue(User.class);
                    suggestList.add(item.getFirstName());
                }
                materialSearchBar.setLastSuggestions(suggestList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadListEvent(String categoryId){

        //Create query by category Id
        Query search = companyList.orderByChild("menuId").equalTo(categoryId);

        //Create options with query
        FirebaseRecyclerOptions<User> companyOptions = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(search, User.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<User, CompanyViewHolder>(companyOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final CompanyViewHolder viewHolder, final int position, @NonNull final User model) {

                viewHolder.event_name.setText(model.getFirstName());
                viewHolder.package_price.setText("Starting from RM "+model.getPrice());
                viewHolder.state_location.setText(model.getState());
                Picasso.with(getBaseContext()).load(model.getCompanyImage()).into(viewHolder.event_image);

                //Click to share
                viewHolder.share_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Picasso.with(getApplicationContext())
                                .load(model.getCompanyImage())
                                .into(target);
                    }
                });
                final User local = model;
               viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent eventDetail = new Intent (EventList.this,EventDetail.class);
                        eventDetail.putExtra("eventId",adapter.getRef(position).getKey());
                        currentCompany = model;
                        startActivity(eventDetail);
                    }
                });
            }

            @NonNull
            @Override
            public CompanyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.event_item,parent,false);
                return new CompanyViewHolder(itemView);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
        if(searchAdapter != null) {
            searchAdapter.stopListening();
        }
    }

}
