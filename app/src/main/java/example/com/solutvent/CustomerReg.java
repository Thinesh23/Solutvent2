package example.com.solutvent;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.List;

import example.com.solutvent.Common.Common;
import example.com.solutvent.Model.User;

public class CustomerReg extends AppCompatActivity {
    MaterialEditText edtCustPhone, edtCustName, edtCustPassword, edtCustSecureCode, edtCustAddress, edtCustEmail;
    Button btnSignUp;
    MaterialSpinner state_select;
    String stateName = "";
    List<String> stateList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_reg);

        btnSignUp = (Button)findViewById(R.id.btnCustomerReg);
        edtCustPhone = (MaterialEditText) findViewById(R.id.edtCustPhone);
        edtCustName = (MaterialEditText) findViewById(R.id.edtCustName);
        edtCustEmail = (MaterialEditText) findViewById(R.id.edtCustEmail);
        edtCustPassword = (MaterialEditText) findViewById(R.id.edtCustPassword);
        edtCustSecureCode = (MaterialEditText) findViewById(R.id.edtCustSecureCode);
        edtCustAddress = (MaterialEditText) findViewById(R.id.edtCustAdd);
        state_select = (MaterialSpinner) findViewById(R.id.state_select);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        //set material spinner location
        stateList.add("Choose State");
        stateList.add("KL");
        stateList.add("Selangor");

        state_select.setItems(stateList);
        //save the selected spinner item to a string
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


                    final ProgressDialog mDialog = new ProgressDialog(CustomerReg.this);
                    mDialog.setMessage("Please Wait");
                    mDialog.show();

                    table_user.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //check if all the text field is not empty
                            if(edtCustName.getText().toString().trim().length() != 0 &&
                                    edtCustAddress.getText().toString().trim().length() != 0 &&
                                    edtCustEmail.getText().toString().trim().length() != 0 &&
                                    edtCustPhone.getText().toString().trim().length() != 0 &&
                                    edtCustPassword.getText().toString().trim().length() != 0 &&
                                    edtCustSecureCode.getText().toString().trim().length() != 0 &&
                                    !stateName.equals("Choose State")) {

                                //if user phone exist then dont register
                                if (dataSnapshot.child(edtCustPhone.getText().toString()).exists()) {
                                    mDialog.dismiss();
                                    Toast.makeText(CustomerReg.this, "Phone number already exist", Toast.LENGTH_SHORT).show();
                                } else {

                                    mDialog.dismiss();
                                    User user = new User(edtCustName.getText().toString(),
                                            edtCustEmail.getText().toString(),
                                            edtCustPassword.getText().toString(),
                                            edtCustPhone.getText().toString(),
                                            "offline",
                                            "false",
                                            "false",
                                            edtCustSecureCode.getText().toString(),
                                            edtCustAddress.getText().toString(),
                                            stateName);
                                    table_user.child(edtCustPhone.getText().toString()).setValue(user);
                                    Toast.makeText(CustomerReg.this, "Account Registered", Toast.LENGTH_SHORT).show();
                                    finish();

                                }

                            } else {
                                Toast.makeText(CustomerReg.this,"Please fill up all the details",Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                } else {
                    Toast.makeText(CustomerReg.this,"Please check your connection !!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }
    //back button will terminate this activity
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
