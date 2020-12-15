package com.sakusaku.beacon;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class FirebaseUtils {
    private final static String TAG = "Firebase";

    public interface AfterSendSignInLink {
        void done(Task<Void> task);
    }

    public static ActionCodeSettings buildActionCodeSettings() {
        return ActionCodeSettings.newBuilder()
                .setAndroidPackageName("com.sakusaku.beacon", true, null).setHandleCodeInApp(true)
                .setUrl("https://tkg-beacon.firebaseapp.com/verify?uid=1234")
                .build();
    }

    public static void sendSignInLink(String email, ActionCodeSettings actionCodeSettings, AfterSendSignInLink afterSendSignInLink) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.sendSignInLinkToEmail(email, actionCodeSettings)
                .addOnCompleteListener(task -> {
                    afterSendSignInLink.done(task);
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Email sending successful");
                    } else {
                        Log.d(TAG, "Email sending failed");
                    }
                });
    }

    public static void verifySignInLink(Context context, Intent intent) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String emailLink = intent.getData().toString();

        if (auth.isSignInWithEmailLink(emailLink)) {
            String email = PreferenceManager.getDefaultSharedPreferences(context).getString("email", "");
            Log.d("Log", email + " : " + emailLink);

            auth.signInWithEmailLink(email, emailLink)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Successfully signed in with email link!");
                            AuthResult result = task.getResult();
                        } else {
                            Log.e(TAG, "Error signing in with email link", task.getException());
                        }
                    });
        }
    }
}
