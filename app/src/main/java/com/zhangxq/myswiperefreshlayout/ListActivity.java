package com.zhangxq.myswiperefreshlayout;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangxiaoqi on 2019/4/12.
 */

public class ListActivity extends AppCompatActivity implements RefreshLayout.OnRefreshListener {
    private RefreshLayout refreshLayout;
    private ListView listView;

    private ListAdapter adapter;
    private List<String> datas = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        refreshLayout = findViewById(R.id.refreshLayout);
        listView = findViewById(R.id.listView);

        refreshLayout.setOnRefreshListener(this);

        adapter = new ListAdapter();
        listView.setAdapter(adapter);
        down();
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                down();
                refreshLayout.setRefreshing(false);
            }
        }, 1000);
    }

    private void down() {
        datas.clear();
        for (int i = 0; i < 50; i++) {
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
                convertView = LayoutInflater.from(ListActivity.this).inflate(R.layout.view_list_item, null);
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
