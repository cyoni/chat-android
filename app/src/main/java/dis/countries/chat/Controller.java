package dis.countries.chat;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

import dis.countries.chat.ui.home.Home;

public class Controller {
    public static DatabaseReference mDatabase;
    public static FirebaseFunctions mFunctions;


    public static Task<String> logOut(final boolean withExit) {

        Map<String, Object> data = new HashMap<>();
        data.put("nickname", MainActivity.my_nickname);
        data.put("token", MainActivity.myToken);

        MainActivity.my_nickname = null;
        MainActivity.myToken = null;

        return Controller.mFunctions
                .getHttpsCallable("logOut")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        if (withExit)
                            System.exit(0);

                        return null;
                    }
                });
    }


    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null){
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void exit() {
        System.exit(0);
    }

    public static void showKeyboard(EditText inputText, Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null)
            inputMethodManager.showSoftInput(inputText, InputMethodManager.SHOW_IMPLICIT);
    }

    public static void showSnackbar(View layout, String msg) {
        Snackbar snackbar = Snackbar.make(layout , msg, Snackbar.LENGTH_LONG);
        snackbar.show();
    }
}
