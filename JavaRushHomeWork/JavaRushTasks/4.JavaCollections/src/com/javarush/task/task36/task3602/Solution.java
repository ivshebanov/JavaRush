package com.javarush.task.task36.task3602;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

/*
Найти класс по описанию
*/
public class Solution {
    public static void main(String[] args) {
        System.out.println(getExpectedClass());
    }

    public static Class getExpectedClass() {
        Class[] classes = Collections.class.getDeclaredClasses();
        for (Class clas : classes) {
            if (Modifier.isPrivate(clas.getModifiers())) {
                if (Modifier.isStatic(clas.getModifiers())) {
                    if (List.class.isAssignableFrom(clas)) {
                        try {
                            Constructor constructor = clas.getDeclaredConstructor();
                            constructor.setAccessible(true);
                            List list = (List) constructor.newInstance();
                            list.get(0);
                        } catch (IndexOutOfBoundsException e) {
                            return clas;
                        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {

                        }
                    }
                }
            }
        }
        return null;
    }
}
