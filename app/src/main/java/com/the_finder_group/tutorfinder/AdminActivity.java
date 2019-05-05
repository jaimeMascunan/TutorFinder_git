package com.the_finder_group.tutorfinder;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.the_finder_group.tutorfinder.Helper.Helper;
import com.the_finder_group.tutorfinder.Helper.SQLiteHandler;
import com.the_finder_group.tutorfinder.LlistarAdsAdmin.TabActivityAdmin;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Activitat principal per un usuari de tipus administrador
 */
public class AdminActivity extends AppCompatActivity {
    //Constants per l'acces a l'emmagatzament intern del telefon i la galeria d'imatges
    private int STORAGE_PERMISSION_CODE= 1111;
    private int GALLERY_REQUEST_CODE = 1112;
    //Declarem les diferents variables
    private TextView txtName;
    private CircleImageView userPhoto;
    private CardView llistarUsuaris, llistarProductes;

    private SQLiteHandler db;
    private Helper helper;

    private String name, user_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_admin);
        //Definim la toolbar per a l'usuari admin
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_admin);
        setSupportActionBar(myToolbar);
        ActionBar actionBar  = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        //Inicialitzem els diferents elements de la layout
        txtName = (TextView) findViewById(R.id.user_name);
        userPhoto = (CircleImageView) findViewById(R.id.user_photo);
        llistarUsuaris = (CardView) findViewById(R.id.card_view_llistar_usuaris);
        llistarProductes = (CardView) findViewById(R.id.card_view_llistar_productes);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        //Helper amb els diferents metodes de suport
        helper = new Helper(AdminActivity.this);

        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();
        name = user.get(getResources().getString(R.string.name));
        user_id  = user.get("user_id");

        //Fetching details of the image from sqlite
        Bitmap imageBtmp;
        if ((imageBtmp = (db.getImagePath(Integer.parseInt(user_id))))!=null) {
            userPhoto.setImageBitmap(imageBtmp);
        }

        // Displaying the user details on the screen
        txtName.setText(name);

        //Edit la foto d'usuari
        userPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(AdminActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    //Alliberem el bitmat posat en primera instancia
                    helper.pickFromGallery();
                }else{
                    helper.requestStoragePermission();
                }
            }
        });
        //En seleccionar aquesta cardview, l'usuari admin obtindra un llistat de tots els usuaris de l'aplicacio
        llistarUsuaris.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_b = new Intent(AdminActivity.this, LlistarUsers.class);
                startActivity(intent_b);
                finish();
            }
        });
        /**
         * //En seleccionar aquesta cardvieb, l'usuari admin obtindra un llistat de tots els anuncis publicats per usuaris
         * Aquest es troba dividit en dos fragments, un pers usuaris estudiants, i l'altre per als tutors
         * Pot veure tots els anuncis tant publicats com np
         */
        llistarProductes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminActivity.this, TabActivityAdmin.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_usuari, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            //Opcio de la toolbar per editar les dades de l'usuari admin logegat
            case R.id.action_edit_user:
                Intent intent = new Intent(AdminActivity.this, EditUser.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.action_setting:
                return true;
            case R.id.action_logout:
                //Sortim de l'aplicacio i esborrem l'usuari de la base de dades local.
                helper.logoutUser();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    /**
     * Metode que gestiona si podem o no seleccionar una imatge de la galeria en funcio de l'eleccio de l'usuari en temps d'execucio
     * @param requestCode el codi que identifica l'startactivityforresult
     * @param permissions els permissos solicitats
     * @param grantResults el resultat
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if(requestCode == STORAGE_PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                helper.pickFromGallery();
            }
        }
    }
    /**
     * Metode que gestiona la seleccio d'imatge per al perfil que hem realitzat amb el helper
     * @param requestCode codi que identifica l'intent de l'startactivityforresult
     * @param resultCode el resultat obtingut
     * @param data les dades obtingudes
     */
    public void onActivityResult(int requestCode,int resultCode, Intent data){
        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK)
            if(requestCode == GALLERY_REQUEST_CODE){

                //data.getData return the content URI for the selected Image
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();
                //Get the column index of MediaStore.Images.Media.DATA
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                //Gets the String value in the column
                String imgDecodableString = cursor.getString(columnIndex);
                Bitmap bm = helper.setScaledImageBitmap(imgDecodableString);
                //Afegim la ruta de la imatge a la base de dades
                if ((db.getImagePath(Integer.parseInt(user_id)))==null){
                    db.addImageBitmap((Integer.parseInt(user_id)), bm);
                }else{
                    db.updateImageBitmap((Integer.parseInt(user_id)), bm);
                }
                //Tanquem el cursor
                cursor.close();
                // Set the Image in ImageView after decoding the String
                userPhoto.setImageBitmap(bm);
            }
    }

    @Override
    public void onBackPressed(){
        //desabilitamos volver atras de la MainActivity
        moveTaskToBack(true);
    }


}