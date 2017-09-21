package picnroll.shilpa_cispl.com.picnroll.userlistview;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import picnroll.shilpa_cispl.com.picnroll.R;
import picnroll.shilpa_cispl.com.picnroll.navigationFiles.DashboardActivity;

public class UsersListActivity extends AppCompatActivity {


    List<DataAdapter> ListOfdataAdapter;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManagerOfrecyclerView;
    RecyclerView.Adapter recyclerViewadapter;
    ArrayList<String> ImageTitleNameArrayListForClick;
    private Firebase mRef, mRef1;
    private DatabaseReference mDatabase;
    ArrayList<String> userName = new ArrayList<>();
    ArrayList<String> profileImageUrl = new ArrayList<>();
    ArrayList<String> userIdArray = new ArrayList<>();
    ArrayList<String> imageKeysFromDB = new ArrayList<>();
    ArrayList<String> imageUrlFromDB = new ArrayList<>();
    ArrayList<String> shareImageUrls = new ArrayList<>();
    ArrayList<String> usersEmail = new ArrayList<>();
    ArrayList<String> sharedFolderUserId = new ArrayList<>();
    String userId, selectedFolderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Firebase.setAndroidContext(this);

        selectedFolderName = getIntent().getStringExtra("folderName");
        sharedFolderUserId = getIntent().getStringArrayListExtra("sharedUsersIdArray");

        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = currentFirebaseUser.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        ImageTitleNameArrayListForClick = new ArrayList<>();
        ListOfdataAdapter = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview1);
        recyclerView.setHasFixedSize(true);
        layoutManagerOfrecyclerView = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManagerOfrecyclerView);

        //Read profile imageurl and username
        mRef = new Firebase("https://pick-n-roll.firebaseio.com/Users");

        mRef.addValueEventListener(new com.firebase.client.ValueEventListener() {
            @Override
            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {

                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    usersEmail.add(String.valueOf(childDataSnapshot.child("Email").getValue()));
                    userName.add(String.valueOf(childDataSnapshot.child("Name").getValue()));
                    profileImageUrl.add(String.valueOf(childDataSnapshot.child("profileImageUrl").getValue()));
                    userIdArray.add(childDataSnapshot.getKey());

                    //Remove logged in username from list
                    if (String.valueOf(childDataSnapshot.child("Email").getValue()).endsWith(currentFirebaseUser.getEmail())) {
                        userIdArray.remove(childDataSnapshot.getKey());
                        userName.remove(String.valueOf(childDataSnapshot.child("Name").getValue()));
                    }
                }
                JSONArray jsArray = new JSONArray(userName);
                ParseJSonResponse(jsArray);

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        //Read imageurls of loggedin user
        mRef1 = new Firebase("https://pick-n-roll.firebaseio.com/Files/" + userId + "");

        mRef1.addValueEventListener(new com.firebase.client.ValueEventListener() {
            @Override
            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {

                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    imageKeysFromDB.add(childDataSnapshot.getKey());
                    imageUrlFromDB.add(String.valueOf(childDataSnapshot.getValue()));
                }
                for (int j = 0; j < imageKeysFromDB.size(); j++) {
                    if (imageKeysFromDB.get(j).contains(selectedFolderName)) {
                        shareImageUrls.add(imageUrlFromDB.get(j));

                    } else {

                    }
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void ParseJSonResponse(JSONArray array) {
        for (int i = 0; i < userName.size(); i++) {
            DataAdapter GetDataAdapter2 = new DataAdapter();
            GetDataAdapter2.setImageTitle(userName.get(i));
            GetDataAdapter2.setImageUrl(profileImageUrl.get(i));
            ListOfdataAdapter.add(GetDataAdapter2);
        }
        recyclerViewadapter = new RecyclerViewAdapter(ListOfdataAdapter, this);
        recyclerView.setAdapter(recyclerViewadapter);
    }


    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        Context context;
        List<DataAdapter> dataAdapters;
        ImageLoader imageLoader;

        public RecyclerViewAdapter(List<DataAdapter> getDataAdapter, Context context) {
            super();
            this.dataAdapters = getDataAdapter;
            this.context = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview, parent, false);

            ViewHolder viewHolder = new ViewHolder(view);

            return viewHolder;
        }


        @Override
        public void onBindViewHolder(ViewHolder Viewholder, int position) {

            DataAdapter dataAdapterOBJ = dataAdapters.get(position);

            imageLoader = ImageAdapter.getInstance(context).getImageLoader();

            imageLoader.get(dataAdapterOBJ.getImageUrl(),
                    ImageLoader.getImageListener(
                            Viewholder.VollyImageView,//Server Image
                            R.mipmap.ic_launcher,//Before loading server image the default showing image.
                            android.R.drawable.ic_dialog_alert //Error image if requested image dose not found on server.
                    )
            );


            Viewholder.VollyImageView.setImageUrl(dataAdapterOBJ.getImageUrl(), imageLoader);

            StringBuilder sb = new StringBuilder(dataAdapterOBJ.getImageTitle());
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
            Viewholder.ImageTitleTextView.setText(sb);
        }

        @Override
        public int getItemCount() {

            return dataAdapters.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            public TextView ImageTitleTextView;
            public NetworkImageView VollyImageView;

            public ViewHolder(final View itemView) {

                super(itemView);

                ImageTitleTextView = (TextView) itemView.findViewById(R.id.ImageNameTextView);

                VollyImageView = (NetworkImageView) itemView.findViewById(R.id.VolleyImageView);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final int pos = getAdapterPosition();

                        if (sharedFolderUserId.contains(userIdArray.get(pos))) {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(UsersListActivity.this);
                            builder1.setTitle("Share Album");
                            builder1.setMessage("Already shared");
                            builder1.setCancelable(false);

                            builder1.setPositiveButton(
                                    "Ok",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();

                                        }
                                    });

                            builder1.setNegativeButton(
                                    "Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });


                            builder1.setIcon(R.drawable.appicon);

                            builder1.show();

                        } else {

                            mDatabase.child("Albums").child(userIdArray.get(pos)).child(String.valueOf(UUID.randomUUID())).setValue(selectedFolderName);
                            mDatabase.child("SharedUsers").child(userId).child(selectedFolderName).child(String.valueOf(UUID.randomUUID())).setValue(userIdArray.get(pos));
                            for (int m = 0; m < shareImageUrls.size(); m++) {
                                mDatabase.child("Files").child(userIdArray.get(pos)).child(String.valueOf(UUID.randomUUID())).setValue(shareImageUrls.get(m));

                            }

                        }

                    }
                });

            }


        }
    }


}



