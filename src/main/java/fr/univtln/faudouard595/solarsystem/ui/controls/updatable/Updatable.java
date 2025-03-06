package fr.univtln.faudouard595.solarsystem.ui.controls.updatable;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@SuperBuilder
@Slf4j
@ToString
@Setter
public abstract class Updatable {

    public static final List<Updatable> updatableList = new ArrayList<>();

    @Builder.Default
    private Runnable actionFunction = () -> {
    };
    @Builder.Default
    private int analogSpeed = 3;

    @Builder.Default
    private boolean isAnalog = false;

    @Builder.Default
    private boolean analogStarting = false;

    @Builder.Default
    private boolean firstUpdate = true;

    @Builder.Default
    private int currentSpeed = 0;
    @Builder.Default
    private Runnable updateFunction = () -> {
    };
    @Builder.Default
    private int waitTimeBeforeAnalog = 30;

    @Builder.Default
    private boolean isActivate = false;

    {
        updatableList.add(this);
    }

    public void setUpdateFunction(Runnable command) {
        updateFunction = command;
    }

    public void activateByBoolean(boolean b) {
        if (b) {
            activate();
        } else {
            deactivate();
        }
    }

    public void activate() {
        isActivate = true;
        currentSpeed = 0;
        analogStarting = true;
        firstUpdate = true;
    }

    public void deactivate() {
        isActivate = false;
    }

    public void update() {
        if (updateFunction != null) {
            updateFunction.run();
        }
        if (!isActivate) {
            return;
        }
        if (isAnalog) {
            if (currentSpeed >= analogSpeed && (!analogStarting || currentSpeed >= waitTimeBeforeAnalog)) {
                analogStarting = false;
                currentSpeed = 0;
                actionFunction.run();
            }
            currentSpeed++;
        }
        if (firstUpdate) {
            actionFunction.run();
            firstUpdate = false;
        }

    }

    public static void updateAll() {
        updatableList.forEach(Updatable::update);
    }

}
