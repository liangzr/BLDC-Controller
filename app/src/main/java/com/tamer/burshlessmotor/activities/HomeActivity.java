package com.tamer.burshlessmotor.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;

import com.tamer.burshlessmotor.R;
import com.tamer.burshlessmotor.adapter.CardsAdapter;
import com.tamer.burshlessmotor.bean.Card;

import java.util.ArrayList;
import java.util.List;

import app.akexorcist.bluetotohspp.library.DeviceList;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private List<Card> cardList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        /* ListView初始化及设置适配器 */
        initCards();
        CardsAdapter adapter = new CardsAdapter(HomeActivity.this, R.layout.controlbar_list_item, cardList);
        ListView cardListView = (ListView) findViewById(R.id.cards_list);
        cardListView.setAdapter(adapter);

        Card card = new Card(Card.CONTROL_BAR);
        cardList.add(card);

    }

    private void initCards() {
        Card card1 = new Card(Card.SWITCH);
        cardList.add(card1);
        Card card2 = new Card(Card.CONTROL_BAR);
        cardList.add(card2);
    }

    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.fab:
                intent = new Intent(getApplicationContext(), DeviceList.class);
                startActivity(intent);
                break;
            default:
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

    }
}
