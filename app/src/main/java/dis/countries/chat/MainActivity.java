package dis.countries.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.Timer;
import java.util.TimerTask;

import dis.countries.chat.ui.chat.Container;
import dis.countries.chat.ui.chat.Conversation;
import dis.countries.chat.ui.home.Entrance;

public class MainActivity extends AppCompatActivity {

    public static String my_nickname, myToken, myRoom = "lobby";
    //public static TabLayout tabs;
    public static int NEW_MESSAGES = 0;
    public static boolean imOnConversationTab = true, imOnPeopleTab = false;

    public final Container chat_container = new Container(this);
    public final Participants participants = new Participants(this);
    public final Conversation conversation = new Conversation();
    public static final Rooms rooms = new Rooms();

    public static BottomNavigationView bottomNavigationView;

    public static void participantsSetBadge() {
        // tabs.getTabAt(0).getOrCreateBadge();

    }

    public static void updateOnlineTitle() {

//        tabs.getTabAt(PEOPLE).setText("ONLINE ("+ Participants.participants.size() +")");
  //      tabs.getTabAt(ConversationTab).setText("Lobby");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setActionbar();

        getIntentData(); // has to be first
        keepMeAlive();
        listenBottomMenu();

        changeFragment(rooms);
    }

    private void listenBottomMenu() {

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
           @Override
           public boolean onNavigationItemSelected(@NonNull MenuItem item) {

               switch (item.getItemId()){
                   case R.id.home:
                       changeFragment(chat_container);
                        break;
                   case R.id.chats:
                       changeFragment(rooms);
                       break;
                   case R.id.favorite:
                       changeFragment(rooms);
                       break;
                   case R.id.rooms:
                       changeFragment(rooms);
                       break;
               }
            return true;
           }
        });
    }

    public void changeFragment(Object fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.contentFragment, (Fragment) fragment);
        transaction.commit();
    }

    private void tabsListener() {
/*
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int index = tab.getPosition();

                if (index == PEOPLE){
                    tabs.getTabAt(0).removeBadge();
                    imOnPeopleTab = true;
                } else
                    imOnPeopleTab = false;
                if (index != ConversationTab){
                    hideKeyboard();
                    imOnConversationTab = false;
                }
                else {
                    imOnConversationTab = true;
                    removeBadge();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
*/

    }

    private void setActionbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar supportActionBar = getSupportActionBar();

        if (supportActionBar != null)
            supportActionBar.setTitle(Html.fromHtml("<font color=\"#ffffff\">Chat</font>"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                Controller.logOut(false);
                finish();
                openLogInScreen();
                break;
            case R.id.exit:
                Controller.logOut(true);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openLogInScreen() {
        startActivity( new Intent(this, Entrance.class));
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null){
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void setBadge(){
        //tabs.getTabAt(1).getOrCreateBadge().setNumber(++NEW_MESSAGES);
    }

    public static void removeBadge(){
        NEW_MESSAGES = 0;
     //   tabs.getTabAt(1).removeBadge();
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

    private void getIntentData() {
        my_nickname = getIntent().getStringExtra("nickname");
        myToken = getIntent().getStringExtra("token");
    }
}