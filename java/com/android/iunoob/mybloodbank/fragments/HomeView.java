package com.android.iunoob.mybloodbank.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.iunoob.mybloodbank.R;
import com.android.iunoob.mybloodbank.adapters.BloodRequestAdapter;
import com.android.iunoob.mybloodbank.viewmodels.CustomUserData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeView extends Fragment {

    private View view;
    private RecyclerView recentPosts;

    private DatabaseReference donor_ref;
    FirebaseAuth mAuth;
    private BloodRequestAdapter restAdapter;
    private List<CustomUserData> postLists;
    private ProgressDialog pd;

    public HomeView() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.home_view_fragment, container, false);
        recentPosts = (RecyclerView) view.findViewById(R.id.recycleposts);

        recentPosts.setLayoutManager(new LinearLayoutManager(getContext()));

        donor_ref = FirebaseDatabase.getInstance().getReference();
        postLists = new ArrayList<>();
        pd = new ProgressDialog(getActivity());
        pd.setMessage("Loading...");
        pd.setCancelable(true);
        pd.setCanceledOnTouchOutside(false);

        mAuth = FirebaseAuth.getInstance();
        getActivity().setTitle("Virtual Blood Bank");
        restAdapter = new BloodRequestAdapter(postLists, getContext());
        RecyclerView.LayoutManager pmLayout = new LinearLayoutManager(getContext());
        recentPosts.setLayoutManager(pmLayout);
        recentPosts.setItemAnimator(new DefaultItemAnimator());
        recentPosts.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recentPosts.setAdapter(restAdapter);

        AddPosts();
        return view;

    }
    private void AddPosts() {
        Query allposts = donor_ref.child("posts");
        pd.show();
        allposts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postLists.clear(); // Clear existing list

                if(dataSnapshot.exists()) {
                    Log.d("HomeView", "Total posts found: " + dataSnapshot.getChildrenCount());

                    for (DataSnapshot singlepost : dataSnapshot.getChildren()) {
                        // Log the raw data first
                        String key = singlepost.getKey();
                        Log.d("HomeView", "Post key from Firebase: " + key);
                        Log.d("HomeView", "Post data: " + singlepost.getValue().toString());

                        try {
                            // Create new object instead of using getValue(CustomUserData.class)
                            CustomUserData customUserData = new CustomUserData();

                            // Set post ID and verify
                            customUserData.setPostId(key);
                            Log.d("HomeView", "Post ID after setting: " + customUserData.getPostId());

                            // Manual mapping of remaining fields
                            Map<String, Object> postData = (Map<String, Object>) singlepost.getValue();
                            if (postData != null) {
                                customUserData.setName((String) postData.get("name"));
                                customUserData.setAddress((String) postData.get("address"));
                                customUserData.setDivision((String) postData.get("division"));
                                customUserData.setBloodGroup((String) postData.get("bloodGroup"));
                                customUserData.setContact((String) postData.get("contact"));
                                customUserData.setTime((String) postData.get("time"));
                                customUserData.setDate((String) postData.get("date"));
                                customUserData.setEmail((String) postData.get("email"));
                            }

                            // Final verification
                            Log.d("HomeView", "Final post ID: " + customUserData.getPostId());

                            postLists.add(customUserData);
                        } catch (Exception e) {
                            Log.e("HomeView", "Error parsing post: " + e.getMessage(), e);
                        }
                    }
                    restAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getActivity(), "Database is empty now!", Toast.LENGTH_LONG).show();
                }
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("User", databaseError.getMessage());
                pd.dismiss();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
