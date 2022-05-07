package com.github.thorbenkuck.di.test.inner;

import com.github.thorbenkuck.di.runtime.WireRepository;
import com.github.thorbenkuck.di.annotations.Wire;
import com.github.thorbenkuck.di.test.IDependency;
import com.github.thorbenkuck.di.test.NonExisting;
import com.github.thorbenkuck.di.test.PrintParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@Wire
public class SuperDi {

    @Inject
    private IDependency iDependency;

    @Inject
    private IDependency iDependency2;

    @Inject
    private WireRepository wireRepository;

    @Inject
    @Nullable
    private NonExisting nonExisting;

	@Inject
    SuperDi(@NotNull final IDependency iDependency) {
        System.out.println("[CONSTRUCTOR   |SuperDi   ] instantiated with Dependency " + iDependency.id());
    }

    public void foo() {
        System.out.println("\n#####");
        System.out.println("Dependency 1 = " + iDependency.id());
        System.out.println("Dependency 2 = " + iDependency2.id());
        System.out.println("NonExisting = " + nonExisting);
        System.out.println("Repository = " + wireRepository);
        System.out.println("#####\n");
    }

    @PostConstruct
    private void constructed() {
        System.out.println("[POST_CONSTRUCT|SuperDi   ] called");
    }

    @PrintParameter
    public void print(String message) {
         System.out.println("[SuperDi]: " + message);
    }

    @PrintParameter
    public String printAndReturn(String message) {
         print(message);
         return message;
    }
}
