package com.atodogas.brainycar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;


import com.atodogas.brainycar.AsyncTasks.CallbackInterface;
import com.atodogas.brainycar.AsyncTasks.CheckUserAndInsertBD;
import com.atodogas.brainycar.Database.Entities.UserEntity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;


public class AuthenticationActivity extends AppCompatActivity implements View.OnClickListener, CallbackInterface<UserEntity>{
    private static final String TAG = AuthenticationActivity.class.getSimpleName();
    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    public String personName;
    public String personPhotoUrl;
    public String email;
    public ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        findViewById(R.id.sign_in_button).setOnClickListener(this);


        Task<GoogleSignInAccount> silentSignIn = mGoogleSignInClient.silentSignIn();

        if (silentSignIn.isSuccessful()) {
            initializeData(silentSignIn.getResult());
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                signIn();
                break;
            // ...
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Task<GoogleSignInAccount> silentSignIn = mGoogleSignInClient.silentSignIn();

        if (silentSignIn.isSuccessful()) {
            initializeData(silentSignIn.getResult());
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            initializeData(account);

            new CheckUserAndInsertBD(this, getApplicationContext()).execute(account.getId());
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());

        }
    }

    private void initializeData(GoogleSignInAccount account){
        personName = account.getDisplayName();
        if(account.getPhotoUrl() != null){
            personPhotoUrl = account.getPhotoUrl().toString();
        }
        email = account.getEmail();

        new CheckUserAndInsertBD(this, getApplicationContext()).execute(account.getId());
    }


    @Override
    public void doCallback(UserEntity user) {
        if(user.getId() > 0){
            Log.e(TAG, "Name: " + personName + ", email: " + email
                    + ", Image: " + personPhotoUrl);
            // Signed in successfully, show next UI.
            Intent intent = new Intent(this, OnboardingActivity.class);
            intent.putExtra("idUser", user.getId());
            intent.putExtra("personName", personName);
            intent.putExtra("personPhotoUrl", personPhotoUrl);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}