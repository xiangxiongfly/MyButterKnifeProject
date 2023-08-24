package com.example.app_apt;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.annotation.BindView;
import com.example.apt_library.MyButterKnife;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.textView)
    TextView textView;
    @BindView(R.id.imageView)
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyButterKnife.bind(this);
        textView.setText("hello");
        imageView.setImageResource(R.mipmap.ic_launcher);
    }
}