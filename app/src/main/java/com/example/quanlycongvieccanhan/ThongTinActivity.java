package com.example.quanlycongvieccanhan;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class ThongTinActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarInfo);
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert);
        toolbar.setNavigationOnClickListener(v -> finish());

        MaterialButton btnInfoBack = findViewById(R.id.btnInfoBack);
        btnInfoBack.setOnClickListener(v -> finish());
    }
}