package com.the_finder_group.tutorfinder.LlistarAdsReservatsUsers;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
 * Fragment que conte el llistat de anuncis de l'usuari loggegat a l'aplicacio que han estat reservats per altres
 * Recordo que en aquests moment l'aplicacio nomes permet un usuari loggegat al mateix temps
 */
public class FragmentAdsBookedByOthers extends Fragment {
    //Definim les varialbes
    View v;
    private RecyclerView recyclerView;
    private List<AdDTO> productList;
    private ContactsAdapterProducts mAdapter;
    private TFClientImple tfClientImple;
    private SQLiteHandler db;
    private Integer  queryUserId;
    //Constuctor buit, ja que nomes mostrarem el llistat de anuncis
    public FragmentAdsBookedByOthers(){

    }

    @Nullable
    @Override
    /**
     * Override del metode onCreateView en que inflemm la layout amb l'acces a la recycleview
     */
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.list_tutors_fragment, container, false);
        return v;
    }
    @Override
    /**
     * Override el metode onViewCreate en que inicialitzem els diferents elements que ens permetran veure la recycleview
     */
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Inicialitzem les varialbles
        recyclerView = getView().findViewById(R.id.recycler_view_products);
        //Llistat amb els elements seleccionats
        productList = new ArrayList<>();
        //Adaptador per als productes amb les opcions del popupmenu i la vista dels diferents items
        mAdapter = new ContactsAdapterProducts(getActivity(), productList);
        // SqLite database handler
        db = new SQLiteHandler(getActivity().getApplicationContext());
        //Accedim a les dades emmagatzemades a la base de dades local
        HashMap<String, String> user = db.getUserDetails();
        //Id de l'usuari que esta realitzant la consulta
        queryUserId = Integer.parseInt(user.get("user_id"));
        //TFClient implementation
        tfClientImple = new TFClientImple();
        fetchAdsBookedByOthers();
        //Inicialitzem i definim la recyclevieb
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
    }

    /**
     * Metode per obtenir el llistat de anuncis amb els parametres seleccionats
     */
    public void fetchAdsBookedByOthers(){ new FragmentAdsBookedByOthers.obtindreLlistatBookedOthers().execute();
    }
    private class obtindreLlistatBookedOthers extends AsyncTask<String, Void, List<AdDTO>> {

        @Override
        protected void onPreExecute() {
        }
        @Override
        //Instaniem un objecte de la clase tfClientImpl per realitzar la conexio
        protected List<AdDTO> doInBackground(String... strings) {
            //Obtenim el llistat dels anuncis de l'usuari reservats per altres a la base de dades
            List<AdDTO> listAds = tfClientImple.listBookdAdsOther(queryUserId, getActivity());

            return listAds;
        }
        @Override

        protected void onPostExecute(List<AdDTO> result) {
            super.onPostExecute(result);
            //Afegim el resultat a la llista i la pasem al adaptador
            productList.clear();
            productList.addAll(result);
            // refreshing recycler view
            mAdapter.notifyDataSetChanged();
        }
    }



}