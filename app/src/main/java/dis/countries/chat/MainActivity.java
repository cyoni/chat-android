package dis.countries.chat;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

import dis.countries.chat.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity {

    public static String my_nickname, myToken, myRoom = "lobby";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setCurrentItem(1);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        getData();
        keepMeAlive();
    }

    private void keepMeAlive() {

        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                Controller.mDatabase.getRef().child("users").child(my_nickname).child("timestamp").setValue(now);
            }
        }, 0, 10000);


    }

    private void getData() {
        my_nickname = getIntent().getStringExtra("nickname");
        myToken = getIntent().getStringExtra("token");
    }
}