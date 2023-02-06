package org.atinject.tck.auto;

import com.github.thorbenkuck.di.annotations.Wire;
import org.atinject.tck.auto.accessories.Cupholder;

@Drivers
@Wire
public class DriversSeat extends Seat {
    DriversSeat(Cupholder cupholder) {
        super(cupholder);
    }
}
