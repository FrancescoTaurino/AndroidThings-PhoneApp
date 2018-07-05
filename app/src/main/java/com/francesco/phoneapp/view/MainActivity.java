package com.francesco.phoneapp.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.francesco.phoneapp.R;
import com.francesco.phoneapp.viewmodel.MainActivityVM;
import com.francesco.phoneapp.viewmodel.Message;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.LinkedHashMap;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private static final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("messages");

    private EditText editText;
    private RecyclerView recyclerView;

    private MainActivityVM mainActivityVM;
    private MessagesAdapter messagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);
        recyclerView = findViewById(R.id.recyclerView);

        mainActivityVM = ViewModelProviders.of(this).get(MainActivityVM.class);
        messagesAdapter = new MessagesAdapter(mainActivityVM);

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(messagesAdapter);
        messagesAdapter.notifyDataSetChanged();

        mainActivityVM.getLiveDataMessagesMap().observe(this, new Observer<LinkedHashMap<String, Message>>() {
            @Override
            public void onChanged(@Nullable LinkedHashMap<String, Message> stringMessageLinkedHashMap) {
                messagesAdapter.notifyDataSetChanged();
            }
        });

        dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mainActivityVM.addMessage(dataSnapshot.getKey(), dataSnapshot.getValue(Message.class));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                mainActivityVM.updateMessage(dataSnapshot.getKey(), dataSnapshot.getValue(Message.class));
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                mainActivityVM.removeMessage(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onCancelled: nothing to do");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
            }
        });
    }


    public void send(View view) {
        String text = editText.getText().toString();

        if (!mainActivityVM.messagesMapIsFull()) {
            if (text.matches("^[a-zA-Z\\s]+$")) {
                dbRef.push().setValue(new Message(text.toUpperCase(), false));
                editText.setText("");
                Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_LONG).show();
            }
            else
                Toast.makeText(getApplicationContext(), "Invalid message", Toast.LENGTH_LONG).show();
        }
        else
            Toast.makeText(getApplicationContext(), "Registry full", Toast.LENGTH_LONG).show();
    }
}
