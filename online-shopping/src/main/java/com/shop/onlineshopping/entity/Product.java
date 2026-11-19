package com.shop.onlineshopping.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int qoh;

    @Enumerated(EnumType.STRING)
    // Store the category as a plain VARCHAR string, NOT a native MySQL ENUM. Hibernate maps a
    // STRING enum to a native ENUM column on MySQL by default, which freezes the allowed values
    // at table-creation time — so later adding a Category (e.g. SPORTS_EQUIPMENT) would be
    // rejected ("Data truncated") until someone hand-alters the column. VARCHAR avoids that:
    // new category values just work, no database migration needed.
    @Column(nullable = false, columnDefinition = "varchar(40)")
    private Category category;

    // Product image stored inline as a Base64 "data URL" (e.g. "data:image/jpeg;base64,...").
    // @Lob + LONGTEXT = "this column holds very long text" — a normal column is far too short for
    // an encoded image. Nullable: products without a picture fall back to a placeholder in the UI.
    // NOTE: fine for a small catalog (demo scale). At hundreds of products, serve images from a
    // dedicated endpoint/CDN instead so list queries don't drag every image along.
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String image;
}
