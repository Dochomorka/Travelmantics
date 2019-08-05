package com.wozader.travelmantics;


import android.content.Intent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.wozader.travelmantics.Constants.TRAVELDEALS;

public class ListActivityRecyclerViewAdapter extends
        RecyclerView.Adapter<ListActivityRecyclerViewAdapter.ViewHolder> {
    ArrayList<TravelDeal> deals;
    FirebaseDatabase mDatabase;
    DatabaseReference mReference;
    ChildEventListener mChildedListener;

    ListActivityRecyclerViewAdapter(ListActivity mActivity) {
        FirebaseUtils.openFbReference(TRAVELDEALS, mActivity);
        mDatabase = FirebaseUtils.mFirebaseDatabase;
        mReference = FirebaseUtils.mDatabaseReference;
        deals = FirebaseUtils.mDeals;
        mChildedListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                TravelDeal td = dataSnapshot.getValue(TravelDeal.class);
                td.setId(dataSnapshot.getKey());
                deals.add(td);
                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mReference.addChildEventListener(mChildedListener);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        TravelDeal travelDeal = deals.get(position);
        holder.bindData(travelDeal);
    }


    @Override
    public int getItemCount() {

        return deals == null ? 0 : deals.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTitleTextView;
        private TextView mDescriptionTextView;
        private TextView mPriceTextView;
        private ImageView mImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTitleTextView = itemView.findViewById(R.id.titleTextview);
            mDescriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            mPriceTextView = itemView.findViewById(R.id.priceTextView);
            mImageView = itemView.findViewById(R.id.dealImageView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent dealIntent = new Intent(view.getContext(), InsertActivity.class);
                    TravelDeal travelDeal = deals.get(getAdapterPosition());
                    dealIntent.putExtra("deal", travelDeal);
                    view.getContext().startActivity(dealIntent);
                }
            });
        }

        public void bindData(TravelDeal travelDeal) {
            mTitleTextView.setText(travelDeal.getTitle());
            mDescriptionTextView.setText(travelDeal.getDescription());
            mPriceTextView.setText(travelDeal.getPrice());
            showImage(travelDeal.getImageUrl());
        }

        private void showImage(String url) {
            if (url != null && url.isEmpty() != true) {
                Picasso.get().load(url)
                        .resize(100, 100)
                        .centerCrop()
                        .into(mImageView);
            }
        }
    }


}
