package com.the_finder_group.tutorfinder.Helper;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe que ens serveid d'adaptador per als fragments de les activitats TabActivityAdmin i TacActivityUsers
 * En proporcionara els metodes per poder agegir els fragments al tablayaout
 */
public class ViewPageAdapter extends FragmentPagerAdapter {
    //Defunin les llistes amb els fragments i els titols corresponents
    private final List<Fragment> listFragment = new ArrayList<>();
    private final List<String> titles = new ArrayList<>();
    //Construtcor de la classe
    public ViewPageAdapter (FragmentManager fm){
        super(fm);
    }

    @Override
    //Accedim a un fragment concret situat a una posicio
    public Fragment getItem(int position) {
        return listFragment.get(position);
    }

    @Override
    //Obtenim la quantitat de fragments presents a la llista
    public int getCount() {
        return titles.size();
    }

    @Override
    //Obtenim el titol d'un fragment en una posicio concreta
    public CharSequence getPageTitle(int position){
        return titles.get(position);
    }
    //Afegim un fragment al viewPageAdapter amb el seu titol corresponent
    public void addFragment (Fragment fragment, String title){
        listFragment.add(fragment);
        titles.add(title);
    }
}
