package com.example.xaviercastillo.m8_u2_a2_camera;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;


public class MainActivity extends AppCompatActivity {
    private static final int TAKE_PICTURE = 22;
    private static final int GALERIA = 20;
    private Uri imageUri;
    private Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

    }


    public void takePhoto(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(photo));
        imageUri = Uri.fromFile(photo);
        startActivityForResult(intent, TAKE_PICTURE);
    }

    public void mostrarGaleria(View view) {
        Intent i=new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i,GALERIA);
    }

    public void rotarImatge(View view) {
        ImageView imageView = (ImageView) findViewById(R.id.imgApp);
        Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap(); //Imatge d’un imatge view
        Bitmap rotacio = rotar(bitmap,bitmap);
        imageView.setImageBitmap(rotacio);
    }



    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageView imageView = (ImageView) findViewById(R.id.imgApp);
        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = imageUri;
                    getContentResolver().notifyChange(selectedImage, null);
                    ContentResolver cr = getContentResolver();
                    try {
                        bitmap = android.provider.MediaStore.Images.Media
                                .getBitmap(cr, selectedImage);

                        imageView.setImageBitmap(bitmap);
                        Toast.makeText(this, selectedImage.toString(),
                                Toast.LENGTH_LONG).show();
                        Bitmap reduit = redimensionar(bitmap, imageView);
                        imageView.setImageBitmap(reduit);
                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT)
                                .show();
                        Log.e("Camera", e.toString());
                    }

                }
                break;
            case GALERIA:
                if (resultCode==RESULT_OK){
                Uri seleccio= data.getData();
                String[] columna={MediaStore.Images.Media.DATA};
                Cursor cursor= null;
                cursor = getContentResolver().query(seleccio,columna,null,null,null);
                cursor.moveToFirst();
                int index=cursor.getColumnIndex(columna[0]);
                String ruta =cursor.getString(index);
                cursor.close();
                bitmap = BitmapFactory.decodeFile(ruta);
                //Presentar imatge redimensiona la imatge i la presenta en un ImageView

                    Bitmap reduit = redimensionar(bitmap,imageView);
                    imageView.setImageBitmap(reduit);
            }
        }
    }

    public static Bitmap redimensionar(Bitmap bitmap, ImageView imageView){
        //Redimensionar imatge mantenint les proporcions
        int alt=bitmap.getHeight()*1080/bitmap.getWidth();
        //Càrrega de la imatge a l'imageview
        Bitmap reduit=bitmap.createScaledBitmap(bitmap,1080,alt,true);
        return reduit;
    }


    public static Bitmap rotar(Bitmap bitmap, Bitmap reduit){

        Matrix matrix=new Matrix();
        matrix.postRotate(90.0f);
        Bitmap rotate=
                Bitmap.createBitmap(bitmap,0,0,reduit.getWidth(),reduit.getHeight(),matrix,true);
        return  rotate;
    }

}
