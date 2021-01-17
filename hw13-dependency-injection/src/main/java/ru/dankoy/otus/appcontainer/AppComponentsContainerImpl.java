package ru.dankoy.otus.appcontainer;

import ru.dankoy.otus.appcontainer.api.AppComponent;
import ru.dankoy.otus.appcontainer.api.AppComponentsContainer;
import ru.dankoy.otus.appcontainer.api.AppComponentsContainerConfig;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class AppComponentsContainerImpl implements AppComponentsContainer {

    private final List<Object> appComponents = new ArrayList<>();
    private final Map<String, Object> appComponentsByName = new HashMap<>();

    public AppComponentsContainerImpl(Class<?> initialConfigClass) {
        processConfig(initialConfigClass);
    }

    private void processConfig(Class<?> configClass) {
        checkConfigClass(configClass);
        // You code here...
    }

    private void checkConfigClass(Class<?> configClass) {
        if (!configClass.isAnnotationPresent(AppComponentsContainerConfig.class)) {
            throw new IllegalArgumentException(String.format("Given class is not config %s", configClass.getName()));
        }
    }

    @Override
    public <C> C getAppComponent(Class<C> componentClass) {
        return null;
    }

    @Override
    public <C> C getAppComponent(String componentName) {
        return null;
    }

    //TODO: Сделаль метод приватным

    /**
     * Получает список методов с нужной аннотацией и сортирует их пополю order в аннотации @AppComponent
     *
     * @param clazz
     * @return
     */
    public List<Method> getMethods(Class<?> clazz) {

        final Method[] methods = clazz.getDeclaredMethods();

        return Arrays.stream(methods).filter(method -> method.isAnnotationPresent(AppComponent.class))
                .sorted(Comparator.comparing(method -> method.getAnnotation(AppComponent.class).order()))
                .collect(Collectors.toList());

    }


    /**
     * Получает объект типа класса передаваемого в аргументах
     *
     * @param clazz
     * @return
     */
    private Object initInstanceConstructor(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new AppComponentContainerException(
                    String.format("Failed to instantiate class %s", clazz.getCanonicalName()));
        }
    }


}
