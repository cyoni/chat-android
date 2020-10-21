package dis.countries.chat;

import android.animation.ObjectAnimator;
import android.view.View;

public class Animator {

    public static void shake(View view){
        ObjectAnimator
                .ofFloat(view, "translationX", 0, 25, -25, 25, -25,15, -15, 6, -6, 0)
                .setDuration(1000)
                .start();
    }
}
