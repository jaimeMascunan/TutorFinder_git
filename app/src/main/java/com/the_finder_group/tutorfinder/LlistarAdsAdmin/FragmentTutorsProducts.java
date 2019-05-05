package com.the_finder_group.tutorfinder.LlistarAdsAdmin;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.the_finder_group.tutorfinder.ConnManager.AdDTO;
import com.the_finder_group.tutorfinder.ConnManager.TFClientImple;
import com.the_finder_group.tutorfinder.Helper.ContactsAdapterProducts;
import com.the_finder_group.tutorfinder.Helper.SQLiteHandler;
import com.the_finder_group.tutorfinder.R;

import java.util.ArrayList;
import java.util.List;
/**
 * Fragment en que mostrarem per a l'usuari administrador tots els anuncis publicats pels usuaris tutor
 * que hi ha registrats a l'aplicacio independenment si estan disponibles o reservats per altres usuaris
 */
public class FragmentTutorsProducts extends Fragment {
    //Inicialitzem les constants i declarem les variables
    View v;
    private static final int USER_CODE_TUTOR = 2;
    private static final int POPUP_MENU_ADMIN = 1;
    private RecyclerView recyclerView;
    private List<AdDTO> productList;
    private ContactsAdapterProducts mAdapter;
    private TFClientImple tfClientImple;
    private SQLiteHandler db;
    private Integer popupOption;

    //Constructor de la classe que deixem buit. La farem servir per mostrar la recycleview amb els anuncis dels usuaris del llistat obtingut
    public FragmentTutorsProducts(){

    }
    @Nullable
    @Override
    /**
     * Override del metode onCreateView en que inflarem la recycleview que correspon al fragment on visualitzarem els anuncis dels usuaris tutor
     */
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.list_tutors_fragment, container, false);
        return v;
    }

    @Override
    /**
     * Override del metode onViewCreated on inicialitzem la recycleview i hi asignem el llistat d'anuncis pel usuaris tutor i l'adaptador
     */
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Inicialitzem la recycleview al xml content_list_products
        recyclerView = getView().findViewById(R.id.recycler_view_products);
        //Inicialitzem la llista i cridem al constructor de l'adaptador per el llistat
        productList = new ArrayList<>();
        mAdapter = new ContactsAdapterProducts(getActivity(), productList);

        // SqLite database handler
        db = new SQLiteHandler(getActivity().getApplicationContext());
        if (((popupOption = (db.getOptionPopUP()))==null)||((db.getOptionPopUP()==0))) {
            db.addPopUpOption(POPUP_MENU_ADMIN);
            Log.d("popupOption", String.valueOf(popupOption));
        }else{
            db.updateMenuPoputOption(popupOption, POPUP_MENU_ADMIN);
            Log.d("popupOption", String.valueOf(popupOption));
        }

        //TFClient implementation
        tfClientImple = new TFClientImple();
        //Metode per obtenir cridar la asynctask en que obtindrem el llistat del servidor
        fetchCoursesUser();
        //Definim la recycleview
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
    }
    /**
     * Accedim a la base de dades en segon pla, pasant parametres en format string i obteninr con a resultat una llista d'objectes AdDTO
     */
    public void fetchCoursesUser(){
        new FragmentTutorsProducts.obtindreLlistatRole().execute();
    }

    /**
     * Llancem l'asynktask amb un execute();
     */
    private class obtindreLlistatRole extends AsyncTask<String, Void, List<AdDTO>> {

        @Override
        protected void onPreExecute() {
        }
        @Override
        //Instaniem un objecte de la clase tfClientImpl per realitzar la conexio
        protected List<AdDTO> doInBackground(String... strings) {
            List<AdDTO> llistatProductesRole = tfClientImple.listProductsAdmin(USER_CODE_TUTOR, getActivity().getApplicationContext());

            return llistatProductesRole;
        }
        @Override
        //Afegim el resultat al llistat que utilitzarem per mostrar les dades a la recycleview
        protected void onPostExecute(List<AdDTO> result) {
            super.onPostExecute(result);
            productList.clear();
            productList.addAll(result);
            // refreshing recycler view
            mAdapter.notifyDataSetChanged();
        }
    }



}