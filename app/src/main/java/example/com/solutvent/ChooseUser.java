package example.com.solutvent;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import example.com.solutvent.Common.Common;
import example.com.solutvent.Model.Category;
import example.com.solutvent.Model.User;


public class ChooseUser extends AppCompatActivity {

    User newUser;
    Uri saveUri;

    String categoryId = "";
    String categoryName = "";

    MaterialEditText edtFirstName, edtPhone, edtPassword, edtEmail, edtCompanyName, edtSecureCode;

    Button btnUpload, btnSelect, btnPlanner, btnOrganizer;
    MaterialSpinner spinner;

    FirebaseStorage storage;
    StorageReference storageReference;


    FirebaseDatabase database;
    DatabaseReference table_user, table_category;

    List<String> categoryNameList = new ArrayList<>();
    List<Category> categoryList = new ArrayList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_choose_user);

        btnPlanner = (Button)findViewById(R.id.btnPlanner);
        btnOrganizer = (Button) findViewById(R.id.btnOrganizer);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        database = FirebaseDatabase.getInstance();
        table_user = database.getReference("User");
        table_category = database.getReference("Category");
        categoryNameList.add("Choose category");

        table_category.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                    if(dataSnapshot.exists()){
                        Category item = postSnapshot.getValue(Category.class);
                        categoryNameList.add(item.getName());
                        categoryList.add(item);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnPlanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddPlanner();
            }
        });

        btnOrganizer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddOrganizer();
            }
        });
    }

    private void uploadImage() {
        if (saveUri != null) {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            // set image name
            String imageName = UUID.randomUUID().toString();
            // set folder
            final StorageReference imageFolder = storageReference.child("images/" + imageName);

            // uploading image to folder
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(ChooseUser.this, "Uploaded!!!", Toast.LENGTH_SHORT).show();
                            // set value for new Category if image uploaded
                            // and we can get download link 'uri'
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    for(Category catlist : categoryList){
                                        if (categoryName == catlist.getName()){
                                            categoryId = catlist.getId();
                                        }
                                    }
                                    newUser = new User();
                                    //set value for newEvent if image uploaded
                                    newUser.setFirstName(edtFirstName.getText().toString());
                                    newUser.setEmail(edtEmail.getText().toString());
                                    newUser.setPassword(edtPassword.getText().toString());
                                    newUser.setPhone(edtPhone.getText().toString());
                                    newUser.setIsPlanner("true");
                                    newUser.setIsStaff("false");
                                    newUser.setSecureCode(edtSecureCode.getText().toString());
                                    newUser.setStatus("offline");
                                    newUser.setCompanyName(edtCompanyName.getText().toString());
                                    newUser.setCompanyImage(uri.toString());
                                    newUser.setMenuId(categoryId);

                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(ChooseUser.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            // don't worry about this error
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() /
                                    taskSnapshot.getTotalByteCount());
                            mDialog.setMessage("Uploaded " + progress + "%");
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
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Common.PICK_IMAGE_REQUEST);
    }

    private void showAddPlanner(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ChooseUser.this);
        alertDialog.setTitle("Vendor Registration");
        alertDialog.setIcon(R.drawable.ic_person_black_24dp);
        alertDialog.setMessage("Please fill all information");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_home = inflater.inflate(R.layout.add_planner,null);

        edtFirstName = (MaterialEditText)layout_home.findViewById(R.id.edtFirstName);
        edtPhone = (MaterialEditText)layout_home.findViewById(R.id.edtPhone);
        edtPassword = (MaterialEditText)layout_home.findViewById(R.id.edtPassword);
        edtEmail = (MaterialEditText)layout_home.findViewById(R.id.edtEmail);
        edtCompanyName = (MaterialEditText)layout_home.findViewById(R.id.edtCompanyName);
        edtSecureCode = (MaterialEditText)layout_home.findViewById(R.id.edtSecureCode);

        spinner = (MaterialSpinner) layout_home.findViewById(R.id.status_spinner);
        spinner.setItems(categoryNameList);

        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                categoryName = item.toString();
            }
        });

        btnUpload = layout_home.findViewById(R.id.btnUpload);
        btnSelect = layout_home.findViewById(R.id.btnSelect);


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

        alertDialog.setView(layout_home);

        alertDialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {

                dialog.dismiss();


                if(edtFirstName.getText().toString().trim().length() != 0 &&
                        edtCompanyName.getText().toString().trim().length() != 0 &&
                        edtEmail.getText().toString().trim().length() != 0 &&
                        edtPhone.getText().toString().trim().length() != 0 &&
                        edtPassword.getText().toString().trim().length() != 0 &&
                        edtSecureCode.getText().toString().trim().length() != 0 &&
                        !categoryId.isEmpty()) {

                    table_user.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child(edtPhone.getText().toString()).exists()) {
                                dialog.dismiss();
                                Toast.makeText(ChooseUser.this, "Phone number already exist", Toast.LENGTH_SHORT).show();
                            } else {

                                dialog.dismiss();
                                if (newUser != null) {
                                    table_user.child(edtPhone.getText().toString()).setValue(newUser);
                                    Toast.makeText(ChooseUser.this,"Account Registered",Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(ChooseUser.this,"Please fill up all the details",Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });


                } else {
                    Toast.makeText(ChooseUser.this,"Please fill up all the details",Toast.LENGTH_SHORT).show();
                }



            }
        });

        alertDialog.show();
    }

    private void showAddOrganizer(){
        AlertDialog.Builder alertDialog1 = new AlertDialog.Builder(ChooseUser.this);
        alertDialog1.setTitle("Customer Registration");
        alertDialog1.setIcon(R.drawable.ic_person_black_24dp);
        alertDialog1.setMessage("Please fill all information");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_home = inflater.inflate(R.layout.add_organizer,null);

        edtFirstName = (MaterialEditText)layout_home.findViewById(R.id.edtFirstName1);
        edtPhone = (MaterialEditText)layout_home.findViewById(R.id.edtPhone1);
        edtPassword = (MaterialEditText)layout_home.findViewById(R.id.edtPassword1);
        edtEmail = (MaterialEditText)layout_home.findViewById(R.id.edtEmail1);
        edtCompanyName = (MaterialEditText)layout_home.findViewById(R.id.edtCompanyName1);
        edtSecureCode = (MaterialEditText)layout_home.findViewById(R.id.edtSecureCode1);

        alertDialog1.setView(layout_home);
        alertDialog1.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();


                if(edtFirstName.getText().toString().trim().length() != 0 &&
                        edtCompanyName.getText().toString().trim().length() != 0 &&
                        edtEmail.getText().toString().trim().length() != 0 &&
                        edtPhone.getText().toString().trim().length() != 0 &&
                        edtPassword.getText().toString().trim().length() != 0 &&
                        edtSecureCode.getText().toString().trim().length() != 0) {

                    final ProgressDialog mDialog = new ProgressDialog(ChooseUser.this);
                    mDialog.setMessage("Please Wait");
                    mDialog.show();

                    table_user.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child(edtPhone.getText().toString()).exists()) {
                                mDialog.dismiss();
                                Toast.makeText(ChooseUser.this, "Phone number already exist", Toast.LENGTH_SHORT).show();
                            } else {

                                mDialog.dismiss();
                                User user = new User(
                                        edtFirstName.getText().toString(),
                                        edtEmail.getText().toString(),
                                        edtPassword.getText().toString(),
                                        edtPhone.getText().toString(),
                                        "false",
                                        edtSecureCode.getText().toString(),
                                        edtCompanyName.getText().toString(),
                                        "null",
                                        "null");
                                table_user.child(edtPhone.getText().toString()).setValue(user);
                                Toast.makeText(ChooseUser.this, "User Account Registered", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                } else {
                    Toast.makeText(ChooseUser.this,"Please fill up all the details",Toast.LENGTH_SHORT).show();
                }



            }
        });

        alertDialog1.show();
    }

}