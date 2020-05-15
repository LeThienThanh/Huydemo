package com.example.th2khungchohuy;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference mref;
    StorageReference msto;

    ImageView imgclothe, imgcustom, imgtest;
    Button btnclothe, btncustom, btntest;
    private static final int    CUSTOM_IMG= 1;
    private static final int    CLOTHE_IMG= 2;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imgclothe = findViewById(R.id.imgclothe);
        imgcustom = findViewById(R.id.imgcustom);
        imgtest = findViewById(R.id.imgtestclothe);

        btnclothe = findViewById(R.id.btnclothe);
        btncustom = findViewById(R.id.btncustom);
        btntest = findViewById(R.id.btntestclothe);

        mAuth = FirebaseAuth.getInstance();
        mref = FirebaseDatabase.getInstance().getReference();
        msto = FirebaseStorage.getInstance().getReference();

        /// Click vào sẽ vào thư mục chọn ảnh
        btncustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                MainActivity.this.startActivityForResult(intent, CUSTOM_IMG);
            }
        });

        /// Click vào sẽ vào thư mục chọn ảnh
        btnclothe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                MainActivity.this.startActivityForResult(intent, CLOTHE_IMG);
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            /// Hủy tác vụ
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();

                /// chọn ảnh của custom
            } else if (requestCode == CUSTOM_IMG){
                imageUri = data.getData();
                Picasso.get().load(imageUri.toString()).into(imgcustom);
                upload("custom",imgcustom);

                /// chọn ảnh của clothe
            } else if (requestCode == CLOTHE_IMG){
                imageUri = data.getData();
                Picasso.get().load(imageUri.toString()).into(imgclothe);
                upload("clothe",imgclothe);
            }
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void upload(String type, ImageView img){
        //// xử lí ảnh
        img.setDrawingCacheEnabled(true);
        img.buildDrawingCache();

        @SuppressLint({"NewApi", "LocalSuppress"}) Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(Objects.requireNonNull(MainActivity.this).getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        //// tạo đường link
        final StorageReference filepath = msto.child(type).child(imageUri.getLastPathSegment());

        /// push lên firebase
        filepath.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(MainActivity.this,"Post successful", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"Post Fail", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
