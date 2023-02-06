package org.atinject.tck.auto;

import com.github.thorbenkuck.di.annotations.Wire;
import org.atinject.tck.auto.accessories.Cupholder;

@Wire
public class NonDriversSeat extends Seat {
    NonDriversSeat(Cupholder cupholder) {
        super(cupholder);
    }
}
