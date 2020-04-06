package edu.stanford.bmir.protege.web.shared.crud;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.annotations.GwtCompatible;
import com.google.common.base.MoreObjects;
import com.google.gwt.user.client.rpc.IsSerializable;
import edu.stanford.bmir.protege.web.shared.HasIRIPrefix;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 14/08/2013
 */
@AutoValue
@GwtCompatible(serializable = true)
public abstract class EntityCrudKitPrefixSettings implements HasIRIPrefix, IsSerializable {

    private static final String DEFAULT_IRI_PREFIX = "http://www.example.org/";

    @Nonnull
    public static EntityCrudKitPrefixSettings get() {
        return get(DEFAULT_IRI_PREFIX);
    }

    @JsonCreator
    @Nonnull
    public static EntityCrudKitPrefixSettings get(String iriPrefix) {
        return new AutoValue_EntityCrudKitPrefixSettings(iriPrefix);
    }

    @JsonProperty("iriPrefix")
    @Override
    public abstract String getIRIPrefix();

}
