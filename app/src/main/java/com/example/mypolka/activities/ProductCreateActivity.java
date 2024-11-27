package com.example.mypolka.activities;


import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mypolka.R;
import com.example.mypolka.Utils;
import com.example.mypolka.adapters.AdapterImagesPicked;
import com.example.mypolka.databinding.ActivityProductCreateBinding;
import com.example.mypolka.models.ModelImagePicked;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProductCreateActivity extends AppCompatActivity {

    private ActivityProductCreateBinding binding;

    private static final String TAG = "PRODUCT_CREATE_TAG";

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    private Uri imageUri = null;

    private ArrayList<ModelImagePicked> imagePickedArrayList;
    private AdapterImagesPicked adapterImagesPicked;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Пожалуйста подождите...");
        progressDialog.setCanceledOnTouchOutside(false);


        firebaseAuth = FirebaseAuth.getInstance();

        ArrayAdapter<String> adapterCategories = new ArrayAdapter<>(this, R.layout.row_category_act, Utils.categories);
        binding.categoryAct.setAdapter(adapterCategories);

        imagePickedArrayList = new ArrayList<>();
        loadImages();


        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.toolbarProductImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickOptions();
            }
        });

        binding.postProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });



    }
    private void loadImages() {
        Log.d(TAG, "LoadImages: ");

        adapterImagesPicked = new AdapterImagesPicked(this, imagePickedArrayList);

        binding.imagesRv.setAdapter(adapterImagesPicked);

    }

    private void showImagePickOptions(){
        Log.d(TAG, "showImagePickOptions: ");

        PopupMenu popupMenu = new PopupMenu(this, binding.toolbarProductImageBtn);

        popupMenu.getMenu().add(Menu.NONE, 2,2,"Gallery");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem Item) {
                int itemId = Item.getItemId();
                if (itemId == 1){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        String cameraPermissions[] = new String[]{Manifest.permission.CAMERA};
                        requestCameraPermisssions.launch(cameraPermissions);
                    }else{
                        String cameraPermissions[] = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestCameraPermisssions.launch(cameraPermissions);
                    }
                } else if (itemId == 2){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        pickImageGallery();
                    }else{
                        String storagePermissions[] = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestCameraPermisssions.launch(storagePermissions);
                    }
                }
                return true;
            }
        });
    }
    private ActivityResultLauncher<String> requestStoragePermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean o) {
                    Log.d(TAG, "onActivityResult: isGranted: "+o);

                    if (o){
                        pickImageGallery();
                    }else {

                        Utils.toast(ProductCreateActivity.this, "Ошибка хранилища...");
                    }
                }
            }
    );

    private ActivityResultLauncher<String[]> requestCameraPermisssions = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> o) {
                    Log.d(TAG, "onActivityResult: ");
                    Log.d(TAG, "onActivityResult: "+ o.toString());

                    boolean areAllGranted = true;
                    for (Boolean isGranted : o.values()){
                        areAllGranted = areAllGranted && isGranted;
                    }

                    if (areAllGranted){
                        pickImageCamera();
                    }else {
                        Utils.toast(ProductCreateActivity.this, "Ошибка камеры или хранилища...");
                    }
                }
            }
    );

    private void pickImageGallery(){
        Log.d(TAG, "pickImageGallery: ");

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private void pickImageCamera(){
        Log.d(TAG, "pickImageCamera: ");

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "TEMPORARY_IMAGE");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TEMPORARY_IMAGE_DESCRIPTION");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    Log.d(TAG, "onActivityResult: ");
                    if (o.getResultCode() == Activity.RESULT_OK){
                        Intent data = o.getData();

                        imageUri = data.getData();

                        Log.d(TAG, "onActivityResult: imageUri "+ imageUri);

                        String timestamp = ""+ Utils.getTimestamp();

                        ModelImagePicked modelImagePicked = new ModelImagePicked(timestamp, imageUri, null, false);
                        imagePickedArrayList.add(modelImagePicked);

                        loadImages();
                    }else {
                        Utils.toast(ProductCreateActivity.this, "Canceled...");
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    Log.d(TAG, "onActivityResult: ");
                    if (o.getResultCode() == Activity.RESULT_OK){

                        Log.d(TAG, "onActivityResult: imageUri "+ imageUri);

                        String timestamp = ""+ Utils.getTimestamp();

                        ModelImagePicked modelImagePicked = new ModelImagePicked(timestamp, imageUri, null, false);
                        imagePickedArrayList.add(modelImagePicked);

                        loadImages();
                    }else {
                        Utils.toast(ProductCreateActivity.this, "Canceled...");
                    }
                }
            }
    );

    private String category = "";
    private String price = "";
    private String title = "";
    private String description = "";

    private void validateData(){
        Log.d(TAG, "validateData: ");

        category = binding.categoryAct.getText().toString().trim();
        price = binding.priceEt.getText().toString().trim();
        title = binding.titleEt.getText().toString().trim();
        description = binding.descEt.getText().toString().trim();
        if (category.isEmpty()) {
            binding.categoryAct.setError("Выберите категорию");
            binding.categoryAct.requestFocus();
        }else if (price.isEmpty()){
            binding.priceEt.setError("Напишите цену");
            binding.priceEt.requestFocus();
        }else if (title.isEmpty()){
            binding.titleEt.setError("Напишите заголовок");
            binding.titleEt.requestFocus();
        }else if (description.isEmpty()){
            binding.descEt.setError("Напишите описание");
            binding.descEt.requestFocus();
        }else if (imagePickedArrayList.isEmpty()){
            Utils.toast(this, "Выберите фотографию для товара");
        }else {
            postProduct();
        }
    }
    private void postProduct(){
        Log.d(TAG, "postProduct: ");

        progressDialog.setMessage("Публикую продукт");
        progressDialog.show();

        long timestamp = Utils.getTimestamp();
        DatabaseReference refProduct = FirebaseDatabase.getInstance().getReference("Products");
        String keyId = refProduct.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", ""+ keyId);
        hashMap.put("uid", ""+ firebaseAuth.getUid());
        hashMap.put("category", ""+ category);
        hashMap.put("price", ""+ price);
        hashMap.put("title", ""+ title);
        hashMap.put("description", ""+ description);
        hashMap.put("timestamp", timestamp);


        refProduct.child(keyId)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        uploadImageStorage(keyId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Utils.toast(ProductCreateActivity.this, "Ошибка добавления в базу данных"+e.getMessage());
                    }
                });

    }
    private void uploadImageStorage(String adId){
        for (int i=0;i<imagePickedArrayList.size(); i++){
            ModelImagePicked modelImagePicked = imagePickedArrayList.get(i);
            String imageName = modelImagePicked.getId();
            String filePathAndName = "Products/"+ imageName;
            int imageIndexForProgress = i+1;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);

            storageReference.putFile(modelImagePicked.getImageUri())
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();

                            String message = "Uploading " + imageIndexForProgress + " of " + imagePickedArrayList.size() + " images... \nProgress " + (int) progress + "%";

                            progressDialog.setMessage(message);
                            progressDialog.show();
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()){
                                // Wait for the download URL to become available
                            }
                            Uri uploadedImageUrl = uriTask.getResult();

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("id", ""+modelImagePicked.getImageUri());
                            hashMap.put("imageUrl", ""+uploadedImageUrl);

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Products").child(adId).child("Images").child(imageName);
                            ref.setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "Image uploaded successfully");
                                            progressDialog.dismiss();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "Error uploading image: " + e.getMessage());
                                            progressDialog.dismiss();

                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Error uploading image: " + e.getMessage());
                            progressDialog.dismiss();

                        }
                    });
        }
    }
}