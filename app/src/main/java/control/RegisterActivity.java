
package control;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.user.android2.AvailableTripsFragment;
import com.example.user.android2.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import entities.Driver;
import model.backend.Backend;
import model.backend.BackendFactory;
import model.datasource.Action;
import model.datasource.FireBaseDataBase;

public class RegisterActivity extends AppCompatActivity  {

    private EditText inputEmail, inputPassword;
    private Button register;
    private FirebaseAuth auth;

    private Backend backend;
    private Driver driver;

    private EditText FirstName,LastName,PhoneNumber, ID, CrerditCardNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        backend=BackendFactory.getInstance();
        loadTexts();



        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        register = (Button) findViewById(R.id.registerButton);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }

                driver = getDriver();

                if (saveSharedPrefences()) {
                    //create user
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    Toast.makeText(RegisterActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();

                                    // If sign in fails, display a message to the user. If sign in succeeds
                                    // the auth state listener will be notified and logic to handle the
                                    // signed in user can be handled in the listener.
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, "Authentication failed." + task.getException(),
                                                Toast.LENGTH_SHORT).show();
                                    } else {

                                        backend.addDriver(driver, new Action<String>() {
                                            @Override
                                            public void onSuccess(String obj) {
                                                Toast.makeText(getBaseContext(), "Registered successfully", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }

                                            @Override
                                            public void onFailure(Exception exception) {
                                                Toast.makeText(getBaseContext(), "Invalid information\n" + exception.getMessage(), Toast.LENGTH_SHORT).show();
                                            }

                                        });


                                        FireBaseDataBase.NotifyToDriversList(new FireBaseDataBase.NotifyDataChange<List<Driver>>() {
                                            @Override
                                            public void OnDataChanged(List<Driver> obj) {
                                                FireBaseDataBase.driverList = obj;
                                            }

                                            @Override
                                            public void onFailure(Exception exception) {
                                                Toast.makeText(getBaseContext(), "error to get Drivers list\n" + exception.toString(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                                        startService(new Intent(getBaseContext(), AvailableTripsFragment.class));
                                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                        finish();


                                    }
                                }
                            });
                }

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private void loadTexts(){
        FirstName = (EditText) findViewById(R.id.firstName);
        LastName = (EditText) findViewById(R.id.lastName);
        ID = (EditText) findViewById(R.id.id);
        PhoneNumber = (EditText) findViewById(R.id.phoneNumber);
        CrerditCardNumber = (EditText) findViewById(R.id.creditCard);
    }

    private Driver getDriver(){
        Driver driver=new Driver();
        driver.setFirstName(FirstName.getText().toString());
        driver.setLastName(LastName.getText().toString());
        //driver.setID(Long.valueOf(ID.getText().toString()));
        driver.setID(ID.getText().toString());
        driver.setEmail(inputEmail.getText().toString());
        driver.setPhneNumber(PhoneNumber.getText().toString());
        driver.setPassword(inputPassword.getText().toString());
        driver.setCreditCardNumber(CrerditCardNumber.getText().toString());
        return driver;

    }

    private boolean saveSharedPrefences() {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("userName", inputEmail.getText().toString());
            editor.putString("userPassword", inputPassword.getText().toString());
            editor.putBoolean("SavePassword", false);
            editor.commit();
            return true;
        } catch (Exception ex) {
            Toast.makeText(this, "faild to save Preference", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}

