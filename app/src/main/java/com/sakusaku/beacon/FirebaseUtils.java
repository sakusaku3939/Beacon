package com.sakusaku.beacon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class FirebaseUtils {
    private final static String TAG = "Firebase";

    public interface AfterSendSignInLink {
        void done(Task<AuthResult> task);
    }

    public static ActionCodeSettings buildActionCodeSettings() {
        return ActionCodeSettings.newBuilder()
                .setAndroidPackageName("com.sakusaku.beacon", true, null).setHandleCodeInApp(true)
                .setUrl("https://tkg-beacon.firebaseapp.com")
                .build();
    }

    public static void sendSignInLink(String email, Activity activity, ActionCodeSettings actionCodeSettings) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.sendSignInLinkToEmail(email, actionCodeSettings)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Email sending successful");
                    } else {
                        Log.d(TAG, "Email sending failed");
                        Toast.makeText(activity, "メールの送信に失敗しました", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public static void verifySignInLink(Context context, Intent intent, AfterSendSignInLink afterSendSignInLink) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String emailLink = intent.getData().toString();

        if (auth.isSignInWithEmailLink(emailLink)) {
            String email = PreferenceManager.getDefaultSharedPreferences(context).getString("email", "");

            auth.signInWithEmailLink(email, emailLink)
                    .addOnCompleteListener(task -> {
                        afterSendSignInLink.done(task);
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Successfully signed in with email link!");
                        } else {
                            Log.e(TAG, "Error signing in with email link", task.getException());
                        }
                    });
        }
    }
}
