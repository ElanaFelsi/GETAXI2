package model.backend;

public final class BackendFactory {
    static Backend instance = null;

    public static final Backend getInstance() {

        if (instance == null)
            instance = new model.datasource.FireBaseDataBase();
        return instance;
    }
}