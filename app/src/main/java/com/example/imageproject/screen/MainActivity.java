package com.example.imageproject.screen;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.imageproject.FileModel;
import com.example.imageproject.FileUtils;
import com.example.imageproject.HttpService;
import com.example.imageproject.R;
import com.example.imageproject.RetrofitBuilder;
import com.example.imageproject.utils.NotificationApp;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private CardView cardView;
    private ImageView imageView;
    private Button button;
    private CharSequence[] options= {"Camera","Gallery","Cancel"};
    private FloatingActionButton fabMulti;
    private NotificationManagerCompat notificationManagerCompat;
    private String selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cardView = findViewById(R.id.cardview);
        imageView = findViewById(R.id.imageview);
        button = findViewById(R.id.button);
        fabMulti = findViewById(R.id.fab_multi);

        requirePermission();
        notificationManagerCompat = NotificationManagerCompat.from(this);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Select Image");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(options[which].equals("Camera")){
                            Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePic, 0);
                        }
                        else if(options[which].equals("Gallery")) {
                            Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(gallery, 1);
                        }
                        else {
                            dialog.dismiss();
                        }
                    }
                });

                builder.show();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFileToServer();
            }
        });

        fabMulti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(MainActivity.this, MultiUpload.class);
                startActivity(in);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode !=RESULT_CANCELED){

            switch (requestCode){
                case 0:
                    if(resultCode == RESULT_OK && data !=null){
                        Bitmap image = (Bitmap) data.getExtras().get("data");
                        selectedImage = FileUtils.getPath(MainActivity.this, getImageUri(MainActivity.this,image));
                       imageView.setImageBitmap(image);
                    }
                    break;
                case 1:
                    if(resultCode == RESULT_OK && data !=null){

                        Uri image = data.getData();
                        selectedImage = FileUtils.getPath(MainActivity.this,image);
                        Picasso.get().load(image).into(imageView);
                    }
            }

        }
    }

    public Uri getImageUri(Context context, Bitmap bitmap){
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "myImage","");

        return Uri.parse(path);
    }


    public void requirePermission(){
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }


    public void uploadFileToServer(){

        File file = new File(Uri.parse(selectedImage).getPath());

        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"),file);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("sendimage",file.getName(),requestBody);

        HttpService service = RetrofitBuilder.getClient().create(HttpService.class);

        Call<FileModel> call = service.callUploadApi(filePart);
        call.enqueue(new Callback<FileModel>() {
            @Override
            public void onResponse(Call<FileModel> call, Response<FileModel> response) {
                FileModel fileModel = response.body();
                Log.i("file", "File success");
                sendNotif();
//                Toast.makeText(MainActivity.this, fileModel.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<FileModel> call, Throwable t) {
                Log.i("file", "File failed");
                t.printStackTrace();
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void sendNotif()  {
        final int progressMax = 100;
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, NotificationApp.CHANNEl_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("upload")
                .setContentText("Upload")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setSilent(true)
                .setProgress(progressMax, 0, true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(2000);
                for (int progress = 0; progress <= progressMax; progress += 20) {
                    notification.setProgress(progressMax, progress, false);
                    notificationManagerCompat.notify(2, notification.build());
                    SystemClock.sleep(1000);
                }
                notification.setContentText("Download finished")
                        .setProgress(0, 0, false)
                        .setOngoing(false);
                notificationManagerCompat.notify(2, notification.build());
            }
        }).start();
    }
}







