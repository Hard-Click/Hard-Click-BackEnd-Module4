package com.wanted.backend.domain.community.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "post_files")
@Getter
public class PostFileJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    protected PostFileJpaEntity() {}

    public PostFileJpaEntity(Long postId, String fileUrl, int sortOrder) {
        this.postId = postId;
        this.fileUrl = fileUrl;
        this.sortOrder = sortOrder;
    }
}