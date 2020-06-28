package com.cxwl.shawn.wuzhishan.decision.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.adapter.ShawnSelectPictureAdapter;
import com.cxwl.shawn.wuzhishan.decision.dto.DisasterDto;
import com.cxwl.shawn.wuzhishan.decision.util.CommonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取本地相册图片
 */
public class SelectPictureActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;
    private TextView tvControl;
    private ShawnSelectPictureAdapter mAdapter;
    private List<DisasterDto> dataList = new ArrayList<>();
    private int lastCount = 0;//上一次已经选了几张
    private int selectCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_picture);
        mContext = this;
        initWidget();
        initGridView();
    }

    private void initWidget() {
        LinearLayout llBack = findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("选择图片");
        tvControl = findViewById(R.id.tvControl);
        tvControl.setOnClickListener(this);
        tvControl.setText("完成");
        tvControl.setVisibility(View.INVISIBLE);

        lastCount = getIntent().getIntExtra("count", 0);

        loadImages();
    }

    private void initGridView() {
        GridView gridView = findViewById(R.id.gridView);
        mAdapter = new ShawnSelectPictureAdapter(mContext, dataList);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DisasterDto dto = dataList.get(position);
                if (dto.isSelected) {
                    dto.isSelected = false;
                    selectCount--;
                }else {
                    if ((selectCount+lastCount) >= 6) {
                        Toast.makeText(mContext, "最多只能选择"+(6-lastCount)+"张图片", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dto.isSelected = true;
                    selectCount++;
                }
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }

                tvControl.setText("完成("+selectCount+"/"+(6-lastCount)+")");
                if (selectCount <= 0) {
                    tvControl.setVisibility(View.INVISIBLE);
                }else {
                    tvControl.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * 获取相册信息
     */
    private void loadImages() {
        dataList.clear();
        dataList.addAll(CommonUtil.getAllLocalImages(mContext));

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                finish();
                break;
            case R.id.tvControl:
                if (selectCount <= 0) {
                    Toast.makeText(mContext, "请选择需要上传的图片！", Toast.LENGTH_SHORT).show();
                }else {
                    List<DisasterDto> list = new ArrayList<>();
                    for (int i = 0; i < dataList.size(); i++) {
                        DisasterDto dto = dataList.get(i);
                        if (dto.isSelected) {
                            list.add(dto);
                        }
                    }
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("dataList", (ArrayList<? extends Parcelable>) list);
                    intent.putExtras(bundle);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                break;

            default:
                break;
        }
    }

}
