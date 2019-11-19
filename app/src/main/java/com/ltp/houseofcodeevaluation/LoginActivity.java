package com.ltp.houseofcodeevaluation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupAuth();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 123) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if(resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null) {
                    startActivity(new Intent(LoginActivity.this, ChatRoomsActivity.class));
                }
                finish();
            } else {
                Error();
            }
        }
    }

    /**
     * Set up authentication methods, in this case google and facebook
     */
    private void setupAuth() {
        AuthUI.IdpConfig p[] = new AuthUI.IdpConfig[] {
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build()
        };

        List<AuthUI.IdpConfig> providers = Arrays.asList(p);
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(), 123);
    }

    /**
     * Create and show error in case of failure
     */
    private void Error() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("An error occurred while logging in, please try again later");
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
