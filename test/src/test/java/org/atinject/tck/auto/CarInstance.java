package org.atinject.tck.auto;

import com.github.thorbenkuck.di.annotations.Wire;
import jakarta.inject.Provider;

@Wire
public class CarInstance extends Convertible {
    public CarInstance(
            Seat plainSeat,
            Seat driversSeat,
            Tire plainTire,
            Tire spareTire,
            Provider<Seat> plainSeatProvider,
            Provider<Seat> driversSeatProvider,
            Provider<Tire> plainTireProvider,
            Provider<Tire> spareTireProvider
    ) {
        super(plainSeat, driversSeat, plainTire, spareTire, plainSeatProvider, driversSeatProvider, plainTireProvider, spareTireProvider);
    }
}
