package huimeitimes.com.draglayoutdemo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private DragLayout mDragLayout;
    private ListView mListView;
    private BaseAdapter mAdapter;
    private ArrayList<String> mArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDragLayout = (DragLayout) findViewById(R.id.dragLayout);
        findViewById(R.id.handle).setOnClickListener(this);
        mListView = (ListView) findViewById(R.id.content);
        initData();
        mListView.setAdapter(mAdapter);
    }

    private void initData() {
        mArrayList = new ArrayList<>();
        for (int i = 1; i <= 40; i++) {
            mArrayList.add(String.format(Locale.CHINA, "这是第%02d个", i));
        }
        mAdapter = new MyAdapter(mArrayList);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.handle:
                mDragLayout.animateToggle();
                break;
        }
    }

    class MyAdapter extends BaseAdapter {
        private List<String> mList;

        public MyAdapter(List<String> mList) {
            this.mList = mList;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView;
            if (convertView == null) {
                textView= (TextView) getLayoutInflater().inflate(android.R.layout.test_list_item,parent, false);
//                textView = new TextView(MainActivity.this);
                textView.setBackgroundColor(Color.YELLOW);
                textView.setTextSize(20f);
            } else {
                textView = (TextView) convertView;
            }
            textView.setText(mList.get(position));
            return textView;
        }
    }
}
