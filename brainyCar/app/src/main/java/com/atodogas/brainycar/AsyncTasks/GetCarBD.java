package com.atodogas.brainycar.AsyncTasks;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;

import com.atodogas.brainycar.DTOs.CarDTO;
import com.atodogas.brainycar.Database.AppDatabase;
import com.atodogas.brainycar.Database.Entities.CarEntity;
import com.atodogas.brainycar.Database.Entities.TripDataEntity;
import com.atodogas.brainycar.Database.Entities.TripEntity;

import java.util.List;

public class GetCarBD extends AsyncTask<Integer, Void, CarDTO> {
    CallbackInterface callback;
    AppDatabase db;

    public GetCarBD(CallbackInterface callback, Context context) {
        this.callback = callback;
        this.db = AppDatabase.getInstance(context);
    }

    @Override
    protected CarDTO doInBackground(Integer... integers) {
        int userId = integers[0];

        CarEntity carEntity = db.carDao().getCarByIdUser(userId);
        TripEntity tripEntity = db.tripDao().getLastTrip(carEntity.getId());
        List<TripDataEntity> tripDataEntities = null;

        if(tripEntity != null){
            tripDataEntities = db.tripDataDao().getAllTripData(tripEntity.getId());
        }

        CarDTO carDTO  = new CarDTO();
        carDTO.setId(carEntity.getId());
        carDTO.setModel(carEntity.getModel());
        carDTO.setFuelType(carEntity.getFuelType());
        carDTO.setKms(carEntity.getKms());
        carDTO.setFuelConsumptionAVG(carEntity.getAVGFuelConsumption());
        carDTO.setSpeedAVG(carEntity.getAVGSpeed());

        if(tripDataEntities != null && tripDataEntities.size() > 0){
            carDTO.setFuelTankLevel(tripDataEntities.get(tripDataEntities.size() - 1).getFuelTankLevel());
            carDTO.setLatitude(tripDataEntities.get(tripDataEntities.size() - 1).getLatitude());
            carDTO.setLongitude(tripDataEntities.get(tripDataEntities.size() - 1).getLongitude());
            for (int i = tripDataEntities.size() - 1; i >= 0; i--){
                if(tripDataEntities.get(i).getBattery() > 1){
                    carDTO.setBattery(tripDataEntities.get(i).getBattery());
                    break;
                }
            }
        }
        else {
            carDTO.setFuelTankLevel(-1);
            carDTO.setLatitude(999);
            carDTO.setLongitude(999);
        }

        return carDTO;
    }

    @Override
    protected void onPostExecute(CarDTO carDTO) {
        super.onPostExecute(carDTO);
        callback.doCallback(carDTO);
    }
}
