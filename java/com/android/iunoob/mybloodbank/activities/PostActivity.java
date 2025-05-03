package com.android.iunoob.mybloodbank.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.iunoob.mybloodbank.R;
import com.android.iunoob.mybloodbank.viewmodels.UserData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PostActivity extends AppCompatActivity {

    ProgressDialog pd;
    EditText text1, text2;
    Spinner spinner1, spinner2;
    Button btnpost;

    FirebaseDatabase fdb;
    DatabaseReference db_ref;
    FirebaseAuth mAuth;

    Calendar cal;
    String uid, userEmail;
    String Time, Date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        pd = new ProgressDialog(this);
        pd.setMessage("Loading...");
        pd.setCancelable(true);
        pd.setCanceledOnTouchOutside(false);

        getSupportActionBar().setTitle("Post Blood Request");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        text1 = findViewById(R.id.getMobile);
        text2 = findViewById(R.id.getLocation);
        spinner1 = findViewById(R.id.SpinnerBlood);
        spinner2 = findViewById(R.id.SpinnerDivision);
        btnpost = findViewById(R.id.postbtn);

        cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);
        int hour = cal.get(Calendar.HOUR);
        int min = cal.get(Calendar.MINUTE);
        String ampm = (cal.get(Calendar.AM_PM) == 1) ? "PM" : "AM";

        Time = String.format("%02d:%02d %s", hour, min, ampm);
        Date = String.format("%d/%d/%d", day, month, year);

        mAuth = FirebaseAuth.getInstance();
        fdb = FirebaseDatabase.getInstance();
        db_ref = fdb.getReference("posts");

        FirebaseUser cur_user = mAuth.getCurrentUser();
        if (cur_user == null) {
            startActivity(new Intent(PostActivity.this, LoginActivity.class));
            return;
        } else {
            uid = cur_user.getUid();
            userEmail = cur_user.getEmail();  // Use email instead of contact
        }

        btnpost.setOnClickListener(v -> {
            pd.show();

            if (text2.getText().length() == 0) {
                Toast.makeText(getApplicationContext(), "Enter your location!", Toast.LENGTH_LONG).show();
                pd.dismiss();
                return;
            }

            Query findname = fdb.getReference("users").child(uid);
            findname.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        Toast.makeText(getApplicationContext(), "Database error occurred.", Toast.LENGTH_LONG).show();
                        pd.dismiss();
                        return;
                    }

                    String postId = db_ref.push().getKey(); // Generate unique post ID

                    Map<String, Object> postMap = new HashMap<>();
                  //  postMap.put("postId", postId);
                    postMap.put("name", dataSnapshot.getValue(UserData.class).getName());
                    postMap.put("address", text2.getText().toString());
                    postMap.put("division", spinner2.getSelectedItem().toString());
                    postMap.put("bloodGroup", spinner1.getSelectedItem().toString());
                    postMap.put("contact", text1.getText().toString());
                    postMap.put("time", Time);
                    postMap.put("date", Date);
                    postMap.put("email", userEmail); // Store email for user identification
                    Log.d("PostActivity", "Creating post with ID: " + postId);

                    db_ref.child(postId).setValue(postMap).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(PostActivity.this, "Your post has been created successfully", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(PostActivity.this, Dashboard.class));
                        } else {
                            Toast.makeText(PostActivity.this, "Failed to create post.", Toast.LENGTH_LONG).show();
                        }
                        pd.dismiss();
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d("User", databaseError.getMessage());
                    pd.dismiss();
                }
            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
