package example.com.solutvent;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import static java.util.concurrent.TimeUnit.*;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import dmax.dialog.SpotsDialog;
import example.com.solutvent.Common.Common;
import example.com.solutvent.Interface.ItemClickListener;
import example.com.solutvent.Model.Category;
import example.com.solutvent.Model.DataMessage;
import example.com.solutvent.Model.MyResponse;
import example.com.solutvent.Model.Request;
import example.com.solutvent.Model.Token;
import example.com.solutvent.Model.User;
import example.com.solutvent.Remote.APIService;
import example.com.solutvent.ViewHolder.MenuViewHolder;
import example.com.solutvent.ViewHolder.ShowBookingViewHolder;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class HomePlanner extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseDatabase database;
    DatabaseReference category,event,request,table_user;
    FirebaseStorage storage;
    StorageReference storageReference;

    TextView txtFullName;

    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Request,ShowBookingViewHolder> adapter;

    SwipeRefreshLayout swipeRefreshLayout;
    //Add New Menu Layout
    MaterialEditText edtName;
    Button btnUpload,btnSelect;

    Request currentRequest;

    String stateName = "";
    List<String> stateList = new ArrayList<>();
    String adminPhone = "";
    String spinnerstatus = "",currentDate="";
    long bookingDate;
    SimpleDateFormat inputParser = new SimpleDateFormat("HH:mm", Locale.getDefault());


    Category newCategory;
    MaterialSpinner spinner;

    APIService mService;

    Uri saveUri;

    FloatingActionButton addCategory;

    String bookingid="";

    Request currentRequest2;

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
        setContentView(R.layout.activity_home_planner);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        addCategory = (FloatingActionButton) findViewById(R.id.btn_category);

        database = FirebaseDatabase.getInstance();
        request = database.getReference("Requests");
        table_user = database.getReference("User");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        stateList.add("Choose State");
        stateList.add("KL");
        stateList.add("Selangor");

        table_user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) { ;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user.getIsStaff().equals("true")){
                        adminPhone = user.getPhone();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Query query = request.orderByChild("plannerPhone").equalTo(Common.currentUser.getPhone());

        FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(query,Request.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Request, ShowBookingViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ShowBookingViewHolder holder, final int position, @NonNull Request model) {
                holder.txtUserPhone.setText(model.getCustomerPhone());
                holder.txtUserEmail.setText(model.getCustomerEmail());
                holder.txtUserName.setText(model.getCustomerName());
                holder.txtStatusState.setText(convertCodeToStatus(model.getStatus()));
                if(holder.txtStatusState.getText().toString().equals("Awaiting Planner")) {
                    holder.btnUpdate.setVisibility(View.VISIBLE);
                    holder.btnUpdate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            confirmDeal();
                            bookingid = adapter.getRef(position).getKey();
                            currentRequest2 = adapter.getItem(position);
                        }
                    });
                } else if (holder.txtStatusState.getText().toString().equals("Awaiting Customer")){
                    holder.txtbookingstate.setText("Awaiting Customer");
                    holder.btnUpdate.setVisibility(View.GONE);
                }else if (holder.txtStatusState.getText().toString().equals("100% Complete")){
                    holder.txtbookingstate.setText("Pending Payment");
                    holder.btnUpdate.setVisibility(View.GONE);
                } else if (holder.txtStatusState.getText().toString().equals("Completed")){
                    holder.txtbookingstate.setText("Booking Completed");
                    holder.btnUpdate.setVisibility(View.GONE);
                } else {
                    holder.btnUpdate.setText("Update");
                    holder.btnUpdate.setVisibility(View.VISIBLE);
                    holder.btnUpdate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showUpdateBooking(adapter.getRef(position).getKey(),adapter.getItem(position));
                        }
                    });
                    holder.txtbookingstate.setText("Track Progress");
                }

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                currentDate = model.getDate();
                try {
                    Date strDate = sdf.parse(currentDate);
                    Calendar c = Calendar.getInstance();
                    c.setTime(strDate);
                    c.add(Calendar.DATE,1);
                    if(c.getTime().compareTo(new Date()) < 0 && (convertCodeToStatus(model.getStatus()).equals("Awaiting Planner")
                                || convertCodeToStatus(model.getStatus()).equals("Awaiting Customer"))){
                        deleteBooking(adapter.getRef(position).getKey());
                    }
                } catch (java.text.ParseException e){
                    e.printStackTrace();
                }

                bookingDate = Long.parseLong(model.getBookingTime());
                Long currentime = System.currentTimeMillis();
                Long difference = currentime - bookingDate;

                if ( difference >= Common.TWO_MINUTES && convertCodeToStatus(model.getStatus()).equals("Awaiting Planner")){
                    deleteBooking(adapter.getRef(position).getKey());
                    Toast.makeText(getBaseContext(), "Booking is deleted after 5 mins !!", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public ShowBookingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.show_booking_layout,parent,false);
                return new ShowBookingViewHolder(view);
            }
        };

        addCategory.hide();

        setSupportActionBar(toolbar);

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Common.isConnectedToInternet(getBaseContext())) {
                    loadMenu();
                }else {
                    Toast.makeText(getBaseContext(), "Please check your connection !!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        //Default, load for first time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (Common.isConnectedToInternet(getBaseContext())) {
                    loadMenu();
                } else {
                    Toast.makeText(getBaseContext(), "Please check your connection !!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawer.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Menu nav_menu = navigationView.getMenu();
        if(Common.currentUser.getIsPlanner().equals("true")){
            nav_menu.findItem(R.id.nav_booking_history).setVisible(false);
            nav_menu.findItem(R.id.nav_show_feedback).setVisible(true);
            nav_menu.findItem(R.id.nav_manage_user).setVisible(false);
        } else {

            if (Common.currentUser.getIsStaff().equals("true")) {
                nav_menu.findItem(R.id.nav_manage_user).setVisible(true);
                nav_menu.findItem(R.id.nav_booking_history).setVisible(false);
                nav_menu.findItem(R.id.nav_show_feedback).setVisible(false);
            }
            else {
                nav_menu.findItem(R.id.nav_manage_user).setVisible(false);
                nav_menu.findItem(R.id.nav_booking_history).setVisible(true);
                nav_menu.findItem(R.id.nav_show_feedback).setVisible(false);
            }
        }

        View headerView = navigationView.getHeaderView(0);
        txtFullName = (TextView) headerView.findViewById(R.id.txtFullName);
        table_user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.child(Common.currentUser.getPhone()).getValue(User.class);
                txtFullName.setText(user.getFirstName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        recycler_menu = (RecyclerView) findViewById(R.id.recycler_menu);
        recycler_menu.setLayoutManager(new LinearLayoutManager(this));
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recycler_menu.getContext(),
                R.anim.layout_fall_down);

        mService = Common.getFCMService();
        recycler_menu.setLayoutAnimation(controller);

        updateToken(FirebaseInstanceId.getInstance().getToken());

    }

    private Date parseDate(String date) {

        try {
            return inputParser.parse(date);
        } catch (java.text.ParseException e) {
            return new Date(0);
        }
    }


    private void deleteBooking(String key) {
        request.child(key).removeValue(); // delete item from firebase json Requests
        loadMenu();
    }

    private void confirmDeal(){
        request.child(bookingid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Request item = dataSnapshot.getValue(Request.class);
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("status", "2");
                request.child(bookingid).updateChildren(hashMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                sendOrderStatusToUser(bookingid, currentRequest2);
                                bookingid = "";
                                Toast.makeText(HomePlanner.this, "Planner confirmed the deal", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void uploadImage() {
        if(saveUri!=null) {

            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/" + imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //set value for newCategory if image upload and we can get download link
                            mDialog.dismiss();
                            Toast.makeText(HomePlanner.this, "Uploaded !!!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    String order_number = String.valueOf(System.currentTimeMillis());
                                    newCategory = new Category();
                                    newCategory.setName(edtName.getText().toString());
                                    newCategory.setImage(uri.toString());
                                    newCategory.setId(order_number);

                                }
                            });
                        }

                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            mDialog.dismiss();
                            Toast.makeText(HomePlanner.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //Don'r worry about this error
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mDialog.setMessage("Uploaded"+progress+"%");
                        }
                    });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null)
        {
            saveUri = data.getData();
            btnSelect.setText("Image Selected");
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select picture"), Common.PICK_IMAGE_REQUEST);
    }

    private void updateToken(String token){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token data = new Token(token);
        tokens.child(Common.currentUser.getPhone()).setValue(data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        if(adapter != null)
            adapter.startListening();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    private void loadMenu(){
        adapter.startListening();
        recycler_menu.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
        //Animation
        recycler_menu.getAdapter().notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.home,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.refresh) {
            loadMenu();
        } else if (item.getItemId() == R.id.message_admin){
            Intent chatIntent = new Intent(HomePlanner.this,ChattingPrivate.class);
            chatIntent.putExtra("userId",adminPhone);
            startActivity(chatIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item){

        int id = item.getItemId();
        if (id == R.id.nav_menu) {

        } else if (id == R.id.nav_booking_history){
            Intent historyIntent = new Intent(HomePlanner.this,BookingHistory.class);
            startActivity(historyIntent);

        } else if (id == R.id.nav_log_out){
            status("offline");
            Paper.book().destroy();
            Intent signIn = new Intent(HomePlanner.this, SignIn.class);
            signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(signIn);
        } else if (id == R.id.nav_change_pwd){
            showChangePasswordDialog();
        } else if (id == R.id.nav_update_profile){
            showUpdatePlannerProfileDialog();
        } else if (id == R.id.nav_show_feedback) {
            Intent userIntent = new Intent(HomePlanner.this,ShowFeedbackPlanner.class);
            startActivity(userIntent);
        } else if (id == R.id.nav_manage_user) {
            Intent userIntent = new Intent(HomePlanner.this,ManageUser.class);
            startActivity(userIntent);
        } else if (item.getItemId() == R.id.nav_message){
            Intent chatIntent = new Intent(HomePlanner.this,ChattingMenu.class);
            startActivity(chatIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }

    private void changeCompanyImage() {
        if(saveUri!=null) {

            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/" + imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //set value for newCategory if image upload and we can get download link
                            mDialog.dismiss();
                            Toast.makeText(HomePlanner.this, "Uploaded !!!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Common.currentUser.setCompanyImage(uri.toString());
                                }
                            });
                        }

                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            mDialog.dismiss();
                            Toast.makeText(HomePlanner.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //Don'r worry about this error
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mDialog.setMessage("Uploaded"+progress+"%");
                        }
                    });

        }
    }

    private void showUpdatePlannerProfileDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomePlanner.this);
        alertDialog.setTitle("Update Planner Profile");
        alertDialog.setIcon(R.drawable.ic_person_black_24dp);
        alertDialog.setMessage("Please fill all information");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_home = inflater.inflate(R.layout.update_planner_profile_layout,null);

        final MaterialEditText updateFirstName = (MaterialEditText)layout_home.findViewById(R.id.edtFirstName);
        final MaterialEditText updateEmail = (MaterialEditText)layout_home.findViewById(R.id.edtEmail);
        final MaterialEditText updateAddress = (MaterialEditText)layout_home.findViewById(R.id.edtAddress);
        final MaterialEditText updatePrice = (MaterialEditText)layout_home.findViewById(R.id.edtPrice);
        final MaterialEditText updateSecureCode = (MaterialEditText)layout_home.findViewById(R.id.edtSecureCode);
        final MaterialSpinner state_update_planner = (MaterialSpinner)layout_home.findViewById(R.id.state_update_planner);


        btnSelect = layout_home.findViewById(R.id.btnSelect);
        btnUpload = layout_home.findViewById(R.id.btnUpload);

        //Event for button
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                chooseImage();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCompanyImage();
            }
        });

        final DatabaseReference table_request = database.getReference("Requests");

        //Set default
        updateFirstName.setText(Common.currentUser.getFirstName());
        updateEmail.setText(Common.currentUser.getEmail());
        updateAddress.setText(Common.currentUser.getAddress());
        updatePrice.setText(Common.currentUser.getPrice());
        updateSecureCode.setText(Common.currentUser.getSecureCode());

        alertDialog.setView(layout_home);

        state_update_planner.setItems(stateList);
        state_update_planner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                stateName = item.toString();
            }
        });

        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();


                if(updateFirstName.getText().toString().trim().length() != 0 &&
                        updateEmail.getText().toString().trim().length() != 0 &&
                        updateAddress.getText().toString().trim().length() != 0 &&
                        updatePrice.getText().toString().trim().length() != 0 &&
                        updateSecureCode.getText().toString().trim().length() != 0 &&
                        !stateName.equals("Choose State")) {

                    //updated
                    Common.currentUser.setFirstName(updateFirstName.getText().toString());
                    Common.currentUser.setEmail(updateEmail.getText().toString());
                    Common.currentUser.setAddress(updateAddress.getText().toString());
                    Common.currentUser.setPrice(updatePrice.getText().toString());
                    Common.currentUser.setSecureCode(updateSecureCode.getText().toString());
                    Common.currentUser.setState(stateName);

                    FirebaseDatabase.getInstance().getReference("User")
                            .child(Common.currentUser.getPhone())
                            .setValue(Common.currentUser)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(HomePlanner.this,"Profile updated",Toast.LENGTH_SHORT).show();
                                    table_request.orderByChild("plannerPhone").equalTo(Common.currentUser.getPhone()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()){
                                                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                                                    Request item = dataSnapshot1.getValue(Request.class);
                                                    HashMap<String, Object> hashMap = new HashMap<>();
                                                    hashMap.put("plannerPhone", Common.currentUser.getPhone());
                                                    hashMap.put("plannerEmail", Common.currentUser.getEmail());
                                                    hashMap.put("plannerCompanyName", Common.currentUser.getFirstName());
                                                    table_request.child(item.getId()).updateChildren(hashMap)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                }
                                                            });
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                            });

                } else {
                    Toast.makeText(HomePlanner.this,"Please fill up all the details",Toast.LENGTH_SHORT).show();
                }
            }
        });

        alertDialog.show();
    }

    private void status (String status){
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status",status);
        DatabaseReference table_user = FirebaseDatabase.getInstance().getReference("User");
        table_user.child(Common.currentUser.getPhone())
                .updateChildren(hashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });

    }

    private void showChangePasswordDialog(){
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomePlanner.this);
        alertDialog.setTitle("CHANGE PASSWORD");
        alertDialog.setMessage("Please fill all information");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_pwd = inflater.inflate(R.layout.change_password_layout,null);

        final MaterialEditText edtPassword = (MaterialEditText)layout_pwd.findViewById(R.id.edtPassword);
        final MaterialEditText edtNewPassword = (MaterialEditText)layout_pwd.findViewById(R.id.edtNewPassword);
        final MaterialEditText edtRepeatPassword = (MaterialEditText)layout_pwd.findViewById(R.id.edtRepeatPassword);

        alertDialog.setView(layout_pwd);
        alertDialog.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(HomePlanner.this).setCancelable(false).build();
                waitingDialog.show();

                if (edtPassword.getText().toString().isEmpty() || edtNewPassword.getText().toString().isEmpty()
                        || edtRepeatPassword.getText().toString().isEmpty()){
                    waitingDialog.dismiss();
                    Toast.makeText(HomePlanner.this, "Please fill up all the details !!",Toast.LENGTH_SHORT).show();
                } else {
                    if(edtPassword.getText().toString().equals(Common.currentUser.getPassword())){
                        if(edtNewPassword.getText().toString().equals(edtRepeatPassword.getText().toString())){
                            Map<String,Object> passwordUpdate = new HashMap<>();
                            passwordUpdate.put("password",edtNewPassword.getText().toString());

                            DatabaseReference user = FirebaseDatabase.getInstance().getReference("User");
                            user.child(Common.currentUser.getPhone())
                                    .updateChildren(passwordUpdate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            waitingDialog.dismiss();
                                            Toast.makeText(HomePlanner.this, "Password updated !!",Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            Toast.makeText(HomePlanner.this, e.getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            waitingDialog.dismiss();
                            Toast.makeText(HomePlanner.this, "New Password doesnt match",Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        waitingDialog.dismiss();
                        Toast.makeText(HomePlanner.this, "Wrong Old Password",Toast.LENGTH_SHORT).show();
                    }
                }


            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private void showUpdateBooking(String key, final Request item){
        final String localKey=key;
        // create Dialog
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Update Status");
        alertDialog.setMessage("Please choose status");
        // get Inflater
        LayoutInflater inflater=this.getLayoutInflater();
        // create View
        final View view=inflater.inflate(R.layout.update_booking_layout,null);
        // find View in this layout
        final MaterialSpinner spinner=view.findViewById(R.id.status_spinner);
        final MaterialEditText payment = view.findViewById(R.id.edtPayment);
        payment.setVisibility(View.GONE);
        spinner.setItems("Update Progress","25% Complete", "50% Complete", "75% Complete", "100% Complete"); // set item of spinner

        spinner.setSelectedIndex(0);

        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                spinnerstatus = item.toString();
                if (position == 4){
                    payment.setVisibility(View.VISIBLE);
                }
            }
        });

        // set View in dialog
        alertDialog.setView(view);
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // change status in item
                if (spinnerstatus.equals("25% Complete")) {
                    item.setStatus("3");
                } else if (spinnerstatus.equals("50% Complete")) {
                    item.setStatus("4");
                } else if (spinnerstatus.equals("75% Complete")) {
                    item.setStatus("5");
                } else if (spinnerstatus.equals("100% Complete")) {
                    item.setStatus("6");
                }

                if(spinner.getSelectedIndex() == 4){
                    item.setPayment(payment.getText().toString());
                }
                request.child(localKey).setValue(item); // update item in Requests Json
                adapter.notifyDataSetChanged();
                sendOrderStatusToUser(localKey,item);
                Toast.makeText(HomePlanner.this, "Status updated", Toast.LENGTH_SHORT).show();

            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        //show dialog
        alertDialog.show();
    }

    private void sendOrderStatusToUser(final String key, final Request item){
        DatabaseReference tokens = database.getReference("Tokens");
        tokens.orderByKey().equalTo(item.getCustomerPhone())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapShot:dataSnapshot.getChildren()){
                            Token token = postSnapShot.getValue(Token.class);

                            Map<String,String> dataSend = new HashMap<>();
                            dataSend.put("title","Booking Status");
                            dataSend.put("message","Your Booking" + key +" was updated by " + Common.currentUser.getFirstName());
                            DataMessage dataMessage = new DataMessage(token.getToken(),dataSend);

                            mService.sendNotification(dataMessage)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                            if(response.body().success == 1){
                                                Toast.makeText(HomePlanner.this, "Booking updated", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(HomePlanner.this, "Booking was updated but failed to send notification", Toast.LENGTH_SHORT).show();
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

    private String convertCodeToStatus(String status) {

        if(status.equals("0"))
            return "Awaiting Customer";
        else if(status.equals("1"))
            return "Awaiting Planner";
        else if(status.equals("2"))
            return "Deal Confirmed";
        else if(status.equals("3"))
            return "25% Complete";
        else if(status.equals("4"))
            return "50% Complete";
        else if(status.equals("5"))
            return "75% Complete";
        else if(status.equals("6"))
            return "100% Complete";
        else if(status.equals("7"))
            return "Completed";
        else
            return "Pending";
    }

}