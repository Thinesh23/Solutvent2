package example.com.solutvent.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import example.com.solutvent.Adapter.UserAdapter;
import example.com.solutvent.Common.Common;
import example.com.solutvent.Model.Chat;
import example.com.solutvent.Model.Chatlist;
import example.com.solutvent.Model.User;
import example.com.solutvent.R;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;

    private UserAdapter userAdapter;
    private List<User> mUsers;

    DatabaseReference reference,chat;

    private List<Chatlist> usersList;
    private List<String> chats;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        usersList = new ArrayList<>();
        chats = new ArrayList<>();
        chat = FirebaseDatabase.getInstance().getReference("Chats");
        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(Common.currentUser.getPhone());
        chat.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                chats.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);

/*                    if (chat.getSender().equals(Common.currentUser.getPhone())){
                        chats.add(chat.getReceiver());
                    }*/
                    if (chat.getReceiver().equals(Common.currentUser.getPhone())){
                        chats.add(chat.getSender());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    usersList.add(chatlist);

                }

                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    private void chatList() {
        mUsers = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("User");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    for (Chatlist chatlist : usersList){
                        if (Common.currentUser != null && Common.currentUser.getIsStaff().equals("true")){
                            if (!user.getPhone().equals(Common.currentUser.getPhone())) {
                                mUsers.add(user);
                            }
                        } else {
                            if (user.getPhone() != null && user.getPhone().equals(chatlist.getId())){
                                mUsers.add(user);
                            }
                        }

                    }

                    for (String phone : chats){
                        if (user.getPhone() != null && user.getPhone().equals(phone) && !mUsers.contains(user)){
                            mUsers.add(user);
                        }
                    }

                }
                userAdapter = new UserAdapter(getContext(), mUsers, true);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



}
