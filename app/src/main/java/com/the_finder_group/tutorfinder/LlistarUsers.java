package com.the_finder_group.tutorfinder;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.the_finder_group.tutorfinder.ConnManager.TFClientImple;
import com.the_finder_group.tutorfinder.ConnManager.UserDTO;
import com.the_finder_group.tutorfinder.Helper.ContactsAdapterUsers;
import com.the_finder_group.tutorfinder.Helper.SQLiteHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Clase que obte el llistat d'usuaris de l'aplicacio que passarem al adapter de la recycleview per mostrarlos
 */
public class LlistarUsers extends AppCompatActivity {
    //Declarem les varialbes de l'activitat
    private static final String TAG = LlistarUsers.class.getSimpleName();
    private RecyclerView recyclerView;
    private ArrayList<UserDTO> contactList;
    private ContactsAdapterUsers mAdapter;
    private SearchView searchView;
    private Integer queryUserId;
    private UserDTO userToRemove;

    private SQLiteHandler db;
    private TFClientImple tfClientImple;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llistar_users);
        //Afegim el toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_search);
        setSupportActionBar(toolbar);

        // toolbar fancy stuff
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Inicialitzem la view de la recycleview
        recyclerView = findViewById(R.id.recycler_view);
        //Inicialitzem el arraylost dels objectes userDTO que obtindreem
        contactList = new ArrayList<>();
        //Inicialitzem l'adaptador que omplira la vista
        mAdapter = new ContactsAdapterUsers(this, contactList);

        //TFClient implementation
        tfClientImple = new TFClientImple();

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());
        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();
        //Id de l'usuari que esta realitzant la consulta
        queryUserId = Integer.parseInt(user.get("user_id"));

        // white background notification bar
        whiteNotificationBar(recyclerView);

        //Posem en marxa la recycleview
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        //Obtenim el llistat d'usuaris de la aplicacio
        fetchContacts();
    }

    /**
     * Obtenim la llista d'usuaris
     */
    private void fetchContacts() {
        new obtindreLlistat().execute();
    }

    /**
     * Clase per realitzar la conexio amb la base de dades per obtenir un llistat d'usuaris de l'aplicacio
     * Aquesta taska rebra parametres de tipus string.retornara una lista d'objectes userDTO i no definim cap tipus d'unitat de progressio
     */
    private class obtindreLlistat extends AsyncTask<String, Void, List<UserDTO>> {

        @Override
        protected void onPreExecute() {
        }
        @Override
        //Instaniem un objecte de la clase tfClientImpl per realitzar la conexio
        protected List<UserDTO> doInBackground(String... strings) {
            ArrayList<UserDTO> llistatUsuaris = tfClientImple.listUsers(getApplicationContext());
            //Eliminem l'usuari que esta loggegat de la llista per a que no es pugi esborrar a si mateix
            Iterator<UserDTO> users_iterator = llistatUsuaris.iterator();
            while (users_iterator.hasNext()){
                UserDTO userDTO = users_iterator.next();
                if((userDTO.getUserId())== queryUserId){
                    userToRemove = userDTO;
                }
            }
            llistatUsuaris.remove(userToRemove);
            return llistatUsuaris;
        }
        @Override
        protected void onPostExecute(List<UserDTO> result) {
            super.onPostExecute(result);
            //Afegim els resultats obtinguts a la llista de la recycleview
            contactList.clear();
            contactList.addAll(result);
            // refreshing recycler view
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    /**
     * Menu per a implementar la filtracio dels resultats introduint text. Aquest es comparara amb el nom dels usuaris
     * per nomes mostrar els que coincideixin
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                mAdapter.getFilter().filter(query);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }
        //COm l'usuari admin es l'unic que pot definir aquest llistat, tornem a l'activitat per aquest usuari
        if(id == android.R.id.home) {
            Intent intent = new Intent(getApplicationContext(), AdminActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //desabilitamos volver atras de la MainActivity
        moveTaskToBack(true);
    }

    private void whiteNotificationBar(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }
}