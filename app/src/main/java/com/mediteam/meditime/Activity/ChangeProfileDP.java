package com.mediteam.meditime.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mediteam.meditime.R;
import com.squareup.picasso.Picasso;

public class ChangeProfileDP extends AppCompatActivity {

    private ImageButton backBTN;
    private Button openFile, uploadFile;
    private ImageView dpView;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private StorageReference storageReference;
    private FirebaseUser firebaseUser;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri uriImage;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_profile_dp);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("DisplayPic");

        backBTN = findViewById(R.id.backProfile);
        openFile = findViewById(R.id.openFile);
        uploadFile = findViewById(R.id.uploadFile);
        dpView = findViewById(R.id.dpView);
        progressBar = findViewById(R.id.uploadProgress);

        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                onBackPressed();
            }
        });

        Uri uri = firebaseUser.getPhotoUrl();
        Picasso.get()
                .load(uri)
                .placeholder(R.drawable.display_pic)
                .resize(293, 293)
                .centerCrop()
                .into(dpView);

        openFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                openFileManager();
            }
        });

        uploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                //start progress bar
                uploadFile.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                UploadPic();
            }
        });
    }

    private void UploadPic () {
        if(uriImage != null){
            StorageReference fileReference = storageReference.child(firebaseUser.getUid() + "/displayPic."
            + getFileExtension(uriImage));

            fileReference.putFile(uriImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess (UploadTask.TaskSnapshot taskSnapshot) {
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess (Uri uri) {
                            Uri downloadUri = uri;

                            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(downloadUri).build();
                            firebaseUser.updateProfile(profileUpdate);
                        }
                    });
                    // stop progress bar
                    progressBar.setVisibility(View.GONE);
                    uploadFile.setVisibility(View.VISIBLE);

                    Toast.makeText(ChangeProfileDP.this, "Upload Successful!",
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ChangeProfileDP.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure (@NonNull Exception e) {
                    Toast.makeText(ChangeProfileDP.this, e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    uploadFile.setVisibility(View.VISIBLE);
                }
            });
        } else {
            //stop progress bar
            progressBar.setVisibility(View.GONE);
            uploadFile.setVisibility(View.VISIBLE);

            Toast.makeText(ChangeProfileDP.this, "Select photo first",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension (Uri uriImage) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uriImage));
    }

    private void openFileManager() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            uriImage = data.getData();
            dpView.setImageURI(uriImage);
        }
    }
}