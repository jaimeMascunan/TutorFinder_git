package com.the_finder_group.tutorfinder;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import com.the_finder_group.tutorfinder.ConnManager.TFClientImple;
import com.the_finder_group.tutorfinder.Helper.Helper;
import com.the_finder_group.tutorfinder.Helper.SQLiteHandler;

import java.util.HashMap;

/**
 * Classe que implementa la publicacio d'anuncis per part d'usuaris
 */
public class AdActivity extends AppCompatActivity {

    //Definim les variables
    private static final String TAG = AdActivity.class.getSimpleName();

    EditText ad_titol, ad_descripcio, ad_preu;
    Button publish_btn;
    AppCompatSpinner ad_type;
    private Integer queryUserId, adTypeId;;
    private String userType;
    private ProgressDialog pDialog;
    private AlertDialog.Builder aDialog;

    private TFClientImple tfClientImple;
    private SQLiteHandler db;
    private Helper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad);
        //Inicialitzem els diferents elements de la layout
        ad_titol = (EditText)findViewById(R.id.ad_title);
        ad_descripcio = (EditText)findViewById(R.id.ad_descripcio);
        ad_preu = (EditText)findViewById(R.id.ad_price);
        publish_btn = (Button)findViewById(R.id.publish_ad);
        ad_type = (AppCompatSpinner) findViewById(R.id.ad_type_spinner);

        // Progress dialog per mostrar el progres de l'asynctask
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        //Alert dialog que demana confirmacio del usuari a l'hora de publicar anunci
        aDialog = new AlertDialog.Builder(this);
        aDialog.setTitle("Publicar anunci");
        aDialog.setIcon(android.R.drawable.ic_dialog_info);
        aDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        //TFClient implementation
        tfClientImple = new TFClientImple();

        //Helper
        helper = new Helper(getApplicationContext());
        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());
        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();
        //Id de l'usuari que esta realitzant la publicacio
        queryUserId = Integer.parseInt(user.get("user_id"));
        userType = user.get(getResources().getString(R.string.user_type));

        //Adaptador de l'spinner amb els diferents tipus de tipus de cursos a publicar
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.ad_typer_publish, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ad_type.setAdapter(adapter);
        //Implementem el onclicklistener per al boto que confirma la publicacio de l'anunci amb les dades introduides per l'usuari
        publish_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishAd();
            }
        });
    }

    /**
     * Metode per realitzar la conexio al servidor i guardar a la base de dades un objecte usuari
     */
    public void publishAd(){
        Log.d(TAG, "PublishAd");
        //Validem que les dades tinguin el format definit. En cas contrari informem a l'usuari/a
        if(!helper.validate_ad(ad_titol, ad_descripcio, ad_preu, ad_type)){
            aDialog.setMessage("No s'ha pogut realitzar la publicacio");
            aDialog.show();
            return;
        }
        String categoria = ad_type.getSelectedItem().toString();
        //Abans de guardar l'anunci necessitem obtenir el id de l'anunci que es correspon a la categoria escollida
        new getAdTypeByName().execute(categoria);

    }
    /**
     * Clase per realitzar la conexio amb la base de dades i guardar un objecte de tipus usuari
     */
    private class publishAd extends AsyncTask<String, Void, Boolean> {
        String  titol, descripcio, disponibilitat;
        Integer userId, preu, adTypeId;

        @Override
        //Mmpstrem el progres dialog mentre conectem a la base de dades
        protected void onPreExecute(){
            pDialog.setMessage("Publishing");
            showDialog();
        }

        @Override
        //Obtenim els valors pasats per parametre
        protected Boolean doInBackground(String... strings) {
            userId = Integer.parseInt(strings[0]);
            titol = strings[1];
            descripcio = strings[2];
            preu = Integer.parseInt(strings[3]);
            adTypeId = Integer.parseInt(strings[4]);
            //TFC implementation per a crear l'objecte anunci a la base da dades per aquest usuari
            boolean publish = tfClientImple.createAd(userId, titol, descripcio, adTypeId, preu, getApplicationContext());
            return publish;
        }

        @Override
        protected void onPostExecute(Boolean result){
            super.onPostExecute(result);
            //En cas que el resultat sigui true, amagem el progresdialog i redirecionem
            // a l'usuari a la pantalla principal en funcio del seu tipus
            hideDialog();
            if (result){
                helper.redirectUserTypeAct(userType);
            }else{
                aDialog.setMessage(getResources().getString(R.string.error_registration));
                aDialog.show();
            }
        }
    }

    /**
     * Clase per realitzar la conexio amb la base de dades i obtenir l'id que correspon al nom d'una categoria
     */
    private class getAdTypeByName extends AsyncTask<String, Void, Integer> {
        String categoria;

        @Override
        //En aquest cas no volem que l'usuari sigui conscient, i per tant no declarem el progresdialog
        protected void onPreExecute(){}

        @Override
        protected Integer doInBackground(String... strings) {
            //Obtenim els parametres i conectem amb la base de dades
            categoria = strings[0];
            Integer adTypeId = tfClientImple.getAdTypeByName(categoria, getApplicationContext());
            Log.d(TAG, String.valueOf(adTypeId));
            return adTypeId;
        }

        @Override
        protected void onPostExecute(Integer result){
            super.onPostExecute(result);
            //Una vegada em obtingut l'id, ja podem realitzar la publicacio de l'anunci amb les dades en el format adequat
            String titol = ad_titol.getText().toString().trim();
            String descripcio = ad_descripcio.getText().toString().trim();
            String preu = ad_preu.getText().toString().trim();
            adTypeId = result;
            Log.d(TAG, String.valueOf(adTypeId));

            //Llancem l'asynctask per realitzar la conexio en segon pla
            new publishAd().execute(String.valueOf(queryUserId), titol, descripcio,  preu, String.valueOf(adTypeId));

        }
    }
    //Mostrem el progres dialog
    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }
    //Amaguem el progres dialog
    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    public void onBackPressed(){
        //desabilitamos volver atras de la MainActivity
        moveTaskToBack(true);
    }

}
