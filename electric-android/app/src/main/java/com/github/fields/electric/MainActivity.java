package com.github.fields.electric;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import java.util.Random;

public class MainActivity extends Activity implements View.OnClickListener {

    private ElectricFieldsView electricFieldsView;
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        electricFieldsView = (ElectricFieldsView) findViewById(R.id.electric_fields);
        electricFieldsView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == electricFieldsView) {
            electricFieldsView.clear();

            int n = 1 + random.nextInt(9);
            int x, y, c;
            for (int i = 0; i < n; i++) {
                x = random.nextInt(700);
                y = random.nextInt(1200);
                c = random.nextInt(10) * (random.nextBoolean() ? +1 : -1);
                electricFieldsView.addField(x, y, c);
            }

            electricFieldsView.start();
        }
    }
}
