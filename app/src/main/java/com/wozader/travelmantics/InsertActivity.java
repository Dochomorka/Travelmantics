package com.wozader.travelmantics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import static com.wozader.travelmantics.Constants.RC_PIC_IMAGE;

public class InsertActivity extends AppCompatActivity {


    TravelDeal mDeal;

    private ConstraintLayout mConstriantLayout;
    private TextInputLayout mTitle;
    private TextInputLayout mDescription;
    private TextInputLayout mPrice;

    private ImageView imageView;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mFirebaseDatabase = FirebaseUtils.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtils.mDatabaseReference;

        imageView = findViewById(R.id.dealImageView);
        mConstriantLayout = findViewById(R.id.rootView);

        mTitle = findViewById(R.id.titleTextInputLayout);
        mDescription = findViewById(R.id.descriptionTextInputLayout);
        mPrice = findViewById(R.id.priceTextInputLayout);

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> dataSnapshots = dataSnapshot.getChildren();

                for (DataSnapshot s : dataSnapshots) {
                    TravelDeal travelDeal = s.getValue(TravelDeal.class);

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("deal");
        if (deal == null) {
            deal = new TravelDeal();
        }

        this.mDeal = deal;
        mTitle.getEditText().setText(mDeal.getTitle());
        mDescription.getEditText().setText(mDeal.getDescription());
        mPrice.getEditText().setText(mDeal.getPrice());
        showImage(mDeal.getImageUrl());
        Button button = findViewById(R.id.button);
        final ImageView imageButton = findViewById(R.id.dealImageView);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent imageIntent = new Intent(Intent.ACTION_GET_CONTENT);
                imageIntent.setType("image/jpeg");
                imageIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(imageIntent, "Pic the image"), RC_PIC_IMAGE);
            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PIC_IMAGE) {
            if (resultCode == RESULT_OK) {

                Uri imageUri = data.getData();
                final StorageReference ref = FirebaseUtils.mStorageReference.child(imageUri.getLastPathSegment());
                ref.putFile(imageUri)
                        .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                String url = ref.getDownloadUrl().toString();
                                String pictureName = taskSnapshot.getStorage().getPath();
                                mDeal.setImageUrl(url);
                                mDeal.setImageName(pictureName);

                                showImage(url);
                            }
                        });


            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem saveItem = menu.findItem(R.id.save);
        MenuItem deleteItem = menu.findItem(R.id.delete);

        if (FirebaseUtils.isAdmin == true) {
            saveItem.setVisible(true);
            deleteItem.setVisible(true);
            enableEditText(true);
        } else {
            saveItem.setVisible(false);
            deleteItem.setVisible(false);
            enableEditText(false);
        }
        return true;
    }

    public void enableEditText(boolean isAdmin) {
        mTitle.getEditText().setEnabled(isAdmin);
        mDescription.getEditText().setEnabled(isAdmin);
        mPrice.getEditText().setEnabled(isAdmin);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.save:
                saveDeal();
                onComplete();
                return true;
            case R.id.delete:
                deleteDeal();
                onComplete();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }


    }

    public void showImage(String url) {
        if (url != null && url.isEmpty() == false) {
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.get().load(url)
                    .resize(width, width * 2 / 3)
                    .centerCrop()
                    .into(imageView);
        }
    }

    private void clear() {
        mDescription.getEditText().setText("");
        mDescription.setError(null);
        mPrice.getEditText().setText("");
        mPrice.setError(null);
        mTitle.getEditText().setText("");
        mTitle.setError(null);
        mTitle.requestFocus();

    }

    private void deleteDeal() {
        if (mDeal != null) {
            mDatabaseReference.child(mDeal.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), " Travel Deal Deleted", Toast.LENGTH_SHORT).show();
                    } else {

                    }
                }
            });
            if (mDeal.getImageName() != null) {
                StorageReference pictureReference =
                        FirebaseUtils.mFirebaseStorage.getReference().child(mDeal.getImageName());
                pictureReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                        }
                    }
                });
            }


        }
    }

    private void onComplete() {
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    private void saveDeal() {
        String title;
        String description;
        String price;

        if (TextUtils.isEmpty(mPrice.getEditText().getText().toString())) {
            mPrice.setError(this.getString(R.string.price_required));
        }
        if (TextUtils.isEmpty(mDescription.getEditText().getText().toString())
        ) {
            mDescription.setError(this.getString(R.string.description_required));
        }
        if (TextUtils.isEmpty(mTitle.getEditText().getText().toString())
        ) {
            mTitle.setError(this.getString(R.string.title_required));
        }

        if (!TextUtils.isEmpty(mTitle.getEditText().getText().toString()) &&
                !TextUtils.isEmpty(mDescription.getEditText().getText().toString()) &&
                !TextUtils.isEmpty(mPrice.getEditText().getText().toString())) {

            mDeal.setTitle(mTitle.getEditText().getText().toString());
            mDeal.setDescription(mDescription.getEditText().getText().toString());
            mDeal.setPrice(mPrice.getEditText().getText().toString());
            if (mDeal.getId() == null) {
                mDatabaseReference.push().setValue(mDeal)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), " Travel Deal Saved", Toast.LENGTH_SHORT).show();
                                } else {

                                }
                            }
                        });
            } else {
                mDatabaseReference.child(mDeal.getId()).setValue(mDeal)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), " Travel Deal Updated", Toast.LENGTH_SHORT).show();
                                } else {
                                    Snackbar.make(mConstriantLayout, "Something went wrong :(", Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

            clear();

        }


        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }


    }
}
