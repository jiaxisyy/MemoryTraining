package com.example.hekd.myapplication;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import java.util.Set;
import java.util.TreeSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

import static android.R.id.list;

public class MainActivity extends Activity {

    private static int MAXNUM = 20;
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
    private int customs_num_all;
    private int leftPicNum;
    private boolean flag = true;
    private List<Integer> showAfterNum = new ArrayList<>();
    private List<Integer> integers = new ArrayList<>();
    private boolean isShuffle = true;
    private int count = 1;


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
//        int customs_num = CacheUtils.getInt(MainActivity.this, "customs_num", 1);//获取保存关卡
        //获取总的关卡数
        customs_num_all = CacheUtils.getInt(MainActivity.this, "customs_num_all", 1);
        if (customs_num_all == 1) {
            MAXNUM = 10;
        } else if (customs_num_all == 2) {
            MAXNUM = 15;
        } else if (customs_num_all == 3) {
            MAXNUM = 20;
        }
        Log.d("TAG", "MAXNUM=" + MAXNUM + "");

        if (isShuffle) {//是否打乱
            for (int i = 1; i <= MAXNUM; i++) {
                integers.add(i);
            }
            Collections.shuffle(integers);//打乱集合
            isShuffle = false;
        }
        Log.d("TAG", "integers=" + integers.toString());
        leftPicNum = showRandomPic(integers.get(count - 1));
//        fun1();
        List<Integer> numList = new ArrayList<>();
        numList.add(leftPicNum);
        Log.d("TAG", "leftPicNum2=" + leftPicNum + "");
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

    private void fun1() {
        List<Integer> integers = new ArrayList<>();

        for (int i = 1; i <= MAXNUM; i++) {
            integers.add(i);
        }

        while (flag) {
            if (showAfterNum.contains(leftPicNum)) {//重新取值
                leftPicNum = showRandomPic(MAXNUM);
                Log.d("TAG", "leftPicNum1=" + leftPicNum + "");
            } else {
                flag = false;
                showAfterNum.add(leftPicNum);
                Log.d("TAG", showAfterNum.toString());
            }
        }
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
                        flag = true;
                        count++;
                        adapter.afterClick(CLICK_RIGHT, position);
                        int customs_num = CacheUtils.getInt(MainActivity.this, "customs_num", 1);//获取保存关卡
                        if (customs_num == MAXNUM) {
                            if (customs_num_all == 3) {
                                pass();
                            } else {
                                CacheUtils.putInt(MainActivity.this, "customs_num_all", customs_num_all + 1);//保存关卡
                                CacheUtils.putInt(MainActivity.this, "customs_num", 1);//保存关卡
                                count = 1;
                                isShuffle = true;
                                integers.clear();
                            }
                        } else {
                            CacheUtils.putInt(MainActivity.this, "customs_num", customs_num + 1);//保存关卡
                        }
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
                        isShuffle = true;
                        count = 1;
                        adapter.afterClick(CLICK_ERROR, position);
                        showDialogReminder();
                        CacheUtils.putInt(MainActivity.this, "customs_num", 1);
                        integers.clear();
                    }
                }
            }
        });

        rvSelect.setAdapter(adapter);
    }

    /**
     * 通关
     */
    private void pass() {
        CustomToast.showToast(this, "恭喜你已通关,游戏重置", Toast.LENGTH_SHORT);

        reset();
    }

    /**
     * 随机显示最大数的图片
     *
     * @param
     */
    private int showRandomPic(int picNum) {
//        int picNum = new Random().nextInt(maxNum + 1);
//        if (picNum == 0) {
//            showCustoms();
//        }
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
        int customs_num_all = CacheUtils.getInt(MainActivity.this, "customs_num_all", 1);//获取保存关卡
//        if (customs_num_all == 21) {
//            CustomToast.showToast(this, "恭喜你已通关,游戏重置", Toast.LENGTH_SHORT);
//            reset();
//        }
        if (customs_num_all == 1) {
            tvCustomsNum.setText("第一关");
        } else if (customs_num_all == 2) {
            tvCustomsNum.setText("第二关");
        } else if (customs_num_all == 3) {
            tvCustomsNum.setText("第三关");
        }
        showDialogCustoms(customs_num_all);
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

        int customs_num = CacheUtils.getInt(MainActivity.this, "customs_num", 1);//获取保存关卡
        if (customs_num == 1) {
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_customs, null, false);
            final Dialog dialog = new Dialog(this, R.style.input_dialog);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setContentView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            TextView tvNum = (TextView) view.findViewById(R.id.tv_dialog_customsNum);
            if (customsNnum == 1) {
                tvNum.setText("第一关");
            } else if (customsNnum == 2) {
                tvNum.setText("第二关");
            } else if (customsNnum == 3) {
                tvNum.setText("第三关");
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
        CacheUtils.putInt(MainActivity.this, "customs_num_all", 1);
        count = 1;
        isShuffle=true;
        integers.clear();
        init();
    }

    /**
     * 随机指定范围内N个不重复的数
     * 在初始化的无重复待选数组中随机产生一个数放入结果中，
     * 将待选数组被随机到的数，用待选数组(len-1)下标对应的数替换
     * 然后从len-2里随机产生下一个随机数，如此类推
     *
     * @param max 指定范围最大值
     * @param min 指定范围最小值
     * @param n   随机数个数
     * @return int[] 随机数结果集
     */
    public static int[] randomArray(int min, int max, int n) {
        int len = max - min + 1;
        if (max < min || n > len) {
            return null;
        }
        //初始化给定范围的待选数组
        int[] source = new int[len];
        for (int i = min; i < min + len; i++) {
            source[i - min] = i;
        }
        int[] result = new int[n];
        Random rd = new Random();
        int index = 0;
        for (int i = 0; i < result.length; i++) {
            //待选数组0到(len-2)随机一个下标
            index = Math.abs(rd.nextInt() % len--);
            //将随机到的数放入结果集
            result[i] = source[index];
            //将待选数组中被随机到的数，用待选数组(len-1)下标对应的数替换
            source[index] = source[len];
        }
        return result;
    }
}
