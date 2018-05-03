package com.atodogas.brainycar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by belenvb on 03/05/2018.
 */

public class OnboardingActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    private Button btnSkip, btnNext;
    private PrefManager prefManager;
    private String carBrand;
    private Integer carBrandPosition;
    private String brandModel;
    private Integer brandModelPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Checking for first time launch - before calling setContentView()
        prefManager = new PrefManager(this);
        if (!prefManager.isFirstTimeLaunch()) {
            launchHomeScreen();
            finish();
        }

        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.view_pager);
        dotsLayout = findViewById(R.id.layoutDots);
        btnSkip = findViewById(R.id.btn_skip);
        btnNext = findViewById(R.id.btn_next);
        btnSkip.setVisibility(View.GONE);

        // layouts of all onboarding sliders
        layouts = new int[]{
                R.layout.fragment_onboarding_slide1,
                R.layout.fragment_onboarding_slide2,
                R.layout.fragment_onboarding_slide3};

        // adding bottom dots
        addBottomDots(0);

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchHomeScreen();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checking for last page
                // if last page home screen will be launched
                int current = getItem(+1);
                if (current < layouts.length) {
                    // move to next screen
                    viewPager.setCurrentItem(current);
                } else {
                    launchHomeScreen();
                }
            }
        });
    }

    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    private void launchHomeScreen() {
        prefManager.setFirstTimeLaunch(false);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("idUser", getIntent().getIntExtra("idUser", - 1));
        finish();
    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);

            switch (position) {
                case 0:
                    btnNext.setText(getString(R.string.next));
                    btnSkip.setVisibility(View.GONE);
                    break;
                case 1:
                    btnNext.setText(getString(R.string.next));
                    btnSkip.setVisibility(View.VISIBLE);

                    String carBrands[] = {"Brand1", "Brand2", "Brand3", "Brand4", "Brand5", "Brand6", "Brand7", "Brand8"};
                    ListView carBrandsListView = findViewById(R.id.carBrandsListView);
                    carBrandsListView.setAdapter(new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, carBrands));
                    if (carBrandPosition != null) {
                        carBrandsListView.setItemChecked(carBrandPosition,true);
                    }
                    carBrandsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            // Get the selected item text from ListView
                            carBrandPosition = position;
                            carBrand = (String) parent.getItemAtPosition(position);

                            CharSequence text = "clicked on " + carBrand;
                            int duration = Toast.LENGTH_SHORT;
                            Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                            toast.show();
                        }
                    });
                    break;
                case 2:
                    btnNext.setText(getString(R.string.end));
                    btnSkip.setVisibility(View.GONE);

                    String brandModels[] = {"Model1", "Model2", "Model3", "Model4", "Model5", "Model6"};
                    ListView brandModelsListView = findViewById(R.id.brandModelsListView);
                    brandModelsListView.setAdapter(new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, brandModels));
                    if (brandModelPosition != null) {
                        brandModelsListView.setItemChecked(brandModelPosition,true);
                    }
                    brandModelsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            // Get the selected item text from ListView
                            brandModelPosition = position;
                            brandModel = (String) parent.getItemAtPosition(position);

                            CharSequence text = "clicked on " + brandModel;
                            int duration = Toast.LENGTH_SHORT;
                            Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                            toast.show();
                        }
                    });
                    break;
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}
