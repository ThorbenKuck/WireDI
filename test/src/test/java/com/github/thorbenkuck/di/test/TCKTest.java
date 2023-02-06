package com.github.thorbenkuck.di.test;

import com.github.thorbenkuck.di.domain.WireConflictStrategy;
import com.github.thorbenkuck.di.runtime.WireRepository;
import org.atinject.tck.Tck;
import org.atinject.tck.auto.Car;
import org.junit.jupiter.api.Test;

public class TCKTest {

    @Test
    public void test() {
        WireRepository repository = WireRepository.open();
        repository.configuration().setWireConflictStrategy(WireConflictStrategy.BEST_MATCH);

        Tck.testFor(repository.get(Car.class), true, true);
    }

}
