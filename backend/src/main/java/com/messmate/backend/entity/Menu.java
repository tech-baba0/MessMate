package com.messmate.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "menus")
@CompoundIndex(def = "{'messId': 1, 'dayOfWeek': 1}", unique = true)
public class Menu {
    @Id
    private String id;

    private String messId;

    // 1 = Monday, 2 = Tuesday, ..., 7 = Sunday
    private Integer dayOfWeek;

    private List<String> lunchItems;
    private List<String> dinnerItems;

    private Boolean isPublished;
}
