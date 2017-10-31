package com.javarush.task.task27.task2712.kitchen;

import com.javarush.task.task27.task2712.ConsoleHelper;

import java.util.Observable;
import java.util.Observer;

public class Cook extends Observable implements Observer {
    private String name;

    public Cook(String cookName) {
        this.name = cookName;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void update(Observable tablet, Object order) {
        ConsoleHelper.writeMessage("Start cooking - " + order);
        setChanged();
        notifyObservers(order);
    }
}
