package com.the_finder_group.tutorfinder.LlistarAdsReservatsUsers;

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
import java.util.HashMap;
import java.util.List;

/**
 * Fragment amb el que obtindrem els anuncis de l'usuari loggegat que ha reservat
 */
public class FragmentAdsBookedByUser extends Fragment {
    //Inicialitzem les varialbes
    View v;
    private static final int POPUP_MENU_BOOKED_COURSES = 3;
    private RecyclerView recyclerView;
    private List<AdDTO> productList;
    private ContactsAdapterProducts mAdapter;
    private TFClientImple tfClientImple;
    private SQLiteHandler db;
    //Nomes es necessari definir la opcio del popupmenu en un dels fragments, ja que l'altre heredara en valor al compartir activitat
    private Integer popupOption, queryUserId;
    //Constructor buit ja que nomes necessitem obtenir el llistat
    public FragmentAdsBookedByUser(){
    }

    @Nullable
    @Override
    /**
     * Override del metode onCreateView en que inflarem la layout que te l'acces a la recycleview
     */
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.list_students_fragment, container, false);
        return v;
    }

    @Override
    /**
     * Override del metode onViewCreated en que inicialitzem les diferents variables
     */
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Inicialitzem la layout per al recycleview
        recyclerView = getView().findViewById(R.id.recycler_view_products);
        //Llistat a on emmagatzemarem el resultat de la consulta al servidor
        productList = new ArrayList<>();
        //Adaptador amb el format del popup menu i la definicio dels diferents items
        mAdapter = new ContactsAdapterProducts(getActivity(), productList);
        // SqLite database handler
        db = new SQLiteHandler(getActivity().getApplicationContext());
        //Accedim a les dades guardades a la base de de dades local
        HashMap<String, String> user = db.getUserDetails();
        //Id de l'usuari que esta realitzant la consulta
        queryUserId = Integer.parseInt(user.get("user_id"));
        //Emmagatzemem l'opcio del menu que volem mostrar a l'usuari, en aquest cas la corresponent a les reserves realtzades
        if (((popupOption = (db.getOptionPopUP()))==null)||((db.getOptionPopUP()==0))) {
            db.addPopUpOption(POPUP_MENU_BOOKED_COURSES);
            Log.d("popupOption user", String.valueOf(popupOption));
        }else{
            db.updateMenuPoputOption(popupOption, POPUP_MENU_BOOKED_COURSES);
            Log.d("popupOption user", String.valueOf(popupOption));
        }

        //TFClient implementation
        tfClientImple = new TFClientImple();
        fetchAdsBookedByUser();
        //Inicialitzem la recycleview amb els diferents parametres
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
    }

    /**
     * Metode per ontenir el llistat d'anuncis reservats per l'usuari que esta loggegat a l'aplicacio
     */
    public void fetchAdsBookedByUser(){
        new obtindreLlistatAdsBookedByUser().execute();
    }

    /**
     * Asynctask amb la que obtindrem un llistat d'aunucis amb els parametres solicitats
     */
    private class obtindreLlistatAdsBookedByUser extends AsyncTask<String, Void, List<AdDTO>> {

        @Override
        protected void onPreExecute() {
        }
        @Override
        //Instaniem un objecte de la clase tfClientImpl per realitzar la conexio
        protected List<AdDTO> doInBackground(String... strings) {
            List<AdDTO> listAds = tfClientImple.listBookAdsUser(queryUserId, getActivity());
            //Obtenim el llistat
            return listAds;
        }
        @Override

        protected void onPostExecute(List<AdDTO> result) {
            super.onPostExecute(result);
            //El pasem al adaptador de productes per a mostrar les diferents files de la recycleview
            productList.clear();
            productList.addAll(result);
            // refreshing recycler view
            mAdapter.notifyDataSetChanged();
        }
    }
}
