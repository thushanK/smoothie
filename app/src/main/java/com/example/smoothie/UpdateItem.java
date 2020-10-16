package com.example.smoothie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UpdateItem extends AppCompatActivity {

    public static final String TAG = "TAG";
    EditText editName, editPrice, editDescription;
    ImageView editProductImage;
    Button btnSave;
    private FirebaseFirestore fStore;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_item);

        Intent data = getIntent();
        final String name = data.getStringExtra("name");
        String price = data.getStringExtra("price");
        String description = data.getStringExtra("description");
//        String imageUri = data.getStringExtra("image");


        editName = findViewById(R.id.txtName);
        editPrice = findViewById(R.id.txtPrice);
        editDescription = findViewById(R.id.txtDescription);
        btnSave = findViewById(R.id.btn_save);
        editProductImage = findViewById(R.id.EditProductImage);

        fStore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        editProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (editName.getText().toString().isEmpty() || editPrice.getText().toString().isEmpty() || editDescription.getText().toString().isEmpty()) {
                    Toast.makeText(UpdateItem.this, "one or many fields are empty", Toast.LENGTH_SHORT).show();
                    return;
                }


                DocumentReference docRef = fStore.collection("item").document(name);

                Map<String, Object> edited = new HashMap<>();
                edited.put("name", editName.getText().toString());
                edited.put("price", editPrice.getText().toString());
                edited.put("description", editDescription.getText().toString());
                edited.put("image", editProductImage.toString());

                docRef.update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(UpdateItem.this, "Item updated", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        finish();

                    }


                });


            }

        });

        editName.setText(name);
        editPrice.setText(price);
        editDescription.setText(description);
//        editProductImage.setImageURI(Uri.parse(imageUri));

        Log.d(TAG, "onCreate: " + name + " " + price + " " + description + " " + imageUri);



    }
    private void openGallery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
//        sendposttodatabase();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null){
            imageUri = data.getData();
            editProductImage.setImageURI(imageUri);
            uploadPicture();
        }
    }
    private void uploadPicture() {

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Uploading Image....");
        pd.show();

        final String randomKey = UUID.randomUUID().toString();
        StorageReference riversRef = storageReference.child("images/" + randomKey);

        riversRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        pd.dismiss();
                        Snackbar.make(findViewById(android.R.id.content),"Image Uploaded", Snackbar.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        pd.dismiss();
                        Toast.makeText(getApplicationContext(),"Failed to Upload",Toast.LENGTH_LONG).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>(){
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progressPercent = (100.00 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        pd.setMessage("Progress " + (int) progressPercent + "%");
                    }


                });
    }
}