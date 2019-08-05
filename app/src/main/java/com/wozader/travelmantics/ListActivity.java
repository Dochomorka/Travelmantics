package com.wozader.travelmantics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import static com.wozader.travelmantics.Constants.TRAVELDEALS;

public class ListActivity extends AppCompatActivity {


    private RecyclerView mRecyclerView;
    private ListActivityRecyclerViewAdapter mAdapter;
    private LinearLayoutManager mLinearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

         mRecyclerView = findViewById(R.id.dealsRecyclerView);
         mLinearLayoutManager = new LinearLayoutManager(this);
         mAdapter = new ListActivityRecyclerViewAdapter(this);
         mRecyclerView.setLayoutManager(mLinearLayoutManager);
         mRecyclerView.setAdapter(mAdapter);



    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUtils.detachListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUtils.attachListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        new MenuInflater(this).inflate(R.menu.user_menu,menu);
        MenuItem insert = menu.findItem(R.id.add);
        if(FirebaseUtils.isAdmin == true)
        {
            insert.setVisible(true);
        }else{
            insert.setVisible(false);
        }
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.add:
                Intent intent = new Intent(this,InsertActivity.class);
                startActivity(intent);
                return true;
            case R.id.logout:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                               FirebaseUtils.attachListener();
                            }
                        });
                FirebaseUtils.detachListener();

                return true;
              default:
                    super.onOptionsItemSelected(item);
        }
        return true;
    }
    public void showMenu(){
        invalidateOptionsMenu();
    }
}
