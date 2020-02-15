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
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dmax.dialog.SpotsDialog;
import example.com.solutvent.Common.Common;
import example.com.solutvent.Interface.ItemClickListener;
import example.com.solutvent.Model.Category;
import example.com.solutvent.Model.Request;
import example.com.solutvent.Model.Token;
import example.com.solutvent.Model.User;
import example.com.solutvent.ViewHolder.MenuViewHolder;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseDatabase database;
    DatabaseReference category,event,request,table_user;
    FirebaseStorage storage;
    StorageReference storageReference;

    TextView txtFullName;

    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Category,MenuViewHolder> adapter;

    SwipeRefreshLayout swipeRefreshLayout;
    //Add New Menu Layout
    MaterialEditText edtName;
    Button btnUpload,btnSelect;

    Request currentRequest;

    String stateName = "";
    List<String> stateList = new ArrayList<>();
    String adminPhone = "";


    Category newCategory;

    Uri saveUri;

    FloatingActionButton addCategory;

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
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");

        addCategory = (FloatingActionButton) findViewById(R.id.btn_category);

        database = FirebaseDatabase.getInstance();
        category = database.getReference("Category");

        event = database.getReference("Event");
        request = database.getReference("Request");
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

        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(category,Category.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder viewHolder, int position, @NonNull Category model) {
                viewHolder.txtMenuName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.imageView);
                final Category clickItem = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent eventList = new Intent(Home.this,EventList.class);
                        eventList.putExtra("CategoryId",adapter.getRef(position).getKey());
                        startActivity(eventList);
                    }
                });
            }

            @NonNull
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.menu_item,parent,false);
                return new MenuViewHolder(itemView);
            }
        };

        if (Common.currentUser.getIsStaff().equals("true")){
            addCategory.show();
            //addCategory.hide();
            addCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShowAddCategoryDialog();
                }
            });
        } else {
            addCategory.hide();
        }


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

        //show and hide based on customer login/admin login or planner login
        Menu nav_menu = navigationView.getMenu();
        if(Common.currentUser.getIsPlanner().equals("true")){
            nav_menu.findItem(R.id.nav_booking_history).setVisible(false);
            nav_menu.findItem(R.id.nav_booking_manage).setVisible(false);
            nav_menu.findItem(R.id.nav_show_feedback).setVisible(true);
            nav_menu.findItem(R.id.nav_manage_user).setVisible(false);
        } else {

            if (Common.currentUser.getIsStaff().equals("true")) {
                nav_menu.findItem(R.id.nav_manage_user).setVisible(true);
                nav_menu.findItem(R.id.nav_booking_history).setVisible(false);
                nav_menu.findItem(R.id.nav_booking_manage).setVisible(true);
                nav_menu.findItem(R.id.nav_show_feedback).setVisible(false);
            }
            else {
                nav_menu.findItem(R.id.nav_manage_user).setVisible(false);
                nav_menu.findItem(R.id.nav_booking_history).setVisible(true);
                nav_menu.findItem(R.id.nav_booking_manage).setVisible(false);
                nav_menu.findItem(R.id.nav_show_feedback).setVisible(false);
            }
        }

        //display name on the header view, get the name from database instead of
        //current user because data is only updated when user sign out and login back
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
        recycler_menu.setLayoutManager(new GridLayoutManager(this,2));
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recycler_menu.getContext(),
                R.anim.layout_fall_down);
        recycler_menu.setLayoutAnimation(controller);

        //token is used for sending notifications
        //unique to every phone
        updateToken(FirebaseInstanceId.getInstance().getToken());

    }

    //admin can add new category
    private void ShowAddCategoryDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Add new Category");
        alertDialog.setMessage("Please fill full information");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_category_layout, null);

        edtName = add_menu_layout.findViewById(R.id.edtName);
        btnSelect = add_menu_layout.findViewById(R.id.btnSelect);
        btnUpload = add_menu_layout.findViewById(R.id.btnUpload);

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
                uploadImage();
            }
        });

        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_add_black_24dp);

        //Set button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                if(newCategory !=null)
                {
                    category.push().child(newCategory.getId()).setValue(newCategory);
                    Snackbar.make(swipeRefreshLayout,"New Category"+ newCategory.getName()+"was added",
                            Snackbar.LENGTH_SHORT ).show();
                }

            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
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
                            Toast.makeText(Home.this, "Uploaded !!!", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(Home.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
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

    //Update/Delete
    // Press Crtl+O
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(Common.currentUser.getIsStaff().equals("true")){
            if(item.getTitle().equals(Common.UPDATE))
            {
                showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
            }
            else if(item.getTitle().equals(Common.DELETE))
            {
                deleteCategory(adapter.getRef(item.getOrder()).getKey());
            }
        } else {
            Toast.makeText(this, "You are not Authorized to do this action !!", Toast.LENGTH_SHORT).show();
        }

        return super.onContextItemSelected(item);
    }

    private void deleteCategory(String key) {

        //First, we need get all the event in Category
        Query eventInCategory = event.orderByChild("menuId").equalTo(key);
        eventInCategory.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapShot:dataSnapshot.getChildren())
                {
                    postSnapShot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        category.child(key).removeValue();
        Toast.makeText(this, "Category Deleted !!", Toast.LENGTH_SHORT).show();
    }

    //update category image, name
    private void showUpdateDialog(final String key, final Category item) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Update Category");
        alertDialog.setMessage("Please fill full information");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_category_layout, null);

        edtName = add_menu_layout.findViewById(R.id.edtName);
        btnSelect = add_menu_layout.findViewById(R.id.btnSelect);
        btnUpload = add_menu_layout.findViewById(R.id.btnUpload);

        //Set Default name
        edtName.setText(item.getName());

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
                changeImage(item);
            }
        });

        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_add_black_24dp);

        //Set button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                item.setName(edtName.getText().toString());
                category.child(key).setValue(item);

            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    private void changeImage(final Category item) {
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
                            Toast.makeText(Home.this, "Uploaded !!!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    item.setImage(uri.toString());
                                }
                            });
                        }

                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            mDialog.dismiss();
                            Toast.makeText(Home.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
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


    //update token when new user added
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
        recycler_menu.scheduleLayoutAnimation();
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

        MenuItem item = menu.findItem(R.id.message_admin);
        if (Common.currentUser.getIsStaff().equals("true")){
            item.setVisible(false);
        } else {
            item.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.refresh) {
            loadMenu();
        } else if (item.getItemId() == R.id.message_admin){
            Intent chatIntent = new Intent(Home.this,ChattingPrivate.class);
            chatIntent.putExtra("userId",adminPhone);
            startActivity(chatIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    //drawer at the left and it's options
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item){

        int id = item.getItemId();
        if (id == R.id.nav_menu) {

        } else if (id == R.id.nav_booking_history){
            Intent historyIntent = new Intent(Home.this,BookingHistory.class);
            startActivity(historyIntent);

        } else if (id == R.id.nav_log_out){
            status("offline");
            Paper.book().destroy();
            Intent signIn = new Intent(Home.this, SignIn.class);
            signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(signIn);
        } else if (id == R.id.nav_change_pwd){
            showChangePasswordDialog();
        } else if (id == R.id.nav_update_profile){
            if(Common.currentUser.getIsPlanner().equals("true")){
                showUpdatePlannerProfileDialog();
            } else if (Common.currentUser.getIsStaff().equals("true")) {
                showUpdateAdminProfileDialog();
            } else {
                showUpdateCustomerProfileDialog();
            }

        } else if (id == R.id.nav_manage_user) {
            Intent userIntent = new Intent(Home.this,ManageUser.class);
            startActivity(userIntent);
        } else if (item.getItemId() == R.id.nav_message){
            Intent chatIntent = new Intent(Home.this,ChattingMenu.class);
            startActivity(chatIntent);
        } else if (item.getItemId() == R.id.nav_booking_manage){
            Intent chatIntent = new Intent(Home.this,ManageBooking.class);
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
                            Toast.makeText(Home.this, "Uploaded !!!", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(Home.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
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

    //admin update details
    private void showUpdateAdminProfileDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Update Admin Profile");
        alertDialog.setIcon(R.drawable.ic_person_black_24dp);
        alertDialog.setMessage("Please fill all information");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_home = inflater.inflate(R.layout.update_admin_profile_layout,null);

        final MaterialEditText updateFirstName = (MaterialEditText)layout_home.findViewById(R.id.edtFirstNameAdmin);
        final MaterialEditText updateEmail = (MaterialEditText)layout_home.findViewById(R.id.edtEmailAdmin);
        final MaterialEditText updateAddress = (MaterialEditText)layout_home.findViewById(R.id.edtAddressAdmin);
        final MaterialEditText updateSecureCode = (MaterialEditText)layout_home.findViewById(R.id.edtSecureCodeAdmin);

        //Set default
        updateFirstName.setText(Common.currentUser.getFirstName());
        updateEmail.setText(Common.currentUser.getEmail());
        updateAddress.setText(Common.currentUser.getAddress());
        updateSecureCode.setText(Common.currentUser.getSecureCode());
        alertDialog.setView(layout_home);

        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                if(updateFirstName.getText().toString().trim().length() != 0 &&
                        updateEmail.getText().toString().trim().length() != 0 &&
                        updateAddress.getText().toString().trim().length() != 0 &&
                        updateSecureCode.getText().toString().trim().length() != 0) {

                    //updated
                    Common.currentUser.setFirstName(updateFirstName.getText().toString());
                    Common.currentUser.setEmail(updateEmail.getText().toString());
                    Common.currentUser.setAddress(updateAddress.getText().toString());
                    Common.currentUser.setSecureCode(updateSecureCode.getText().toString());

                    FirebaseDatabase.getInstance().getReference("User")
                            .child(Common.currentUser.getPhone())
                            .setValue(Common.currentUser)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(Home.this,"Profile updated",Toast.LENGTH_SHORT).show();
                                }
                            });

                } else {
                    Toast.makeText(Home.this,"Please fill up all the details",Toast.LENGTH_SHORT).show();
                }
            }
        });

        alertDialog.show();
    }

    //customer update
    private void showUpdateCustomerProfileDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Update Customer Profile");
        alertDialog.setIcon(R.drawable.ic_person_black_24dp);
        alertDialog.setMessage("Please fill all information");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_home = inflater.inflate(R.layout.update_customer_profile_layout,null);

        final MaterialEditText updateFirstName = (MaterialEditText)layout_home.findViewById(R.id.edtFirstName);
        final MaterialEditText updateEmail = (MaterialEditText)layout_home.findViewById(R.id.edtEmail);
        final MaterialEditText updateAddress = (MaterialEditText)layout_home.findViewById(R.id.edtAddress);
        final MaterialEditText updateSecureCode = (MaterialEditText)layout_home.findViewById(R.id.edtSecureCode);
        final MaterialSpinner state_update_customer = (MaterialSpinner)layout_home.findViewById(R.id.state_update_customer);
        final DatabaseReference table_request = database.getReference("Requests");

        //Set default
        updateFirstName.setText(Common.currentUser.getFirstName());
        updateEmail.setText(Common.currentUser.getEmail());
        updateAddress.setText(Common.currentUser.getAddress());
        updateSecureCode.setText(Common.currentUser.getSecureCode());
        alertDialog.setView(layout_home);

        state_update_customer.setItems(stateList);
        state_update_customer.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
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
                        updateSecureCode.getText().toString().trim().length() != 0 &&
                        !stateName.equals("Choose State")) {

                        //updated
                        Common.currentUser.setFirstName(updateFirstName.getText().toString());
                        Common.currentUser.setEmail(updateEmail.getText().toString());
                        Common.currentUser.setAddress(updateAddress.getText().toString());
                        Common.currentUser.setSecureCode(updateSecureCode.getText().toString());
                        Common.currentUser.setState(stateName);

                    FirebaseDatabase.getInstance().getReference("User")
                            .child(Common.currentUser.getPhone())
                            .setValue(Common.currentUser)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(Home.this,"Profile updated",Toast.LENGTH_SHORT).show();
                                    //if profile update successful, then update the details of the customer in the booking as well
                                    table_request.orderByChild("customerPhone").equalTo(Common.currentUser.getPhone()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()){
                                                for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
                                                    Request item = dataSnapshot1.getValue(Request.class);
                                                    HashMap<String, Object> hashMap = new HashMap<>();
                                                    hashMap.put("customerPhone", Common.currentUser.getPhone());
                                                    hashMap.put("customerEmail", Common.currentUser.getEmail());
                                                    hashMap.put("customerName", Common.currentUser.getFirstName());
                                                    hashMap.put("customerAddress", Common.currentUser.getAddress());
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
                    Toast.makeText(Home.this,"Please fill up all the details",Toast.LENGTH_SHORT).show();
                }
            }
        });

        alertDialog.show();
    }

    //update planner details
    private void showUpdatePlannerProfileDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
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
                                    Toast.makeText(Home.this,"Profile updated",Toast.LENGTH_SHORT).show();
                                    //if profile update successful, then update the details of the planner in the booking as well
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
                    Toast.makeText(Home.this,"Please fill up all the details",Toast.LENGTH_SHORT).show();
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

    //change password function,logics are the same with all the previous explanations in login/sign up
    private void showChangePasswordDialog(){
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
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
                final AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(Home.this).setCancelable(false).build();
                waitingDialog.show();

                if (edtPassword.getText().toString().isEmpty() || edtNewPassword.getText().toString().isEmpty()
                        || edtRepeatPassword.getText().toString().isEmpty()){
                    waitingDialog.dismiss();
                    Toast.makeText(Home.this, "Please fill up all the details !!",Toast.LENGTH_SHORT).show();
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
                                            Toast.makeText(Home.this, "Password updated !!",Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            Toast.makeText(Home.this, e.getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            waitingDialog.dismiss();
                            Toast.makeText(Home.this, "New Password doesnt match",Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        waitingDialog.dismiss();
                        Toast.makeText(Home.this, "Wrong Old Password",Toast.LENGTH_SHORT).show();
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

}