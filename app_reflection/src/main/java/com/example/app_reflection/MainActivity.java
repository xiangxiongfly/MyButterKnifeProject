package com.example.app_reflection;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lib_reflection.BindView;
import com.example.lib_reflection.MyButterKnife;
import com.example.lib_reflection.OnClick;
import com.example.lib_reflection.OnLongClick;

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

    @OnClick({R.id.btn1, R.id.btn2})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.btn1:
                Toast.makeText(this, "btn1 click", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn2:
                Toast.makeText(this, "btn2 click", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @OnLongClick({R.id.btn1, R.id.btn2})
    public boolean longClick(View view) {
        switch (view.getId()) {
            case R.id.btn1:
                Toast.makeText(this, "btn1 longClick", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn2:
                Toast.makeText(this, "btn2 longClick", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }
}