package com.weather.sweet.xww.colorfulweather.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.weather.sweet.xww.applibaray.app.AppConfiguration;
import com.weather.sweet.xww.applibaray.net.rest.RestClient;
import com.weather.sweet.xww.applibaray.utils.log.LogUtil;
import com.weather.sweet.xww.colorfulweather.R;
import com.weather.sweet.xww.colorfulweather.adapters.DailyAdapter;
import com.weather.sweet.xww.colorfulweather.adapters.HourlyAdapter;
import com.weather.sweet.xww.colorfulweather.base.BaseFragment;
import com.weather.sweet.xww.colorfulweather.entity.CitiesListEntity;
import com.weather.sweet.xww.colorfulweather.entity.DailyEntity;
import com.weather.sweet.xww.colorfulweather.entity.HourlyEntity;
import com.weather.sweet.xww.colorfulweather.event.RefreshWeatherDataEvent;
import com.weather.sweet.xww.colorfulweather.utils.DateUtil;
import com.weather.sweet.xww.colorfulweather.utils.Icon;
import com.weather.sweet.xww.colorfulweather.utils.OccupyView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import skin.support.SkinCompatManager;

/**
 * ?????????app?????????????????????
 * ???????????????????????????????????????
 *
 * @author : xww
 * @created at : 19-3-25
 * @time : ??????08:12
 */
public class FragmentWeather extends BaseFragment {

    @BindView(R.id.imgv_drawer_menu_icon)
    AppCompatImageView ivDrawerMenu;//?????????????????????

    @BindView(R.id.imgv_bottom_menu_icon)
    AppCompatImageView ivBottomMenu;//?????????????????????

    @BindView(R.id.linear_header)
    LinearLayout linearLayoutHeader;//?????????????????????????????????????????????

    @BindView(R.id.tv_now_weather)
    AppCompatTextView tvNowWeather;//??????

    @BindView(R.id.tv_now_temperature)
    AppCompatTextView tvNowTemperature;//????????????

    @BindView(R.id.tv_now_aqi)
    AppCompatTextView tvNowAqi;//????????????

    @BindView(R.id.tv_now_pm25)
    AppCompatTextView tvNowPM25;//pm2.5

    @BindView(R.id.imgv_now_weather_icon)
    AppCompatImageView imgvNowWeatherIcon;//????????????

    @BindView(R.id.tv_location)
    AppCompatTextView tvLocation;//??????

    @BindView(R.id.tv_hourly_card_title)
    AppCompatTextView tvHorelyCardTitle;//???????????????

    @BindView(R.id.tv_daily_card_title)
    AppCompatTextView tvDailyCardTitle;//???????????????

    @BindView(R.id.recycler_hourly_forecast)
    RecyclerView recyclerHourlyForecast;//?????????????????????

    @BindView(R.id.recycler_daily_forecast)
    RecyclerView recyclerDailyForecast;//??????????????????

    @BindView(R.id.imgv_card_weather_now_bg)
    AppCompatImageView imgvCardWeatherNowBg;

    // ??????????????????
    private OnDrawerMenuToggleListener onDrawerMenuToggleListener;

    public void setOnDrawerMenuToggleListener(OnDrawerMenuToggleListener onDrawerMenuToggleListener) {
        this.onDrawerMenuToggleListener = onDrawerMenuToggleListener;
    }

    @Override
    protected View setContentView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_weather, container, false);
    }

    @Override
    protected void setupView() {
        initView();
        initRecyclerView();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void setupData(@NonNull View view, @Nullable Bundle savedInstanceState) {

        final String bingPic = mSharedPreferences.getString("bing_pic", "null");

        if (!"null".equals(bingPic)) {
            setupBingPicture(bingPic);
        } else {
            RestClient.Builder()
                    .url(AppConfiguration.getInstance().getBingApi())
                    .success(response -> {
                        setupBingPicture(response);
                        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
                        editor.putString("bing_pic", response).apply();
                    })
                    .error((code, msg) -> {
                        Toast.makeText(mContext, "?????????????????????Error:" + code, Toast.LENGTH_SHORT).show();
                    })
                    .build()
                    .get();
        }

        final String weatherData = mSharedPreferences.getString("weather_data", "null");
        if (!"null".equals(weatherData)) {
            setupWeatherData(weatherData);
        }
    }

    private void initView() {
        //??????????????????
        linearLayoutHeader.addView(OccupyView.getOccupyStatusbarView(mContext), 0);
        /**
         * ????????????
         */
        tvNowWeather.setTypeface(mTypeface);
        tvLocation.setTypeface(mTypeface);
        tvHorelyCardTitle.setTypeface(mTypeface);
        tvDailyCardTitle.setTypeface(mTypeface);

        /**
         * drawer ???????????????????????? activity
         */
        ivDrawerMenu.setOnClickListener(v -> {
            if (onDrawerMenuToggleListener != null) {
                onDrawerMenuToggleListener.onClick(v);
            }
        });
        ivBottomMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSkinDialog();
            }
        });
    }

    private List<DailyEntity> mDailyEntities;
    private DailyAdapter mDailyAdapter;

    private List<HourlyEntity> mHourlyEntities;
    private HourlyAdapter mHourlyAdapter;

    private void initRecyclerView() {
        /**
         *  ????????????
         */
        mHourlyEntities = new ArrayList<>();
        mHourlyAdapter = new HourlyAdapter(R.layout.recy_hourly_forecast_item, mHourlyEntities);
        final LinearLayoutManager manager = new LinearLayoutManager(mContext);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerHourlyForecast.setLayoutManager(manager);
        recyclerHourlyForecast.setAdapter(mHourlyAdapter);

        /**
         *  ????????????
         */
        mDailyEntities = new ArrayList<>();
        mDailyAdapter = new DailyAdapter(R.layout.recy_daily_forecast_item, mDailyEntities);
        recyclerDailyForecast.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerDailyForecast.setAdapter(mDailyAdapter);
    }

    private void setupBingPicture(String response) {
        LogUtil.logD(response);
        Glide.with(mContext)
                .load(response)
                .into(imgvCardWeatherNowBg);
    }

    private String mCondCode = null;
    private String mWeather = null;
    private String mTemperature = null;
    private String mLocation = null;
    private String mWeatherId = null;

    @SuppressLint("SetTextI18n")
    private void setupWeatherData(String weatherData) {
        final JSONObject heWeather6 = (JSONObject) JSONObject.parse(weatherData);
        final JSONObject object = heWeather6.getJSONArray("HeWeather6").getJSONObject(0);

        //?????? update
        final JSONObject update = object.getJSONObject("update");
        final String loc[] = update.getString("loc").split(" ");
        tvNowPM25.setText(loc[1]);

        //?????? now
        final JSONObject now = object.getJSONObject("now");

        mCondCode = now.getString("cond_code");
        mWeather = now.getString("cond_txt");
        mTemperature = now.getString("tmp");
        mTemperature = mTemperature + "??C";

        //?????? basic
        final JSONObject basic = object.getJSONObject("basic");
        mLocation = basic.getString("location");
        mWeatherId = basic.getString("cid");

        tvLocation.setText(mLocation);
        tvNowTemperature.setText(mTemperature);
        imgvNowWeatherIcon.setImageDrawable(Icon.getIcon(mContext, mCondCode));
        tvNowWeather.setText(mWeather);

        // ???????????????????????????
        final CitiesListEntity entity = new CitiesListEntity(mLocation, mCondCode, mTemperature, mWeather, mWeatherId);
        updateToSQL(entity);

        //?????? daily_forecast
        final JSONArray dailyArray = object.getJSONArray("daily_forecast");
        final int dailySize = dailyArray.size();
        mDailyEntities.clear();
        for (int i = 0; i < dailySize; i++) {

            //?????????????????????
            if (i == 0) {
                final String tmp_max = dailyArray.getJSONObject(0).getString("tmp_max");
                final String tmp_min = dailyArray.getJSONObject(0).getString("tmp_min");

                final String todayTmpRange = tmp_min + " ~ " + tmp_max + "??C";
                tvNowAqi.setText(todayTmpRange);
            }

            final String tmp_max = dailyArray.getJSONObject(i).getString("tmp_max");
            final String tmp_min = dailyArray.getJSONObject(i).getString("tmp_min");
            final String tepRange = tmp_min + "??  ~ " + tmp_max + "??";

            final String condCode = dailyArray.getJSONObject(i).getString("cond_code_d");
            final String date = dailyArray.getJSONObject(i).getString("date").substring(5, 10);
            final String weather = dailyArray.getJSONObject(i).getString("cond_txt_d");

            mDailyEntities.add(new DailyEntity(condCode, date, DateUtil.getWeek(), tepRange, weather));
        }

        //?????? hourly_forecast
        final JSONArray hourlyArray = object.getJSONArray("hourly");
        final int hourlySize = dailyArray.size();
        mHourlyEntities.clear();

        for (int i = 0; i < hourlySize; i++) {
            final String[] times = hourlyArray.getJSONObject(i).getString("time").split(" ");
            final String cond_code = hourlyArray.getJSONObject(i).getString("cond_code");
            final String tmp = hourlyArray.getJSONObject(i).getString("tmp");
            mHourlyEntities.add(new HourlyEntity(times[1], cond_code, tmp + "??C"));
        }

        if (mDailyEntities.size() > 0) {
            mDailyAdapter.notifyDataSetChanged();
        }
        if (mHourlyEntities.size() > 0) {
            mHourlyAdapter.notifyDataSetChanged();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshWeatherData(RefreshWeatherDataEvent refreshEvent) {
        final String weatherData = refreshEvent.getWeatherData();
        setupWeatherData(weatherData);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void updateToSQL(CitiesListEntity entity) {
        final String cityName = entity.getCityName();
        final List<CitiesListEntity> entities = LitePal.where("cityName = ?", cityName).find(CitiesListEntity.class);
        if (entities.size() > 0) {
            ContentValues values = new ContentValues();
            values.put("condCode", entity.getCondCode());
            values.put("temperature", entity.getTemperature());
            values.put("weather", entity.getWeather());
            LitePal.updateAll(CitiesListEntity.class, values, "cityName = ?", cityName);
        }
    }


    private void showSkinDialog() {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_skin, null);
        TextView tvDialogTitle = view.findViewById(R.id.tv_skin_dialog_title);
        ImageView ivClose = view.findViewById(R.id.imgv_dialog_close);
        LinearLayout skinBlue = view.findViewById(R.id.ll_skin_blue);
        LinearLayout skinGreen = view.findViewById(R.id.ll_skin_green);
        LinearLayout skinPink = view.findViewById(R.id.ll_skin_pink);
        LinearLayout skinPurple = view.findViewById(R.id.ll_skin_purple);
        LinearLayout skinYellow = view.findViewById(R.id.ll_skin_yellow);
        LinearLayout skinBrown = view.findViewById(R.id.ll_skin_brown);
        LinearLayout skinGrey = view.findViewById(R.id.ll_skin_grey);
        LinearLayout skinBlack = view.findViewById(R.id.ll_skin_black);

        tvDialogTitle.setTypeface(mTypeface);

        final Dialog dialog = new Dialog(mContext);
        // ????????????????????????
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        dialog.show();

        ivClose.setOnClickListener(v -> dialog.cancel());

        // ??????????????????
        skinBlue.setOnClickListener(v -> SkinCompatManager.getInstance().restoreDefaultTheme());
        // ?????? assets/skins ????????????????????????skin??????????????????????????????????????????????????????
        skinGreen.setOnClickListener(v -> SkinCompatManager.getInstance()
                .loadSkin("green.skin", SkinCompatManager.SKIN_LOADER_STRATEGY_ASSETS));
        skinPink.setOnClickListener(v -> SkinCompatManager.getInstance()
                .loadSkin("pink.skin", SkinCompatManager.SKIN_LOADER_STRATEGY_ASSETS));
        skinPurple.setOnClickListener(v -> SkinCompatManager.getInstance()
                .loadSkin("purple.skin", SkinCompatManager.SKIN_LOADER_STRATEGY_ASSETS));
        skinYellow.setOnClickListener(v -> SkinCompatManager.getInstance()
                .loadSkin("yellow.skin", SkinCompatManager.SKIN_LOADER_STRATEGY_ASSETS));
        skinBrown.setOnClickListener(v -> SkinCompatManager.getInstance()
                .loadSkin("brown.skin", SkinCompatManager.SKIN_LOADER_STRATEGY_ASSETS));
        skinGrey.setOnClickListener(v -> SkinCompatManager.getInstance()
                .loadSkin("grey.skin", SkinCompatManager.SKIN_LOADER_STRATEGY_ASSETS));
        skinBlack.setOnClickListener(v -> SkinCompatManager.getInstance()
                .loadSkin("black.skin", SkinCompatManager.SKIN_LOADER_STRATEGY_ASSETS));
    }
}
