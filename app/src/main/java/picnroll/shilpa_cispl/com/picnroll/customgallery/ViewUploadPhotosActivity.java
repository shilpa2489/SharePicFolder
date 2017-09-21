package picnroll.shilpa_cispl.com.picnroll.navigationFiles;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.UUID;

import picnroll.shilpa_cispl.com.picnroll.R;
import picnroll.shilpa_cispl.com.picnroll.customgallery.Action;
import picnroll.shilpa_cispl.com.picnroll.customgallery.CustomGallery;
import picnroll.shilpa_cispl.com.picnroll.customgallery.GalleryAdapter;

import static android.R.attr.path;

public class ViewUploadPhotosActivity extends AppCompatActivity implements View.OnClickListener {

    private int REQUEST_CAMERA = 0;

    FloatingActionButton fab;
    GridView gridGallery;
    Handler handler;
    GalleryAdapter adapter;
    ImageView imgSinglePick;
    ViewSwitcher viewSwitcher;
    ImageLoader imageLoader;

    //creating reference to firebase storage
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://pick-n-roll.appspot.com");


    DatabaseReference ref;

    Uri filePath;
    String userId, selectedFolderName, selectedFolderIndex, uniquekey;
    FirebaseUser currentFirebaseUser;

    ArrayList<String> sharedFolderUserId = new ArrayList<>();
    ArrayList<String> allImageKeys = new ArrayList<>();
    ArrayList<String> allImageUrl = new ArrayList<>();
    ArrayList<String> selectedFolderImageUrl = new ArrayList<>();

    String extension;
    String dir;
    File newdir;
    public static int count = 0;
    int TAKE_PHOTO_CODE = 0;
    Uri outputFileUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_upload_photos);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = currentFirebaseUser.getUid();
        ref = FirebaseDatabase.getInstance().getReference();
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        selectedFolderName = getIntent().getStringExtra("selectedFolderName");
        selectedFolderIndex = getIntent().getStringExtra("selectedFolderPosition");
        sharedFolderUserId = getIntent().getStringArrayListExtra("sharedUsersIdArray");
        allImageKeys = getIntent().getStringArrayListExtra("imageKeys");
        allImageUrl = getIntent().getStringArrayListExtra("imageUrl");

        Log.d("tag", "folders" + selectedFolderName + selectedFolderIndex + "\n" + sharedFolderUserId.size() + "\n" + allImageKeys.size());

        initImageLoader();
        init();

        // Here, we are making a folder named picFolder to store
        // pics taken by the camera using this application.
        dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/picFolder/";
        newdir = new File(dir);
        newdir.mkdirs();

//        for (int i = 0; i < allImageKeys.size(); i++) {
//            if (allImageKeys.get(i).contains(selectedFolderName)) {
//                selectedFolderImageUrl.add(allImageUrl.get(i));
//                dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/picDBFolder/";
//                newdir = new File(dir);
//                newdir.mkdirs();
//
//                String file = dir+allImageUrl.get(i)+".jpg";
//                File newfile = new File(file);
//                Log.d("CameraDemo", "Pic db" +newfile);
//                try {
//                    newfile.createNewFile();
//                }
//                catch (IOException e)
//                {
//                }
//
//                //  outputFileUri = Uri.fromFile(newfile);
//                outputFileUri = Uri.parse(Uri.decode(String.valueOf(newfile)));
//                Log.d("CameraDemo", "Pic dbsaved" +outputFileUri +"\n"+newfile);
//
//
//                item.sdcardPath = String.valueOf(newfile);
//                dataT.add(item);
//
//            }
//        }
//        // viewSwitcher.setDisplayedChild(0);
//        viewSwitcher.setDisplayedChild(0);
//        adapter.addAll(dataT);



    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.fab) {
            boolean result = Utility.checkPermission(ViewUploadPhotosActivity.this);
            if (result) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, 300);
            }


        }


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
            Intent i = new Intent(Action.ACTION_MULTIPLE_PICK);
            startActivityForResult(i, 200);

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

        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            String[] all_path = data.getStringArrayExtra("all_path");


            ArrayList<CustomGallery> dataT = new ArrayList<CustomGallery>();
            for (String string : all_path) {
                CustomGallery item = new CustomGallery();

                Log.d("tag","allpathspic"+"\n"+string);
                item.sdcardPath = string;
                dataT.add(item);

                int lastDot = string.lastIndexOf('/');
                if (lastDot == -1) {
                    // No dots - what do you want to do?
                } else {
                    extension = string.substring(lastDot);
                    Log.d("tag","extension"+extension);
                }


                // item.sdcardPath = string;
                filePath = Uri.fromFile(new File(string));
                Log.d("tag","gallerypath"+filePath +"\n"+item.sdcardPath);

                if(filePath != null) {
                    // pd.show();

//                    StorageReference childRef = storageRef.child("Files").child(userId).child(extension);
//                    //uploading the image
//                    UploadTask uploadTask = childRef.putFile(filePath);
//
//                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                            //  pd.dismiss();
//                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
//                            ref.child("Files").child(userId).child("imageKey").setValue(downloadUrl.toString());
//                            Toast.makeText(ViewUploadPhotosActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            //  pd.dismiss();
//                            Toast.makeText(ViewUploadPhotosActivity.this, "Upload Failed -> " + e, Toast.LENGTH_SHORT).show();
//                        }
//                    });
                }

                // dataT.add(item);
            }
            //  viewSwitcher.setDisplayedChild(0);
            adapter.addAll(dataT);
        }

        else if (requestCode == 300 && resultCode == Activity.RESULT_OK) {

            Log.d("CameraDemo", "Pic saved");



            viewSwitcher.setDisplayedChild(1);
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imgSinglePick.setImageBitmap(photo);
//            viewSwitcher.setDisplayedChild(1);
//            String single_path = String.valueOf(outputFileUri);
//            imageLoader.displayImage("file://" + single_path, imgSinglePick);
//                Bitmap photo = (Bitmap) data.getExtras().get("data");
//               // imageView.setImageBitmap(photo);
//                imgSinglePick.setImageBitmap(photo); //imageView is your ImageView

        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //  cameraIntent();

                } else {
                    //code for deny
                }
                break;
        }
    }



    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }


//    private void onCaptureImageResult(Intent data) {
//        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
//        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
//
//        File destination = new File(Environment.getExternalStorageDirectory(),
//                System.currentTimeMillis() + ".jpg");
//
//        item.sdcardPath = String.valueOf(destination);
//        dataT.add(item);
//        adapter.addAll(dataT);
//
//        filePath = Uri.fromFile(new File(String.valueOf(destination)));
//
//        Log.d("tag","filepath"+filePath +"\n" +thumbnail.toString()+"\n"+destination +"\n"+item.sdcardPath);
//
//        int lastDot = String.valueOf(destination).lastIndexOf('/');
//        if (lastDot == -1) {
//            // No dots - what do you want to do?
//        } else {
//            extension = String.valueOf(destination).substring(lastDot);
//            Log.d("tag","camera extension"+extension);
//        }
//        if(filePath != null) {
//            // pd.show();
//
////
////            StorageReference childRef = storageRef.child("Files").child(userId).child(extension);
////            ref.child("Files").child(currentFirebaseUser.getUid()).child(currentFirebaseUser.getUid()).setValue(filePath);
////
////            //uploading the image
////            UploadTask uploadTask = childRef.putFile(filePath);
////
////            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
////                @Override
////                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
////                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
////                    ref.child("Files").child(userId).child("imageKey").setValue(downloadUrl.toString());
////                    //  pd.dismiss();
////                    Toast.makeText(ViewUploadPhotosActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
////                }
////            }).addOnFailureListener(new OnFailureListener() {
////                @Override
////                public void onFailure(@NonNull Exception e) {
////                    //  pd.dismiss();
////                    Toast.makeText(ViewUploadPhotosActivity.this, "Upload Failed -> " + e, Toast.LENGTH_SHORT).show();
////                }
////            });
//        }
//
//
//
//
//    }

}