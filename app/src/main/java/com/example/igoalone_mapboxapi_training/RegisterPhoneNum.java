package com.example.igoalone_mapboxapi_training;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterPhoneNum extends AppCompatActivity {

    EditText inputName;
    EditText inputPhone;
    String name;
    String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_phone_num);
        inputName = findViewById(R.id.name);
        inputPhone = findViewById(R.id.phone);

    }

    public void registerButtonClick(View view){
        name = inputName.getText().toString();
        System.out.println("name"+name);
        phone = inputPhone.getText().toString();
        System.out.println("phone:"+phone);
        if(name.length()==0||phone.length()==0){
            Toast.makeText(this,"정보를 입력해주세요.",Toast.LENGTH_LONG).show();
        }
        else{
            Friend friend = new Friend();
            friend.setName(name);
            friend.setNumber(phone);
            Intent intent = new Intent(this,MainActivity.class);
            intent.putExtra("friend",friend);

            startActivity(intent);
        }
    }
}
