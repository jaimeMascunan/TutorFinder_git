package com.the_finder_group.tutorfinder.ConnManager;

import android.content.Context;
import android.util.Log;

import com.the_finder_group.tutorfinder.R;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * @author José Luis Puentes Jiménez <jlpuentes74@gmail.com>
 * Classe amb els metodes que conecten al servidor.
 */
public class ServiceLocator  {
    /**
     * Constants que indiquen al servidor quin es el proces que demana l'usuari/a
     */
    private static final int LOGIN = 0;
    private static final int USER_DATA = 1;
    private static final int REGISTER = 2;
    private static final int EDIT_USER = 3;
    private static final int LIST_USERS = 5;
    private static final int DELETE_USER = 4;
    private static final int PUBLISH_AD = 8;
    private static final int LIST_PRODUCTS_ROLE = 11;
    private static final int LIST_PRODUCTS_USER = 10;
    private static final int DELETE_PRODUCT = 14;
    private static final int EDIT_USER_PASSWORD = 6;
    private static final int EDIT_PRODUCT = 13;
    private static final int BOOKING_PRODUCT = 12;
    private static final int LIST_PRODUCTS_BOOKED_USER = 7;
    private static final int LIST_PRODUCTS_BOOKED_OTHER = 18;
    private static final int CANCEL_BOOKING_PRODUCT = 9;
    private static final int GET_AD_TYPE_BY_NAME = 17;
    private static final int LIST_PRODUCTS_BY_ADMIN = 19;
    private static final int CREATE_MESSAGE = 20;
    private static final int LIST_MESSAGES_BY_USER = 21;

    // Dades de configuració del servidor
    private static final String serverIp = "35.205.239.237";
    private static final int port = 7474;

    /**
     * Realitzem les conexions segure mitjançant sslsockets.
     * @param context tenim que passar un context que ens permet obrir els resources a la carpeta raw
     *                . En aquesta carpeta tenim la keysytore i la trustcertificate store
     * @return un socket segur en format ssl
     */
    public static SSLSocket getSSLSocket(Context context){
        SSLSocket client = null;

        try {

            //Obrim una instancia de la trustedstore en format BKS. Format suportat per android
            KeyStore trustedStore = KeyStore.getInstance("BKS");
            final InputStream intrust = context.getResources().openRawResource( R.raw.clientkeystore);
            trustedStore.load(intrust, "tutorfinder".toCharArray());

            //Instanciem la trustManagerFactory
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustedStore);

            //Obtenim un sslcontext
            SSLContext sc = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = tmf.getTrustManagers();

            sc.init(null, trustManagers, null);

            SSLSocketFactory sslFactory =(SSLSocketFactory)sc.getSocketFactory();

            client = (SSLSocket) sslFactory.createSocket(serverIp, port);

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e1) {
            e1.printStackTrace();
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        return client;
    }

        /**
     * Metode per realitzar el login dels usuaris.
     * @param userName es el camp unique i not null de la base de dades amb el que realitzem la comprobacio
     * @param context pasem un context per a poder obtenir el sslsocket
     * @return el password encriptat emmagatzemat a la base de dades, que es correspon a l'usuari
     */
    public static Boolean login(String userName, String password, Context context) {

        Boolean login;
        SSLSocket client = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;

        try {
            // Instanciem el Socket i els Input i Output
            // per comunicar amb el server
            client = (SSLSocket) getSSLSocket(context);
            dis = new DataInputStream(client.getInputStream());
            dos = new DataOutputStream(client.getOutputStream());

            // Solicitem el login al servidor
            dos.writeInt(LOGIN);
            dos.writeUTF(userName);
            dos.writeUTF(password);
            dos.flush();

            // Llegim la resposta
            login = dis.readBoolean();

        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException(e);

        } finally {

            try {
                // Tanquem connexions
                if (dis != null) { dis.close();}
                if (dos != null) { dos.close();}
                if (client != null) { client.close();}

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return login;
    }

    /**
     * Metode per obtenir els valors definits per a un usuari en concret
     * @param userName el valor de la columna not null i unique que ens permet filtrar l'usuari desitjat
     * @param context  pasem un context per a poder obtenir el sslsocket
     * @return un objecte UserDTO amb les dades solicitades de l'usuari
     */
    public static UserDTO userData(String userName, Context context) {

        UserDTO user = new UserDTO();

        SSLSocket client = null;
        DataInputStream dis = null;
        DataOutputStream dos =null;

        try {
            // Instanciem el Socket i els Input i Output
            // per comunicar amb el server
            client = (SSLSocket) getSSLSocket(context);
            dis = new DataInputStream(client.getInputStream());
            dos = new DataOutputStream(client.getOutputStream());

            // Solicitem les dades de l'usuari al servidor
            dos.writeInt(USER_DATA);
            dos.writeUTF(userName);
            dos.flush();

            // Llegim la resposta i creem la resposta
            user.setUserId(dis.readInt());
            user.setUserName(dis.readUTF());
            user.setUserMail(dis.readUTF());
            user.setUserPswd(dis.readUTF());
            user.setUserRol(dis.readUTF());


        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException(e);

        } finally {

            try {
                // Tanquem connexions
                if (dis != null) { dis.close();}
                if (dos != null) { dos.close();}
                if (client != null) { client.close();}

            } catch (Exception e) {
            }
        }
        return user;
    }

    /**
     * Metode que ens permet crear un objecte usuari i introduir-lo a la base de dades del servidor
     * @param userName el nom de l'usuari/a
     * @param email el correu-e de l'usuair/a
     * @param password el password que ha estat previament encriptat
     * @param userType el tipus d'usuair/a. Pot ser student o tutor
     * @param context pasem un context per a poder obtenir el sslsocket
     * @return un boolean amb valor true de poderse realitzar la insercio de l'usuari/a a la base de dades
     */
    public static boolean newUser(String userName, String email, String password, String userType, Context context ) {

        boolean ret = false;

        SSLSocket client = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;

        try {
            // Instanciem el Socket i els Input i Output
            // per comunicar amb el server
            client = (SSLSocket) getSSLSocket(context);
            dis = new DataInputStream(client.getInputStream());
            dos = new DataOutputStream(client.getOutputStream());

            // Solicitem el registre d'un usuari/a a la base de dades i pasem els valors
            dos.writeInt(REGISTER);
            dos.writeUTF(userName);
            dos.writeUTF(email);
            dos.writeUTF(password);
            dos.writeUTF(userType);
            dos.flush();

            // Llegim la resposta
            ret = dis.readBoolean();

        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException(e);

        } finally {

            try {
                // Tanquem connexions
                if (dis != null) { dis.close();}
                if (dos != null) { dos.close();}
                if (client != null) { client.close();}

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return ret;
    }

    /**
     * Metode per a realitzar el canvi de la contrasenya d'un usuari/a a la base de dades
     * @param userName el nom de l'usuari/a definit a la base de dades com a unique i not null que ens permet
     *                 identificar la fila que volem modificar
     * @param password el password encriptat previament que volem introduir com a nou valor
     * @param context pasem un context per a poder obtenir el sslsocket
     * @return un boolean amb valor true de poder-se realitzar el canvi de contrasenya per a l'usuari/a
     */
    public static boolean editUserPswd (String userName, String password, Context context){
        boolean ret = false;

        SSLSocket client = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;

        try {
            // Instanciem el Socket i els Input i Output
            // per comunicar amb el server
            client = (SSLSocket) getSSLSocket(context);
            dis = new DataInputStream(client.getInputStream());
            dos = new DataOutputStream(client.getOutputStream());

            // Solicitem la edicio del password al servidor
            dos.writeInt(EDIT_USER_PASSWORD);
            dos.writeUTF(userName);
            dos.writeUTF(password);
            dos.flush();

            // Llegim la resposta
            ret = dis.readBoolean();

        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException(e);

        } finally {

            try {
                // Tanquem connexions
                if (dis != null) { dis.close();}
                if (dos != null) { dos.close();}
                if (client != null) { client.close();}

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return ret;
    }

    /**
     * Metode per a realitzar el canvi de les dades d'un usuari/a a la base de dades. EL canvi de contrasenya es realitza en un altre metode
     * @param user_id l'id de l'usuari que ens servira per identificar la fila sobre la que volem realitzar els canvis
     * @param userName l'username de l'usuari/a que introduirem en substitucio del que esta guardat a la base de dades
     * @param email el correu-e de l'usuair/a que introduirem en substitucio del que esta guardat a la base de dades
     * @param userRole el rol d'usuari que introduirem en substitucio del que esta guardat a la base de dades
     * @param context pasem un context per a poder obtenir el sslsocket
     * @return un boolean amb valor true de poder-se realitzar el canvi de valors per als camps que defineixen a l'ojecte usuari/a
     */
    public static boolean editUser(Integer user_id, String userName, String email, String userRole, Context context ) {

        boolean ret = false;

        SSLSocket client = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;

        try {
            // Instanciem el Socket i els Input i Output
            // per comunicar amb el server
            client = (SSLSocket) getSSLSocket(context);
            dis = new DataInputStream(client.getInputStream());
            dos = new DataOutputStream(client.getOutputStream());

            // Solicitem la edicio de l'usuari/a guardat a la base de dades i pasem les dades a modificar.
            dos.writeInt(EDIT_USER);
            dos.writeInt(user_id);
            dos.writeUTF(userName);
            dos.writeUTF(email);
            dos.writeUTF(userRole);
            dos.flush();

            // Llegim la resposta
            ret = dis.readBoolean();

        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException(e);

        } finally {

            try {
                // Tanquem connexions
                if (dis != null) { dis.close();}
                if (dos != null) { dos.close();}
                if (client != null) { client.close();}

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return ret;
    }

    /**
     * Metode per obtenir un llistat de tots els usuaris registrats a la base de dades
     * @param context pasem un context per a poder obtenir el sslsocket
     * @return el llistat amb tots els usuaris/es de l'aplicacio
     */
    public static ArrayList<UserDTO> listUsers(Context context){

        ArrayList<UserDTO> listUsers= new ArrayList<UserDTO>();

        SSLSocket client = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;

        try {
            // Instanciem el Socket i els Input i Output
            // per comunicar amb el server
            client = (SSLSocket) getSSLSocket(context);
            dis = new DataInputStream(client.getInputStream());
            dos = new DataOutputStream(client.getOutputStream());

            // Solicitem el llistat de tots els usuaris
            dos.writeInt(LIST_USERS);
            dos.writeUTF("listUsers");
            dos.flush();

            // Llegim la resposta
            //Revem el nombre d'ususaris que hi haurà de resposta
            int nUsers = dis.readInt();
            Log.d("user", String.valueOf(nUsers));

                for (int i = 0; i < nUsers; i++) {
                    //Rebem los dades del servidor i construï un UserDTO
                    //i el fiquem l'ArrayList
                    UserDTO user = new UserDTO();
                    user.setUserId(dis.readInt());
                    user.setUserName(dis.readUTF());
                    user.setUserMail(dis.readUTF());
                    user.setUserPswd(dis.readUTF());
                    user.setUserRol(dis.readUTF());
                    listUsers.add(user);
                }


        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException(e);

        } finally {

            try {
                // Tanquem connexions
                if (dis != null) { dis.close();}
                if (dos != null) { dos.close();}
                if (client != null) { client.close();}

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return listUsers;
    }

    /**
     * Metode per esborrar un usuari/a present a la base de dades
     * @param userName el nom de l'usuari/a definida a la base de dades com a not null i unique,
     *                 que ens serivar per identificar la fila a esborrar
     * @param context pasem un context per a poder obtenir el sslsocket
     * @return un boolean amb valor true de poder-se realitzar l'esborrament de l'usuari/a
     */
    public static boolean deleteUser(String userName, Context context){

        boolean ret = false;

        SSLSocket client = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;

        try {
            // Instanciem el Socket i els Input i Output
            // per comunicar amb el server
            client = (SSLSocket) getSSLSocket(context);
            dis = new DataInputStream(client.getInputStream());
            dos = new DataOutputStream(client.getOutputStream());

            // Solicitem esborrar un usuari de la base de dades
            dos.writeInt(DELETE_USER);
            //Enviem el num de l'usuari a esborrar
            dos.writeUTF(userName);
            dos.flush();

            // Llegim la resposta
            ret = dis.readBoolean();

        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException(e);

        } finally {

            try {
                // Tanquem connexions
                if (dis != null) { dis.close();}
                if (dos != null) { dos.close();}
                if (client != null) { client.close();}

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return ret;
    }

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
    public static boolean createAd(Integer ad_user_id, String ad_title, String ad_description,
                                   Integer ad_type, Integer ad_price, Context context) {

        boolean ret = false;

        SSLSocket client = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;

        try {
            // Instanciem el Socket i els Input i Output
            // per comunicar amb el server
            client = (SSLSocket) getSSLSocket(context);
            dis = new DataInputStream(client.getInputStream());
            dos = new DataOutputStream(client.getOutputStream());

            // Solicitem la publicacio d'un anunci i hi pasem les dades
            dos.writeInt(PUBLISH_AD);
            dos.writeInt(ad_user_id);
            dos.writeUTF(ad_title);
            dos.writeUTF(ad_description);
            dos.writeInt(ad_type);
            dos.writeInt(ad_price);
            dos.flush();

            // Llegim la resposta
            ret = dis.readBoolean();

        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException(e);

        } finally {

            try {
                // Tanquem connexions
                if (dis != null) { dis.close();}
                if (dos != null) { dos.close();}
                if (client != null) { client.close();}

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return ret;
    }

    /**
     * Metode per obtenir les publicaccions realitzades en funcio del tipus d'usuari
     * @param user_role_id el valor numeric que serveix per a identificar el tipus d'usuari del que volem obtenir les publicacions
     * @param context el context que ens serveix per a obtenir un sslsocket
     * @return el llistat amb els usuaris que compleixen els requisits
     */
    public static List<AdDTO> listProductsRole(Integer user_role_id, Context context){
        //L'arraylist a on guardarem el llistat obtingut
        List<AdDTO> listProducts = new ArrayList<AdDTO>();

        SSLSocket client = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;

        try {
            // Instanciem el Socket i els Input i Output
            // per comunicar amb el server
            client = (SSLSocket) getSSLSocket(context);
            dis = new DataInputStream(client.getInputStream());
            dos = new DataOutputStream(client.getOutputStream());

            // Solicitem el llistat dels productes
            dos.writeInt(LIST_PRODUCTS_ROLE);
            //Enviem el tipus d'usuari que volem llistar
            dos.writeInt(user_role_id);
            dos.flush();

            // Llegim la resposta
            //Rebem el nombre d'ususaris que hi haurà de resposta
            int nProducts = dis.readInt();

            for (int i = 0; i < nProducts; i++) {
                //Rebem los dades del servidor i construï un UserDTO
                //i el fiquem l'ArrayList
                AdDTO ad = new AdDTO();
                ad.setAdId(dis.readInt());
                ad.setAdUserId(dis.readInt());
                ad.setUserName(dis.readUTF());
                ad.setAdTittle(dis.readUTF());
                ad.setAdDescription(dis.readUTF());
                ad.setAdTypeId(dis.readInt());
                ad.setTypesName(dis.readUTF());
                ad.setAdPrice(dis.readInt());

                listProducts.add(ad);
            }
        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException(e);

        } finally {

            try {
                // Tanquem connexions
                if (dis != null) { dis.close();}
                if (dos != null) { dos.close();}
                if (client != null) { client.close();}

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return listProducts;

    }

    /**
     * Metode per a llistar els productes d'un tipus d'usuari per part de l'usuari admin. Es diferencia en que aquest pot veure tots els
     * anuncis, independentment si estan resrvats o no.
     * @param user_role_id el tipus d'usuari del que volem veure els anuncis
     * @param context necessari per a crear el sslSocket
     * @return el llistat dels anuncis que compleixen els requisits
     */
    public static List<AdDTO> listProductsAdmin(Integer user_role_id, Context context){
        //L'arraylist a on guardarem el llistat obtingut
        List<AdDTO> listProducts = new ArrayList<AdDTO>();

        SSLSocket client = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;

        try {
            // Instanciem el Socket i els Input i Output
            // per comunicar amb el server
            client = (SSLSocket) getSSLSocket(context);
            dis = new DataInputStream(client.getInputStream());
            dos = new DataOutputStream(client.getOutputStream());

            // Solicitem el llistat per a l'admin dels anuncis per tipus dusuari
            dos.writeInt(LIST_PRODUCTS_BY_ADMIN);
            //Enviem el tipus d'usuari que volem llistar
            dos.writeInt(user_role_id);
            dos.flush();

            // Llegim la resposta
            //Revem el nombre d'ususaris que hi haurà de resposta
            int nProducts = dis.readInt();

            for (int i = 0; i < nProducts; i++) {
                //Rebem los dades del servidor i construï un UserDTO
                //i el fiquem l'ArrayList
                AdDTO ad = new AdDTO();
                ad.setAdId(dis.readInt());
                ad.setAdUserId(dis.readInt());
                ad.setUserName(dis.readUTF());
                ad.setAdTittle(dis.readUTF());
                ad.setAdDescription(dis.readUTF());
                ad.setAdTypeId(dis.readInt());
                ad.setTypesName(dis.readUTF());
                ad.setAdPrice(dis.readInt());

                listProducts.add(ad);
            }
        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException(e);

        } finally {

            try {
                // Tanquem connexions
                if (dis != null) { dis.close();}
                if (dos != null) { dos.close();}
                if (client != null) { client.close();}

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return listProducts;
    }

    /**
     * Llistem els anuncis que ha realitzat un usuari en concret
     * @param user_id l'identificador de l'usuari del que volem obtindre el llistat d'anuncis
     * @param context necessari per a la creacio del sslSocket
     * @return el llistat dels anuncis publicats per l'usuari/a amb l'id passat per parametre
     */
    public static List<AdDTO> listProductsUser(Integer user_id, Context context){
        //El llistat a on desarem els resultats
        List<AdDTO> listProducts = new ArrayList<AdDTO>();

        SSLSocket client = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;

        try {
            // Instanciem el Socket i els Input i Output
            // per comunicar amb el server
            client = (SSLSocket) getSSLSocket(context);
            dis = new DataInputStream(client.getInputStream());
            dos = new DataOutputStream(client.getOutputStream());

            // Solicitem el llistat dels anuncis publicats per un usuari en concret
            dos.writeInt(LIST_PRODUCTS_USER);
            //Enviem el tipus d'usuari que volem llistar
            dos.writeInt(user_id);
            dos.flush();

            // Llegim la resposta
            //Revem el nombre d'ususaris que hi haurà de resposta
            int nProducts = dis.readInt();


            for (int i = 0; i < nProducts; i++) {
                //Rebem los dades del servidor i construï un UserDTO
                //i el fiquem l'ArrayList
                AdDTO ad = new AdDTO();
                ad.setAdId(dis.readInt());
                ad.setAdUserId(dis.readInt());
                ad.setUserName(dis.readUTF());
                ad.setAdTittle(dis.readUTF());
                ad.setAdDescription(dis.readUTF());
                ad.setAdTypeId(dis.readInt());
                ad.setTypesName(dis.readUTF());
                ad.setAdPrice(dis.readInt());

                listProducts.add(ad);
            }
        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException(e);

        } finally {

            try {
                // Tanquem connexions
                if (dis != null) { dis.close();}
                if (dos != null) { dos.close();}
                if (client != null) { client.close();}

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return listProducts;
    }

    /**
     * Metode per a obtindre el llistat dels anuncis reservats per un usuari/a en concret
     * @param user_id l'identificador de l'usuari/a del que volem obtenir el llistat d'anuncis reservats
     * @param context ens servira per a obtindre el sslsocket per a realitzar la consexio
     * @return el llistat d'objectes AdDTO que compleixen els requisits
     */
    public static List<AdDTO> listProductsBookedByUser(Integer user_id, Context context){
        //Arraylist a on dessarem els resultats
        List<AdDTO> listProducts = new ArrayList<AdDTO>();

        SSLSocket client = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;

        try {
            // Instanciem el Socket i els Input i Output
            // per comunicar amb el server
            client = (SSLSocket) getSSLSocket(context);
            dis = new DataInputStream(client.getInputStream());
            dos = new DataOutputStream(client.getOutputStream());

            // Solicitem el llistat dels anuncis reservats per l'usuari/a
            dos.writeInt(LIST_PRODUCTS_BOOKED_USER);
            //Enviem l'identificador de l'usuari que volem llistar
            dos.writeInt(user_id);
            dos.flush();

            // Llegim la resposta
            //Revem el nombre d'ususaris que hi haurà de resposta
            int nProducts = dis.readInt();

            for (int i = 0; i < nProducts; i++) {
                //Rebem los dades del servidor i construï un UserDTO
                //i el fiquem l'ArrayList
                AdDTO product = new AdDTO();

                product.setAdId(dis.readInt());
                product.setAdUserId(dis.readInt());
                product.setUserName(dis.readUTF());
                product.setAdTittle(dis.readUTF());
                product.setAdDescription(dis.readUTF());
                product.setAdTypeId(dis.readInt());
                product.setTypesName(dis.readUTF());
                product.setAdPrice(dis.readInt());
                product.setAdUserReservaId(dis.readInt());
                product.setAdUserReservaName(dis.readUTF());

                listProducts.add(product);
            }

        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException(e);

        } finally {

            try {
                // Tanquem connexions
                if (dis != null) { dis.close();}
                if (dos != null) { dos.close();}
                if (client != null) { client.close();}

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return listProducts;
    }

    /**
     * Metode per a obtenir el llistat d'anuncis d'un usuari que han estat reservats per altres usuaris
     * @param user_id l'identificador de l'usuari/a del que volem veure els anuncis reservats per altres
     * @param context per aa obtenir el sslsocket
     * @return el llistat amb els anuncis de l'usuari reservat per altres usuaris/es
     */
    public static List<AdDTO> listProductsBookedByOther(Integer user_id, Context context){
        //El llistat a on guardarem la resposta obtinguda
        List<AdDTO> listProducts = new ArrayList<AdDTO>();

        SSLSocket client = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;

        try {
            // Instanciem el Socket i els Input i Output
            // per comunicar amb el server
            client = (SSLSocket) getSSLSocket(context);
            dis = new DataInputStream(client.getInputStream());
            dos = new DataOutputStream(client.getOutputStream());

            // Solicitem el login al servidor
            dos.writeInt(LIST_PRODUCTS_BOOKED_OTHER);
            //Enviem el tipus d'usuari que volem llistar
            dos.writeInt(user_id);
            dos.flush();

            // Llegim la resposta
            //Revem el nombre d'ususaris que hi haurà de resposta
            int nProducts = dis.readInt();

            for (int i = 0; i < nProducts; i++) {
                //Rebem los dades del servidor i construï un UserDTO
                //i el fiquem l'ArrayList
                AdDTO product = new AdDTO();

                product.setAdId(dis.readInt());
                product.setAdUserId(dis.readInt());
                product.setUserName(dis.readUTF());
                product.setAdTittle(dis.readUTF());
                product.setAdDescription(dis.readUTF());
                product.setAdTypeId(dis.readInt());
                product.setTypesName(dis.readUTF());
                product.setAdPrice(dis.readInt());
                product.setAdUserReservaId(dis.readInt());
                product.setAdUserReservaName(dis.readUTF());

                listProducts.add(product);
            }
        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException(e);

        } finally {

            try {
                // Tanquem connexions
                if (dis != null) { dis.close();}
                if (dos != null) { dos.close();}
                if (client != null) { client.close();}

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return listProducts;
    }

    /**
     * Metode per a esborrar un anunci en concret
     * @param productId l'identificador de l'anunci que volem esborrar
     * @param context per a la creacio del sslsocket
     * @return un boolean en valor true d'haver-se pugut realitzar l'esborrat
     */
    public static boolean delAd(Integer productId, Context context){

        boolean ret = false;

        SSLSocket client = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;

        try {
            // Instanciem el Socket i els Input i Output
            // per comunicar amb el server
            client = (SSLSocket) getSSLSocket(context);
            dis = new DataInputStream(client.getInputStream());
            dos = new DataOutputStream(client.getOutputStream());

            // Solicitem l'esborrat d'un anunci
            dos.writeInt(DELETE_PRODUCT);
            //Enviem l'identificador de l'anunci que volem esborrar
            dos.writeInt(productId);
            dos.flush();

            // Llegim la resposta
            ret = dis.readBoolean();

        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException(e);

        } finally {

            try {
                // Tanquem connexions
                if (dis != null) { dis.close();}
                if (dos != null) { dos.close();}
                if (client != null) { client.close();}

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return ret;
    }

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
    public static boolean editAd(Integer ad_id, String ad_title, String ad_description,
                                 Integer ad_type_id, Integer ad_price, Context context) {
        boolean ret = false;

        SSLSocket client = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;

        try {
            // Instanciem el Socket i els Input i Output
            // per comunicar amb el server
            client = (SSLSocket) getSSLSocket(context);
            dis = new DataInputStream(client.getInputStream());
            dos = new DataOutputStream(client.getOutputStream());

            // Solicitem la edicio de l'anunci i enviem les noves dades
            dos.writeInt(EDIT_PRODUCT);
            dos.writeInt(ad_id);
            dos.writeUTF(ad_title);
            dos.writeUTF(ad_description);
            dos.writeInt(ad_type_id);
            dos.writeInt(ad_price);
            dos.flush();

            // Llegim la resposta
            ret = dis.readBoolean();

        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException(e);

        } finally {

            try {
                // Tanquem connexions
                if (dis != null) { dis.close();}
                if (dos != null) { dos.close();}
                if (client != null) { client.close();}

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return ret;
    }

    /**
     * Metode per realitzar la reserva d'un anunci per part d'un usuari
     * @param ad_id l'identificador de l'anunci
     * @param ad_user_booking_id l'idetificador de l'usuari/a que ha realitzar la reserva
     * @param ad_user_booking_name el nom corresponent a l'identificador
     * @param context per a obtenir la consexio encriptada mitjançant sslsocket
     * @return un boolea en valor true de haver-se pogut realitzar la reserva de forma satisfactoria
     */
    public static boolean adsBookByUser(Integer ad_id, Integer ad_user_booking_id, String ad_user_booking_name, Context context) {

        boolean ret = false;

        SSLSocket client = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;

        try {
            // Instanciem el Socket i els Input i Output
            // per comunicar amb el server
            client = (SSLSocket) getSSLSocket(context);
            dis = new DataInputStream(client.getInputStream());
            dos = new DataOutputStream(client.getOutputStream());

            // Solicitem la modificacio de l'anunci per a indicar que esta reservat per l'usuari passat per parametre
            dos.writeInt(BOOKING_PRODUCT);
            dos.writeInt(ad_id);
            dos.writeInt(ad_user_booking_id);
            dos.writeUTF(ad_user_booking_name);

            dos.flush();

            // Llegim la resposta
            ret = dis.readBoolean();

        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException(e);

        } finally {

            try {
                // Tanquem connexions
                if (dis != null) { dis.close();}
                if (dos != null) { dos.close();}
                if (client != null) { client.close();}

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return ret;
    }

    /**
     * Metode per cancelar la reserva d'un anunci
     * @param ad_id identificador de l'anunci que volem modificar per a indicar que ja no esta reservat
     * @param context per a obtenir el sslsocket
     * @return un boolea amb valor true de haver-se pogut realitzar la cancelacio
     */
    public static boolean cancelBookingProductByUser(Integer ad_id, Context context) {

        boolean ret = false;

        SSLSocket client = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;

        try {
            // Instanciem el Socket i els Input i Output
            // per comunicar amb el server
            client = (SSLSocket) getSSLSocket(context);
            dis = new DataInputStream(client.getInputStream());
            dos = new DataOutputStream(client.getOutputStream());

            // Solicitem la cancelacio de la reserva al servidor
            dos.writeInt(CANCEL_BOOKING_PRODUCT);
            dos.writeInt(ad_id);

            dos.flush();

            // Llegim la resposta
            ret = dis.readBoolean();

        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException(e);

        } finally {

            try {
                // Tanquem connexions
                if (dis != null) { dis.close();}
                if (dos != null) { dos.close();}
                if (client != null) { client.close();}

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return ret;
    }

    /**
     * Metode per obtindre el valor de l'id a la columna adTypeId de la base de dades, corresponent al nom passat per parametre
     * @param adTypeName el nom del tipus de anunci del que volem obtenir el id
     * @param context per a instanciar el sslsocket
     * @return el identificador del tipus d'anunci en format numeric
     */
    public static Integer getAdTypeByName(String adTypeName, Context context){

        SSLSocket client = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;

        Integer ret = -1;

        try {
            // Instanciem el Socket i els Input i Output
            // per comunicar amb el server
            client = (SSLSocket) getSSLSocket(context);
            dis = new DataInputStream(client.getInputStream());
            dos = new DataOutputStream(client.getOutputStream());

            // Solicitem el id del tipus pel seu nom i l'enviem al servidor
            dos.writeInt(GET_AD_TYPE_BY_NAME);
            dos.writeUTF(adTypeName);

            //Revem la resposta
            ret = dis.readInt();

        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException(e);

        } finally {

            try {
                // Tanquem connexions
                if (dis != null) { dis.close();}
                if (dos != null) { dos.close();}
                if (client != null) { client.close();}

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        return ret;
    }

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
    public static boolean createMessage(Integer sender_id, String sender_name, String message,
                                        String date, Integer receiver_id, String receiver_name, Context context) {

        boolean ret = false;

        SSLSocket client = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;

        try {

            // Instanciem el Socket i els Input i Output
            // per comunicar amb el server
            client = (SSLSocket) getSSLSocket(context);
            dis = new DataInputStream(client.getInputStream());
            dos = new DataOutputStream(client.getOutputStream());

            // Solicitem la creacio de l'objecte missatge a la base de dades i enviem els parametres
            dos.writeInt(CREATE_MESSAGE);
            dos.writeInt(sender_id);
            dos.writeUTF(sender_name);
            dos.writeUTF(message);
            dos.writeUTF(date);
            dos.writeInt(receiver_id);
            dos.writeUTF(receiver_name);
            dos.flush();

            // Llegim la resposta
            ret = dis.readBoolean();

        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException(e);

        } finally {

            try {
                // Tanquem connexions
                if (dis != null) { dis.close();}
                if (dos != null) { dos.close();}
                if (client != null) { client.close();}

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return ret;
    }

    /**
     * Metode per obtenir un llistat amb els missatges intercanviats en tre dos objectes de tipus usuari
     * @param user_id l'identificador de l'emisor dels missatges
     * @param receiver_id l'identificador del receptor dels missatges
     * @param context per a obtenir el sslsocket
     * @return un llistat de objectes UserMessageDTO amb els missatges intercanviats entre els dos usuaris/es
     */
    public static List<UserMessageDTO> listtMessagesByUser(Integer user_id, Integer receiver_id, Context context){
        //List a on emmagatzemarem els missatges de trobar-se'n
        List<UserMessageDTO> messages = new ArrayList<UserMessageDTO>();

        SSLSocket client = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;

        try {

            // Instanciem el Socket i els Input i Output
            // per comunicar amb el server
            client = (SSLSocket) getSSLSocket(context);
            dis = new DataInputStream(client.getInputStream());
            dos = new DataOutputStream(client.getOutputStream());

            // Solicitem el llistat
            dos.writeInt(LIST_MESSAGES_BY_USER);
            //Enviem els identificadors dels usuaris dels quals volem obtindre els llistats-
            dos.writeInt(user_id);
            dos.writeInt(receiver_id);
            dos.flush();

            // Llegim la resposta
            //Revem el nombre d'ususaris que hi haurà de resposta
            int nMessages = dis.readInt();

            for (int i = 0; i < nMessages; i++) {
                //Rebem los dades del servidor i construï un UserDTO
                //i el fiquem l'ArrayList
                UserMessageDTO message = new UserMessageDTO();
                message.setMessageId(dis.readInt());
                message.setMessageUserId(dis.readInt());
                message.setMessageUserName(dis.readUTF());
                message.setMessageText(dis.readUTF());
                message.setMessageDate(dis.readUTF());
                message.setReceiverUserId(dis.readInt());
                message.setReceiverUserName(dis.readUTF());
                messages.add(message);
            }
        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException(e);

        } finally {

            try {
                // Tanquem connexions
                if (dis != null) { dis.close();}
                if (dos != null) { dos.close();}
                if (client != null) { client.close();}

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        return messages;
    }
}
