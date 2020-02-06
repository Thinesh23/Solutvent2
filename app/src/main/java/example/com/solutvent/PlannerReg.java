package example.com.solutvent;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class PlannerReg extends AppCompatActivity {
    MaterialEditText edtPlanPhone, edtPlanName, edtPlanPassword, edtPlanSecureCode, edtPlanAddress, edtPlanEmail, edtPlanPrice;
    Button btnSignUp, btnSelect, btnUpload;
    MaterialSpinner state_select, category_select;
    Uri saveUri;
    User newUser;

    String categoryId = "";
    String categoryName = "";
    String stateName = "";

    boolean imageSelect = false, imageUpload = false;

    FirebaseStorage storage;
    StorageReference storageReference;

    FirebaseDatabase database;
    DatabaseReference table_user, table_category;

    List<String> categoryNameList = new ArrayList<>();
    List<Category> categoryList = new ArrayList<>();
    List<String> stateList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planner_reg);

        btnSignUp = (Button)findViewById(R.id.btnPlannerReg);
        edtPlanPhone = (MaterialEditText) findViewById(R.id.edtPlanPhone);
        edtPlanName = (MaterialEditText) findViewById(R.id.edtPlanName);
        edtPlanPassword = (MaterialEditText) findViewById(R.id.edtPlanPassword);
        edtPlanSecureCode = (MaterialEditText) findViewById(R.id.edtPlanSecureCode);
        edtPlanAddress = (MaterialEditText) findViewById(R.id.edtPlanAdd);
        edtPlanEmail = (MaterialEditText) findViewById(R.id.edtPlanEmail);
        edtPlanPrice = (MaterialEditText) findViewById(R.id.edtPlanPrice);
        state_select = (MaterialSpinner) findViewById(R.id.state_select);
        category_select = (MaterialSpinner) findViewById(R.id.category_select);

        database = FirebaseDatabase.getInstance();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        table_user = database.getReference("User");
        table_category = database.getReference("Category");
        categoryNameList.add("Choose Category");
        stateList.add("Choose State");
        stateList.add("KL");
        stateList.add("Selangor");

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

        category_select.setItems(categoryNameList);
        category_select.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                categoryName = item.toString();
            }
        });
        state_select.setItems(stateList);
        state_select.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                stateName = item.toString();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Common.isConnectedToInternet(getBaseContext())) {

                    if (edtPlanName.getText().toString().trim().length() != 0 &&
                            edtPlanAddress.getText().toString().trim().length() != 0 &&
                            edtPlanEmail.getText().toString().trim().length() != 0 &&
                            edtPlanPhone.getText().toString().trim().length() != 0 &&
                            edtPlanPrice.getText().toString().trim().length() != 0 &&
                            edtPlanPassword.getText().toString().trim().length() != 0 &&
                            edtPlanSecureCode.getText().toString().trim().length() != 0 &&
                            !stateName.equals("Choose State")) {
                        image_upload();

                    } else {
                        Toast.makeText(PlannerReg.this,"Please fill up all the details",Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(PlannerReg.this,"Please check your connection !!", Toast.LENGTH_SHORT).show();
                    return;
                }
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
                            Toast.makeText(PlannerReg.this, "Uploaded!!!", Toast.LENGTH_SHORT).show();
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
                                    newUser.setFirstName(edtPlanName.getText().toString());
                                    newUser.setEmail(edtPlanEmail.getText().toString());
                                    newUser.setPassword(edtPlanPassword.getText().toString());
                                    newUser.setPhone(edtPlanPhone.getText().toString());
                                    newUser.setIsPlanner("true");
                                    newUser.setIsStaff("false");
                                    newUser.setSecureCode(edtPlanSecureCode.getText().toString());
                                    newUser.setStatus("offline");
                                    newUser.setCompanyImage(uri.toString());
                                    newUser.setAddress(edtPlanAddress.getText().toString());
                                    newUser.setState(stateName);
                                    newUser.setPrice(edtPlanPrice.getText().toString());
                                    newUser.setMenuId(categoryId);

                                }
                            });
                            imageUpload = true;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(PlannerReg.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            imageUpload = false;
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
            imageSelect = true;
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Common.PICK_IMAGE_REQUEST);
    }

    private void image_upload(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(PlannerReg.this);
        alertDialog.setTitle("Select and Upload Image");
        alertDialog.setIcon(R.drawable.ic_image_black_24dp);
        alertDialog.setMessage("Please choose Image");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_home = inflater.inflate(R.layout.image_upload, null);

        btnUpload = (Button) layout_home.findViewById(R.id.btnUpload);
        btnSelect = (Button) layout_home.findViewById(R.id.btnSelect);


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
        alertDialog.setPositiveButton("Complete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final ProgressDialog mDialog = new ProgressDialog(PlannerReg.this);
                mDialog.setMessage("Please Wait");
                mDialog.show();
                table_user.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(imageSelect && imageUpload && !categoryId.isEmpty()) {

                            if (dataSnapshot.child(edtPlanPhone.getText().toString()).exists()) {
                                Toast.makeText(PlannerReg.this, "Phone number already exist", Toast.LENGTH_SHORT).show();
                            } else {
                                if (newUser != null) {
                                    table_user.child(edtPlanPhone.getText().toString()).setValue(newUser);
                                    Toast.makeText(PlannerReg.this,"Account Registered",Toast.LENGTH_SHORT).show();
                                    mDialog.dismiss();
                                    finish();
                                } else {
                                    Toast.makeText(PlannerReg.this,"Please fill up all the details",Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
