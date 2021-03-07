package com.raymai97.helloandroidsendemail;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.v = new V();
        v.goSendEmail.setOnClickListener(z -> goSendEmail());
        v.emailSubject.setText("Hello Android Send Email");
        v.emailBodyText.setText("This is a program-generated email.");
        v.presetNone.setOnClickListener(z -> {
            v.smtpHost.setText(null);
            v.smtpPort.setText(null);
            v.securityNone.setChecked(true);
        });
        v.presetGmailSsl.setOnClickListener(z -> {
            v.smtpHost.setText("smtp.gmail.com");
            v.smtpPort.setText("465");
            v.securitySsl.setChecked(true);
        });
        v.presetGmailTls.setOnClickListener(z -> {
            v.smtpHost.setText("smtp.gmail.com");
            v.smtpPort.setText("587");
            v.securityTls.setChecked(true);
        });
    }

    @NonNull
    private SimpleSmtpMailSender.Cfg uiToCfg() {
        final SimpleSmtpMailSender.Cfg cfg = new SimpleSmtpMailSender.Cfg();
        if (v.securitySsl.isChecked()) {
            cfg.wantAuth = true;
            cfg.wantSslEnable = true;
        }
        if (v.securityTls.isChecked()) {
            cfg.wantAuth = true;
            cfg.wantStartTlsEnable = true;
        }
        cfg.smtpHost = safeGetText(v.smtpHost);
        cfg.smtpPort = Integer.parseInt(safeGetText(v.smtpPort));
        cfg.smtpUserName = safeGetText(v.smtpUserName);
        cfg.smtpPassword = safeGetText(v.smtpPassword);
        return cfg;
    }

    private void goSendEmail() {
        final SimpleSmtpMailSender.Cfg cfg = uiToCfg();
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending e-mail...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        new Thread(() -> {
            final Throwable[] thrown = new Throwable[1];
            try {
                new SimpleSmtpMailSender().send(cfg,
                        safeGetText(v.emailTo),
                        safeGetText(v.emailSubject),
                        safeGetText(v.emailBodyText),
                        null);
            } catch (final Throwable e) {
                Log.e(TAG, "goSendEmail: ", e);
                thrown[0] = e;
            }
            runOnUiThread(() -> {
                progressDialog.dismiss();
                if (thrown[0] != null) {
                    newAlert().setTitle("Unexpected Thrown")
                            .setMessage(thrown[0].toString())
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                } else {
                    newAlert().setTitle("E-mail sent out")
                            .setMessage("E-mail has been sent out successfully.")
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                }
            });
        }).start();
    }

    @NonNull
    protected AlertDialog.Builder newAlert() {
        return new AlertDialog.Builder(this);
    }

    @NonNull
    private String safeGetText(@NonNull EditText editText) {
        final CharSequence cs = editText.getText();
        return cs == null ? "" : cs.toString();
    }

    private class V {
        final Button presetNone = findViewById(R.id.main_presetNone);
        final Button presetGmailTls = findViewById(R.id.main_presetGmailTls);
        final Button presetGmailSsl = findViewById(R.id.main_presetGmailSsl);
        final RadioButton securityNone = findViewById(R.id.main_securityNone);
        final RadioButton securityTls = findViewById(R.id.main_securityTls);
        final RadioButton securitySsl = findViewById(R.id.main_securitySsl);
        final TextInputEditText smtpHost = findViewById(R.id.main_smtpHost);
        final TextInputEditText smtpPort = findViewById(R.id.main_smtpPort);
        final TextInputEditText smtpUserName = findViewById(R.id.main_smtpUserName);
        final TextInputEditText smtpPassword = findViewById(R.id.main_smtpPassword);
        final TextInputEditText emailTo = findViewById(R.id.main_emailTo);
        final TextInputEditText emailSubject = findViewById(R.id.main_emailSubject);
        final TextInputEditText emailBodyText = findViewById(R.id.main_emailBodyText);
        final Button goSendEmail = findViewById(R.id.main_sendEmail);
    }

    private V v;
}
