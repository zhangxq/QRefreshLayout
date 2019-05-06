package com.zhangxq.test.recyclerview;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhangxq.refreshlayout.QRefreshLayout;
import com.zhangxq.test.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangxiaoqi on 2019/4/18.
 */

public class RecyclerViewActivity extends AppCompatActivity implements QRefreshLayout.OnRefreshListener, QRefreshLayout.OnLoadListener {
    private RecyclerView recyclerView;
    private QRefreshLayout qRefreshLayout;

    private List<String> datas = new ArrayList<>();
    private RecyclerAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recyclerview);
        setTitle("RecyclerView示例");
        recyclerView = findViewById(R.id.recyclerView);
        qRefreshLayout = findViewById(R.id.refreshLayout);
        qRefreshLayout.setOnRefreshListener(this);
        qRefreshLayout.setOnLoadListener(this);
        qRefreshLayout.setRefreshing(true);
        qRefreshLayout.setIsCanSecondFloor(true);
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.img_test);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        qRefreshLayout.setSecondFloorView(imageView);

        adapter = new RecyclerAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
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
    public void onBackPressed() {
        if (qRefreshLayout.isSecondFloor()) {
            qRefreshLayout.setBackToFirstFloor();
        } else {
            super.onBackPressed();
        }
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<ItemHolder> {

        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ItemHolder(LayoutInflater.from(RecyclerViewActivity.this).inflate(R.layout.view_list_item, null));
        }

        @Override
        public void onBindViewHolder(ItemHolder holder, int position) {
            holder.tvTest.setText(datas.get(position));
            if (position % 2 == 0) {
                holder.tvTest.setBackgroundColor(0xffabcdef);
            } else {
                holder.tvTest.setBackgroundColor(0xffffffff);
            }
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        TextView tvTest;

        public ItemHolder(View itemView) {
            super(itemView);
            tvTest = itemView.findViewById(R.id.tvTest);
        }
    }
}
