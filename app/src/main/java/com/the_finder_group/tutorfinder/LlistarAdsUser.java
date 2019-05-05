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

import com.the_finder_group.tutorfinder.ConnManager.AdDTO;
import com.the_finder_group.tutorfinder.ConnManager.TFClientImple;
import com.the_finder_group.tutorfinder.Helper.ContactsAdapterProducts;
import com.the_finder_group.tutorfinder.Helper.Helper;
import com.the_finder_group.tutorfinder.Helper.SQLiteHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Clase que contecta amb la base de dades del servidor per obtenir un llistat dels anuncis d'usuaris per id o role
 */
public class LlistarAdsUser extends AppCompatActivity {
    //Definim les variables de l'activitat
    private static final String TAG = LlistarAdsUser.class.getSimpleName();
    private static final int POPUP_MENU_OWN_COURSES = 1;
    private static final int POPUP_MENU_OTHER_COURSES = 2;

    private RecyclerView recyclerView;
    private List<AdDTO> productList;
    private ContactsAdapterProducts mAdapter;
    private SearchView searchView;
    private String db_loggedUserType, db_user_id;
    private Integer userRoleIdList, userId, popupOption;

    private SQLiteHandler db;
    private TFClientImple tfClientImple;
    private Helper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llistar_productes);
        //Afegim una toolbar per a l'activitat amb opcions de filtrar per text
        Toolbar toolbar = findViewById(R.id.toolbar_search_producte);
        setSupportActionBar(toolbar);

        // toolbar fancy stuff
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Inicialitzem els diferents elements de la recycleview en la que mostrarem els llistats obtinguts
        recyclerView = findViewById(R.id.recycler_view_products);
        productList = new ArrayList<>();
        mAdapter = new ContactsAdapterProducts(this, productList);

        //TFClient implementation
        tfClientImple = new TFClientImple();

        //Acces als metodes de suport de l'aplicacio
        helper = new Helper(getApplicationContext());

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());
        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();
        //Id de l'usuari que esta realitzant la publicacio
        db_loggedUserType = user.get(getResources().getString(R.string.user_type));
        //Es user_id en compte de id com esta definit a la creacio de la taula perque ho tenim definit aixi a getUserDetails de la DB
        db_user_id = user.get("user_id");
        //Obtenim les dades que em pasat per l'intent al llançar l'activitat
        Intent intentGetListType = getIntent();
        //En cas que de tinguem un element dintre de l'intent amb el seguent nom, obtindrem un llistat amb els cursos
        //disponibles per al tipus d'usuari desitget. En aquest cas tutor o estudiant
        if(intentGetListType.hasExtra("user_role_id")){
            userRoleIdList = Integer.parseInt(intentGetListType.getStringExtra("user_role_id"));
            fetchCoursesRole();
            //Indiquem el popup menu que volem mostrar per aquest tipus de llistat i usuari
            if (((popupOption = (db.getOptionPopUP()))==null)||((db.getOptionPopUP()==0))) {
                db.addPopUpOption(POPUP_MENU_OTHER_COURSES);
            }else{
                db.updateMenuPoputOption(popupOption, POPUP_MENU_OTHER_COURSES);
            }
        }
        //En cas que de tinguem un element dintre de l'intent amb el seguent nom, obtindrem un llistat amb els cursos
        //disponibles per al usuari amb id pasat per l'ontent.
        if(intentGetListType.hasExtra("user_id_list_published_courses")){
           userId = Integer.parseInt(intentGetListType.getStringExtra("user_id_list_published_courses"));
           if(userId == Integer.parseInt(db_user_id)){
               fetchCoursesUser();
               //Indiquem el popup menu que volem mostrar per aquest tipus de llistat i usuari
               if (((popupOption = (db.getOptionPopUP()))==null)||((db.getOptionPopUP()==0))) {
                   db.addPopUpOption(POPUP_MENU_OWN_COURSES);
               }else{
                   db.updateMenuPoputOption(popupOption, POPUP_MENU_OWN_COURSES);
               }
           }
        }
        // white background notification bar
        whiteNotificationBar(recyclerView);
        //Iniciem el adapter i layout per a la recycleview
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

    }
    /**
     * Obtenim la llista de cursos disponibles per tipus d'usuari
     */
    public void fetchCoursesRole() {
        new obtindreLlistatRole().execute();
    }

    /**
     * Obtenim el llistat de cursos disponibles d'un usuari registrat en concret
     */
    public void fetchCoursesUser() {new obtindreLlistatUser().execute(); }

    /**
     * AsyncTask per a l'obtencio d'un llistat d'anuncis per role d'usuari
     */
    private class obtindreLlistatRole extends AsyncTask<String, Void, List<AdDTO>> {

        @Override
        //En aquest cas volem que sigui transparent al usuari/a i per aixo no indiquem un progress dialog
        protected void onPreExecute() {
        }
        @Override
        //Instanciem un objecte de la clase tfClientImpl per realitzar la conexio indicant els parametres desitjats
        protected List<AdDTO> doInBackground(String... strings) {
            List<AdDTO> llistatProductesRole = tfClientImple.listProductsRole(userRoleIdList, getApplicationContext());
            //Obtenim un llistat d'obtectes AdDTO amb els resultats desitjats
            return llistatProductesRole;
        }
        @Override

        protected void onPostExecute(List<AdDTO> result) {
            super.onPostExecute(result);
            productList.clear();
            productList.addAll(result);
            // refreshing recycler view
            mAdapter.notifyDataSetChanged();
        }
    }
    /**
     * AsyncTask per a l'obtencio d'un llistat d'anuncis per a un usuari en concret
     */
    private class obtindreLlistatUser extends AsyncTask<String, Void, List<AdDTO>> {

        @Override
        //En aquest cas volem que sigui transparent al usuari/a i per aixo no indiquem un progress dialog
        protected void onPreExecute() {
        }
        @Override
        //Instaniem un objecte de la clase tfClientImpl per realitzar la conexio indicant els parametres desitjats
        protected List<AdDTO> doInBackground(String... strings) {
            List<AdDTO> llistatProductesUser = tfClientImple.listProductsUser(userId, getApplicationContext());
            //Obtenim un llistat d'obtectes AdDTO amb els resultats desitjats
            return llistatProductesUser;
        }
        @Override

        protected void onPostExecute(List<AdDTO> result) {
            super.onPostExecute(result);
            productList.clear();
            productList.addAll(result);
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
        if (id == R.id.action_search) { return true; }

        //Redireccionam a l'usuari a la seva pantalla principal en funcio del seu tipus
        if(id == android.R.id.home) {
            helper.redirectUserTypeAct(db_loggedUserType);
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
