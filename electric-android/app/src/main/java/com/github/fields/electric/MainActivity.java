package com.github.fields.electric;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity implements View.OnClickListener {

    private ElectricFieldsView electricFieldsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        electricFieldsView = (ElectricFieldsView) findViewById(R.id.electric_fields);
        electricFieldsView.addField(100, 100, 1);
        electricFieldsView.addField(400, 400, 3);
        electricFieldsView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == electricFieldsView) {
            electricFieldsView.start();
        }
    }
}
