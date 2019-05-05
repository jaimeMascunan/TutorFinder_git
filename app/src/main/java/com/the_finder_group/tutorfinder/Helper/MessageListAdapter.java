package com.the_finder_group.tutorfinder.Helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.the_finder_group.tutorfinder.ConnManager.UserMessageDTO;
import com.the_finder_group.tutorfinder.R;

import java.util.HashMap;
import java.util.List;

import static android.support.v7.widget.RecyclerView.Adapter;
import static android.support.v7.widget.RecyclerView.ViewHolder;

/**
 * Classe que fa d'adaptador per a la classe MessageListActivity i defineix la recycleview amb els missatges seleccionats en aquesta
 */
public class MessageListAdapter extends Adapter {
    //Definim les variables i inicialitzem les constants
    private Context mContext;
    private List<UserMessageDTO> mMessageList;
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private SQLiteHandler db;
    private Integer db_user_id;

    /**
     * Constructor de la clase
     * @param context el contexte que passem des de l'activitat MessageListActivity
     * @param messageList la llista que conte els objectes userMessageDTO que compleixen els requisits,
     *                    resultat de consultar la base de dades
     */
    public MessageListAdapter(Context context, List<UserMessageDTO> messageList) {
        mContext = context;
        mMessageList = messageList;
    }

    @NonNull
    @Override
    /**
     * Override del metode onCreateViewHolder on definim dos posibles vireTypes
     * Una per els missatges enviats i una altra per als reburs
     */
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view;
        //En uncio de si el resultat que obtenim al metode getItemView inflarem una layout o un altra
        //per als items de la recycleview
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_message_sent, viewGroup, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_message_received, viewGroup, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }

    @Override
    /**
     * Override del metode onBindViewHolder en quefem el bind en funcio del tipus de viewType
     */
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        final UserMessageDTO message = (UserMessageDTO) mMessageList.get(position);


        switch (viewHolder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                //Carregem el viewholder per als missatges enviats
                ((SentMessageHolder) viewHolder).bind(message);
                break;
                //Carregem el viewholder per als missatges rebuts
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) viewHolder).bind(message);
                break;
        }
    }

    @Override
    /**
     * Override del metode getItemCount en que obtenim la quantitat de missatges que s'han passat a l'adaptador
     */
    public int getItemCount() {
        return mMessageList.size();
    }

    @Override
    /**
     * Override del metode getItemViewType en que obtenim el viewtype per a un objecte concret en la variable position
     */
    public int getItemViewType(int position) {
        // SqLite database handler
        db = new SQLiteHandler(mContext);
        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();
        //Id de l'usuari que esta realitzant lel registre
        db_user_id = Integer.parseInt(user.get("user_id"));
        //Obtenim el objecte userMessageDTO present a la llista pasada a l'adaptador en la posicio indicada
        final UserMessageDTO message = (UserMessageDTO) mMessageList.get(position);
        if (message.getMessageUserId()== db_user_id) {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    /**
     * Extensio de la clase viewHolder per als missatges rebuts.
     * Per aquesta he definit una imatge d'usuari i nom, per seguir el que acostuma a fer-se en aplicacions de
     * missatgeria standars (whats, telegram, etc).
     */
    private class ReceivedMessageHolder extends ViewHolder {
        //Declarem les varialbes
        TextView messageText, timeText, nameText;
        ImageView profileImage;
        //Inicialitzem els diferents elements de la vista
        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.text_message_body);
            timeText = (TextView) itemView.findViewById(R.id.text_message_time);
            nameText = (TextView) itemView.findViewById(R.id.text_message_name);
            profileImage = (ImageView) itemView.findViewById(R.id.image_message_profile);
        }
        //Definim els diferents valors per als elements en funcio del contingut del objecte obtingut
        void bind(UserMessageDTO message) {
            messageText.setText(message.getMessageText());

            // Obtenim el nom de l'usuari i el moment en que es va enviar el missatge
            timeText.setText(message.getMessageDate());
            nameText.setText(message.getMessageUserName());

            //Obtenim la imatge de la base de dades local i la mostrem al camp corresponennt
            Bitmap imageBtmp;
            if ((imageBtmp = (db.getImagePath(message.getMessageUserId())))!=null) {
                profileImage.setImageBitmap(imageBtmp);
            }
        }
    }

    /**
     * Extensio del metode viewHolder per als missatges enviats. Al ser nosaltres els que enviem el missatge,
     * no fa falta definir una imatge de perfil o el nom de l'usuari
     */
    private class SentMessageHolder extends ViewHolder {
        //Definim les variables
        TextView messageText, timeText;
        //Inicialitzem les variables
        SentMessageHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.text_message_body);
            timeText = (TextView) itemView.findViewById(R.id.text_message_time);
        }
        //Vinculem les dades del objecte missatge a les variables inicialitzades
        void bind(UserMessageDTO message) {

            messageText.setText(message.getMessageText());
            // Format the stored timestamp into a readable String using method.
            timeText.setText(message.getMessageDate());
        }
    }

}