package com.atodogas.brainycar.OBD;

import java.io.IOException;
import java.util.ArrayList;


public abstract class OBDAdapter {
    public abstract OBDDTO getRealTimeData() throws IOException, InterruptedException;
    public abstract String getFuelType() throws IOException, InterruptedException;
    public abstract ArrayList<String> getTroubleCodes() throws IOException, InterruptedException;
}
