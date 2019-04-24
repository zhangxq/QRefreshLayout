package com.zhangxq.test.listview;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.zhangxq.refreshlayout.QRefreshLayout;
import com.zhangxq.test.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangxiaoqi on 2019/4/16.
 */

public class ListViewActivity extends AppCompatActivity implements QRefreshLayout.OnRefreshListener, QRefreshLayout.OnLoadListener, AdapterView.OnItemClickListener {
    private ListView listView;
    private ListAdapter adapter;
    private List<String> datas = new ArrayList<>();
    private QRefreshLayout qRefreshLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);
        qRefreshLayout = findViewById(R.id.refreshLayout);

        qRefreshLayout.setOnRefreshListener(this);
        qRefreshLayout.setOnLoadListener(this);
        qRefreshLayout.setRefreshView(new MyRefreshView(this));
        qRefreshLayout.setLoadView(new MyLoadView(this));

        listView = findViewById(R.id.listView);
        listView.setOnItemClickListener(this);
        adapter = new ListAdapter();
        listView.setAdapter(adapter);
        down();
    }

    private void down() {
        datas.clear();
        for (int i = 0; i < 20; i++) {
            datas.add("测试数据" + i);
        }
        adapter.notifyDataSetChanged();
    }

    private void up() {
        int size = datas.size();
        for (int i = size; i < size + 10; i++) {
            datas.add("测试数据" + i);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                down();
                qRefreshLayout.setRefreshing(false);
            }
        }, 1000);
    }

    @Override
    public void onLoad() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                up();
                qRefreshLayout.setLoading(false);
            }
        }, 1000);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(this, position + "", Toast.LENGTH_SHORT).show();
    }

    private class ListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public String getItem(int position) {
            return datas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(ListViewActivity.this).inflate(R.layout.view_list_item, null);
            }
            TextView tvTest = convertView.findViewById(R.id.tvTest);
            tvTest.setText(getItem(position));
            if (position % 2 == 0) {
                tvTest.setBackgroundColor(0xffabcdef);
            } else {
                tvTest.setBackgroundColor(0xffffffff);
            }

            return convertView;
        }
    }
}
