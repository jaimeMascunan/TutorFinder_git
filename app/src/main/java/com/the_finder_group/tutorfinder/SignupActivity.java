package com.the_finder_group.tutorfinder;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.the_finder_group.tutorfinder.ConnManager.TFClientImple;
import com.the_finder_group.tutorfinder.Helper.Helper;
import com.the_finder_group.tutorfinder.Helper.SQLiteHandler;
import com.the_finder_group.tutorfinder.Helper.SessionManager;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;

/**
 * Pantalla de registre mitjançant la introduccio d'un usuari, email, contrassenya i
 * tipus d'usuari/a que seran enviats al servidor i guardats a la base de dades
 */
public class SignupActivity extends AppCompatActivity {
    //Definim les variables
    private static final String TAG = SignupActivity.class.getSimpleName();

    EditText name_signup, email_signup, password_signup;
    Button signup_btn;
    AppCompatSpinner user_type_login;
    TextView login_link;
    private ProgressDialog pDialog;
    private AlertDialog.Builder aDialog;

    private SessionManager session;
    private SQLiteHandler db;
    private TFClientImple tfClientImple;
    private Helper helper;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        //Inicialitzem les variables/buttons amb els id presents a la layout inflada
        name_signup = (EditText) findViewById(R.id.input_name_signup);
        email_signup = (EditText) findViewById(R.id.input_email_signup);
        password_signup = (EditText) findViewById(R.id.input_password_signup);
        user_type_login = (AppCompatSpinner) findViewById(R.id.login_spinner);
        signup_btn = (Button) findViewById(R.id.signup_button);
        login_link = (TextView) findViewById(R.id.login_link);

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

        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        //Helper
        helper = new Helper(SignupActivity.this);

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
        //Adaptador de l'spinner amb els diferents tipus d'usuaris
        ArrayAdapter <CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.user_type_register, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        user_type_login.setAdapter(adapter);

        //Si l'usuari selecciona la opcio signup es llança el metode signup
        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    signup();
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
        });

        //En cas que es vulgui realitzar el login, es llança l'activitat login
        login_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    /**
     * Metode per realitzar la conexio al servidor i guardar a la base de dades un objecte usuari
     */
    public void signup() throws InvalidKeySpecException, NoSuchAlgorithmException {
        Log.d(TAG, "Signup");
        //Validem que les dades tinguin el format definit. En cas contrari informem a l'usuari/a
        if(!helper.validate_signup(name_signup, email_signup, password_signup, user_type_login)){
            aDialog.setMessage(getResources().getString(R.string.error_registration));
            aDialog.show();
            return;
        }
        //Obtenim els valors introduits per l'usuari/a al layout de registre
        String name = name_signup.getText().toString().trim();
        String email = email_signup.getText().toString().trim();
        String password = password_signup.getText().toString().trim();
        //Encriptem el password
        String pswdEncrypted = helper.cryptWithMD5(password);
        //Obtenim el tipus d'usuari seleccionat
        String user_type = user_type_login.getSelectedItem().toString();
        //Llancem l'asynctask per realitzar la conexio en segon pla
        new registerUser().execute(name, email, pswdEncrypted, user_type);
    }
    /**
     * Clase per realitzar la conexio amb la base de dades i guardar un objecte de tipus usuari
     */
    private class registerUser extends AsyncTask<String, Void, Boolean> {
        String userName, email, password, user_type;
        //Mostrem el progress dialog mentre es realitza la transaccio
        @Override
        protected void onPreExecute(){
            pDialog.setMessage("Registering");
            showDialog();
        }
        //Obtenim els valors passats per parametre i contectem amb la base de dades per realitzar el registre d'usuari
        @Override
        protected Boolean doInBackground(String... strings) {
            userName = strings[0];
            email = strings[1];
            password = strings[2];
            user_type = strings[3];
            //Obtindrem true de haver-se realitzat amb exit
            boolean register = tfClientImple.newUser(userName, email, password, user_type, getApplicationContext());
            return register;
        }
        //En cas de haver realitzat el registre amb exit, redirigim a l'usuari a l'activitat login
        @Override
        protected void onPostExecute(Boolean result){
            super.onPostExecute(result);
            hideDialog();
            if (result){
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
                finish();
            }else{
                aDialog.setMessage(getResources().getString(R.string.error_registration));
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
