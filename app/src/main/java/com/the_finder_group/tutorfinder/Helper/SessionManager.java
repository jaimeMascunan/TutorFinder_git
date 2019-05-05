package com.the_finder_group.tutorfinder.Helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 *Classe que ens permet registrar si un usuari/a esta logegat a l'aplicacio guardant les dades
 * a shared preferences
 */
public class SessionManager {
    // LogCat tag
    private static String TAG = SessionManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences pref;

    Editor editor;
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // El nom del fitxer amb les shared preferences
    private static final String PREF_NAME = "TutorFinder";

    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";

    /**
     * inicialitzem les shared preferencies en private mode, indicant en nom del fitxer on es dessaran
     * @param context el context de l'aplicacio
     */

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Afegim a l'objecte editor un bolea indicant true or false per els estats logejat o no logejat
     * @param isLoggedIn el boolea que ens indica l'estat
     */
    public void setLogin(boolean isLoggedIn) {

        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);

        // commit changes
        editor.commit();

        Log.d(TAG, "User login session modified!");
    }

    /**
     * Ens retorna el valor del bolea que indica l'estat
     * @return true or false depenent de si tenim o no un usuari/a logejat. Per defete false
     */
    public boolean isLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }
}