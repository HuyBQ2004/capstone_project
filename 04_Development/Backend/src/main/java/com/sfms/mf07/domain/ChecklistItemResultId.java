package com.sfms.mf07.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/** {@code @IdClass} của {@link ChecklistItemResult} — PK ghép {@code (inspection_id, item_id)}. */
public class ChecklistItemResultId implements Serializable {

    private UUID inspection;
    private UUID item;

    public ChecklistItemResultId() {
    }

    public ChecklistItemResultId(UUID inspection, UUID item) {
        this.inspection = inspection;
        this.item = item;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChecklistItemResultId other)) {
            return false;
        }
        return Objects.equals(inspection, other.inspection) && Objects.equals(item, other.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inspection, item);
    }
}
