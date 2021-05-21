package com.example.app2;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lib_reflection.BindView;
import com.example.lib_reflection.MyButterknife;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.textView)
    TextView textView;

    @BindView(R.id.imageView)
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyButterknife.bind(this);
        textView.setText("hello");
        imageView.setImageResource(R.mipmap.ic_launcher);
    }
}