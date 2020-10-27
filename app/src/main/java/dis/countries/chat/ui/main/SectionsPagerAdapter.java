package dis.countries.chat.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import dis.countries.chat.MainActivity;
import dis.countries.chat.Participants;
import dis.countries.chat.R;
import dis.countries.chat.Rooms;
import dis.countries.chat.ui.chat.Conversation;
import dis.countries.chat.ui.home.Home;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_0, R.string.tab_text_1};
    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new Participants();
            case 1:
                return new Participants();
            case 2:
                return new Rooms(MainActivity.myToken, MainActivity.my_nickname);
            case 3:
                return new Rooms(MainActivity.myToken, MainActivity.my_nickname);

            default:
                return null;
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return 2;
    }
}