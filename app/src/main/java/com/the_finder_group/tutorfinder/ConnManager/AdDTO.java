package com.the_finder_group.tutorfinder.ConnManager;

/**
 * Definicio dels atributs l'objecte anunci
 */
public class AdDTO {

    //Atributs
    private int adId;
    private int adUserId;
    private String userName;
    private String adTittle;
    private String adDescription;
    private int adTypeId;
    private String typesName;
    private int adPrice;
    private boolean reservat;
    private int adUserReservaId;
    private String adUserReservaName;


    //Getters
    public int getAdId() {
        return adId;
    }

    public int getAdUserId() {
        return adUserId;
    }

    public String getUserName() {
        return userName;
    }

    public String getAdTittle() {
        return adTittle;
    }

    public String getAdDescription() {
        return adDescription;
    }

    public int getAdTypeId() {
        return adTypeId;
    }

    public String getTypesName() {
        return typesName;
    }

    public int getAdPrice() {
        return adPrice;
    }

    public boolean reservat(){
        return reservat;
    }

    public int getAdUserReservaId() { return adUserReservaId; }

    public String getAdUserReservaName() { return adUserReservaName; }

    //Setters
    public void setAdId(int adId) {
        this.adId = adId;
    }

    public void setAdUserId(int adUserId) {
        this.adUserId = adUserId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setAdTittle(String adTittle) {
        this.adTittle = adTittle;
    }

    public void setAdDescription(String adDescription) {
        this.adDescription = adDescription;
    }

    public void setAdTypeId(int adTypeId) {
        this.adTypeId = adTypeId;
    }

    public void setTypesName(String typesName) {
        this.typesName = typesName;
    }

    public void setAdPrice(int adPrice) {
        this.adPrice = adPrice;
    }

    public void setReservat (boolean reservat){
        this.reservat = reservat;
    }

    public void setAdUserReservaId(int adUserReservaId) { this.adUserReservaId = adUserReservaId; }

    public void setAdUserReservaName(String adUserReservaName) { this.adUserReservaName = adUserReservaName; }
}


