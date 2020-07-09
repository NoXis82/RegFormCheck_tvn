package com.example.regformcheck;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {
    private EditText mLoginEdit;
    private EditText mPasswordEdit;
    private Button btnLogin;
    private Button btnReg;
    private CheckBox checkValue;
    private static final String FILE_USER = "user.txt";
    public static final String CHECK_PREF = "check";
    public static final String CHECK_STATUS = "check_status";
    private String loginValue;
    private String passValue;
    private SharedPreferences myCheckPreferences;
    public static final int REQUEST_CODE_PERMISSION_WRITE_STORAGE = 100;
    private File userFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        readCheckPrefStart();
        regBtnClick();
        logBtnClick();
        saveCheckStatus();
   }

    private void readCheckPrefStart() {
        if (myCheckPreferences.contains(CHECK_STATUS)) {
            checkValue.setChecked(myCheckPreferences
                    .getBoolean(CHECK_STATUS, false));
        }
    }

    private void saveCheckStatus() {
        checkValue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = myCheckPreferences.edit();
                editor.putBoolean(CHECK_STATUS, isChecked);
                editor.apply();
            }
        });
    }

    private void logBtnClick() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValue.isChecked()) {
                    readUserExternalStorage();
                } else {
                    readUserInternalStorage();
                }
            }
        });
    }

    private void regBtnClick() {
        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginValue = mLoginEdit.getText().toString();
                passValue = mPasswordEdit.getText().toString();
                if (!loginValue.equals("") && !passValue.equals("")) {
                    if (checkValue.isChecked()) {
                        userFile.delete();
                        saveUserExternalStorage();
                    } else {
                        saveUserInternalStorage();
                    }
                } else {
                    Toast.makeText(v.getContext(), R.string.fill_blanks,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void readUserInternalStorage() {
        loginValue = mLoginEdit.getText().toString();
        passValue = mPasswordEdit.getText().toString();
        try {
            FileInputStream fileUser = openFileInput(FILE_USER);
            InputStreamReader inputLoginRead = new InputStreamReader(fileUser);
            BufferedReader reader = new BufferedReader(inputLoginRead);
            String lineUser = reader.readLine();
            StringBuilder outputLogin = new StringBuilder();
            StringBuilder outputPass = new StringBuilder();
            while (lineUser != null) {
                outputLogin = outputLogin.append(lineUser);
                lineUser = reader.readLine();
                outputPass = outputPass.append(lineUser);
                lineUser = reader.readLine();
            }
            mLoginEdit.setText("");
            mPasswordEdit.setText("");
            if (outputLogin.toString().equals(loginValue) &&
                    outputPass.toString().equals(passValue)) {
                Toast.makeText(this, R.string.access,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.no_access,
                        Toast.LENGTH_SHORT).show();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        Toast.makeText(this, R.string.read_internal_storage,
                Toast.LENGTH_SHORT).show();
    }

    private void saveUserInternalStorage() {
        loginValue = mLoginEdit.getText().toString();
        passValue = mPasswordEdit.getText().toString();
        try {
            FileOutputStream fileUser = openFileOutput(FILE_USER,
                    Context.MODE_PRIVATE);
            OutputStreamWriter outputLoginWriter =
                    new OutputStreamWriter(fileUser);
            BufferedWriter bw = new BufferedWriter(outputLoginWriter);
            String userInfo = loginValue + "\n" + passValue;
            bw.write(userInfo);
            bw.close();
            mLoginEdit.setText("");
            mPasswordEdit.setText("");
            Toast.makeText(this, R.string.success_reg,
                    Toast.LENGTH_SHORT).show();
            Toast.makeText(this, R.string.internal_storage,
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        mLoginEdit = findViewById(R.id.edit_login);
        mPasswordEdit = findViewById(R.id.edit_password);
        btnLogin = findViewById(R.id.btnLogin);
        btnReg = findViewById(R.id.btnReg);
        checkValue = findViewById(R.id.checkbox);
        myCheckPreferences = getSharedPreferences(CHECK_PREF, Context.MODE_PRIVATE);
        userFile = new File(getApplicationContext().getExternalFilesDir(null),
                FILE_USER);
    }

    private void writeFile() {
        if (isExternalStorageWritable()) {
            userFile = new File(getApplicationContext().getExternalFilesDir(null),
                    FILE_USER);
            try (FileWriter fileWriter = new FileWriter(userFile, true)) {
                fileWriter.append(mLoginEdit.getText().toString()).append("\n")
                        .append(mPasswordEdit.getText().toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            mLoginEdit.setText("");
            mPasswordEdit.setText("");
            Toast.makeText(this, R.string.success_reg,
                    Toast.LENGTH_SHORT).show();
            Toast.makeText(this, R.string.external_storage,
                    Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private void saveUserExternalStorage() {
        int permissionStatus = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        );
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            writeFile();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_PERMISSION_WRITE_STORAGE
            );
        }
    }

    private void readUserExternalStorage() {
        loginValue = mLoginEdit.getText().toString();
        passValue = mPasswordEdit.getText().toString();
        if (isExternalStorageWritable()) {
            try (FileReader fileReader = new FileReader(userFile)) {
                BufferedReader reader = new BufferedReader(fileReader);
                String lineUser = reader.readLine();
                StringBuilder outputLogin = new StringBuilder();
                StringBuilder outputPass = new StringBuilder();
                while (lineUser != null) {
                    outputLogin = outputLogin.append(lineUser);
                    lineUser = reader.readLine();
                    outputPass = outputPass.append(lineUser);
                    lineUser = reader.readLine();
                }
                mLoginEdit.setText("");
                mPasswordEdit.setText("");
                if (outputLogin.toString().equals(loginValue) &&
                        outputPass.toString().equals(passValue)) {
                    Toast.makeText(this, R.string.access,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.no_access,
                            Toast.LENGTH_SHORT).show();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            Toast.makeText(this, R.string.read_external_storage,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION_WRITE_STORAGE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                writeFile();
            } else {
                Toast.makeText(this,
                        R.string.not_run_request,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}