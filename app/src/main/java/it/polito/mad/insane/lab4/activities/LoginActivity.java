package it.polito.mad.insane.lab4.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.polito.mad.insane.lab4.data.Restaurant;
import it.polito.mad.insane.lab4.managers.Cryptography;
import it.polito.mad.insane.lab4.R;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

//    /**
//     * Id to identity READ_CONTACTS permission request.
//     */
//    private static final int REQUEST_READ_CONTACTS = 0;


    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mUserView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private String typeConsumer = "User";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        // Set up the login form.
        mUserView = (EditText) findViewById(R.id.user);
//        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);

        //Radio batton gruop
        RadioButton typeUserRadioButton = (RadioButton) findViewById(R.id.user_login_radiobutton);
        if(typeUserRadioButton != null) {
            typeUserRadioButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    typeConsumer = "User";
                }
            });
        }

        RadioButton typeRestaurateurRadioButton = (RadioButton) findViewById(R.id.restaurateur_login_radiobutton);
        if(typeRestaurateurRadioButton != null) {
            typeRestaurateurRadioButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    typeConsumer = "Rest";
                }
            });
        }

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);

        if(mEmailSignInButton != null) {
            mEmailSignInButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO aggiungere controllo id e password
                    try {
                        attemptLogin();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
//                    if (typeConsumer.equals("User")) {
//                        //TODO far partire l'activity utente, bisogna pensare come differenziare la cosa dalla app senza log quindi per ora lascio stare (Federico)
//                        Toast.makeText(v.getContext(), "Sono un utente", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(v.getContext(), "Sono un ristoratore", Toast.LENGTH_SHORT).show();
//                        Intent intent = new Intent(LoginActivity.this, HomeRestaurateur.class);
//                        startActivity(intent);
//                    }
                }
            });
        }

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

//    private void populateAutoComplete() {
////        if (!mayRequestContacts()) {
////            return;
////        }
//
//        getLoaderManager().initLoader(0, null, this);
//    }

//    private boolean mayRequestContacts() {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//            return true;
//        }
//        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
//            return true;
//        }
//        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
//            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
//                    .setAction(android.R.string.ok, new View.OnClickListener() {
//                        @Override
//                        @TargetApi(Build.VERSION_CODES.M)
//                        public void onClick(View v) {
//                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
//                        }
//                    });
//        } else {
//            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
//        }
//        return false;
//    }

//    /**
//     * Callback received when a permissions request has been completed.
//     */
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        if (requestCode == REQUEST_READ_CONTACTS) {
//            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                populateAutoComplete();
//            }
//        }
//    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() throws NoSuchAlgorithmException, UnsupportedEncodingException{
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUserView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String user =  mUserView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            //mPasswordView.setError(getString(R.string.error_invalid_password));
            mPasswordView.setError("Password non valida");
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid user address.
        if (TextUtils.isEmpty(user)) {
            //mUserView.setError(getString(R.string.error_field_required));
            mUserView.setError("E' richiesto un nome utente");
            focusView =  mUserView;
            cancel = true;
        } else if (!isUserValid(user)) {
            //mUserView.setError(getString(R.string.error_invalid_email));
            mUserView.setError("Nome utente non valido");
            focusView =  mUserView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(user, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isUserValid(String email) {
        //TODO: Replace this with your own logic
        return true;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return true;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUser;
        private final String mPassword;

        UserLoginTask(String user, String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
            Cryptography cryptography = new Cryptography();
            mUser = user;
            //mPassword = cryptography.SHA1(password);
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            String[] CREDENTIALS = new String[2];
            CREDENTIALS[0] = "federico";
            CREDENTIALS[1] = "vibrati";
//            FirebaseDatabase database = FirebaseDatabase.getInstance();
//            DatabaseReference myRef = database.getReference("/restaurants/");
//
//            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    HashMap<String,Restaurant> r = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, Restaurant>>() {
//                        @Override
//                        protected Object clone() throws CloneNotSupportedException {
//                            return super.clone();
//                        }
//                    });
//                    setUpRestaurantsRecycler(new ArrayList<>(r.values()));
//                }
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//
//            FirebaseDatabase database = FirebaseDatabase.getInstance();
//            DatabaseReference myRef = database.getReference("/restaurants");
//
//
//
//            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    HashMap<String,Restaurant> r = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, Restaurant>>() {
//                        @Override
//                        protected Object clone() throws CloneNotSupportedException {
//                            return super.clone();
//                        }
//                    });
//                    setUpRestaurantsRecycler(new ArrayList<>(r.values()));
//                }
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

                String[] pieces = CREDENTIALS;
                if (pieces[0].equals(mUser)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                if (typeConsumer.equals("User")) {
                    //TODO far partire l'activity utente, bisogna pensare come differenziare la cosa dalla app senza log quindi per ora lascio stare (Federico)
                    //Toast.makeText(v.getContext(), "Sono un utente", Toast.LENGTH_SHORT).show();
                } else {
                    //Toast.makeText(v.getContext(), "Sono un ristoratore", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, HomeRestaurateur.class);
                    startActivity(intent);
                }
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

