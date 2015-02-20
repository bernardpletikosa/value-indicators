package com.github.bernardpletikosa.indicators.pie;

import com.github.bernardpletikosa.indicators.consts.Direction;
import com.github.bernardpletikosa.indicators.consts.Orientation;

public class StartAngleUtil {


    public static int quarterPieAngle(Orientation orientation, Direction direction) {
        int startAngle = 0;
        switch (orientation) {
            case NORTH_EAST:
                startAngle = 270; break;
            case SOUTH_EAST:
                startAngle = 0; break;
            case SOUTH_WEST:
                startAngle = 90; break;
            case NORTH_WEST:
                startAngle = 180; break;
        }

        return direction == Direction.CLOCKWISE ? startAngle : (startAngle + 90) % 360;
    }

    public static int halfPieAngle(Orientation orientation, Direction direction) {
        int startAngle = 0;
        switch (orientation) {
            case SOUTH:
                startAngle = 0; break;
            case NORTH:
                startAngle = 180; break;
            case EAST:
                startAngle = 270; break;
            case WEST:
                startAngle = 90; break;
        }

        return direction == Direction.CLOCKWISE ? startAngle : (startAngle + 180) % 360;
    }
}
