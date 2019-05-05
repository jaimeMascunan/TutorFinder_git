package com.the_finder_group.tutorfinder.ConnManager;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * @author José Luis Puentes Jiménez <jlpuentes74@gmail.com>
 */
public interface TFClient {
    /**
     * Demana per fer login al servidor
     * @param userName Nom d'usuari al servidor
     * @return True si l'usuari és al server
     */
    public Boolean login(String userName, String password, Context context);

    /**
     * Demana les dades de l'usuari
     * @param userName Nom de l'usuari
     * @return Retorna el objecte amb les dades d'usuari
     */
    public UserDTO userData(String userName, Context context);

    /**
     * Introdueix les dades de registre a la base de dades
     * @param userName nom de l'usuari
     * @param email    email de l'usuari
     * @param pswd     password de l'usuari
     * @param userType tipus d'usuari
     * @return True si em pogut emmagatzemar l'usuari a la base de dades
     */
    public boolean newUser(String userName, String email, String pswd, String userType, Context context);

    /**
     * Metode per a realitzar el canvi de les dades d'un usuari/a a la base de dades. EL canvi de contrasenya es realitza en un altre metode
     * @param user_id l'id de l'usuari que ens servira per identificar la fila sobre la que volem realitzar els canvis
     * @param userName l'username de l'usuari/a que introduirem en substitucio del que esta guardat a la base de dades
     * @param email el correu-e de l'usuair/a que introduirem en substitucio del que esta guardat a la base de dades
     * @param userRole el rol d'usuari que introduirem en substitucio del que esta guardat a la base de dades
     * @param context pasem un context per a poder obtenir el sslsocket
     * @return un boolean amb valor true de poder-se realitzar el canvi de valors per als camps que defineixen a l'ojecte usuari/a
     */
    public boolean editUser(Integer user_id, String userName, String email, String userRole, Context context);

    /**
     * Metode per a realitzar el canvi de la contrasenya d'un usuari/a a la base de dades
     * @param userName el nom de l'usuari/a definit a la base de dades com a unique i not null que ens permet
     *                 identificar la fila que volem modificar
     * @param password el password encriptat previament que volem introduir com a nou valor
     * @param context pasem un context per a poder obtenir el sslsocket
     * @return un boolean amb valor true de poder-se realitzar el canvi de contrasenya per a l'usuari/a
     */
    public boolean editUserPswd (String userName, String password, Context context);

    /**
     * Metode per obtenir un llistat de tots els usuaris registrats a la base de dades
     * @param context pasem un context per a poder obtenir el sslsocket
     * @return el llistat amb tots els usuaris/es de l'aplicacio
     */
    public ArrayList<UserDTO> listUsers(Context context);

    /**
     * Metode per esborrar un usuari/a present a la base de dades
     * @param userName el nom de l'usuari/a definida a la base de dades com a not null i unique,
     *                 que ens serivar per identificar la fila a esborrar
     * @param context pasem un context per a poder obtenir el sslsocket
     * @return un boolean amb valor true de poder-se realitzar l'esborrament de l'usuari/a
     */
    public boolean delUser(String userName, Context context);

    /**
     * Metode per introduir a la base de dades l'anunci lligat a un usuari
     * @param ad_user_id l'identificador de l'usuari que crea l'anunci
     * @param ad_title el text identificador de l'anunci
     * @param ad_description la descripcio de l'anunci
     * @param ad_type el valor numeric que serveix d'identificador a la base de  dades del tipus d'anunci
     * @param ad_price el preu que volem cobrar/pagar per l'anunci
     * @param context el context que ens serveix per obtenir un sslsocket
     * @return un boolean amb valor true de podre-se realitzar la creacio de l'anunci
     */
    public boolean createAd(Integer ad_user_id, String ad_title, String ad_description, Integer ad_type, Integer ad_price, Context context);

    /**
     * Metode per obtenir les publicaccions realitzades en funcio del tipus d'usuari
     * @param user_role_id el valor numeric que serveix per a identificar el tipus d'usuari del que volem obtenir les publicacions
     * @param context el context que ens serveix per a obtenir un sslsocket
     * @return el llistat amb els usuaris que compleixen els requisits
     */
    public List<AdDTO> listProductsRole(Integer user_role_id, Context context);

    /**
     * Metode per a llistar els productes d'un tipus d'usuari per part de l'usuari admin. Es diferencia en que aquest pot veure tots els
     * anuncis, independentment si estan resrvats o no.
     * @param user_role_id el tipus d'usuari del que volem veure els anuncis
     * @param context necessari per a crear el sslSocket
     * @return el llistat dels anuncis que compleixen els requisits
     */
    public List<AdDTO> listProductsAdmin(Integer user_role_id, Context context);

    /**
     * Llistem els anuncis que ha realitzat un usuari en concret
     * @param user_id l'identificador de l'usuari del que volem obtindre el llistat d'anuncis
     * @param context necessari per a la creacio del sslSocket
     * @return el llistat dels anuncis publicats per l'usuari/a amb l'id passat per parametre
     */
    public List<AdDTO> listProductsUser(Integer user_id, Context context);

    /**
     * Metode per a esborrar un anunci en concret
     * @param productId l'identificador de l'anunci que volem esborrar
     * @param context per a la creacio del sslsocket
     * @return un boolean en valor true d'haver-se pugut realitzar l'esborrat
     */
    public boolean delAd(Integer productId, Context context);

    /**
     * Metode per realitzar l'edicio de les variables d'un objecte anunci en concret
     * @param ad_id l'identificador unic del objecte anunci que volem esborrar
     * @param ad_title el nou titol per a l'anunc i
     * @param ad_description la nova descripcio per a l'anunci
     * @param ad_type_id el nou identificador del tipus d'anunci
     * @param ad_price el nou preu
     * @param context per a realitzar la conexio encriptada mitnançant sslsocket
     * @return un boolea amb valor true de haver-se pogut realitzar la edicio dels valors
     */
    public boolean editAd (Integer ad_id, String ad_title, String ad_description, Integer ad_type_id, Integer ad_price, Context context);

    /**
     * Metode per realitzar la reserva d'un anunci per part d'un usuari
     * @param ad_id l'identificador de l'anunci
     * @param ad_user_booking_id l'idetificador de l'usuari/a que ha realitzar la reserva
     * @param ad_user_booking_name el nom corresponent a l'identificador
     * @param context per a obtenir la consexio encriptada mitjançant sslsocket
     * @return un boolea en valor true de haver-se pogut realitzar la reserva de forma satisfactoria
     */
    public boolean bookAd (Integer ad_id, Integer ad_user_booking_id, String ad_user_booking_name, Context context);

    /**
     * Metode per a obtindre el llistat dels anuncis reservats per un usuari/a en concret
     * @param user_id l'identificador de l'usuari/a del que volem obtenir el llistat d'anuncis reservats
     * @param context ens servira per a obtindre el sslsocket per a realitzar la consexio
     * @return el llistat d'objectes AdDTO que compleixen els requisits
     */
    public List<AdDTO> listBookAdsUser (Integer user_id, Context context);

    /**
     * Metode per a obtenir el llistat d'anuncis d'un usuari que han estat reservats per altres usuaris
     * @param user_id l'identificador de l'usuari/a del que volem veure els anuncis reservats per altres
     * @param context per aa obtenir el sslsocket
     * @return el llistat amb els anuncis de l'usuari reservat per altres usuaris/es
     */
    public List<AdDTO> listBookdAdsOther (Integer user_id, Context context);

    /**
     * Metode per cancelar la reserva d'un anunci
     * @param ad_id identificador de l'anunci que volem modificar per a indicar que ja no esta reservat
     * @param context per a obtenir el sslsocket
     * @return un boolea amb valor true de haver-se pogut realitzar la cancelacio
     */
    public boolean cancelBookAd (Integer ad_id, Context context);

    /**
     * Metode per obtindre el valor de l'id a la columna adTypeId de la base de dades, corresponent al nom passat per parametre
     * @param adTypeName el nom del tipus de anunci del que volem obtenir el id
     * @param context per a instanciar el sslsocket
     * @return el identificador del tipus d'anunci en format numeric
     */
    public int getAdTypeByName(String adTypeName, Context context);

    /**
     * Metode per a crear un objecte missatge a la base de dades, on reflectirem l'emisor i destinatari del mateix
     * @param sender_id identificador de l'objecte user que fa d'emisor
     * @param sender_name nom associat a l'objecte user que fa d'emisor
     * @param message text que es correspon amb el cos del missatge a enviar
     * @param date moment temporal en que es realitza l'enviament
     * @param receiver_id identificador de l'objecte user que fa de receptor
     * @param receiver_name nom associat a l'objecte user que fa de receptor
     * @param context per a obtenir el sslsocket
     * @return un boolea amb valor true d'haver-se pogut realitzar satisfactoriament l'enviament a la base de dades de l'objecte missatge
     */
    public boolean createMessage (Integer sender_id, String sender_name, String message, String date, Integer receiver_id, String receiver_name, Context context);


    /**
     * Metode per obtenir un llistat amb els missatges intercanviats en tre dos objectes de tipus usuari
     * @param user_id l'identificador de l'emisor dels missatges
     * @param receiver_id l'identificador del receptor dels missatges
     * @param context per a obtenir el sslsocket
     * @return un llistat de objectes UserMessageDTO amb els missatges intercanviats entre els dos usuaris/es
     */
    public List<UserMessageDTO> listMessagesByUser (Integer user_id, Integer receiver_id, Context context);

}