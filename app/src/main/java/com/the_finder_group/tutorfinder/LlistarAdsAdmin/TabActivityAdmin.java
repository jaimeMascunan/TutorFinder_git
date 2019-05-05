package com.the_finder_group.tutorfinder.LlistarAdsAdmin;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.the_finder_group.tutorfinder.Helper.Helper;
import com.the_finder_group.tutorfinder.Helper.SQLiteHandler;
import com.the_finder_group.tutorfinder.Helper.ViewPageAdapter;
import com.the_finder_group.tutorfinder.R;

import java.util.HashMap;
/**
 * Activitat on mostrarem els difersos fragments en una tablayout.
 * Cadascun dels fragments tindra una recycleview per als llistats de anuncis indicats
 */
public class TabActivityAdmin extends AppCompatActivity {
    //Iniciem les variables
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPageAdapter viewPageAdapter;
    private Helper helper;
    private SQLiteHandler db;
    private String db_loggedUserType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);
        //Indiqurm la toolbar que volem per a l'activitat
        Toolbar toolbar = findViewById(R.id.toolbar_llistar_producte);
        setSupportActionBar(toolbar);
        // toolbar fancy stuff
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Accedim als metodes de suport de l'aplicacio a la clase helper
        helper = new Helper(getApplicationContext());
        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());
        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();
        //Id de l'usuari que esta realitzant la publicacio
        db_loggedUserType = user.get(getResources().getString(R.string.user_type));
        //Inicialitzem els elements presents a la layout activity_tab
        tabLayout = (TabLayout)findViewById(R.id.tab_layout_id);
        viewPager = (ViewPager)findViewById(R.id.view_pager);
        //Cridem al constructor del pageAdapter
        viewPageAdapter =  new ViewPageAdapter(getSupportFragmentManager());
        //Afegim els dos fragments que volem mostrar i indiquem els titols
        viewPageAdapter.addFragment(new FragmentStudentsProducts(), "Cursos d'estudiants");
        viewPageAdapter.addFragment(new FragmentTutorsProducts(), "Cursos de tutors");
        //Indiquem el adaptador per al viewPager de la vista
        viewPager.setAdapter(viewPageAdapter);
        //Indiquem el viewPager per a la tabLayout
        tabLayout.setupWithViewPager(viewPager);
        //Escollim les icones per als fragments de la tablayout
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_school_black_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_school_black_24dp);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == android.R.id.home) { helper.redirectUserTypeAct(db_loggedUserType); }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //desabilitamos volver atras de la MainActivity
        moveTaskToBack(true);
    }
}
