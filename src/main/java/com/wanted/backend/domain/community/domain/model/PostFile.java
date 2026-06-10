package com.wanted.backend.domain.community.domain.model;

public class PostFile {

    private Long id;
    private Long postId;
    private String fileUrl;
    private int sortOrder;

    private PostFile(Long id, Long postId, String fileUrl, int sortOrder) {
        this.id = id;
        this.postId = postId;
        this.fileUrl = fileUrl;
        this.sortOrder = sortOrder;
    }

    public static PostFile create(Long postId, String fileUrl, int sortOrder) {
        return new PostFile(null, postId, fileUrl, sortOrder);
    }

    public static PostFile restore(Long id, Long postId, String fileUrl, int sortOrder) {
        return new PostFile(id, postId, fileUrl, sortOrder);
    }

    public Long getId() { return id; }
    public Long getPostId() { return postId; }
    public String getFileUrl() { return fileUrl; }
    public int getSortOrder() { return sortOrder; }
}