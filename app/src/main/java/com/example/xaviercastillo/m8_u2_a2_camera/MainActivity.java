package com.example.xaviercastillo.m8_u2_a2_camera;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.io.File;


public class MainActivity extends AppCompatActivity {
    private static final int TAKE_PICTURE = 22;
    private static final int GALERIA = 20;
    private Uri imageUri;
    private Bitmap bitmap;
    private ImageView imatge;

    int id_color_text;
    int id_color_marc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imatge = findViewById(R.id.imgApp);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        //colors inicials per defecte
        id_color_text = ContextCompat.getColor(this, R.color.colorAccent);
        id_color_marc = ContextCompat.getColor(this, R.color.colorAccent);
    }

//-- fi onCreate() ---------------------------------------------------------------------------------

    public void personalitzar(View view) {
        //obre el diàleg de personalització de la imatge, on es pot canviar el text i el marc
            obrirDialogPersonalitzar();
    }

    public void obrirDialogPersonalitzar() {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.dialog_personalitzar, null);


        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(v);
        dialog.show();

        //widgets
        final EditText text_escriure = v.findViewById(R.id.text_escriure);
        final ImageView text_color = v.findViewById(R.id.text_color);
        final ImageView marc_color = v.findViewById(R.id.marc_color);

        Button bt_guardar_canvis = v.findViewById(R.id.bt_guardar_canvis);

        //mostra els colors del text i el marc
        text_color.setBackgroundColor(id_color_text);
        marc_color.setBackgroundColor(id_color_marc);

        //canvia el color del text
        text_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                triar_color(text_color, "Text", MainActivity.this, id_color_text);
            }
        });

        //canvia el color del marc
        marc_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                triar_color(marc_color, "Marc", MainActivity.this, id_color_marc);

            }
        });

        //guarda els canvis realitzats a la imatge
        bt_guardar_canvis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap_actual = ((BitmapDrawable) imatge.getDrawable()).getBitmap();
                Bitmap bitmap_nou = Bitmap.createBitmap(bitmap_actual.getWidth(), bitmap_actual.getHeight(),Bitmap.Config.ARGB_8888);

                Canvas canvas = new Canvas(bitmap_nou);
                canvas.drawBitmap(bitmap_actual, 0 ,0 ,null);

                //per dibuixar el marc
                Paint paint_marc = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint_marc.setColor(id_color_marc);
                paint_marc.setStrokeWidth(15);

                canvas.drawLine(0, 0, bitmap_actual.getWidth(),0 ,paint_marc);
                canvas.drawLine(0, 0, 0,bitmap_actual.getHeight() ,paint_marc);
                canvas.drawLine(bitmap_actual.getWidth(), bitmap_actual.getHeight(), bitmap_actual.getWidth(),0 ,paint_marc);
                canvas.drawLine(bitmap_actual.getWidth(), bitmap_actual.getHeight(), 0,bitmap_actual.getHeight() ,paint_marc);

                //per dibuixar el text
                Paint paint_text = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint_text.setColor(id_color_text);
                canvas.drawText(text_escriure.getText().toString(), 0, bitmap_nou.getHeight()/2, paint_text);

                //Càrrega de la nova imatge a l'ImageView
                imatge.setImageBitmap(bitmap_nou);
                dialog.dismiss();
            }
        });
    }

//--------------------------------------------------------------------------------------------------

    public void triar_color(final ImageView imageView, final String id, Context context, final int color_inicial) {
        //obre un diàleg per triar un color
        ColorPickerDialogBuilder
                .with(context)
                .setTitle("Tria un color")
                .initialColor(color_inicial)
                .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                .density(12)
                .setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) {
                        imageView.setBackgroundColor(selectedColor);

                        if (id.equals("Text")) id_color_text = selectedColor;
                        else id_color_marc = selectedColor;

                    }
                })
                .setPositiveButton("OK", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        imageView.setBackgroundColor(selectedColor);
                        if (id.equals("Text")) id_color_text = selectedColor;
                        else id_color_marc = selectedColor;
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        imageView.setBackgroundColor(color_inicial);
                        if (id.equals("Text")) id_color_text = color_inicial;
                        else id_color_marc = color_inicial;
                    }
                })
                .build()
                .show();
    }

//-- onActivityResult() ----------------------------------------------------------------------------

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case TAKE_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = imageUri;
                    getContentResolver().notifyChange(selectedImage, null);
                    ContentResolver cr = getContentResolver();
                    try {
                        bitmap = android.provider.MediaStore.Images.Media
                                .getBitmap(cr, selectedImage);

                        imatge.setImageBitmap(bitmap);
                        Toast.makeText(this, selectedImage.toString(),
                                Toast.LENGTH_LONG).show();
                        Bitmap reduit = accioRedimensionar(bitmap);
                        imatge.setImageBitmap(reduit);
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
                    Cursor cursor = getContentResolver().query(seleccio,columna,null,null,null);

                    cursor.moveToFirst();

                    int index=cursor.getColumnIndex(columna[0]);
                    String ruta =cursor.getString(index);
                    cursor.close();

                    //Redimensiona la imatge i la presenta en un ImageView
                    bitmap = BitmapFactory.decodeFile(ruta);

                    Bitmap reduit = accioRedimensionar(bitmap);
                    imatge.setImageBitmap(reduit);
                }
            break;
        }
    }

//-- fi onActivityResult() -------------------------------------------------------------------------

    public static Bitmap accioRedimensionar(Bitmap bitmap){
        //Redimensiona la imatge mantenint les proporcions
        int alt=bitmap.getHeight()*1080/bitmap.getWidth();

        return bitmap.createScaledBitmap(bitmap,1080,alt,true);
    }

    public static Bitmap accioRotar(Bitmap bitmap, Bitmap reduit) {
        //rota la imatge 90º
        Matrix matrix = new Matrix();
        matrix.postRotate(90.0f);

        return Bitmap.createBitmap(bitmap, 0, 0, reduit.getWidth(), reduit.getHeight(), matrix, true);
    }

    public void rotarImatge(View view) {
        //rota la imatge 90º
        Bitmap bitmap = ((BitmapDrawable) imatge.getDrawable()).getBitmap(); //Imatge d’un imatge view
        Bitmap rotacio = accioRotar(bitmap,bitmap);
        imatge.setImageBitmap(rotacio);
    }

//-- obrir galeria/fer foto ------------------------------------------------------------------------

    public void takePhoto(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");

        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
        imageUri = Uri.fromFile(photo);

        //obre la càmara i fa una foto
        startActivityForResult(intent, TAKE_PICTURE);
    }


    public void mostrarGaleria(View view) {
        //obre la galeria per seleccionar una imatge
        Intent i=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i,GALERIA);
    }

}
