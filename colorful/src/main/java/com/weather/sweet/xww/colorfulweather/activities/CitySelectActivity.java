package com.weather.sweet.xww.colorfulweather.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.weather.sweet.xww.applibaray.adapt.ViewPageAdapt;
import com.weather.sweet.xww.applibaray.app.AppConfiguration;
import com.weather.sweet.xww.applibaray.net.rest.RestClient;
import com.weather.sweet.xww.colorfulweather.R;
import com.weather.sweet.xww.colorfulweather.adapters.CityAdapter;
import com.weather.sweet.xww.colorfulweather.adapters.CountyAdapter;
import com.weather.sweet.xww.colorfulweather.adapters.HotCitiesSelectAdapter;
import com.weather.sweet.xww.colorfulweather.adapters.ProvinceAdapter;
import com.weather.sweet.xww.colorfulweather.base.BaseActivity;
import com.weather.sweet.xww.colorfulweather.entity.CitiesListEntity;
import com.weather.sweet.xww.colorfulweather.entity.CityEntity;
import com.weather.sweet.xww.colorfulweather.entity.CountyEntity;
import com.weather.sweet.xww.colorfulweather.entity.HotCitiesEntity;
import com.weather.sweet.xww.colorfulweather.entity.ProvinceEntity;
import com.weather.sweet.xww.colorfulweather.utils.Color;
import com.weather.sweet.xww.colorfulweather.utils.Icon;
import com.weather.sweet.xww.colorfulweather.utils.OccupyView;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import skin.support.widget.SkinCompatTextView;

/**
 * ?????????
 *
 * @author : xww
 * @created at : 2019/4/12
 * @time : 12:18
 */
@SuppressLint("RestrictedApi,ResourceAsColor")
public class CitySelectActivity extends BaseActivity {

    @BindView(R.id.vp_city_selected)
    ViewPager vpCitySelected;

    @BindView(R.id.imgv_city_select_image)
    ImageView imgvCitySelectImage;

    @BindView(R.id.imgv_city_select_bg)
    ImageView imgvCitySelectBg;//????????????

    @BindView(R.id.imgv_city_select_close)
    ImageView imgvCitySelectClose;

    @BindView(R.id.fab_city_select_search)
    FloatingActionButton fabReturn;

    @BindView(R.id.fab_city_selected_confirm)
    FloatingActionButton fabConfirm;

    @BindView(R.id.tl_city_selected)
    TabLayout tlCitySelected;

    @BindView(R.id.linear_city_selected)
    LinearLayout linearCitySelected;

    @BindView(R.id.city_selected_name)
    TextView tvCitySelectedName;

    @BindView(R.id.tv_city_selected_sub_name)
    TextView tvCitySelecteSubdName;

    @BindView(R.id.tv_city_selected_now_weather)
    TextView tvCitySelectedWeather;

    @BindView(R.id.imgv_city_select_now_weather_icon)
    ImageView imgvCitySelectIcon;

    @BindView(R.id.tv_city_select_temperature)
    TextView tvCitySelectTemperature;

    private RecyclerView mRecyclerCitySearch;
    private RecyclerView mRecyclerHotCities;

    private int currentPosition = 0;//????????????????????????
    private int previousPosition = 0;//?????????????????????

    private List<HotCitiesEntity> hotCities;

    //?????????????????????
    private boolean isFirstStart = true;
    //??????????????????
    private boolean isAddedSuccessed = false;

    /**
     * ??????????????????
     */
    private List<ProvinceEntity> mProvinceEntities;
    private ProvinceAdapter mProvinceAdapter;
    /**
     * ??????????????????
     */
    private List<CityEntity> mCityEntities;
    private CityAdapter mCityAdapter;

    /**
     * ???????????????
     */
    private List<CountyEntity> mCountyEntities;
    private CountyAdapter mCountyAdapter;

    //????????????????????????
    private String mLevelState;


    /**
     * ??????????????????????????????
     */
    private String mCityName;
    private String mCondCode;
    private String mWeather;
    private String mTemperature;
    private String mWeatherId;


    @OnClick(R.id.imgv_city_select_close)
    void onClickClose() {
        this.finish();
    }

    private enum level {
        PROVINCE,
        CITY,
        COUNTY
    }


    @Override
    protected void setLayout(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_cityselect);
    }

    @Override
    protected void setupView() {
        super.setupView();
        initView();
    }


    private void initView() {
        linearCitySelected.addView(OccupyView.getOccupyStatusbarView(this), 0);
        tvCitySelectedName.setTypeface(mTypeface);
        mProvinceEntities = new ArrayList<>();

        final List<View> views = new ArrayList<>();
        final LayoutInflater inflater = LayoutInflater.from(this);
        final View hotCitiesView = inflater.inflate(R.layout.view_city_select_hot_cities, null);
        final View searchCitiesView = inflater.inflate(R.layout.view_city_select_search, null);
        views.add(hotCitiesView);
        views.add(searchCitiesView);
        final List<String> titles = new ArrayList<>();
        titles.add("????????????");
        titles.add("????????????");

        final ViewPageAdapt adapter = new ViewPageAdapt(views, titles);
        vpCitySelected.setAdapter(adapter);
        vpCitySelected.setOffscreenPageLimit(2);
        tlCitySelected.setSelectedTabIndicatorColor(R.color.colorAccent);
//        tlCitySelected.setTabTextColors(R.color.colorPrimary, R.color.colorAccent);
        tlCitySelected.setupWithViewPager(vpCitySelected);

        initHotCitiesRecyclerView(hotCitiesView);
        initCitySearchRecyclerView(searchCitiesView);


        fabReturn.setOnClickListener(v -> {
            switch (mLevelState) {
                case "PROVINCE":
                    break;
                case "CITY":
                    mRecyclerCitySearch.setAdapter(mProvinceAdapter);
                    mLevelState = level.PROVINCE.name();
                    fabReturn.setVisibility(View.INVISIBLE);
                    break;
                case "COUNTY":
                    mRecyclerCitySearch.setAdapter(mCityAdapter);
                    mLevelState = level.CITY.name();
                    break;
            }
        });

        fabConfirm.setOnClickListener(v -> {
            if (isAddedSuccessed) {
                saveToCitiesList(mCityName, mCondCode, mTemperature, mWeather, mWeatherId);

            } else {
                Toast.makeText(mContext, "???????????????????????????", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void initHotCitiesRecyclerView(View view) {
        mRecyclerHotCities = view.findViewById(R.id.recycler_cities_select);

        hotCities = new ArrayList<>();
        hotCities.add(new HotCitiesEntity("??????", "CN101010100"));
        hotCities.add(new HotCitiesEntity("??????", "CN101020100"));
        hotCities.add(new HotCitiesEntity("??????", "CN101190101"));
        hotCities.add(new HotCitiesEntity("??????", "CN101040100"));
        hotCities.add(new HotCitiesEntity("??????", "CN101270101"));
        hotCities.add(new HotCitiesEntity("??????", "CN101070201"));
        hotCities.add(new HotCitiesEntity("??????", "CN101210101"));
        hotCities.add(new HotCitiesEntity("??????", "CN101030100"));
        hotCities.add(new HotCitiesEntity("??????", "CN101230201"));
        hotCities.add(new HotCitiesEntity("??????", "CN101250101"));
        hotCities.add(new HotCitiesEntity("??????", "CN101200101"));
        hotCities.add(new HotCitiesEntity("??????", "CN101280101"));

        final GridLayoutManager manager = new GridLayoutManager(this, 4);
        mRecyclerHotCities.setLayoutManager(manager);
        final HotCitiesSelectAdapter hotCitiesSelectAdapter = new HotCitiesSelectAdapter(R.layout.recy_cities_select_item, hotCities);
        mRecyclerHotCities.setAdapter(hotCitiesSelectAdapter);

        hotCitiesSelectAdapter.setOnItemClickListener((adapter, itemView, position) -> setHotCitySelected(position));
    }


    private void setHotCitySelected(int position) {
        final String hotCityName = hotCities.get(position).getHotCityName();
        final String hotCityWeatherId = hotCities.get(position).getHotCityWeatherId();
        tvCitySelectedName.setText(hotCityName);
        tvCitySelecteSubdName.setText(hotCityName);

        //???????????????
        mCityName = hotCityName;

        currentPosition = position;

        final View currentView = mRecyclerHotCities.getChildAt(currentPosition).findViewById(R.id.view_city_selected);
        final TextView currentText = mRecyclerHotCities.getChildAt(currentPosition).findViewById(R.id.tv_city_select_name);
        final View previousView = mRecyclerHotCities.getChildAt(previousPosition).findViewById(R.id.view_city_selected);
        final TextView previousText = mRecyclerHotCities.getChildAt(previousPosition).findViewById(R.id.tv_city_select_name);

        if (currentPosition != previousPosition) {
            //??????????????????????????????
//            setCurrentSelectedColor(currentView, currentText);

            //?????????????????????????????????
//            restorePreviousColor(previousView, previousText);

            //??????????????????
            requestNowWeatherApi(hotCityWeatherId);
        } else {//??????????????????????????????????????????
            /**
             * ????????????????????? activity ???
             * ???????????????????????? Item
             * ?????? previousPosition == currentPosition
             * ??????????????????????????????????????????????????????
             */
            if (isFirstStart) {
//                setCurrentSelectedColor(currentView, currentText);

                requestNowWeatherApi(hotCityWeatherId);
                isFirstStart = false;
            }
        }
        previousPosition = currentPosition;
    }


//    private void setCurrentSelectedColor(View currentView, TextView currentText) {
//        currentView.setBackgroundColor(R.color.colorAccent);
//        currentText.setTextColor(R.color.colorAccent);
//    }
//
//    private void restorePreviousColor(View previousView, TextView previousText) {
//        previousView.setBackgroundColor(R.color.colorNormal);
//        previousText.setTextColor(R.color.colorNormal);
//    }


    /**
     * ?????????recyclerview???????????????????????????
     */
    private void initCitySearchRecyclerView(View view) {
        //recyclerview ????????????????????????
        mLevelState = level.PROVINCE.name();
        hideReturnFabutton();

        mRecyclerCitySearch = view.findViewById(R.id.recycler_cities_search);
        mRecyclerCitySearch.setLayoutManager(new LinearLayoutManager(this));
        mProvinceEntities = getProvinceEntitiesList();
        mProvinceAdapter = new ProvinceAdapter(R.layout.recy_city_select_search_item, mProvinceEntities);
        mRecyclerCitySearch.setAdapter(mProvinceAdapter);

        mProvinceAdapter.setOnItemClickListener((adapter, view1, position) -> {
            initCitiesRecyclerView(position);
        });
    }

    private List<ProvinceEntity> getProvinceEntitiesList() {
        final List<ProvinceEntity> provinceEntities = LitePal.findAll(ProvinceEntity.class);

        if (provinceEntities.size() > 0) {
            return provinceEntities;
        } else {
            RestClient.Builder()
                    .url(AppConfiguration.getInstance().getProvinceApi())
                    .success(response -> {
                        provinceEntities.clear();
                        JSONArray jsonArray = JSONArray.parseArray(response);

                        for (int i = 0; i < jsonArray.size(); i++) {
                            JSONObject entity = jsonArray.getJSONObject(i);
                            final int provinceId = entity.getInteger("id");
                            final String provinceName = entity.getString("name");
                            final ProvinceEntity provinceEntity = new ProvinceEntity(provinceId, provinceName);
                            provinceEntity.save();
                            provinceEntities.add(provinceEntity);
                        }

                        if (provinceEntities.size() > 0) {
                            mProvinceAdapter.notifyDataSetChanged();
                            mProvinceEntities = provinceEntities;
                        }
                    })
                    .error((code, msg) -> {
                        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                    })
                    .build()
                    .get();
        }
        return provinceEntities;
    }


    /**
     * ?????????recyclerview?????????????????????????????????????????????????????????
     */
    private void initCitiesRecyclerView(int position) {
        //recyclerview ????????????????????????
        mLevelState = level.CITY.name();
        showReturnFabutton();

        final int provinceId = mProvinceEntities.get(position).getProvinceId();
        final String city_api = AppConfiguration.getInstance().getProvinceApi() + "/" + provinceId;
        mCityEntities = getCityEntitiesList(city_api, provinceId);
        mCityAdapter = new CityAdapter(R.layout.recy_city_select_search_item, mCityEntities);
        mRecyclerCitySearch.setAdapter(mCityAdapter);

        mCityAdapter.setOnItemClickListener((adapter, view, pos) -> {
            initCountyRecyclerView(pos);
        });
    }


    private List<CityEntity> getCityEntitiesList(String cityApi, int provinceId) {
        final List<CityEntity> cityEntities = LitePal.where("provinceId = ?", String.valueOf(provinceId)).find(CityEntity.class);

        if (cityEntities.size() > 0) {
            return cityEntities;
        } else {
            RestClient.Builder()
                    .url(cityApi)
                    .success(response -> {
                        cityEntities.clear();
                        JSONArray jsonArray = JSONArray.parseArray(response);

                        for (int i = 0; i < jsonArray.size(); i++) {
                            JSONObject entity = jsonArray.getJSONObject(i);
                            final int cityId = entity.getInteger("id");
                            final String cityName = entity.getString("name");
                            final CityEntity cityEntity = new CityEntity(cityId, cityName, provinceId);
                            cityEntity.save();
                            cityEntities.add(cityEntity);
                        }

                        if (cityEntities.size() > 0) {
                            mCityAdapter.notifyDataSetChanged();
                            mCityEntities = cityEntities;
                        }
                    })
                    .error((code, msg) -> {
                        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                    })
                    .build()
                    .get();
        }
        return cityEntities;
    }


    /**
     * ?????????recyclerview??????????????????????????????????????????????????????
     */
    private void initCountyRecyclerView(int position) {
        //recyclerview ????????????????????????
        mLevelState = level.COUNTY.name();
        showReturnFabutton();

        final int provinceId = mCityEntities.get(position).getProvinceId();
        final int cityId = mCityEntities.get(position).getCityId();
        final String county_api = AppConfiguration.getInstance().getProvinceApi() + "/" + provinceId + "/" + cityId;
        mCountyEntities = getCountyEntitiesList(county_api, cityId);
        mCountyAdapter = new CountyAdapter(R.layout.recy_city_select_search_item, mCountyEntities);
        mRecyclerCitySearch.setAdapter(mCountyAdapter);

        mCountyAdapter.setOnItemClickListener((adapter, view, pos) -> setSelectedCounty(pos));
    }

    private List<CountyEntity> getCountyEntitiesList(String countyApi, int cityId) {
        final List<CountyEntity> countyEntities = LitePal.where("cityId = ?", String.valueOf(cityId)).find(CountyEntity.class);

        if (countyEntities.size() > 0) {
            return countyEntities;
        } else {
            RestClient.Builder()
                    .url(countyApi)
                    .success(response -> {
                        countyEntities.clear();
                        JSONArray jsonArray = JSONArray.parseArray(response);

                        for (int i = 0; i < jsonArray.size(); i++) {
                            JSONObject entity = jsonArray.getJSONObject(i);
                            final int countyId = entity.getInteger("id");
                            final String countyName = entity.getString("name");
                            final String weatherId = entity.getString("weather_id");
                            final CountyEntity countyEntity = new CountyEntity(countyId, countyName, weatherId, cityId);
                            countyEntity.save();
                            countyEntities.add(countyEntity);
                        }

                        if (countyEntities.size() > 0) {
                            mCountyAdapter.notifyDataSetChanged();
                            mCountyEntities = countyEntities;
                        }
                    })
                    .error((code, msg) -> {
                        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                    })
                    .build()
                    .get();
        }
        return countyEntities;
    }


    @Override
    protected void setupData() {
        super.setupData();
        //??????????????????
        setupBackGround();
    }

    private void setupBackGround() {
        String bingPic = mSharedPreferences.getString("bing_pic", "null");
        if (!"null".equals(bingPic)) {
            Glide.with(this)
                    .load(bingPic)
                    .into(imgvCitySelectImage);
//            Glide.with(this)
//                    .load(bingPic)
//                    .into(imgvCitySelectBg);
        }
    }

    private void showReturnFabutton() {
        if (fabReturn.getVisibility() == View.INVISIBLE) {
            final Animation alpha = new AlphaAnimation(0, 1f);
            alpha.setDuration(500);
            fabReturn.startAnimation(alpha);
        }
        //??????????????????
        fabReturn.setVisibility(View.VISIBLE);
    }

    private void hideReturnFabutton() {
        //??????????????????
        fabReturn.setVisibility(View.INVISIBLE);
    }


    private void setSelectedCounty(int position) {
        final String weatherId = mCountyEntities.get(position).getWeatherId();
        final String countyName = mCountyEntities.get(position).getCountyName();
        tvCitySelectedName.setText(countyName);
        tvCitySelecteSubdName.setText(countyName);

        //????????????
        mCityName = countyName;
        requestNowWeatherApi(weatherId);
    }

    private void requestNowWeatherApi(String weatherId) {
        //????????????ID
        mWeatherId = weatherId;
        final StringBuilder url = new StringBuilder()
                .append(AppConfiguration.getInstance().getApiHost())
                .append("/weather/now?key=")
                .append(AppConfiguration.getInstance().getApiKey())
                .append("&&location=")
                .append(weatherId);

        RestClient.Builder()
                .url(url.toString())
                .success(response -> {
                    setupWeatherData(response);
                })
                .error((code, msg) -> {
                    Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                })
                .build()
                .get();
    }


    private void setupWeatherData(String response) {
        final JSONObject heWeather6 = (JSONObject) JSONObject.parse(response);
        final JSONObject object = heWeather6.getJSONArray("HeWeather6").getJSONObject(0);
        final String status = object.getString("status");

        switch (status) {
            case "ok":
                final JSONObject now = object.getJSONObject("now");

                mCondCode = now.getString("cond_code");
                mWeather = now.getString("cond_txt");
                mTemperature = now.getString("tmp");
                mTemperature = mTemperature + "??C";

                imgvCitySelectIcon.setVisibility(View.VISIBLE);
                imgvCitySelectIcon.setImageDrawable(Icon.getIcon(this, mCondCode));
                tvCitySelectedWeather.setText(mWeather);
                tvCitySelectTemperature.setText(mTemperature);

                isAddedSuccessed = true;
                break;
            case "unknown location":
                Toast.makeText(mContext, "????????????", Toast.LENGTH_SHORT).show();
                clearWeatherData();
                isAddedSuccessed = false;
                break;
            case "no more requests":
                Toast.makeText(mContext, "??????????????????", Toast.LENGTH_SHORT).show();
                clearWeatherData();
                isAddedSuccessed = false;
                break;
        }
    }

    private void clearWeatherData() {
        imgvCitySelectIcon.setVisibility(View.GONE);
        tvCitySelectedWeather.setText("");
    }

    private void saveToCitiesList(String cityName, String cond_code, String temperature, String weather, String weatherId) {
        final List<CitiesListEntity> entity = LitePal.where("cityName = ?", cityName).find(CitiesListEntity.class);
        if (entity.size() > 0) {
            Toast.makeText(mContext, "???????????????", Toast.LENGTH_SHORT).show();
        } else {
            CitiesListEntity citiesListEntity = new CitiesListEntity(cityName, cond_code, temperature, weather, weatherId);
            citiesListEntity.save();
            finishThisAndReturnResult();
        }
    }

    private void finishThisAndReturnResult() {
        Intent intent = new Intent();
        intent.putExtra("changed", true);
        setResult(RESULT_OK, intent);
        finish();
    }
}
