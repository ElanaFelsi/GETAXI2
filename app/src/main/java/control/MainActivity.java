package control;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.user.android2.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import entities.Trip;
import model.backend.Backend;
import model.backend.BackendFactory;
import model.datasource.FireBaseDataBase;

public class MainActivity extends AppCompatActivity {

    private EditText Email;
    private EditText Password;
    private FirebaseAuth userAuth;
    FirebaseUser currentUser;

    private CheckBox checkBox;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private ImageButton RegisterButton;
    private Button SigninButton;

    static ComponentName service = null;
    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Email = (EditText) findViewById(R.id.email);
        Password = (EditText) findViewById(R.id.password);

        checkBox = findViewById(R.id.checkBoxRememberMe);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        userAuth = FirebaseAuth.getInstance();

        if (!loadSharedPreferences())
            Toast.makeText(this, "unable to load data", Toast.LENGTH_SHORT).show();
        SigninButton = (Button) findViewById(R.id.signIn);
        SigninButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validations()) {
                    saveSharedPrefences();
                    signIn(Email.getText().toString(), Password.getText().toString());
                }
            }
        });

        RegisterButton = (ImageButton) findViewById(R.id.registerButton);
        RegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));

            }
        });

           //service does'nt start
            Intent intent = new Intent(getBaseContext(), newTrip.class);//create new notification service intent
            service = startService(intent);// start the intent



    }

    private boolean validations() {
        boolean flag = true;
        String email = Email.getText().toString();
        String password = Password.getText().toString();

        if (email.isEmpty()) {
            Email.setError("Please enter email");
            flag = false;
        }
        if (password.isEmpty()) {
            Password.setError("Please enter password");
            flag = false;
        }
        return flag;

    }

    private boolean loadSharedPreferences() {
        if (sharedPreferences.contains("SavePassword")) {
            if (!sharedPreferences.getBoolean("SavePassword", false))
                return true;
            else
                checkBox.setChecked(true);
            if (sharedPreferences.contains("userName"))
                Email.setText(sharedPreferences.getString("userName", null));
            else
                return false;
        }
        return true;
    }

    private void saveSharedPrefences() {
        if (checkBox.isChecked()) {
            try {
                editor.putBoolean("SavePassword", true);
                editor.putString("userName", Email.getText().toString());
                editor.putString("userPassword", Password.getText().toString());
                editor.commit();
            } catch (Exception ex) {
                Toast.makeText(this, "failed to save Preferences", Toast.LENGTH_SHORT).show();
            }
        } else {
            editor.clear();
            editor.commit();
        }
    }

    private void signIn(String email, String password) {
        userAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    currentUser = userAuth.getCurrentUser();
                    Intent intent = new Intent(getBaseContext(), DriverMenu.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getBaseContext(), "Unable to load", Toast.LENGTH_SHORT).show();
                    Email.setError("Incorrect email");
                    Password.setError("Incorrect password");
                }
            }
        });
    }

}

