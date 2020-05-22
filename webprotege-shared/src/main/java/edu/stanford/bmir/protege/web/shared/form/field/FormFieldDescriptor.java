package edu.stanford.bmir.protege.web.shared.form.field;

import com.fasterxml.jackson.annotation.*;
import com.google.auto.value.AutoValue;
import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.ImmutableMap;
import com.google.gwt.user.client.rpc.IsSerializable;
import edu.stanford.bmir.protege.web.shared.form.HasFormFieldId;
import edu.stanford.bmir.protege.web.shared.lang.LanguageMap;
import org.semanticweb.owlapi.model.OWLProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static edu.stanford.bmir.protege.web.shared.form.field.FormFieldDescriptor.*;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 30/03/16
 */
@JsonPropertyOrder({ID, OWL_BINDING, LABEL, FIELD_RUN, FORM_CONTROL_DESCRIPTOR, REPEATABILITY, OPTIONALITY, READ_ONLY, HELP})
@GwtCompatible(serializable = true)
@AutoValue
public abstract class FormFieldDescriptor implements HasFormFieldId, HasRepeatability, IsSerializable, BoundControlDescriptor {


    public static final String ID = "id";
    public static final String OWL_BINDING = "owlBinding";
    public static final String LABEL = "label";
    public static final String FIELD_RUN = "fieldRun";
    public static final String FORM_CONTROL_DESCRIPTOR = "formControlDescriptor";
    public static final String REPEATABILITY = "repeatability";
    public static final String OPTIONALITY = "optionality";
    public static final String READ_ONLY = "readOnly";
    public static final String HELP = "help";

    @JsonCreator
    @Nonnull
    public static FormFieldDescriptor get(@JsonProperty(ID) @Nonnull FormFieldId id,
                                          @JsonProperty(OWL_BINDING) @Nullable OwlBinding owlBinding,
                                          @JsonProperty(LABEL) @Nullable LanguageMap formLabel,
                                          @JsonProperty(FIELD_RUN) @Nullable FieldRun fieldRun,
                                          @JsonProperty(FORM_CONTROL_DESCRIPTOR) @Nonnull FormControlDescriptor fieldDescriptor,
                                          @JsonProperty(REPEATABILITY) @Nullable Repeatability repeatability,
                                          @JsonProperty(OPTIONALITY) @Nullable Optionality optionality,
                                          @JsonProperty(READ_ONLY) boolean readOnly,
                                          @JsonProperty(HELP) @Nullable LanguageMap help) {
        return new AutoValue_FormFieldDescriptor(id,
                                                 owlBinding,
                                                 formLabel == null ? LanguageMap.empty() : formLabel,
                                                 fieldRun == null ? FieldRun.START : fieldRun,
                                                 fieldDescriptor,
                                                 optionality == null ? Optionality.REQUIRED : optionality,
                                                 repeatability == null ? Repeatability.NON_REPEATABLE : repeatability,
                                                 readOnly,
                                                 help == null ? LanguageMap.empty() : help);
    }

    public static FormFieldDescriptor getDefault() {
        return FormFieldDescriptor.get(
                FormFieldId.get(""),
                null,
                LanguageMap.empty(),
                FieldRun.START,
                new TextControlDescriptor(LanguageMap.empty(),
                                          StringType.SIMPLE_STRING,
                                          LineMode.SINGLE_LINE,
                                          "",
                                          LanguageMap.empty()),
                Repeatability.NON_REPEATABLE,
                Optionality.REQUIRED,
                false,
                LanguageMap.empty()
        );
    }

    @Nonnull
    @Override
    @JsonProperty("id")
    public abstract FormFieldId getId();

    @JsonIgnore
    @Nullable
    protected abstract OwlBinding getOwlBindingInternal();

    @Override
    @Nonnull
    public Optional<OwlBinding> getOwlBinding() {
        return Optional.ofNullable(getOwlBindingInternal());
    }

    @JsonIgnore
    public Optional<OWLProperty> getOwlProperty() {
        return getOwlBinding().flatMap(OwlBinding::getOwlProperty);
    }

    @Nonnull
    public abstract LanguageMap getLabel();

    @Nonnull
    public abstract FieldRun getFieldRun();

    @Override
    @Nonnull
    public abstract FormControlDescriptor getFormControlDescriptor();

    @Nonnull
    public abstract Optionality getOptionality();

    @Nonnull
    public abstract Repeatability getRepeatability();

    public abstract boolean isReadOnly();

    @Nonnull
    public abstract LanguageMap getHelp();
    
    @JsonIgnore
    public boolean isComposite() {
        return getFormControlDescriptor() instanceof SubFormControlDescriptor;
    }

    @JsonIgnore
    public boolean isNonComposite() {
        return !(getFormControlDescriptor() instanceof SubFormControlDescriptor);
    }

    @JsonIgnore
    public boolean isRepeatable() {
        return !getRepeatability().equals(Repeatability.NON_REPEATABLE);
    }
}
