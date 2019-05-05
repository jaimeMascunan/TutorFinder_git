package com.the_finder_group.tutorfinder;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.the_finder_group.tutorfinder.ConnManager.TFClientImple;
import com.the_finder_group.tutorfinder.ConnManager.UserDTO;
import com.the_finder_group.tutorfinder.Helper.Helper;
import com.the_finder_group.tutorfinder.Helper.SQLiteHandler;
import com.the_finder_group.tutorfinder.Helper.SessionManager;

import java.util.HashMap;


/**
 * Pantalla de login mitjançant la introduccio d'un usuari i contrassenya que seran checkejats amb el contingut de la base de dades
 */
public class LoginActivity extends AppCompatActivity {
    //Definim les variables
    private static final String TAG = LoginActivity.class.getSimpleName();
    EditText name_login, password_login;
    Button login_btn;
    TextView signup_link;
    private ProgressDialog pDialog;
    private AlertDialog.Builder aDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private TFClientImple tfClientImple;
    private Helper helper;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        name_login = (EditText) findViewById(R.id.input_name_login);
        password_login = (EditText) findViewById(R.id.input_password_login);
        login_btn = (Button) findViewById(R.id.login_button);
        signup_link = (TextView) findViewById(R.id.signup_link);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        //Alert dialog
        aDialog = new AlertDialog.Builder(this);
        aDialog.setTitle("Atenció!");
        aDialog.setIcon(android.R.drawable.ic_dialog_info);
        aDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());

        //Helper
        helper = new Helper(LoginActivity.this);

        //TFClient implementation
        tfClientImple = new TFClientImple();

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to correct user_type activity
            // Fetching user details from sqlite
            HashMap<String, String> user = db.getUserDetails();

            String user_type = user.get(getResources().getString(R.string.user_type));
            helper.redirectUserTypeAct(user_type);
        }
        //Si l'usuari selecciona la opcio login es llança el metode login
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        //En cas que es vulgui realitzar el registre, es llança l'activitat signup
        signup_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * Metode per realitzar la conexio al servidor i obtenir un string amb
     * la contrasenya guardada a la base de dades en funcio de si s'ha trobat
     * una coincidencia o no amb els registres guardats en aquesta
     */
    public void login(){

        Log.d(TAG, "Login");
        //Validem que les dades tinguin el format definit. En cas contrari informem a l'usuari/a
        if(!helper.validate_login(name_login, password_login)){
            aDialog.setMessage(getResources().getString(R.string.error_login));
            aDialog.show();
            return;
        }
        //Obtenim les dades els textview coorresponents
        String name = name_login.getText().toString();
        String password = password_login.getText().toString();

        //Llancem l'asynctask per realitzar la conexio en segon pla
        new checkLogin().execute(name, password);
    }

    /**
     * Clase per realitzar la conexio amb la base de dades. Aquesta tasca rebra parametres de tipus string
     * retornara un string i no definim cap tipus d'unitat de progressio
     */
    private class checkLogin extends AsyncTask<String, Void, UserDTO> {
        String userName, password;

        //Abans de res llancem el progres dialog per informar a l'usuari/a de l'accio que s'esta realitzant
        @Override
        protected void onPreExecute(){
            pDialog.setMessage("Logging in ...");
            showDialog();
        }

        @Override
        //Instaniem un objecte de la clase tfClientImpl per realitzar la conexio i obtenim els parametres
        protected UserDTO doInBackground(String... strings) {
            userName = strings[0];
            password = strings[1];
            String pswdEncrypted = Helper.cryptWithMD5(password);
            UserDTO userDTO = null;
            //Obtenim el password desat a la base de dades per aquest usuari
            boolean login = tfClientImple.login(userName, pswdEncrypted, getApplicationContext());

            if (login){
                userDTO = tfClientImple.userData(userName, getApplicationContext());
                Log.d(TAG, userDTO.getUserName().toString());
                Log.d(TAG, userDTO.getUserPswd().toString());
                Log.d(TAG, userDTO.getUserMail().toString());
                Log.d(TAG, userDTO.getUserRol().toString());

            }
            return userDTO;
        }
        @Override
        /**
         * Amaguem el progres dialog. En cas que s'hagi trobat un usuari que concideixi amb les credencials pasades,
         * l'afegim a la base de dades local, modifiquem el valor de la sessio i redirigim a la activitat corresponent.
         */
        protected void onPostExecute(UserDTO result){
            super.onPostExecute(result);
            hideDialog();
            if (result != null){
                session.setLogin(true);
                // Inserting row in users table
                db.addUser(result.getUserId(),
                            result.getUserName().toString(),
                            result.getUserMail().toString(),
                            result.getUserPswd().toString(),
                            result.getUserRol().toString());

                helper.redirectUserTypeAct(result.getUserRol().toString());

            }else{
                aDialog.setMessage(getResources().getString(R.string.error_login));
                aDialog.show();
            }
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








