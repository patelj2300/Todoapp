package com.canadore.jaykumar;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {


    private RecyclerView recyclerView;
    FirebaseDatabase database;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    ImageView addNotes;
    TextView welcomeText,logoutText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

       // FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            // Redirect to login if not authenticated
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
            return;
        }


        String userId = currentUser.getUid();
        recyclerView = findViewById(R.id.recyclerView);
        database = FirebaseDatabase.getInstance();
        addNotes = findViewById(R.id.btnAdd);
        welcomeText = findViewById(R.id.welcomeText);
        logoutText = findViewById(R.id.Logout);

        logoutText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });

        database.getReference().child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String name = dataSnapshot.child("name").getValue(String.class);
                    welcomeText.setText("Welcome " +name);
                } else {
                    Log.d("User Info", "User not found.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("User Info", "Error: " + error.getMessage());
            }
        });



        addNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View view1 = LayoutInflater.from(MainActivity.this).inflate(R.layout.note_dialog, null);

                TextInputLayout nameLayout;
                TextInputEditText nameEt;

                nameLayout = findViewById(R.id.stdname);
                nameEt = view1.findViewById(R.id.nameET);

                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Add")
                        .setView(view1)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (Objects.requireNonNull(nameEt.getText()).toString().isEmpty()) {
                                    nameLayout.setError("This field is required!");
                                }
                                else
                                {
                                    ProgressDialog dialog = new ProgressDialog(MainActivity.this);
                                    dialog.setMessage("Storing in Database...");
                                    dialog.show();
                                    String name = nameEt.getText().toString();
                                    Map<String, Object> student = new HashMap<>();
                                    student.put("note", name);
                                    database.getReference().child("users").child(userId).child("Students").push()
                                            .setValue(student)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Toast.makeText(MainActivity.this, "Data Saved Successfully!", Toast.LENGTH_SHORT).show();
                                                    dialog.dismiss();

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(MainActivity.this, "Error saving data", Toast.LENGTH_SHORT).show();
                                                    dialog.dismiss();
                                                }
                                            });
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create();
                alertDialog.show();
            }
        });




        database.getReference().child("users").child(userId).child("Students").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Notes> arrayList = new ArrayList<>();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {

                    Notes notes = dataSnapshot.getValue(Notes.class);
                    Objects.requireNonNull(notes).setKey(dataSnapshot.getKey());
                    arrayList.add(notes);

                }

                NoteAdapter adapter = new NoteAdapter(MainActivity.this, arrayList);
                recyclerView.setAdapter(adapter);

                adapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {

                    @Override
                    public void onClick(Notes std) {

                        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.note_dialog, null);
                        TextInputLayout nameLayout;
                        TextInputEditText nameEt;

                        nameLayout = findViewById(R.id.stdname);
                        nameEt = view.findViewById(R.id.nameET);
                        nameEt.setText(std.getNote());
                        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Edit")
                                .setView(view)
                                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        if (Objects.requireNonNull(nameEt.getText()).toString().isEmpty()) {
                                            nameLayout.setError("This field is required!");
                                        }
                                        else {
                                            progressDialog.setMessage("Saving...");
                                            progressDialog.show();
                                            Notes notes = new Notes();
                                            notes.setNote(nameEt.getText().toString());
                                            database.getReference().child("users").child(userId).child("Students").child(std.getKey()).setValue(notes).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    progressDialog.dismiss();
                                                    dialogInterface.dismiss();
                                                    Toast.makeText(MainActivity.this, "Saved Successfully!", Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(MainActivity.this, "There was an error while saving data", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }
                                })
                                .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();

                                    }
                                })
                                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        progressDialog.setTitle("Deleting...");
                                        progressDialog.show();
                                        database.getReference().child("users").child(userId).child("Students").child(std.getKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                progressDialog.dismiss();
                                                Toast.makeText(MainActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                                                adapter.notifyDataSetChanged();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                            }
                                        });
                                    }
                                }).create();
                        alertDialog.show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}