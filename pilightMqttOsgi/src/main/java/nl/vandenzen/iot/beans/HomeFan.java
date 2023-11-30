package nl.vandenzen.iot.beans;

public class HomeFan {
    public int gettimerToiletMinutes() {
        return timerToiletMinutes;
    }

    public void settimerToiletMinutes(int timerToiletMinutes) {
        this.timerToiletMinutes = timerToiletMinutes;
    }

    public int gettimerHotwaterMinutes() {
        return timerHotwaterMinutes;
    }

    public void settimerHotwaterMinutes(int timerHotwaterMinutes) {
        this.timerHotwaterMinutes = timerHotwaterMinutes;
    }

    public int getRuntimeFanMinutes() {
        return runtimeFanMinutes;
    }

    public void setRuntimeFanMinutes(int runtimeFanMinutes) {
        this.runtimeFanMinutes = runtimeFanMinutes;
    }

    public int getCommandStart1() {
        return commandStart1;
    }

    public void setCommandStart1(int commandStart1) {
        this.commandStart1 = commandStart1;
    }

    public int getCommandStart2() {
        return commandStart2;
    }

    public void setCommandStart2(int commandStart2) {
        this.commandStart2 = commandStart2;
    }

    public int getCommandStart3() {
        return commandStart3;
    }

    public void setCommandStart3(int commandStart3) {
        this.commandStart3 = commandStart3;
    }

    public int getCommandStop() {
        return commandStop;
    }

    public void setCommandStop(int commandStop) {
        this.commandStop = commandStop;
    }

    public int getTimerToiletMinutes() {
        return timerToiletMinutes;
    }

    public void setTimerToiletMinutes(int timerToiletMinutes) {
        this.timerToiletMinutes = timerToiletMinutes;
    }

    public int getTimerHotwaterMinutes() {
        return timerHotwaterMinutes;
    }

    public void setTimerHotwaterMinutes(int timerHotwaterMinutes) {
        this.timerHotwaterMinutes = timerHotwaterMinutes;
    }

    public float getHotWaterTemperatureThreshold() {
        return hotWaterTemperatureThreshold;
    }

    public void setHotWaterTemperatureThreshold(float hotWaterTemperatureThreshold) {
        this.hotWaterTemperatureThreshold = hotWaterTemperatureThreshold;
    }

    public float getHotWaterTemperatureDifference() {
        return hotWaterTemperatureDifference;
    }

    public void setHotWaterTemperatureDifference(float hotWaterTemperatureDifference) {
        this.hotWaterTemperatureDifference = hotWaterTemperatureDifference;
    }
    int timerToiletMinutes=16;
    int timerHotwaterMinutes=20;
    int runtimeFanMinutes=8; // if Timer1 command is issued, fan will run for approx. 10 minutes. So restart it before this time
    int commandStart1=4; // First command to send to start fan
    int commandStart2=8; // Second command, 8=Timer1 (10 minutes), 9=Timer2 (20 minutes), 10=Timer3 (30 minutes)
    int commandStart3=0; // not needed
    int commandStop=4;
    float hotWaterTemperatureThreshold=45.1f; // degrees centigrade, above fan will run
    float hotWaterTemperatureDifference=15.0f; // difference between hot and cold water, above fan will run
}
