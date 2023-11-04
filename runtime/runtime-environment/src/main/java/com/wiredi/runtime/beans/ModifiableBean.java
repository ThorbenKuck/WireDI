package com.wiredi.runtime.beans;

import com.wiredi.domain.provider.IdentifiableProvider;
import com.wiredi.domain.provider.TypeIdentifier;
import com.wiredi.qualifier.QualifierType;
import com.wiredi.runtime.exceptions.DiLoadingException;
import com.wiredi.runtime.exceptions.MultiplePrimaryProvidersRegisteredException;
import com.wiredi.runtime.exceptions.MultipleSameQualifierProviderRegisteredExceptions;

import java.util.List;

public class ModifiableBean<T> extends AbstractBean<T> {

    public static final ModifiableBean<?> EMPTY = new ModifiableBean<>(TypeIdentifier.of(Void.class));

    public ModifiableBean(TypeIdentifier<T> typeIdentifier) {
        super(typeIdentifier);
    }

    public static <T> ModifiableBean<T> empty() {
        return (ModifiableBean<T>) EMPTY;
    }

    /**
     * Automatically registers the {@link IdentifiableProvider}.
     * <p>
     * This method will try to set it as primary, if {@link IdentifiableProvider#primary()} signals that the provider
     * is requesting to be primary.
     * Additionally, this method then sets the provider either as qualified or unqualified.
     *
     * @param identifiableProvider the provider to register
     */
    public void register(TypeIdentifier<T> concreteType, IdentifiableProvider<T> identifiableProvider) {
        if (identifiableProvider.qualifiers().isEmpty()) {
            if (identifiableProvider.primary()) {
                registerPrimaryProvider(concreteType, identifiableProvider);
            } else {
                addUnqualifiedProvider(concreteType, identifiableProvider);
            }
        } else {
            addQualifiedProvider(identifiableProvider, identifiableProvider.qualifiers());

            if (identifiableProvider.primary()) {
                registerPrimaryProvider(concreteType, identifiableProvider);
            }
        }
    }

    /**
     * Sets the provided {@link IdentifiableProvider} as the primary identifiable provider.
     * <p>
     * This method will bypass the check, whether the provided {@link IdentifiableProvider} is primary or not and hard
     * sets the provider as primary.
     * It still validates that no other primary identifiable provider is set.
     *
     * @param identifiableProvider the provider to set as the primary
     * @throws DiLoadingException if the Bean already has a primary {@link IdentifiableProvider}
     */
    public void registerPrimaryProvider(TypeIdentifier<T> concreteType, IdentifiableProvider<T> identifiableProvider) {
        if (concreteType.willErase()) {
            typedUnqualifiedProviders.computeIfAbsent(concreteType, t -> new TypedProviderState<>(identifiableProvider, concreteType)).trySetAsPrimary(identifiableProvider);
        } else {
            if (primary != null) {
                throw new MultiplePrimaryProvidersRegisteredException(concreteType, primary, identifiableProvider);
            }

            primary = identifiableProvider;
        }
    }

    /**
     * Adds the provided {@link IdentifiableProvider} as an unqualified provider.
     * <p>
     * This method will bypass any checks and just add this provider as an unqualified one.
     *
     * @param identifiableProvider the provider to set as the primary
     */
    public void addUnqualifiedProvider(TypeIdentifier<T> typeIdentifier, IdentifiableProvider<T> identifiableProvider) {
        if (typeIdentifier.willErase()) {
            typedUnqualifiedProviders.computeIfAbsent(typeIdentifier, t -> new TypedProviderState<>(identifiableProvider, typeIdentifier));
        } else {
            unqualifiedProviders.add(identifiableProvider);
        }
    }

    /**
     * Sets the provided {@link IdentifiableProvider} as a qualified bean for all {@link QualifierType qualifiers}.
     * <p>
     * This method will bypass the check, whether the provided {@link IdentifiableProvider} is normally qualified and
     * matching the qualifiers under {@link IdentifiableProvider#qualifiers()}
     * It still validates that no other primary identifiable provider is registered for the qualifier.
     *
     * @param newProvider the provider to set as the primary
     * @param qualifiers  the qualifiers which the provider should be registered to
     * @throws DiLoadingException if the Bean already has a primary {@link IdentifiableProvider}
     */
    public void addQualifiedProvider(IdentifiableProvider<T> newProvider, List<QualifierType> qualifiers) {
        for (QualifierType qualifier : qualifiers) {
            IdentifiableProvider<T> existingProvider = qualifiedProviders.get(qualifier);
            if (existingProvider != null) {
                throw new MultipleSameQualifierProviderRegisteredExceptions(qualifier, newProvider, existingProvider);
            }

            qualifiedProviders.put(qualifier, newProvider);
        }
    }
}
