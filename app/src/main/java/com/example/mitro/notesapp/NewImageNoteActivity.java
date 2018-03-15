package com.example.mitro.notesapp;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.browse.MediaBrowser;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class NewImageNoteActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 1;

    private Button mButtonChooseImage;
    //private Button mButtonCaptureImage;
    private Button mButtonUpload;

    private EditText mEditTextFileName;
    private ImageView mImageView;
    private ProgressBar mProgressBar;

    private Uri mImageUri;

    private FirebaseAuth fAuth;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private FirebaseStorage mStorage;
    private ValueEventListener mDBListener;

    private StorageTask mUploadTask;

    private boolean isExist;
    private String noteID;
    private Menu mainMenu;

    private String imageUrl;
    private String title;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_image_note);

        try {
            noteID = getIntent().getStringExtra("noteId");
            if (!noteID.trim().equals("")) {
                isExist = true;
            } else {
                //mainMenu.findItem(1).setVisible(false);
                isExist = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mButtonChooseImage = findViewById(R.id.button_choose_image);
        //mButtonCaptureImage = findViewById(R.id.button_capture_image);
        mButtonUpload = findViewById(R.id.new_note_btn);
        //mTextViewShowUploads = findViewById(R.id.text_view_show_uploads);
        mEditTextFileName = findViewById(R.id.new_note_title);
        mImageView = findViewById(R.id.image_view);
        mProgressBar = findViewById(R.id.progress_bar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Notes").child(fAuth.getCurrentUser().getUid());
        mStorageRef = FirebaseStorage.getInstance().getReference().child("Uploads").child(fAuth.getCurrentUser().getUid());
        mStorage = FirebaseStorage.getInstance();

        mButtonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        /*mButtonCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageCapture();
            }
        });*/

        mButtonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mEditTextFileName.getText().toString().trim();

                if (!TextUtils.isEmpty(title)) {
                    if (mUploadTask != null && mUploadTask.isInProgress()) {
                        Toast.makeText(NewImageNoteActivity.this, "Завантаження ще не закінчено", Toast.LENGTH_SHORT).show();
                    } else {
                        uploadFile();
                    }
                } else {
                    Toast.makeText(NewImageNoteActivity.this, "Невказано заголовок", Toast.LENGTH_SHORT).show();
                }

            }
        });


        putData();
    }

    private void putData() {
        if (isExist) {
            mDBListener = mDatabaseRef.child(noteID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.hasChild("title") && dataSnapshot.hasChild("content")) {
                        title = dataSnapshot.child("title").getValue().toString();
                        imageUrl = dataSnapshot.child("imageUrl").getValue().toString();

                        mEditTextFileName.setText(title);
                        Picasso.get().load(imageUrl).into(mImageView);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(NewImageNoteActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /*private void openImageCapture() {
        /*Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,CAMERA_REQUEST);*/
        /*Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAMERA_REQUEST);
        }
    }*/

    /*private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        //inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();

            Picasso.get().load(mImageUri).fit().centerCrop().into(mImageView);
        }/*else if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bitmap asfd = (Bitmap) data.getExtras().get("data");
            mImageUri = data.getData();
            //mImageView.setImageBitmap(asfd);
            Picasso.get().load(mImageUri).fit().centerCrop().into(mImageView);
        }*/
    }



    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile() {

        if (mImageUri != null) {

            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));

            mUploadTask = fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setProgress(0);
                                }
                            }, 500);

                            //Toast.makeText(NewImageNoteActivity.this, "Завантаження завершено успішно", Toast.LENGTH_LONG).show();

                            String title = mEditTextFileName.getText().toString().trim();

                            if (fAuth.getCurrentUser() != null) {
                                final DatabaseReference newNoteRef = mDatabaseRef.push();

                                final Map noteMap = new HashMap();
                                noteMap.put("title", title);
                                noteMap.put("content", "null");
                                noteMap.put("timestamp", ServerValue.TIMESTAMP);
                                noteMap.put("type", "2");
                                noteMap.put("imageUrl", taskSnapshot.getDownloadUrl().toString());

                                if(isExist){
                                    StorageReference imageRef = mStorage.getReferenceFromUrl(imageUrl);
                                    imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mDatabaseRef.child(noteID).updateChildren(noteMap);
                                            Toast.makeText(NewImageNoteActivity.this, "Запис змінено!", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    });

                                }else{
                                    Thread mainThread = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            newNoteRef.setValue(noteMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(NewImageNoteActivity.this, "Запис додано", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    } else {
                                                        Toast.makeText(NewImageNoteActivity.this, "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }

                                                }
                                            });
                                        }
                                    });
                                    mainThread.start();
                                }
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(NewImageNoteActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mProgressBar.setProgress((int) progress);
                        }
                    });
        } else {
            if(isExist){
            String title = mEditTextFileName.getText().toString().trim();
            if (fAuth.getCurrentUser() != null) {
                final DatabaseReference newNoteRef = mDatabaseRef.push();

                final Map noteMap = new HashMap();
                noteMap.put("title", title);
                noteMap.put("content", "null");
                noteMap.put("timestamp", ServerValue.TIMESTAMP);
                noteMap.put("type", "2");
                noteMap.put("imageUrl", imageUrl);

                    StorageReference imageRef = mStorage.getReferenceFromUrl(imageUrl);

                            mDatabaseRef.child(noteID).updateChildren(noteMap);
                            Toast.makeText(NewImageNoteActivity.this, "Запис змінено!", Toast.LENGTH_SHORT).show();
                            finish();
                }
            }else {
                Toast.makeText(this, "Зображення не вибрано", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.new_note_menu, menu);

        if(!isExist){
            MenuItem delBtn = menu.getItem(0);
            MenuItem edBtn = menu.getItem(1);
            delBtn.setVisible(false);
            edBtn.setVisible(false);
        }else{
            MenuItem edBtn = menu.getItem(1);
            edBtn.setVisible(false);
        }

        mainMenu=menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
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
                Toast.makeText(NewImageNoteActivity.this, "Запис Видалено", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
