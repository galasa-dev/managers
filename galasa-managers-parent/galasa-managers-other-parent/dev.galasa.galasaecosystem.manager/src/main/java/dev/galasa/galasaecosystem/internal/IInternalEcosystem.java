package dev.galasa.galasaecosystem.internal;

import dev.galasa.galasaecosystem.GalasaEcosystemManagerException;

public interface IInternalEcosystem {

    Object getTag();

    void build() throws GalasaEcosystemManagerException;

    void stop();

    void discard();

}
