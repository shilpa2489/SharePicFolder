package picnroll.shilpa_cispl.com.picnroll.customgallery;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import picnroll.shilpa_cispl.com.picnroll.R;
import picnroll.shilpa_cispl.com.picnroll.navigationFiles.Utility;
import picnroll.shilpa_cispl.com.picnroll.navigationFiles.ViewUploadPhotosActivity;

public class FolderImagesActivity extends AppCompatActivity implements View.OnClickListener {

    GridView gridGallery;
    Handler handler;
    GalleryAdapter adapter;

    ImageView imgSinglePick;
    FloatingActionButton fab;

    ViewSwitcher viewSwitcher;
    ImageLoader imageLoader;
    String dir,extension,userId,selectedFolderName,selectedFolderIndex;
    File newdir;
    Uri filePath;
    ArrayList<String> sharedFolderUserId = new ArrayList<>();

    FirebaseUser currentFirebaseUser;
    DatabaseReference ref,refCamera;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://pick-n-roll.appspot.com");
    private Firebase mRef;
    ArrayList<String> imageKeys = new ArrayList<>();
    ArrayList<String> imageValues = new ArrayList<>();
    ArrayList<String> folderImageValues = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_folder_images);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = currentFirebaseUser.getUid();
        ref = FirebaseDatabase.getInstance().getReference();
        refCamera = FirebaseDatabase.getInstance().getReference();
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
        selectedFolderName = getIntent().getStringExtra("selectedFolderName");
        selectedFolderIndex = getIntent().getStringExtra("selectedFolderPosition");
        sharedFolderUserId = getIntent().getStringArrayListExtra("sharedUsersIdArray");


        // Here, we are making a folder named picFolder to store
        // pics taken by the camera using this application.
        dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/picFolder/";
        newdir = new File(dir);
        newdir.mkdirs();
        initImageLoader();
        init();



        dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/picFolderDB/";
        newdir = new File(dir);
        newdir.mkdirs();

        mRef = new Firebase("https://pick-n-roll.firebaseio.com/Files/"+userId+"");

        mRef.addValueEventListener(new com.firebase.client.ValueEventListener() {
            @Override
            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {


                for (com.firebase.client.DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    imageKeys.add(childDataSnapshot.getKey());
                    imageValues.add(String.valueOf(childDataSnapshot.getValue()));
                    Log.d("tag","images-->"+childDataSnapshot.getKey() +"\n" + String.valueOf(childDataSnapshot.getValue()) );
                }

                for (int a=0; a<imageKeys.size(); a++){
                    if(imageKeys.get(a).contains(selectedFolderName)){
                        folderImageValues.add(imageValues.get(a));
                        Log.d("tag","folderImages-->"+folderImageValues.toString() +"\n");
                        String file = dir+String.valueOf(UUID.randomUUID())+".jpg";
                        File newfile = new File(file);

                        ArrayList<String> localFiles = new ArrayList<String>();
                        localFiles.add((String.valueOf(Uri.fromFile(newfile))));
                        Log.d("dbimage", "dbimage" + String.valueOf(Uri.fromFile(newfile)) + "\n" +localFiles.toString());
                        try {
                            newfile.createNewFile();
                        }
                        catch (IOException e)
                        {
                        }
                    }
                }

            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        ArrayList<CustomGallery> dataT = new ArrayList<>();
        for (String string : folderImageValues) {
            CustomGallery item = new CustomGallery();
            item.sdcardPath = string;
            dataT.add(item);
        }
        viewSwitcher.setDisplayedChild(0);
        adapter.addAll(dataT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_upload_photo, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {

            boolean result = Utility.checkPermission(FolderImagesActivity.this);
            if (result) {
                Intent i = new Intent(Action.ACTION_MULTIPLE_PICK);
                startActivityForResult(i, 200);
                return true;
            }
        }

       else if(id ==android.R.id.home) {
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc().imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .bitmapConfig(Bitmap.Config.RGB_565).build();
        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
                this).defaultDisplayImageOptions(defaultOptions).memoryCache(
                new WeakMemoryCache());

        ImageLoaderConfiguration config = builder.build();
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
    }

    private void init() {

        handler = new Handler();
        gridGallery = (GridView) findViewById(R.id.gridGallery);
        gridGallery.setFastScrollEnabled(true);
        adapter = new GalleryAdapter(getApplicationContext(), imageLoader);
        adapter.setMultiplePick(false);
        gridGallery.setAdapter(adapter);

        viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
        viewSwitcher.setDisplayedChild(1);

        imgSinglePick = (ImageView) findViewById(R.id.imgSinglePick);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Gallery image added
        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            String[] all_path = data.getStringArrayExtra("all_path");

            ArrayList<CustomGallery> dataT = new ArrayList<CustomGallery>();

            for (String string : all_path) {
                CustomGallery item = new CustomGallery();
                item.sdcardPath = string;

                dataT.add(item);

                //Read image name and insert in DB
                int lastDot = string.lastIndexOf('/');
                if (lastDot == -1) {
                    // No dots - what do you want to do?
                } else {
                    extension = string.substring(lastDot);

                }


                // item.sdcardPath = string;
                filePath = Uri.fromFile(new File(string));


                if(filePath != null) {
                    // pd.show();

                    StorageReference childRef = storageRef.child("Files").child(userId).child(extension);
                    //uploading the image
                    UploadTask uploadTask = childRef.putFile(filePath);

                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //  pd.dismiss();
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            ref.child("Files").child(userId).child(selectedFolderIndex + selectedFolderName + userId + String.valueOf(UUID.randomUUID())).setValue(downloadUrl.toString());
                            //Share camera photo with already shared users
                            if (sharedFolderUserId == null) {
                                Log.d("tag", "NoDATA");
                            } else {

                                for (int i = 0; i < sharedFolderUserId.size(); i++) {
                                    ref.child("Files").child(sharedFolderUserId.get(i)).child(selectedFolderIndex + selectedFolderName + userId + String.valueOf(UUID.randomUUID())).setValue(downloadUrl.toString());
                                    Log.d("tag", "Camera" +ref);
                                }
                            }
                            Toast.makeText(FolderImagesActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //  pd.dismiss();
                            Toast.makeText(FolderImagesActivity.this, "Upload Failed -> " + e, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }

            viewSwitcher.setDisplayedChild(0);
            adapter.addAll(dataT);
        }
        //Camera image added
        else if (requestCode == 300 && resultCode == Activity.RESULT_OK) {

            viewSwitcher.setDisplayedChild(1);
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imgSinglePick.setImageBitmap(photo);

            String file = dir+String.valueOf(UUID.randomUUID())+".jpg";
                File newfile = new File(file);
                Log.d("CameraDemo", "cameraimage" + String.valueOf(Uri.fromFile(newfile)));
                try {
                    newfile.createNewFile();
                }
                catch (IOException e)
                {
                }

            int lastDot = String.valueOf(Uri.fromFile(newfile)).lastIndexOf('/');
            if (lastDot == -1) {
                // No dots - what do you want to do?
            } else {
                extension = String.valueOf(Uri.fromFile(newfile)).substring(lastDot);
                Log.d("tag","extensioncamera"+extension);
            }
            filePath = (Uri.fromFile(newfile));

            if(filePath != null) {
                // pd.show();

                StorageReference childRefCamera = storageRef.child("Files").child(userId).child(extension);
                //uploading the image
                UploadTask uploadTaskCamera = childRefCamera.putFile(filePath);

                uploadTaskCamera.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //  pd.dismiss();
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        refCamera.child("Files").child(userId).child(selectedFolderIndex + selectedFolderName + userId + String.valueOf(UUID.randomUUID())).setValue(downloadUrl.toString());

                        //Share camera photo with already shared users
                        if (sharedFolderUserId == null) {
                            Log.d("tag", "NoDATA");
                        } else {

                            for (int i = 0; i < sharedFolderUserId.size(); i++) {

                                refCamera.child("Files").child(sharedFolderUserId.get(i)).child(selectedFolderIndex + selectedFolderName + userId + String.valueOf(UUID.randomUUID())).setValue(downloadUrl.toString());

                                Log.d("tag", "refCamera" +refCamera);
                            }
                        }
                        Toast.makeText(FolderImagesActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //  pd.dismiss();
                        Toast.makeText(FolderImagesActivity.this, "Upload Failed -> " + e, Toast.LENGTH_SHORT).show();
                    }
                });
            }


        }
    }

    @Override
    public void onClick(View view) {

        boolean result = Utility.checkPermission(FolderImagesActivity.this);
        if (result) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, 300);
        }

    }
}
