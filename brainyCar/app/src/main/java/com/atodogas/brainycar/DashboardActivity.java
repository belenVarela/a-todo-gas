package com.atodogas.brainycar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.atodogas.brainycar.Services.Extra.DashboardDTO;
import com.atodogas.brainycar.Services.TrackingService;
import com.github.anastr.speedviewlib.TubeSpeedometer;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class DashboardActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = DashboardActivity.class.getSimpleName();

    private TextView temperaturaTextView, bateriaTextView, gasolinaTextView, kmRecorridosTextView,
            tiempoTranscurridoTextView;
    private TubeSpeedometer velocidadMeter, revolucionesMeter;

    private LocalBroadcastManager localBroadcastManager;
    private LinearLayout loadingLayout, dashboardDataLayout;
    private TextView dashboardLoadingTextView;

    private boolean isRunningServices;

    private static final int REQUEST_ENABLE_BT = 1;

    private static final int MY_PERMISSION_LOCATION_FINE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Setting action bar
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Dashboard");

        //Otener campos para rellenar en las actualizaciones
        temperaturaTextView = (TextView) findViewById(R.id.temperaturaTextView);
        bateriaTextView = (TextView) findViewById(R.id.bateriaTextView);
        gasolinaTextView = (TextView) findViewById(R.id.gasolinaTextView);
        kmRecorridosTextView = (TextView) findViewById(R.id.kmRecorridosTextView);
        tiempoTranscurridoTextView = findViewById(R.id.tiempoTranscurridoTextView);
        revolucionesMeter = (TubeSpeedometer) findViewById(R.id.rpmMeter);
        velocidadMeter = (TubeSpeedometer) findViewById(R.id.kmHourMeter);
        dashboardLoadingTextView = (TextView) findViewById(R.id.dashboardLoadingTextView);

        loadingLayout = findViewById(R.id.loadingLayout);
        loadingLayout.setVisibility(View.VISIBLE);
        dashboardDataLayout = findViewById(R.id.dashboardDataLayout);
        dashboardDataLayout.setVisibility(View.INVISIBLE);

        View detenerButton = findViewById(R.id.detenerButton);
        detenerButton.setOnClickListener(this);

        isRunningServices = getIntent().getBooleanExtra("isRunningServices", false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bluetoothConexion();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (localBroadcastManager != null){
            localBroadcastManager.unregisterReceiver(dashboardDTOReceive);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.detenerButton:
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                detener();
                break;
        }
    }

    public void detener() {
        Context context = getApplicationContext();
        CharSequence text = "clicked button Detener";
        int duration = Toast.LENGTH_SHORT;

        Log.d(TAG, "clicked button Detener");
        Toast.makeText(context, text, duration).show();

        stopTrackingService();
        openHome();
    }

    private void startTrackingService(){

        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        if(!isRunningServices){
            int idUser = getIntent().getIntExtra("idUser", - 1);
            String personName = getIntent().getStringExtra("personName");
            String personPhotoUrl = getIntent().getStringExtra("personPhotoUrl");

            Intent trackingServiceIntent = new Intent(this, TrackingService.class);
            trackingServiceIntent.putExtra("idUser", idUser);
            trackingServiceIntent.putExtra("personName", personName);
            trackingServiceIntent.putExtra("personPhotoUrl", personPhotoUrl);
            startService(trackingServiceIntent);
            isRunningServices = true;
        }

        //dashboardLoadingTextView.setText(getResources().getString(R.string.locationLoading));
        if (checkPermissionLocation()) {
            //TODO más precisión con la localización
        } else {
            //askPermission();
        }
    }

    private void stopTrackingService(){
        Intent trackingServiceIntent = new Intent(this, TrackingService.class);
        stopService(trackingServiceIntent);
    }

    private void openHome(){
        Intent intent = new Intent(this, MainActivity.class);
        int idUser = getIntent().getIntExtra("idUser", - 1);
        String personName = getIntent().getStringExtra("personName");
        String personPhotoUrl = getIntent().getStringExtra("personPhotoUrl");

        intent.putExtra("idUser", idUser);
        intent.putExtra("personName", personName);
        intent.putExtra("personPhotoUrl", personPhotoUrl);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // Check botón de atrás
    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Pulse el botón de Detener para salir de este modo", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    ///TODO Obtener los valores a través de bluetooth
    //Metodo para actualizar los valores de la pantalla
    private void updateDashboardInformation(DashboardDTO dashboardDTO) {
        NumberFormat formatter = new DecimalFormat("#0.0");

        if(dashboardDTO.temperature != -1){
            temperaturaTextView.setText(formatter.format(dashboardDTO.temperature) + " ºC");
        }

        if(dashboardDTO.battery != -1){
            bateriaTextView.setText(formatter.format(dashboardDTO.battery) + " V");
        }

        if(dashboardDTO.l100kmavg != -1){
            gasolinaTextView.setText(formatter.format(dashboardDTO.l100kmavg) + " L/100km");
        }
        else {
            gasolinaTextView.setText("-");
        }

        kmRecorridosTextView.setText(formatter.format(dashboardDTO.km) + " km");

        tiempoTranscurridoTextView.setText(dashboardDTO.hours + " h " + dashboardDTO.minutes + " m " + dashboardDTO.seconds + " s");


        if(dashboardDTO.rpm != -1){
            revolucionesMeter.speedTo((float)dashboardDTO.rpm/1000f);
        }

        if(dashboardDTO.speed != -1){
            velocidadMeter.speedTo(dashboardDTO.speed);
        }
    }

    private void bluetoothConexion() {
        //we see if the device has bluetooth
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            dashboardLoadingTextView.setText(getResources().getString(R.string.bluetoothLoading));
            // Device does support Bluetooth
            // We see if it is enabled and if not we enable it
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                dashboardLoadingTextView.setText(getResources().getString(R.string.obdLoading));
                startTrackingService();
                localBroadcastManager.registerReceiver(dashboardDTOReceive, new IntentFilter(TrackingService.DASHBOARD_DTO));
                localBroadcastManager.registerReceiver(dashboardDTOReceive, new IntentFilter(TrackingService.OBD_NOT_CONNECTED));
            }
        } else {
            Toast.makeText(this, getText(R.string.bluetoothUnable), Toast.LENGTH_LONG).show();
            openHome();

        }
    }

    // Results bluetooth request
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    dashboardLoadingTextView.setText(getResources().getString(R.string.obdLoading));
                    startTrackingService();
                    localBroadcastManager.registerReceiver(dashboardDTOReceive, new IntentFilter(TrackingService.DASHBOARD_DTO));
                    localBroadcastManager.registerReceiver(dashboardDTOReceive, new IntentFilter(TrackingService.OBD_NOT_CONNECTED));
                } else {
                    Toast.makeText(this, getText(R.string.bluetoothUnable), Toast.LENGTH_SHORT).show();
                    openHome();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private final BroadcastReceiver dashboardDTOReceive = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(TrackingService.DASHBOARD_DTO.equals(action)) {
                DashboardDTO dashboardDTO = intent.getParcelableExtra("DashboardDTO");
                updateDashboardInformation(dashboardDTO);

                if (loadingLayout.getVisibility() == View.VISIBLE) {
                    loadingLayout.setVisibility(View.INVISIBLE);
                    dashboardDataLayout.setVisibility(View.VISIBLE);
                }
            }
            else if (TrackingService.OBD_NOT_CONNECTED.equals(action)){
                CharSequence text = "OBD no conectado";
                int duration = Toast.LENGTH_SHORT;

                Log.d(TAG, "OBD no conectado");
                Toast.makeText(context, text, duration).show();
                openHome();
            }
        }
    };

    // Check for permission to access Location
    private boolean checkPermissionLocation() {
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    /*// Asks for permission
    @SuppressLint("NewApi")
    private void askPermission() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSION_LOCATION_FINE
        );
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_LOCATION_FINE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    if (checkPermissionLocation()) {
                        //TODO más precisión con la localización
                    }
                } else {
                    Toast.makeText(this, getText(R.string.locationPermissionUnableDashboard), Toast.LENGTH_SHORT).show();
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }*/
}
