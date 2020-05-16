package com.standard.app.card;

public interface ICardCheckListener {

    public void magCardSuccess();
    public void icCardSuccess();
    public void rfCardSuccess();
    public void onError();
    public void onFinish();
}