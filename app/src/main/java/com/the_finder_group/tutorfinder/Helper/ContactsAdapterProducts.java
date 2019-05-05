package com.the_finder_group.tutorfinder.Helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.the_finder_group.tutorfinder.ConnManager.AdDTO;
import com.the_finder_group.tutorfinder.ConnManager.TFClientImple;
import com.the_finder_group.tutorfinder.MessageListActivity;
import com.the_finder_group.tutorfinder.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
/**
 * Clase que implementa la inteficie filterable i exten a RecyclerView.Adapter, que ens servira d'adaptador per mostrar
 * els diferents items amb objectes AdDTO de les diferents recycleviews utilitzades en l'aplicacio
 */
public class ContactsAdapterProducts extends RecyclerView.Adapter<ContactsAdapterProducts.MyViewHolder> implements Filterable {
    //Declarem les constants en que definirem els diferents popup menu per als productes
    private static final int POPUP_MENU_OWN_COURSES = 1;
    private static final int POPUP_MENU_OTHER_COURSES = 2;
    private static final int POPUP_MENU_BOOKED_COURSES = 3;
    //Variables i llistats de l'aplicacio
    private Context context;
    private List<AdDTO> productList;
    private List<AdDTO> productListFiltered;
    private SQLiteHandler db;
    private TFClientImple tfClientImple;
    private AlertDialog.Builder deleteDialog, registerDialog, confirmDialog, edit_ad_dialog, cancelRegDialog;
    private AdDTO productToRemove, productToUpdate;
    private EditText ad_titol_edit, ad_preu_edit, ad_descripcio_edit;
    private AppCompatSpinner ad_type_ad_edit;
    private Integer product_id, ad_user_id, ad_user_booking_id, db_popup_option, db_user_id;
    private String ad_user_name, db_user_name, ad_user_booking_name;
    private Helper helper;

    /**
     * viewHolder en que definim els diferent slemenets a mostrar i els inicialitzem
     */
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView ad_title, ad_description, ad_price;
        public ImageView thumbnail, listItemOptions;

        public MyViewHolder(View view) {
            super(view);
            ad_title = (TextView)view.findViewById(R.id.product_title);
            ad_description = (TextView)view.findViewById(R.id.product_description);
            ad_price = (TextView)view.findViewById(R.id.product_price);
            thumbnail = (ImageView)view.findViewById(R.id.thumbnail);
            listItemOptions = (ImageView)view.findViewById(R.id.lisItemOptions);
        }
    }

    /**
     * Constructor de la classe
     * @param context el context de l'aplicacio que farem arribar al constructor
     * @param productList el llistat amb esl diferents elements a mostrar
     */
    public ContactsAdapterProducts(Context context, List<AdDTO> productList) {
        this.context = context;
        this.productList = productList;
        this.productListFiltered = productList;
    }

    @Override
    /**
     * Metode que fa override de onCreateViewHolder a on inicialitzarem els diferents metodes que farem servir
     * Inflem la layout amb el format dels diferents items
     */
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        //Menu amb les opciosn
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_row_product, viewGroup, false);

        //Instanciem el server
        tfClientImple = new TFClientImple();
        //Acces als metodes de suport de l'aplicacio
        helper = new Helper(context);
        // SqLite database handler
        db = new SQLiteHandler(context);
        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();
        //Id de l'usuari que esta realitzant lel registre
        db_user_id = Integer.parseInt(user.get("user_id"));
        db_user_name = user.get("name");

        //Alert dialog per la edicio del producte
        edit_ad_dialog = new AlertDialog.Builder(context);

        //Alert dialog
        deleteDialog = new AlertDialog.Builder(context);
        deleteDialog.setTitle("Atenció!");
        deleteDialog.setIcon(android.R.drawable.ic_input_delete);
        deleteDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                new deleteProduct().execute(String.valueOf(product_id));
            }
        });
        deleteDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        //Alert dialog per a les inscripcions d'usuaris a anuncis
        registerDialog = new AlertDialog.Builder(context);
        registerDialog.setTitle("Inscripcio a la clase");
        registerDialog.setIcon(android.R.drawable.ic_dialog_info);
        registerDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new registerProduct().execute(String.valueOf(product_id), String.valueOf(db_user_id), db_user_name);
            }
        });
        registerDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        //Alert dialog per a les cancelacions d'inscripcions d'uauaris a anuncis
        cancelRegDialog = new AlertDialog.Builder(context);
        cancelRegDialog.setTitle("Cancelar inscripcio a la clase");
        cancelRegDialog.setIcon(android.R.drawable.ic_dialog_info);
        cancelRegDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new cancelRegisterProduct().execute(product_id);
            }
        });
        cancelRegDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        //Alert dialog per a la confirmacio de les accions dutes a terme en esl diferents alert dialogs
        confirmDialog = new AlertDialog.Builder(context);
        confirmDialog.setTitle("Confirmacio");
        confirmDialog.setIcon(android.R.drawable.ic_dialog_info);
        confirmDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return new MyViewHolder(itemView);
    }

    @Override
    /**
     * Metode que fa override de onBindViewHolder en que vincularem les dades del llistat en una posicio determinada
     * a un element de la recycleview concret
     */
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final AdDTO adDTO = productListFiltered.get(position);
        holder.ad_title.setText(adDTO.getAdTittle());
        holder.ad_description.setText(adDTO.getAdDescription());
        holder.ad_price.setText(Integer.toString(adDTO.getAdPrice()));
        holder.thumbnail.setImageResource(R.drawable.ic_school_black_24dp);
        //Definim el popupmenu per a les interaccions de l'usuari amb la icona que representa les opcions disponibles per l'element
        holder.listItemOptions.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // send selected contact in callback
                //creating a popup menu
                PopupMenu popup = new PopupMenu(context, holder.listItemOptions);
                // Fetching user details from sqlite
                db_popup_option = db.getOptionPopUP();
                //inflating menu from xml resource
                //En funcio del valor de la constant inflarem un determinat poputMenu.
                //Popupmenu amb les opcions per als usuaris que estan veient cursos publicats per ells mateixos
                if(db_popup_option == POPUP_MENU_OWN_COURSES){
                    userPopUpMenu(popup, adDTO, position);
                //Popup menu amb les opcions per als usuaris que estan veient cursos publicats per altres usuaris
                }else if (db_popup_option == POPUP_MENU_OTHER_COURSES){
                    rolePopupMenu(popup, adDTO);
                //Popup menu amb les opcions per als usuaris que estan veient cursos reservats per ells o altres usuaris
                }else if (db_popup_option == POPUP_MENU_BOOKED_COURSES){
                    bookedPopupMenu(popup, adDTO);
                }
            }
        });
    }

    /**
     * Override del metode getItemCount en que obtenim el numero d'objectes presents a la llistafiltrada
     * @return
     */
    @Override
    public int getItemCount() { return productListFiltered.size(); }

    @Override
    /**
     * Metode  on definim el filtrat dels elements de la llista i les afegim a la llista filtrada
     */
    public Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    productListFiltered = productList;
                } else {
                    List<AdDTO> filteredList = new ArrayList<>();
                    for (AdDTO row : productList) {

                        // name match condition. this might differ depending on your requirement
                        if (row.getAdTittle().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                    productListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = productListFiltered;
                return filterResults;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                productListFiltered = (ArrayList<AdDTO>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    /**
     * Metode amb el comportament del popup menu per a usuaris veient cursos/anuncis publicats per ells
     * @param popup el popupmenu per aquest item
     * @param adDTO l'objecte sobre el que apliquem aquestes opcions
     * @param position la opsicio de l'objecte adDTO en el llistat de anuncis obtingut
     */
    public void userPopUpMenu(PopupMenu popup, final AdDTO adDTO, final Integer position){
        popup.inflate(R.menu.menu_search_owned_product);
        //adding click listener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_edit_product:
                    //Vista de la edicio del anunci. Primer iniciem les variables i mostrem el alertdialog per a la edicio
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                    View productItemView = inflater.inflate(R.layout.edit_ad_dialog, null);
                    ad_titol_edit = (EditText) productItemView.findViewById(R.id.ad_title_edit);
                    ad_descripcio_edit = (EditText) productItemView.findViewById(R.id.ad_descripcio_edit);
                    ad_preu_edit = (EditText) productItemView.findViewById(R.id.ad_price_edit);
                    ad_type_ad_edit = (AppCompatSpinner)productItemView.findViewById(R.id.ad_type_spinner_edit);

                    //Adaptador de l'spinner amb els diferents tipus de tipus de cursos a publicar
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                            R.array.ad_typer_publish, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    ad_type_ad_edit.setAdapter(adapter);

                    //Declarem els valors que mostrarem als diferents elements de l'alert dialog i que son els que es poden modificar
                    ad_titol_edit.setText(adDTO.getAdTittle());
                    ad_descripcio_edit.setText(adDTO.getAdDescription());
                    ad_preu_edit.setText(String.valueOf(adDTO.getAdPrice()));
                    ad_type_ad_edit.setSelection(adapter.getPosition(adDTO.getTypesName()));

                    if(productItemView.getParent() != null) {
                        ((ViewGroup)productItemView.getParent()).removeView(productItemView);
                    }
                    //COmportament de l'alert dialog per al positive buton en que es prodeceix a la modificacio de l'anunci amb les dades introduides
                    edit_ad_dialog.setView(productItemView);
                    edit_ad_dialog.setPositiveButton("Guardar canvis", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            product_id = adDTO.getAdId();
                            //Validem les dades introduides per l'usuari
                            if(helper.validate_ad(ad_titol_edit, ad_descripcio_edit, ad_preu_edit, ad_type_ad_edit)){
                                String titol = ad_titol_edit.getText().toString().trim();
                                String descripcio = ad_descripcio_edit.getText().toString().trim();
                                String adType = ad_type_ad_edit.getSelectedItem().toString();
                                String preu = ad_preu_edit.getText().toString().trim();
                                //Llançament de l'asynktask per modificar les dades a la base de dades
                                new getAdTypeByName().execute(adType, String.valueOf(product_id), titol, descripcio,
                                        preu, String.valueOf(position));
                            }
                        }
                    });
                    //Tanquem el dialeg en cas de voler cancelar la operacio
                    edit_ad_dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    //Mostrem el alertdialog
                    edit_ad_dialog.show();

                    break;
                case R.id.menu_delete_product:
                    //Opcio del popupmenu per a l'esborrat d'anuncis propietat de l'usuari que esta veient els anuncis
                    product_id = adDTO.getAdId();
                    Log.d("product_id", String.valueOf(product_id));
                    deleteDialog.setMessage("Estas segur que vols esborrar la classe seleccionada?");
                    deleteDialog.show();
                    break;
            }
            return false;
            }
        });
        //displaying the popup
        popup.show();

    }
    /**
     * Metode amb el comportament del popup menu per a usuaris veient cursos/anuncis publicats per altres tipus d'usuari
     * @param popup el popupmenu per aquest item
     * @param adDTO l'objecte sobre el que apliquem aquestes opcions
     */
    public void rolePopupMenu(PopupMenu popup, final AdDTO adDTO){
        //mostrem el menu amb les opcions disponibles
        popup.inflate(R.menu.menu_search_other_product);
        //adding click listener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_inscriure_product:
                        //Alert dialog per a la inscripcio de l'usuari a anuncis d'altres
                        product_id = adDTO.getAdId();
                        Log.d("product_id", String.valueOf(product_id));
                        registerDialog.setMessage("Estas segur que vols inscriuret a la classe seleccionada?");
                        registerDialog.show();
                        break;

                    case R.id.menu_info_owner_product:
                        //Llancem l'activitat messageListActivity amb les dades de l'usuari propietari del anunci.
                        //Aquest fara de receptord del missatge. S'incicia el chat entre els dos usuaris
                        ad_user_id = adDTO.getAdUserId();
                        ad_user_name = adDTO.getUserName();

                        Intent intent = new Intent(context, MessageListActivity.class);
                        intent.putExtra("ad_owner_id", String.valueOf(ad_user_id));
                        intent.putExtra("ad_owner_name", ad_user_name);
                        //Llancem l'activitat amb els extres definits
                        context.startActivity(intent);
                        ((Activity)context).finish();
                        break;
                }
                return false;
            }
        });
        //displaying the popup
        popup.show();

    }
    /**
     * Metode amb el comportament del popup menu per a usuaris veient cursos/anuncis reservats per ell o altres usuaris
     * @param popup el popupmenu per aquest item
     * @param adDTO l'objecte sobre el que apliquem aquestes opcions
     */
    public void bookedPopupMenu(PopupMenu popup, final AdDTO adDTO){
        //mostrem el menu amb les opcions disponibles
        popup.inflate(R.menu.menu_search_booked_product);
        //En funcio de si l'usuari que ja publicat l'anunci coinicideix amb el que esta loggegat a l'aplicacio, amagarem una de les opcions del menu
        if (adDTO.getAdUserId() == db_user_id){
            //Usuari propietari de l'anunci reservat contactant amb un altre
            popup.getMenu().findItem(R.id.menu_contact_owner_product).setVisible(false);
        }else{
            //Usuari contactant amb el propietari de l'alunci reservat
            popup.getMenu().findItem(R.id.menu_contact_user_reserved).setVisible(false);
        }

        //adding click listener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    //Alert dialog amb l'opcio per cancelar inscripcions del producte
                    case R.id.menu_gestionar_inscripcio_product:
                        product_id = adDTO.getAdId();
                        Log.d("product_id", String.valueOf(product_id));
                        cancelRegDialog.setMessage("Estas segur que vols cancelar la seguent reserva");
                        cancelRegDialog.show();
                        break;
                    //Alert dialog per a contactar amb un altre usuari del que hem reservat un cirs
                    case R.id.menu_contact_owner_product:
                        //Obtenim les dades del usuari propietari de l'anunci
                        ad_user_id = adDTO.getAdUserId();
                        Log.d("userId propi", String.valueOf(ad_user_id));
                        ad_user_name = adDTO.getUserName();
                        Log.d("userName propi", String.valueOf(ad_user_name));
                        //Llancem l'activity per enviar els missatges amb les dades obtingudes
                        Intent intent_owner = new Intent(context, MessageListActivity.class);
                        intent_owner.putExtra("ad_owner_id", String.valueOf(ad_user_id));
                        intent_owner.putExtra("ad_owner_name", ad_user_name);

                        context.startActivity(intent_owner);
                        ((Activity)context).finish();
                        break;
                    //Alert dialog per contactar amb l'usuari que ha realitzat la reserva del nostre anunci
                    case R.id.menu_contact_user_reserved:
                        //obtenim les dades de la reserva
                        ad_user_id = adDTO.getAdUserReservaId();
                        Log.d("userId reserva", String.valueOf(ad_user_id));
                        ad_user_name = adDTO.getAdUserReservaName();
                        Log.d("userName reserva", String.valueOf(ad_user_name));
                        //Llancem l'activity per enviar els missatges amb les dades obtingudes
                        Intent intent_reserved = new Intent(context, MessageListActivity.class);
                        intent_reserved.putExtra("ad_owner_id", String.valueOf(ad_user_id));
                        intent_reserved.putExtra("ad_owner_name", ad_user_name);

                        context.startActivity(intent_reserved);
                        ((Activity)context).finish();
                        break;
                }
                return false;
            }
        });
        //displaying the popup
        popup.show();
    }

    /**
     * Clase per realitzar la conexio amb la base de dades i esborrar un objete de tipus adDTO
     * en funcio de l'usarID d'aquest.
     */
    private class deleteProduct extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected Boolean doInBackground(String... strings) {
            product_id = Integer.parseInt(strings[0]);
            //Obtindrem true de haver-se pogut realitzar correctament l'eliminacio del producte del servidor
            boolean delete = tfClientImple.delAd(product_id, context);
            return delete;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            //Informem a l'usuari del producte esborrat i l'eliminem de la recycleview
            confirmDialog.setMessage("Producte amb id " + product_id + " esborrat");
            confirmDialog.show();
            Iterator<AdDTO> product_iterator = productList.iterator();
            while (product_iterator.hasNext()) {
                AdDTO adDTO = product_iterator.next();
                if ((adDTO.getAdId()) == product_id) {
                    productToRemove = adDTO;
                }
            }
            //Eliminem de la llista el usuari que hem esborrat i notifiquem els canvis al recycleview
            productList.remove(productToRemove);
            notifyDataSetChanged();
        }
    }
    /**
     * Clase per realitzar la conexio amb la base de dades i editar un objecte de tipus AdDTO
     * en funcio de l'usarID d'aquest.
     */
    private class editAd extends AsyncTask<String, Void, Boolean> {
        String titol, descripcio, ad_type;
        Integer preu, ad_type_id, position;
        @Override
        protected void onPreExecute() { }

        @Override
        protected Boolean doInBackground(String... strings) {
            //Inicialitem les variables del anunci a editar
            product_id = Integer.parseInt(strings[0]);
            titol = strings[1];
            descripcio = strings[2];
            ad_type_id = Integer.parseInt(strings[3]);
            preu = Integer.parseInt(strings[4]);
            position = Integer.parseInt(strings[5]);
            ad_type = strings[6];
            //Obtindrem true de haver-se pogut realitzar correctament la edicio de l'anunci
            boolean edit = tfClientImple.editAd(product_id, titol, descripcio, ad_type_id, preu, context);
            return edit;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            //Informem a l'usuari de la modificacio i afegim els canvis a la recycleview
            confirmDialog.setMessage("Producte amb id " + product_id + " modificat");
            confirmDialog.show();
            Iterator<AdDTO> product_iterator = productList.iterator();
            while (product_iterator.hasNext()) {
                AdDTO adDTO = product_iterator.next();
                if ((adDTO.getAdId()) == product_id) {
                    adDTO.setAdTittle(titol);
                    adDTO.setAdDescription(descripcio);
                    adDTO.setAdTypeId(ad_type_id);
                    adDTO.setTypesName(ad_type);
                    adDTO.setAdPrice(preu);
                    productToUpdate = adDTO;
                }
            }
            //Eliminem de la llista el usuari que hem esborrat i notifiquem els canvis al recycleview
            productList.set(position, productToUpdate);
            notifyDataSetChanged();


        }
    }
    /**
     * Clase per realitzar la conexio amb la base de dades i modificar un objecte AdDTO per registrar la reserva
     * en funcio de l'usarID d'aquest.
     */
    private class registerProduct extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected Boolean doInBackground(String...strings) {
            //Dades per a modificar el anunci i mostrar el usuari que ha realitzat la reserva del mateix
            product_id = Integer.parseInt(strings[0]);
            ad_user_booking_id = Integer.parseInt(strings[1]);
            ad_user_booking_name = strings[2];
            //Obtindrem true de haver-se pogut realitzar satisfactoriament la modificacio solicitada
            boolean registrat = tfClientImple.bookAd(product_id, ad_user_booking_id, ad_user_booking_name , context);
            return registrat;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            //Informem al usuari de la modificacio i 'eliminem de la vista al no estar disponible ja per a ser reservat
            confirmDialog.setMessage("Usuari amb id " + ad_user_booking_id + " registrat a producte amb id " + product_id);
            confirmDialog.show();
            Iterator<AdDTO> product_iterator = productList.iterator();
            while (product_iterator.hasNext()) {
                AdDTO adDTO = product_iterator.next();
                if ((adDTO.getAdId()) == product_id) {
                    productToRemove = adDTO;
                }
            }
            //Eliminem de la llista el usuari que hem esborrat i notifiquem els canvis al recycleview
            productList.remove(productToRemove);
            notifyDataSetChanged();
        }
    }

    /**
     * Clase per realitzar la conexio amb la base de dades i modificar un objecte AdDTO per cancelar la reserva
     * en funcio de l'usarID d'aquest.
     */
    private class cancelRegisterProduct extends AsyncTask<Integer, Void, Boolean> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected Boolean doInBackground(Integer...integers) {
            product_id = integers[0];
            //Obtindrem true si es pot esborrar l'anunci amb id pasat per parametre de la base da desde
            boolean registrat = tfClientImple.cancelBookAd(product_id, context);
            return registrat;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            //Informem a l'usuari que s'ha esborrat satisfactoriament l'anunci i l'eliminem de la recycleviww per no mostrar-lo i evitar aixi errors
            confirmDialog.setMessage("Usuari amb id " + ad_user_booking_id + " cancela inscripcio a producte amb id " + product_id);
            confirmDialog.show();
            Iterator<AdDTO> product_iterator = productList.iterator();
            while (product_iterator.hasNext()) {
                AdDTO adDTO = product_iterator.next();
                if ((adDTO.getAdId()) == product_id) {
                    productToRemove = adDTO;
                }
            }
            //Eliminem de la llista el usuari que hem esborrat i notifiquem els canvis al recycleview
            productList.remove(productToRemove);
            notifyDataSetChanged();
        }
    }

    /**
     * Clase per realitzar la conexio amb la base de dades i obtenir el id d'un tipus d'anunci en funcio del seu nom
     */
    private class getAdTypeByName extends AsyncTask<String, Void, Integer> {
        String categoria, productId, titol, descripcio, preu, position;
        Integer adTypeId;

        @Override
        protected void onPreExecute(){}

        @Override
        protected Integer doInBackground(String... strings) {
            categoria = strings[0];
            productId = strings[1];
            titol = strings[2];
            descripcio = strings[3];
            preu = strings[4];
            position = strings[5];

            adTypeId = tfClientImple.getAdTypeByName(categoria, context);
            return adTypeId;
        }

        @Override
        protected void onPostExecute(Integer result){
            super.onPostExecute(result);
            //Llancem l'asynctask per realitzar la conexio en segon pla
            new editAd().execute(productId, titol, descripcio, String.valueOf(adTypeId), preu, position, categoria);

        }
    }
}


