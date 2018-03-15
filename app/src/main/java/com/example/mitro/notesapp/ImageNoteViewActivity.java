package com.example.mitro.notesapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class ImageNoteViewActivity extends AppCompatActivity {

    private TextView etTitle;
    private ImageView etImage;
    private ProgressBar mProgressCircle;


    private String noteID;

    private String imageUrl;
    private String title;

    private FirebaseAuth fAuth;
    private DatabaseReference mDatabaseRef;
    private FirebaseStorage mStorage;
    private ValueEventListener mDBListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_note_view);

        try {
            noteID = getIntent().getStringExtra("noteId");
        } catch (Exception e) {
            e.printStackTrace();
        }

        etTitle = findViewById(R.id.new_note_title);
        etImage = findViewById(R.id.image_view);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageView();
            }
        });

        fAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Notes").child(fAuth.getCurrentUser().getUid());
        mStorage = FirebaseStorage.getInstance();

        putData();
    }

    private void openImageView() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(imageUrl), "image/*");
        startActivity(intent);
    }

    private void putData() {
        mDBListener = mDatabaseRef.child(noteID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("title") && dataSnapshot.hasChild("content")) {
                    title = dataSnapshot.child("title").getValue().toString();
                    imageUrl = dataSnapshot.child("imageUrl").getValue().toString();

                    etTitle.setText(title);
                    Picasso.get().load(imageUrl).fit().centerCrop().into(etImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ImageNoteViewActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.new_note_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.new_note_delete_btn:
                    deleteNote();
                break;
            case R.id.new_note_edit_btn:
                Intent intent = new Intent(ImageNoteViewActivity.this, NewImageNoteActivity.class);
                intent.putExtra("noteId", noteID);
                startActivity(intent);
                break;
        }

        return true;
    }

    private void deleteNote() {

        StorageReference imageRef = mStorage.getReferenceFromUrl(imageUrl);
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mDatabaseRef.child(noteID).removeValue();
                Toast.makeText(ImageNoteViewActivity.this, "Запис Видалено", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabaseRef.removeEventListener(mDBListener);
    }
}
