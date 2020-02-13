package example.com.solutvent;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import example.com.solutvent.Adapter.ChatAdapter;
import example.com.solutvent.Common.Common;
import example.com.solutvent.Model.Chat;
import example.com.solutvent.Model.DataMessage;
import example.com.solutvent.Model.MyResponse;
import example.com.solutvent.Model.Token;
import example.com.solutvent.Model.User;
import example.com.solutvent.Remote.APIService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChattingActivity extends AppCompatActivity {

    TextView username;

    ImageButton btn_send;
    EditText text_send;

    ChatAdapter chatAdapter;
    RecyclerView recyclerView;
    List<Chat> mchats;

    String userId;

    DatabaseReference table_chat;

    Intent intent;

    ValueEventListener seenListener;

    APIService mService;

    boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChattingActivity.this, ChattingMenu.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        username = findViewById(R.id.txtUserName);
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.txt_send);

        mService = Common.getFCMService();


        intent = getIntent();
        userId = intent.getStringExtra("userId");

        //clicks send button, make sure message not empty before sending.
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String msg = text_send.getText().toString();
                if(!msg.equals("")){
                    sendMessage(Common.currentUser.getPhone(), userId, msg);
                } else {
                    Toast.makeText(ChattingActivity.this, "You can't send empty messages", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });


        DatabaseReference table_user = FirebaseDatabase.getInstance().getReference("User");
        table_user.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getFirstName());

                readMessages(Common.currentUser.getPhone(), userId);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        seenMessage(userId);
    }

    //update when message is seen
    private void seenMessage(final String userId){
        DatabaseReference table_chat = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = table_chat.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(Common.currentUser.getPhone()) && chat.getSender().equals(userId)){
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("isSeen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //send message
    private void sendMessage (String sender, String receiver, String message){

        DatabaseReference chatmessage = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message", message);
        hashMap.put("isSeen",false);

        //set the hashmap value to the child in chat table
        chatmessage.child("Chats").push().setValue(hashMap);

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(Common.currentUser.getPhone())
                .child(userId);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            //set the id of the receiver under sender in chatlist
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    chatRef.child("id").setValue(userId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final String msg = message;

        sendNotification(receiver, Common.currentUser.getFirstName(), msg);
        notify = false;
    }

    //send notification using FCM (firebase cloud messaging)
    private void sendNotification(String receiver, final String name, final String message){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference tokens = database.getReference("Tokens");
        tokens.orderByKey().equalTo(receiver)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapShot:dataSnapshot.getChildren()){
                            Token token = postSnapShot.getValue(Token.class);

                            //check the token and send the message to the receiver
                            Map<String,String> dataSend = new HashMap<>();
                            dataSend.put("title","New Message");
                            dataSend.put("message", message + " from " + name);
                            DataMessage dataMessage = new DataMessage(token.getToken(),dataSend);

                            mService.sendNotification(dataMessage)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                            if(response.body().success != 1){
                                                Toast.makeText(ChattingActivity.this, "Failed to send notification", Toast.LENGTH_SHORT).show();
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

    //function to read message
    private void readMessages (final String myid, final String userid){
        mchats = new ArrayList<>();

        table_chat = FirebaseDatabase.getInstance().getReference("Chats");
        table_chat.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mchats.clear();
                //add chats to the layout and set them to left and right
                //based on receiver and sender
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    Chat chat = data.getValue(Chat.class);
                    if(chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(myid)){
                        mchats.add(chat);
                    }

                    chatAdapter = new ChatAdapter(ChattingActivity.this, mchats);
                    recyclerView.setAdapter(chatAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

/*    private void status (String status){
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

    @Override
    protected void onResume(){
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause(){
        super.onPause();
        table_chat.removeEventListener(seenListener);
        status("offline");
    }*/
}
