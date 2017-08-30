package com.example.hekd.myapplication;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class MainActivity extends Activity {

    private static final int MAXNUM = 10;
    private static final int CLICK_NO = 0;
    private static final int CLICK_RIGHT = 1;
    private static final int CLICK_ERROR = -1;
    private static final long DELAYED = 1000;
    @BindView(R.id.btn_top_back)
    ImageButton btnTopBack;
    @BindView(R.id.iv_left)
    ImageView ivLeft;
    @BindView(R.id.iv_startGame)
    ImageView ivStartGame;
    @BindView(R.id.tv_customsNum)
    TextView tvCustomsNum;
    @BindView(R.id.tv_secondNum)
    TextView tvSecondNum;
    @BindView(R.id.ll_top)
    LinearLayout llTop;
    @BindView(R.id.tv_line)
    TextView tvLine;
    @BindView(R.id.rv_select)
    RecyclerView rvSelect;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            init();
            return false;
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init();
    }

    private void init() {

        showCustoms();
        int leftPicNum = showRandomPic(MAXNUM);
        List<Integer> numList = new ArrayList<>();
        numList.add(leftPicNum);
        Random random = new Random();
        boolean flag = true;
        while (flag) {
            int otherNum = random.nextInt(MAXNUM);
            if (!numList.contains(otherNum) && numList.size() != 4) {
                numList.add(otherNum);
            }
            if (numList.size() == 4) flag = false;
        }
        Collections.shuffle(numList);//打乱集合
        showRight(numList, leftPicNum);
    }

    private void showRight(final List<Integer> list, final int leftNum) {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rvSelect.setLayoutManager(gridLayoutManager);
        final SelectAdapter adapter = new SelectAdapter(this, list, CLICK_NO);
        adapter.setOnMyItemClickListener(new SelectAdapter.OnMyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (list.size() != 0) {
                    if (list.get(position) == leftNum) {
                        //正确
                        adapter.afterClick(CLICK_RIGHT);
                        int customs_num = CacheUtils.getInt(MainActivity.this, "customs_num", 1);//获取保存关卡
                        CacheUtils.putInt(MainActivity.this, "customs_num", customs_num + 1);//保存关卡
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(DELAYED);
                                    Message message = handler.obtainMessage();
                                    handler.sendMessage(message);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();

                    } else {
                        adapter.afterClick(CLICK_ERROR);
                        showDialogReminder();
                    }
                }
            }
        });

        rvSelect.setAdapter(adapter);
    }

    /**
     * 随机显示最大数的图片
     *
     * @param maxNum
     */
    private int showRandomPic(int maxNum) {
        int picNum = new Random().nextInt(maxNum + 1);
        if (picNum == 0) {
            showCustoms();
        }
        int id = getResources().getIdentifier("pic" + picNum, "drawable", getPackageName());
        ivLeft.setImageResource(id);
        Animation operatingAnim = AnimationUtils.loadAnimation(this, R.anim.pic_in);
        operatingAnim.setDuration(2000);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
        if (operatingAnim != null) {
            ivLeft.startAnimation(operatingAnim);
        }
        return picNum;
    }


    /**
     * 显示关卡数
     */
    private void showCustoms() {

        int customs_num = CacheUtils.getInt(MainActivity.this, "customs_num", 1);//获取保存关卡
        if (customs_num == 11) {
            CustomToast.showToast(this, "恭喜你已通关,游戏重置", Toast.LENGTH_SHORT);
            reset();
        }

        switch (customs_num) {
            case 1:
                tvCustomsNum.setText("第一关");
                break;
            case 2:
                tvCustomsNum.setText("第二关");
                break;
            case 3:
                tvCustomsNum.setText("第三关");
                break;
            case 4:
                tvCustomsNum.setText("第四关");
                break;
            case 5:
                tvCustomsNum.setText("第五关");
                break;
            case 6:
                tvCustomsNum.setText("第六关");
                break;
            case 7:
                tvCustomsNum.setText("第七关");
                break;
            case 8:
                tvCustomsNum.setText("第八关");
                break;
            case 9:
                tvCustomsNum.setText("第九关");
                break;
            case 10:
                tvCustomsNum.setText("第十关");
                break;
        }

        showDialogCustoms(customs_num);
    }

    /**
     * 提示弹窗
     */
    private void showDialogReminder() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_reminder, null, false);
        final Dialog dialog = new Dialog(this, R.style.input_dialog);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        dialog.show();
        Button btn_dialogSure = (Button) view.findViewById(R.id.btn_dialog_sure);
        btn_dialogSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                init();
            }
        });


    }

    /**
     * 显示关卡弹窗
     */
    private void showDialogCustoms(int customsNnum) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_customs, null, false);
        final Dialog dialog = new Dialog(this, R.style.input_dialog);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        TextView tvNum = (TextView) view.findViewById(R.id.tv_dialog_customsNum);
        switch (customsNnum) {
            case 1:
                tvNum.setText("第一关");
                break;
            case 2:
                tvNum.setText("第二关");
                break;
            case 3:
                tvNum.setText("第三关");
                break;
            case 4:
                tvNum.setText("第四关");
                break;
            case 5:
                tvNum.setText("第五关");
                break;
            case 6:
                tvNum.setText("第六关");
                break;
            case 7:
                tvNum.setText("第七关");
                break;
            case 8:
                tvNum.setText("第八关");
                break;
            case 9:
                tvNum.setText("第九关");
                break;
            case 10:
                tvNum.setText("第十关");
                break;
        }
        dialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(DELAYED);
                    dialog.dismiss();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    @OnClick(R.id.btn_top_back)
    public void onViewClicked() {
        finish();
    }

    @OnLongClick(R.id.iv_startGame)
    public boolean onStartGame() {
//        CustomToast.showToast(this, "游戏开始", Toast.LENGTH_SHORT);
//        init();
        //复位
        reset();
        CustomToast.showToast(this, "游戏关卡已重置", Toast.LENGTH_SHORT);
        return false;
    }

    private void reset() {
        CacheUtils.putInt(MainActivity.this, "customs_num", 1);
        init();
    }
}
