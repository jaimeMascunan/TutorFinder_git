package com.the_finder_group.tutorfinder;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.the_finder_group.tutorfinder.ConnManager.TFClientImple;
import com.the_finder_group.tutorfinder.ConnManager.UserMessageDTO;
import com.the_finder_group.tutorfinder.Helper.Helper;
import com.the_finder_group.tutorfinder.Helper.MessageListAdapter;
import com.the_finder_group.tutorfinder.Helper.SQLiteHandler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Activitat per a la publicacio i desat al servidor de l'aplicacio, de missatges intercanviats entre dos usuaris d'aquesta.
 */
public class MessageListActivity extends AppCompatActivity {
    //Definim les variables
    private static final String TAG = MessageListActivity.class.getSimpleName();

    private RecyclerView recyclerView;
    private MessageListAdapter mMessageAdapter;
    private List<UserMessageDTO> messageList;
    private SQLiteHandler db;
    private TFClientImple tfClientImple;
    private Helper helper;
    private Button send;
    private Integer  db_user_id, ad_owner_id;
    private String db_user_name, db_loggedUserType, message, timestamp, ad_owner_name;
    private EditText chat_box;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
        //Toolbar amb les opcions disponibles aquesta activitat
        Toolbar toolbar = findViewById(R.id.toolbar_chat);
        setSupportActionBar(toolbar);

        // toolbar fancy stuff
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Inicialitzem la recycleview en la que mostrarem els missatges
        recyclerView = (RecyclerView) findViewById(R.id.reyclerview_message_list);
        messageList = new ArrayList<>();
        mMessageAdapter = new MessageListAdapter(this, messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Inicialitzem la capsa de text on els usuaris escriuran els missatges a enviar
        chat_box = (EditText)findViewById(R.id.edittext_chatbox);

        //TFClient implementation
        tfClientImple = new TFClientImple();

        //Ontenim la informacio continguda als extras de l'intent
        Intent intentGetAdDetails = getIntent();
        //Obtenim el id i nom per l'usuari que rebra el missatge i que pot ser el publicador de l'anunci o la persona que l'ha reservat
        if(intentGetAdDetails.hasExtra("ad_owner_id")) {
            ad_owner_id = Integer.parseInt(intentGetAdDetails.getStringExtra("ad_owner_id"));
            ad_owner_name = intentGetAdDetails.getStringExtra("ad_owner_name");
        }
        //Inicialitzem el access a les classes de suport
        helper = new Helper(getApplicationContext());
        //Inicialitzem l'acces a la base de dades local
        db = new SQLiteHandler(getApplicationContext());
        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();
        //Id de l'usuari que esta realitzant lel registre
        db_user_id = Integer.parseInt(user.get("user_id"));
        db_user_name = user.get("name");
        db_loggedUserType = user.get(getResources().getString(R.string.user_type));
        //Obtenim el listat dels missatges que estan guardats a la base de dades i que s'han intercanviat previament els dos usuaris/es
        listPreviousMessages();
        //Inicialitzem l'accio a dur a terme en cas de que es premi el boto send de la interficie
        send = (Button)findViewById(R.id.button_chatbox_send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Obtenim el missatge escrit, el moment de fer-ho en el format indicat i prodecim a guardar el missatge a la base de dades del servidor
                message = chat_box.getText().toString();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM hh:mm");
                timestamp = simpleDateFormat.format(new Date());
                sendMessage();
            }
        });
        //Inicialitzem els parametres i adapter de la recycleview
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mMessageAdapter);

    }

    public void sendMessage(){
        Log.d(TAG, "Send message");
        //En aquest cas no validem les dades, es tindria que resoldre aquest conflicte.
        new sendMessage().execute(String.valueOf(db_user_id),
                db_user_name, message, timestamp,
                String.valueOf(ad_owner_id), ad_owner_name);

    }

    /**
     * Asynctask per a l'enviament d'un missatge entre usuaris. Obtindrem un boolea amb valor true de proder-se dur a terme correctament l'insercio
     */
    private class sendMessage extends AsyncTask<String, Void, Boolean> {
        String  message, userName, time, adReservaName;
        Integer userId, adReservaId;

        //No indiquem prograssbar ja que volem que sigui transparent a l'usuari/a
        @Override
        protected void onPreExecute(){ }

        @Override
        //Obtenim els parametres i realitzem la insercio del objecte missatge a la base de dades
        protected Boolean doInBackground(String... strings) {
            userId = Integer.parseInt(strings[0]);
            userName = strings[1];
            message = strings[2];
            time = strings[3];
            adReservaId = Integer.parseInt(strings[4]);
            adReservaName = strings[5];
            //Obtenim el resutat
            boolean publish = tfClientImple.createMessage(userId, userName, message, time,
                    adReservaId, adReservaName, getApplicationContext());

            return publish;
        }

        @Override
        //Posem el edittext a empty i afegim el missatge a la recycleview per a mostrar-ne el contingut
        protected void onPostExecute(Boolean result){
            super.onPostExecute(result);
            chat_box.setText("");
            if (result){
                UserMessageDTO userMessageDTO = new UserMessageDTO();
                userMessageDTO.setMessageUserId(userId);
                userMessageDTO.setMessageUserName(userName);
                userMessageDTO.setMessageText(message);
                userMessageDTO.setMessageDate(time);
                userMessageDTO.setReceiverUserId(adReservaId);
                userMessageDTO.setReceiverUserName(adReservaName);
                messageList.add(userMessageDTO);
            }
            // refreshing recycler view
            mMessageAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Metode per obtenir el llistat de objectes userMessageDTO intercanviats previament entre els dos usuaris
     */
    public void listPreviousMessages(){
        Log.d(TAG, "Retrieve previous messages");
        //No realitzem validacio en tractar-se de missatges emmagatzemats a la base de dades

        new listPreviousMessages().execute(String.valueOf(db_user_id), String.valueOf(ad_owner_id));

    }

    /**
     * Asynctask que ens permet conectar a la base de dades per obtenir un llitat dels missatges intercanviats entre dos usuaris concrets
     */
    private class listPreviousMessages extends AsyncTask<String, Void, List<UserMessageDTO>> {
        Integer userId, adOwnerId;

        //No indiquem prograssbar ja que volem que sigui transparent a l'usuari/a
        @Override
        protected void onPreExecute(){ }

        @Override
        /**
         * Obtenim els parametres i llançem dos accessos a la base de dades, un en el que obindrem tots els missatges de l'usuari A enviats a B.
         * i un altre de tots els missatges de l'usuari B enviats a A.
         * Despres els afegirem tots a una llista que prodecirem a ordenar
         */
        protected List<UserMessageDTO> doInBackground(String... strings) {
            userId = Integer.parseInt(strings[0]);
            adOwnerId = Integer.parseInt(strings[1]);

            List<UserMessageDTO> messageListSent = tfClientImple.listMessagesByUser(userId, adOwnerId, getApplicationContext());
            List<UserMessageDTO> messageListReceived = tfClientImple.listMessagesByUser(adOwnerId, userId, getApplicationContext());
            List <UserMessageDTO> list = new ArrayList<>();
            list.addAll(messageListSent);
            list.addAll(messageListReceived);
            //Ordenem els missatges en funcio del ID d'aquestos. D'aquesta forma els podrem mostrar d'acord amb la sequencia temporal
            //En la que els vam introduir
            Collections.sort(list, new Comparator<UserMessageDTO>() {
                @Override
                public int compare(@NonNull UserMessageDTO userMessage1, UserMessageDTO userMessage2) {
                    return Integer.valueOf(userMessage1.getMessageId()).compareTo(Integer.valueOf(userMessage2.getMessageId()));
                }
            });
            //Obtenim el llistat dels missatges
            return list;
        }

        @Override
        protected void onPostExecute(List<UserMessageDTO> result){
            super.onPostExecute(result);
            messageList.clear();
            //Netegem el llistat i afegim els resultats
            messageList.addAll(result);
            // refreshing recycler view
            mMessageAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //Tornem a la pantalla principal de l'usuari en funcio del seu tipus
        if(id == android.R.id.home) {
            helper.redirectUserTypeAct(db_loggedUserType);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed(){
        //desabilitamos volver atras de la MainActivity
        moveTaskToBack(true);
    }
}
