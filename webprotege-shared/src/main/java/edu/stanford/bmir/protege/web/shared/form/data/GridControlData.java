package edu.stanford.bmir.protege.web.shared.form.data;

import com.google.auto.value.AutoValue;
import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.ImmutableList;
import edu.stanford.bmir.protege.web.shared.form.field.GridControlDescriptor;
import edu.stanford.bmir.protege.web.shared.pagination.Page;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-11-30
 */
@AutoValue
@GwtCompatible(serializable = true)
public abstract class GridControlData implements ComplexFormControlValue {

    @Nonnull
    public static GridControlData get(@Nonnull GridControlDescriptor descriptor,
                                      @Nonnull Page<GridRowData> rows) {
        return new AutoValue_GridControlData(descriptor, rows);
    }

    @Override
    public <R> R accept(@Nonnull FormControlDataVisitorEx<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public void accept(@Nonnull FormControlDataVisitor visitor) {
        visitor.visit(this);
    }

    @Nonnull
    public abstract GridControlDescriptor getDescriptor();

    @Nonnull
    public abstract Page<GridRowData> getRows();
}