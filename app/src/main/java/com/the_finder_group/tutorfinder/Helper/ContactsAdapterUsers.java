package com.the_finder_group.tutorfinder.Helper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.the_finder_group.tutorfinder.ConnManager.TFClientImple;
import com.the_finder_group.tutorfinder.ConnManager.UserDTO;
import com.the_finder_group.tutorfinder.R;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Clase que implementa la inteficie filterable i exten a RecyclerView.Adapter, que ens servira d'adaptador per mostrar
 * els diferents items amb objectes userDTO de les diferents recycleviews utilitzades en l'aplicacio
 */
public class ContactsAdapterUsers extends RecyclerView.Adapter<ContactsAdapterUsers.MyViewHolder> implements Filterable {
    //Declarem les variables i diferents lists que farem servir
    private Context context;
    private ArrayList<UserDTO> contactList;
    private ArrayList<UserDTO> contactListFiltered;
    private TFClientImple tfClientImple;
    private AlertDialog.Builder deleteDialog, confirmDialog;
    private String user_name;
    private UserDTO userToRemove;

    /**
     * viewHolder en que definim els diferent slemenets a mostrar i els inicialitzem
     */
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, user_type;
        public ImageView thumbnail, listItemOptions;;
        //Inicialitzem les variables amb els elements de la layout
        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            user_type = (TextView) view.findViewById(R.id.user_type);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            listItemOptions = (ImageView) view.findViewById(R.id.lisItemOptions);
        }
    }

    /**
     * Constructor de la classe
     * @param context el context de l'aplicacio que farem arribar al constructor
     * @param contactList el llistat amb esl diferents elements a mostrar
     */
    public ContactsAdapterUsers(Context context, ArrayList<UserDTO> contactList) {
        this.context = context;
        this.contactList = contactList;
        this.contactListFiltered = contactList;
    }

    @Override
    /**
     * Metode que fa override de onCreateViewHolder a on inicialitzarem els diferents metodes que farem servir
     * Inflem la layout amb el format dels diferents items
     */
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_row_user, viewGroup, false);
        //Instanciem el server
        tfClientImple = new TFClientImple();

        //Alert dialog per a l'esborrat d'usuari
        deleteDialog = new AlertDialog.Builder(context);
        deleteDialog.setTitle("Atenció!");
        deleteDialog.setIcon(android.R.drawable.ic_input_delete);
        deleteDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                new deleteUser().execute(user_name);
            }
        });
        deleteDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        //Confirmar delete dialog
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
        //Obtenim l'objexte userDTO del llistat previament filtrat en la posicio indicada
        final UserDTO userDTO = contactListFiltered.get(position);
        //Fixem els valors de l'element de la layout
        holder.name.setText(userDTO.getUserName());
        holder.user_type.setText(userDTO.getUserRol());
        holder.thumbnail.setImageResource(R.drawable.ic_person_black_24dp);
        //Definim el popupmenu per a les interaccions de l'usuari amb la icona que representa les opcions disponibles per l'element
        holder.listItemOptions.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // send selected contact in callback
                //creating a popup menu
                PopupMenu popup = new PopupMenu(context, holder.listItemOptions);
                //inflating menu from xml resource
                popup.inflate(R.menu.menu_search_user);
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_veure_user:
                                //handle menu1 click
                                break;
                            case R.id.menu_edit_user:
                                //handle menu2 click
                                break;
                            case R.id.menu_delete_user:
                                //Nomes s'ha implementat l'esborrar d'usuaris
                                user_name = userDTO.getUserName();
                                deleteDialog.setMessage("Estas segur que vols esborrar el usuari seleccionat?");
                                deleteDialog.show();
                                break;
                        }
                        return false;
                    }
                });
                //displaying the popup
                popup.show();
            }
        });
    }

    @Override
    /**
     * Obtenim el llistat filtrat de usuaris
     */
    public int getItemCount() {
        return contactListFiltered.size();
    }

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
                    contactListFiltered = contactList;
                } else {
                    ArrayList<UserDTO> filteredList = new ArrayList<>();
                    for (UserDTO row : contactList) {

                        // name match condition. this might differ depending on your requirement
                        if (row.getUserName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                    contactListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = contactListFiltered;
                return filterResults;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                contactListFiltered = (ArrayList<UserDTO>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    /**
     * Clase per realitzar la conexio amb la base de dades i esborrar un objete de tipus userDTO
     * en funcio de l'usarID d'aquest.
    */
    private class deleteUser extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute(){ }

        @Override
        protected Boolean doInBackground(String... strings) {
            user_name = (strings[0]);
            //Obtindrem true de haver-se pogut realitzar satisfactoriament l'esborrat
            boolean delete = tfClientImple.delUser(user_name, context);
            return delete;
        }

        @Override
        protected void onPostExecute(Boolean result){
            super.onPostExecute(result);
            //Inflem el dialeg de confirmacio amb les dades de l'usuari esborrat de la base de dades del servidor
            confirmDialog.setMessage("Usuari amb id " + user_name + " esborrat");
            confirmDialog.show();
            //Cerquem a la llista el element que hem esborrat
            Iterator<UserDTO> users_iterator = contactList.iterator();
            while (users_iterator.hasNext()){
                UserDTO userDTO = users_iterator.next();
                if((userDTO.getUserName())==user_name){
                    userToRemove = userDTO;
                }
            }
            //Eliminem de la llista el usuari que hem esborrat i notifiquem els canvis al recycleview
            contactList.remove(userToRemove);
            notifyDataSetChanged();


        }
    }


}
