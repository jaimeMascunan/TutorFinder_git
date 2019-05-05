package com.the_finder_group.tutorfinder.ConnManager;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 *  Clase que implemetarà el client per demanar els serveis del
 *  servidor
 *
 * @author José Luis Puentes Jiménez <jlpuentes74@gmail.com>
 */
public class TFClientImple implements TFClient{


    @Override
    public Boolean login(String userName, String password, Context context) {

        return ServiceLocator.login(userName, password,  context);

    }

    @Override
    public UserDTO userData(String userName, Context context) {

        return ServiceLocator.userData(userName, context);

    }

    @Override
    public boolean newUser(String userName, String email, String pswd, String userType, Context context){

        return ServiceLocator.newUser(userName, email, pswd, userType, context);
    }

    @Override
    public boolean editUser( Integer user_id, String userName, String email, String userRole, Context context){

        return ServiceLocator.editUser(user_id, userName, email, userRole, context);
    }

    @Override
    public boolean editUserPswd (String userName, String password, Context context){

        return ServiceLocator.editUserPswd(userName, password, context);
    }

    @Override
    public ArrayList<UserDTO> listUsers(Context context){

        return ServiceLocator.listUsers(context);
    }

    @Override
    public boolean delUser (String userName, Context context){

        return ServiceLocator.deleteUser(userName, context);
    }

    @Override
    public boolean createAd(Integer ad_user_id, String ad_title, String ad_description, Integer ad_type, Integer ad_price, Context context){

        return ServiceLocator.createAd(ad_user_id, ad_title, ad_description, ad_type, ad_price, context);
    }

    @Override
    public List<AdDTO> listProductsRole(Integer user_role_id, Context context){

        return ServiceLocator.listProductsRole(user_role_id, context);
    }

    @Override
    public List<AdDTO> listProductsAdmin(Integer user_role_id, Context context){

        return ServiceLocator.listProductsAdmin(user_role_id, context);
    }

    @Override
    public List<AdDTO> listProductsUser(Integer user_id, Context context){

        return ServiceLocator.listProductsUser(user_id, context);
    }

    @Override
    public boolean delAd (Integer productId, Context context){

        return ServiceLocator.delAd(productId, context);
    }

    @Override
    public boolean editAd (Integer ad_id, String ad_title, String ad_description, Integer ad_type_id, Integer ad_price, Context context){

        return ServiceLocator.editAd(ad_id, ad_title, ad_description, ad_type_id, ad_price, context);
    }

    @Override
    public boolean bookAd (Integer ad_id, Integer ad_user_booking_id, String ad_user_booking_name, Context context){

        return ServiceLocator.adsBookByUser(ad_id, ad_user_booking_id, ad_user_booking_name, context);
    }

    @Override
    public List<AdDTO> listBookAdsUser (Integer user_id, Context context){

        return ServiceLocator.listProductsBookedByUser(user_id, context);
    }

    @Override
    public List<AdDTO> listBookdAdsOther (Integer user_id, Context context){

        return ServiceLocator.listProductsBookedByOther(user_id, context);
    }

    @Override
    public boolean cancelBookAd (Integer ad_id, Context context){

        return ServiceLocator.cancelBookingProductByUser(ad_id, context);
    }

    @Override
    public int getAdTypeByName(String adTypeName, Context context) {

        return ServiceLocator.getAdTypeByName(adTypeName, context);
    }

    @Override
    public boolean createMessage(Integer sender_id, String sender_name, String message, String date, Integer receiver_id, String receiver_name, Context context) {
        return ServiceLocator.createMessage(sender_id, sender_name, message, date, receiver_id, receiver_name, context);
    }

    @Override
    public List<UserMessageDTO> listMessagesByUser(Integer user_id, Integer receiver_id, Context context) {
        return ServiceLocator.listtMessagesByUser(user_id, receiver_id, context);
    }
}



