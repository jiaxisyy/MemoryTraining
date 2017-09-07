package com.example.hekd.myapplication;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.RequiresApi;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static android.R.id.list;

public class MainActivity extends Activity {

    private static final long ANIMATIONTIME = 500;
    private static final String APP_VERSIONCODE = "app_versioncode";
    private static final String CUSTOMS_NUM = "customs_num";
    private static final String CUSTOMS_NUM_ALL = "customs_num_all";
    private static int MAXNUM = 20;
    private static final int CLICK_NO = 0;
    private static final int CLICK_RIGHT = 1;
    private static final int CLICK_ERROR = -1;
    private static final long DELAYED = 1500;
    private static final long SECONDDOWNTIME_LONG = 1000;
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
    @BindView(R.id.tv_secondWord)
    TextView tvSecondWord;
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
    private AudioService audioService;


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            //这里我们实例化audioService,通过binder来实现
            audioService = ((AudioService.AudioBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            audioService = null;
        }
    };
    private Intent intent;
    private SoundPool soundPool;
    private int soundID;
    private int SECONDDOWNTIME = 15;//倒计时时间
    private CompositeDisposable cd = new CompositeDisposable();
    private boolean isResetSecondDown = true;//是否重置倒计时
    private Disposable disposable;

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

        checkVersionCode();
        init();
        addBGM();
    }

    /**
     * 检查版本号,确认是否需要重置关卡
     */
    private void checkVersionCode() {
        int versionCode = Utils.getVersionCode(this);
        int localVersionCode = CacheUtils.getInt(this, APP_VERSIONCODE,1);
        if (versionCode > localVersionCode) {//重置
            CacheUtils.putInt(MainActivity.this, CUSTOMS_NUM, 1);
            CacheUtils.putInt(MainActivity.this, CUSTOMS_NUM_ALL, 1);
            CacheUtils.putInt(this,APP_VERSIONCODE,versionCode);
        }
    }


    /**
     * 添加背景音乐
     */

    private void addBGM() {
        intent = new Intent();
        intent.setClass(this, AudioService.class);
        startService(intent);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        addBtnBGM();
    }

    /**
     * 按钮声
     */

    private void addBtnBGM() {
        soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 5);
        soundID = soundPool.load(this, R.raw.bgm_button_water_drop, 1);
    }

    private void playSound() {
        soundPool.play(soundID,
                0.1f,   //左耳道音量【0~1】
                0.1f,   //右耳道音量【0~1】
                0,     //播放优先级【0表示最低优先级】
                1,     //循环模式【0表示循环一次，-1表示一直循环，其他表示数字+1表示当前数字对应的循环次数】
                1     //播放速度【1是正常，范围从0~2】
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        stopService(intent);
        if (!cd.isDisposed()) {
            cd.dispose();
        }
    }

    private void init() {


        showCustoms();
//        int customs_num = CacheUtils.getInt(MainActivity.this, "customs_num", 1);//获取保存关卡
        //获取总的关卡数
        customs_num_all = CacheUtils.getInt(MainActivity.this, CUSTOMS_NUM_ALL, 1);
//        //模拟15关
//        customs_num_all = 15;
        if (customs_num_all == 1) {
            MAXNUM = 10;
        } else if (customs_num_all == 2) {
            MAXNUM = 15;
        } else if (customs_num_all == 3) {
            MAXNUM = 20;
        } else if (customs_num_all == 4) {
            MAXNUM = 25;
        } else if (customs_num_all == 5) {
            MAXNUM = 30;
        } else if (customs_num_all == 6) {
            MAXNUM = 35;
        } else if (customs_num_all == 7) {
            MAXNUM = 40;
        } else if (customs_num_all == 8) {
            MAXNUM = 45;
        } else if (customs_num_all == 9) {
            MAXNUM = 50;
        } else if (customs_num_all == 10) {
            MAXNUM = 60;
        } else if (customs_num_all == 11) {
            MAXNUM = 70;
        } else if (customs_num_all == 12) {
            MAXNUM = 80;
        } else if (customs_num_all == 13) {
            MAXNUM = 90;
        } else if (customs_num_all >= 14) {
            MAXNUM = 99;
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

        if (customs_num_all >= 15) {
            //取前三十个数
            List<Integer> integerFor30 = integers.subList(0, 30);
            leftPicNum = showRandomPic(integerFor30.get(count - 1));
        } else {
            leftPicNum = showRandomPic(integers.get(count - 1));
        }
        //********************************************
        //限时30个数字

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


        //开始限时
        if (customs_num_all >= 15) {
            if (customs_num_all == 15) {
                SECONDDOWNTIME = 60;
            } else if (customs_num_all == 16) {
                SECONDDOWNTIME = 55;
            } else if (customs_num_all == 17) {
                SECONDDOWNTIME = 45;
            } else if (customs_num_all == 18) {
                SECONDDOWNTIME = 40;
            } else if (customs_num_all == 19) {
                SECONDDOWNTIME = 35;
            } else if (customs_num_all == 20) {
                SECONDDOWNTIME = 30;
            }
            if (isResetSecondDown) {
                startCountDown(SECONDDOWNTIME);
                isResetSecondDown = false;
            }
        }
    }

    /**
     * 倒计时
     */

    private void startCountDown(final int second) {
        tvSecondNum.setVisibility(View.VISIBLE);
        tvSecondWord.setVisibility(View.VISIBLE);
        Log.d("TAG", "startCountDown");
//        secondDownForJava();
        Observable<Long> observable = Observable.interval(0, 1, TimeUnit.SECONDS).take(second + 1).map(new Function<Long, Long>() {
            @Override
            public Long apply(@NonNull Long aLong) throws Exception {

                return second - aLong;// 由于是倒计时，需要将倒计时的数字反过来
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.newThread());
        observable.subscribe(getObserver());

    }

    private Observer getObserver() {
        Observer<Long> longObserver = new Observer<Long>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
//                cd.add(d);
                disposable = d;
            }

            @Override
            public void onNext(@NonNull Long o) {
                tvSecondNum.setText(o + "");
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {
                showDialogReminder();
            }
        };
        return longObserver;
    }


    private void showRight(final List<Integer> list, final int leftNum) {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rvSelect.setLayoutManager(gridLayoutManager);
        final SelectAdapter adapter = new SelectAdapter(this, list, CLICK_NO);
        adapter.setOnMyItemClickListener(new SelectAdapter.OnMyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                if (Utils.isValidClick()) {
                    if (list.size() != 0) {

                        playSound();
                        if (list.get(position) == leftNum) {
                            //正确
                            flag = true;
                            count++;
                            adapter.afterClick(CLICK_RIGHT, position);
                            int customs_num = CacheUtils.getInt(MainActivity.this, CUSTOMS_NUM, 1);//获取保存关卡
                            if (customs_num_all >= 15) {
                                MAXNUM = 30;
                            }
                            if (customs_num == MAXNUM) {
                                if (customs_num_all == 20) {
                                    pass();
                                } else {
                                    CacheUtils.putInt(MainActivity.this, CUSTOMS_NUM_ALL, customs_num_all + 1);//保存关卡
                                    CacheUtils.putInt(MainActivity.this, CUSTOMS_NUM, 1);//保存关卡
                                    count = 1;
                                    isShuffle = true;
                                    integers.clear();
                                    //下一关
//                                    cd.dispose();
                                    if (disposable != null) {
                                        disposable.dispose();
                                    }
                                    isResetSecondDown = true;
                                }
                            } else {
                                CacheUtils.putInt(MainActivity.this, CUSTOMS_NUM, customs_num + 1);//保存关卡
                            }
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(0);
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
                            CacheUtils.putInt(MainActivity.this, CUSTOMS_NUM, 1);
                            integers.clear();
                        }
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
        operatingAnim.setDuration(ANIMATIONTIME);
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
        int customs_num_all = CacheUtils.getInt(MainActivity.this, CUSTOMS_NUM_ALL, 1);//获取保存关卡
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
        } else if (customs_num_all == 4) {
            tvCustomsNum.setText("第四关");
        } else if (customs_num_all == 5) {
            tvCustomsNum.setText("第五关");
        } else if (customs_num_all == 6) {
            tvCustomsNum.setText("第六关");
        } else if (customs_num_all == 7) {
            tvCustomsNum.setText("第七关");
        } else if (customs_num_all == 8) {
            tvCustomsNum.setText("第八关");
        } else if (customs_num_all == 9) {
            tvCustomsNum.setText("第九关");
        } else if (customs_num_all == 10) {
            tvCustomsNum.setText("第十关");
        } else if (customs_num_all == 11) {
            tvCustomsNum.setText("第十一关");
        } else if (customs_num_all == 12) {
            tvCustomsNum.setText("第十二关");
        } else if (customs_num_all == 13) {
            tvCustomsNum.setText("第十三关");
        } else if (customs_num_all == 14) {
            tvCustomsNum.setText("第十四关");
        } else if (customs_num_all == 15) {
            tvCustomsNum.setText("第十五关");
        } else if (customs_num_all == 16) {
            tvCustomsNum.setText("第十六关");
        } else if (customs_num_all == 17) {
            tvCustomsNum.setText("第十七关");
        } else if (customs_num_all == 18) {
            tvCustomsNum.setText("第十八关");
        } else if (customs_num_all == 19) {
            tvCustomsNum.setText("第十九关");
        } else if (customs_num_all == 20) {
            tvCustomsNum.setText("第二十关");
        }

        showDialogCustoms(customs_num_all);
    }

    /**
     * 提示弹窗
     */
    private void showDialogReminder() {
        if (disposable != null) {
            disposable.dispose();//取消订阅
        }
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
//                cd.dispose();
                isResetSecondDown = true;//重置倒计时
                dialog.dismiss();
                init();
            }
        });
    }

    /**
     * 显示关卡弹窗
     */
    private void showDialogCustoms(int customsNnum) {

        int customs_num = CacheUtils.getInt(MainActivity.this, CUSTOMS_NUM, 1);//获取保存关卡
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
            } else if (customsNnum == 4) {
                tvNum.setText("第四关");
            } else if (customsNnum == 5) {
                tvNum.setText("第五关");
            } else if (customsNnum == 6) {
                tvNum.setText("第六关");
            } else if (customsNnum == 7) {
                tvNum.setText("第七关");
            } else if (customsNnum == 8) {
                tvNum.setText("第八关");
            } else if (customsNnum == 9) {
                tvNum.setText("第九关");
            } else if (customsNnum == 10) {
                tvNum.setText("第十关");
            } else if (customsNnum == 11) {
                tvNum.setText("第十一关");
            } else if (customsNnum == 12) {
                tvNum.setText("第十二关");
            } else if (customsNnum == 13) {
                tvNum.setText("第十三关");
            } else if (customsNnum == 14) {
                tvNum.setText("第十四关");
            } else if (customsNnum == 15) {
                tvNum.setText("第十五关");
            } else if (customsNnum == 16) {
                tvNum.setText("第十六关");
            } else if (customsNnum == 17) {
                tvNum.setText("第十七关");
            } else if (customsNnum == 18) {
                tvNum.setText("第十八关");
            } else if (customsNnum == 19) {
                tvNum.setText("第十九关");
            } else if (customsNnum == 20) {
                tvNum.setText("第二十关");
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
        CacheUtils.putInt(MainActivity.this, CUSTOMS_NUM, 1);
        CacheUtils.putInt(MainActivity.this, CUSTOMS_NUM_ALL, 1);
        count = 1;
        isShuffle = true;
        integers.clear();
        isResetSecondDown = true;
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
