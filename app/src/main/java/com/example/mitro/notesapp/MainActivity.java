package com.example.mitro.notesapp;

import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth fAuth;
    private RecyclerView mNotesList;
    private GridLayoutManager gridLayoutManager;


    private DatabaseReference fNotesDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNotesList = (RecyclerView) findViewById(R.id.notes_list);

        gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);

        mNotesList.setHasFixedSize(true);
        mNotesList.setLayoutManager(gridLayoutManager);

        /*gridLayoutManager.setReverseLayout(true);
        gridLayoutManager.setStackFromEnd(true);*/

        mNotesList.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(20), true));

        fAuth = FirebaseAuth.getInstance();

        if (fAuth.getCurrentUser() != null) {
            fNotesDatabase = FirebaseDatabase.getInstance().getReference().child("Notes").child(fAuth.getCurrentUser().getUid());
        }

        updateUI();

        loadData();
    }

    @Override
    public void onStart() {
        Query query = fNotesDatabase.orderByKey();
        FirebaseRecyclerAdapter<NoteModel, NoteViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<NoteModel, NoteViewHolder>(
                NoteModel.class,
                R.layout.single_note_layout,
                NoteViewHolder.class,
                query
        ) {
            @Override
            protected void populateViewHolder(final NoteViewHolder viewHolder, NoteModel model, int position) {
                final String noteId = getRef(position).getKey();

                fNotesDatabase.child(noteId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("title") && dataSnapshot.hasChild("timestamp") && dataSnapshot.hasChild("type")) {
                            String title = dataSnapshot.child("title").getValue().toString();
                            String timestamp = dataSnapshot.child("timestamp").getValue().toString();
                            String type = dataSnapshot.child("type").getValue().toString();
                            String url = dataSnapshot.child("imageUrl").getValue().toString();
                            viewHolder.setNoteTitle(title);
                            //viewHolder.setNoteTime(timestamp);

                            GetTime getTime = new GetTime();
                            viewHolder.setNoteTime(getTime.getTime(Long.parseLong(timestamp), getApplicationContext()));

                            switch (type) {
                                case "1":
                                    viewHolder.setNoteType(url);
                                    viewHolder.noteCard.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        //перевірка типу запису
                                        public void onClick(View view) {
                                            Intent intent = new Intent(MainActivity.this, NoteViewActivity.class);
                                            intent.putExtra("noteId", noteId);
                                            startActivity(intent);
                                        }
                                    });
                                    break;
                                case "2":
                                    viewHolder.setNoteType(url);
                                    viewHolder.noteCard.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        //перевірка типу запису
                                        public void onClick(View view) {
                                            Intent intent = new Intent(MainActivity.this, ImageNoteViewActivity.class);
                                            intent.putExtra("noteId", noteId);
                                            startActivity(intent);
                                        }
                                    });
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        mNotesList.setAdapter(firebaseRecyclerAdapter);
        super.onStart();
    }

    private void loadData(){

    }

    private void updateUI(){

        if (fAuth.getCurrentUser() != null){
            Log.i("MainActivity", "fAuth != null");
        } else {
            Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
            startActivity(startIntent);
            finish();
            Log.i("MainActivity", "fAuth == null");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.main_new_note_btn:
                Intent newIntent = new Intent(MainActivity.this, NewNoteActivity.class);
                startActivity(newIntent);
                break;
            case R.id.main_new_image_note_btn:
                Intent newIntent1 = new Intent(MainActivity.this, NewImageNoteActivity.class);
                startActivity(newIntent1);
                break;
        }
        return true;
    }


    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
}
