package com.android.iunoob.mybloodbank.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.iunoob.mybloodbank.R;
import com.android.iunoob.mybloodbank.viewmodels.CustomUserData;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class BloodRequestAdapter extends RecyclerView.Adapter<BloodRequestAdapter.ViewHolder> {

    private List<CustomUserData> postLists;
    private Context context;
    private FirebaseAuth mAuth;

    public BloodRequestAdapter(List<CustomUserData> postLists, Context context) {
        this.postLists = postLists;
        this.context = context;
        this.mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CustomUserData post = postLists.get(position);

        holder.targetBG.setText(post.getBloodGroup());
        holder.targetCN.setText(post.getContact());
        holder.reqstUser.setText("Requested by: " + post.getName());
        holder.reqstLocation.setText("From: " + post.getAddress()+", " +post.getDivision());
        holder.posted.setText("Posted: " + post.getTime());

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String currentUserEmail = currentUser != null ? currentUser.getEmail().trim().toLowerCase() : "";

        // Retrieve the latest logged-in user's email from SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyBloodBankPrefs", Context.MODE_PRIVATE);

// Log both values
        String postEmail = post.getEmail() != null ? post.getEmail().trim().toLowerCase() : "";
        Log.d("BloodRequestAdapter", "Position " + position + " Post Email: " + postEmail);
        Log.d("BloodRequestAdapter", "Current User Email: " + currentUserEmail);
        Log.d("BloodRequestAdapter", "Equal? " + postEmail.equals(currentUserEmail));

// Check post ID for debugging
        Log.d("BloodRequestAdapter", "Post ID: " + post.getPostId());

        if (!postEmail.isEmpty() && postEmail.equals(currentUserEmail)) {
            Log.d("BloodRequestAdapter", "Setting button VISIBLE at position " + position);
            holder.btnDelete.setVisibility(View.VISIBLE);
        } else {
            Log.d("BloodRequestAdapter", "Setting button GONE at position " + position);
            holder.btnDelete.setVisibility(View.GONE);
        }

        // Handle post deletion
        holder.btnDelete.setOnClickListener(v -> {
            // Log the full post object for debugging
            Log.d("BloodRequestAdapter", "Post object: " + post.toString());

            String postId = post.getPostId();
            Log.d("BloodRequestAdapter", "Attempting to delete post with ID: " + postId);

            if (postId == null || postId.isEmpty()) {
                Toast.makeText(context, "Error: Invalid post ID. Please contact support.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Proceed with deletion
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("posts")
                    .child(postId);

            ref.removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Post deleted successfully", Toast.LENGTH_SHORT).show();
                        postLists.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, postLists.size());
                    })
                    .addOnFailureListener(e -> {
                        Log.e("BloodRequestAdapter", "Delete failed: " + e.getMessage());
                        Toast.makeText(context, "Failed to delete post: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return postLists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView targetBG, targetCN, reqstUser, reqstLocation, posted;
        Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            targetBG = itemView.findViewById(R.id.targetBG);
            targetCN = itemView.findViewById(R.id.targetCN);
            reqstUser = itemView.findViewById(R.id.reqstUser);
            reqstLocation = itemView.findViewById(R.id.reqstLocation);
            posted = itemView.findViewById(R.id.posted);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
